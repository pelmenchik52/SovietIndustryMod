package com.sovietindustry.tileentities;

import java.util.List;

import com.hbm.tileentity.network.TileEntityPylonBase;
import com.sovietindustry.api.ISideSupportPylon;
import com.sovietindustry.api.ISingleAnglePylon;
import com.sovietindustry.api.IStandoffPylon;
import com.sovietindustry.api.IWiredPylon;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;

/**
 * Окрема анкерно-кутова щогла 750 кВ на ОДНУ фазу (розщеплена фаза 2x4 дроти).
 * Три такі щогли замінюють одну анкерно-кутову опору, коли її не можна поставити на рівний майданчик (гори):
 * кожна стоїть на своїй висоті, а до проміжної опори тягнеться свій дріт. Для NTM це звичайний вузол TRIPLE
 * (щоб котушка з'єднувала з опорами того ж типу), але візуально використовується лише одна точка кріплення.
 *
 * Три варіанти повторюють вежі цілої АКО: середня (низька плоска голівка, без троса), ліва (тросостійка + мала консоль),
 * права (тросостійка + велика консоль із верхніми гірляндами, див. {@link TileEntityLep750SingleRight}).
 * Шлейф між передньою і задньою натяжними гірляндами тримає допоміжна горизонтальна гірлянда на власному упорі щогли,
 * тож щогла нічого не потребує від сусідів і може стояти де завгодно.
 */
public class TileEntityLep750Single extends TileEntityLep750Base implements IStandoffPylon, ISideSupportPylon, ISingleAnglePylon {
    public static final int LEFT = -1;
    public static final int MID = 0;
    public static final int RIGHT = 1;

    private static final double[][] NO_GROUND = new double[0][];

    public TileEntityLep750Single() {
    }

    /** -1 ліва, 0 середня, +1 права (так само, як індекс вежі в моделі АКО). */
    public int getVariant() {
        return MID;
    }

    @Override
    public double getSupportDir() {
        return Lep750.sSupportDir(getVariant());
    }

    @Override
    protected double[][] getLocalMounts() {
        return Lep750.sMounts();
    }

    @Override
    protected double[][] getLocalGround() {
        return getVariant() == MID ? NO_GROUND : Lep750.sGround();
    }

    @Override
    public Vec3 getGroundMount() {
        Vec3[] g = getGroundMounts();
        return g.length == 0 ? null : g[0];
    }

    @Override
    public boolean hasAnchorStrings() {
        return true;
    }

    @Override
    public double getStringLength() {
        return Lep750.A_STRING_LEN;
    }

    @Override
    public double getStringStandoff(int mount, double dirX, double dirZ) {
        return Lep750.A_STANDOFF;
    }

    /** Одна точка кріплення: усі дроти (вхід і вихід) чіпляються до неї. */
    @Override
    public int[] getLinkMounts(int x, int y, int z) {
        return null;
    }

    /**
     * Допоміжна гірлянда шлейфу на власному упорі щогли. Права щогла веде шлейф через верхні гірлянди, тож для неї null.
     * Гірлянда є лише при куті між гірляндами більше порогу (див. {@link #isAuxActive()}). Ліва щогла має упор з протилежного боку (повернутий на 180 градусів).
     */
    @Override
    public Vec3[] getSideSupport(int mount) {
        if (mount != 0 || getVariant() == RIGHT || !isAuxActive()) return null;
        double[] a = Lep750.sSupportAttach(getVariant());
        double[] b = Lep750.sSupportEnd(getVariant());
        double[] wa = toWorldOffset(a[0], a[1], a[2]);
        double[] wb = toWorldOffset(b[0], b[1], b[2]);
        return new Vec3[] {
            Vec3.createVectorHelper(0.5D + wa[0], wa[1], 0.5D + wa[2]),
            Vec3.createVectorHelper(0.5D + wb[0], wb[1], 0.5D + wb[2])
        };
    }

    /**
     * true, коли кут між передньою й задньою натяжними гірляндами (напрямки до двох сусідів) більший за
     * {@link Lep750#S_AUX_MIN_ANGLE_DEG}: 180 градусів це пряма лінія. Тоді є допоміжна гірлянда; при гострішому куті
     * її немає, і шлейф іде збоку під гірляндами. Рахується з тих самих точок кріплення, що й сам дріт.
     */
    public boolean isAuxActive() {
        if (worldObj == null) return false;
        List<int[]> links = getLinks();
        if (links.size() < 2) return false;
        double[][] dir = new double[2][];
        int n = 0;
        for (int[] p : links) {
            double[] d = linkDirection(p);
            if (d == null) continue;
            dir[n++] = d;
            if (n == 2) break;
        }
        if (n < 2) return false;
        double cos = dir[0][0] * dir[1][0] + dir[0][1] * dir[1][1];
        cos = Math.max(-1.0D, Math.min(1.0D, cos));
        return Math.toDegrees(Math.acos(cos)) >= Lep750.S_AUX_MIN_ANGLE_DEG;
    }

    /** Горизонтальний одиничний напрямок від нашої точки кріплення до точки кріплення сусіда, null якщо вироджений. */
    private double[] linkDirection(int[] p) {
        double tx = p[0] - xCoord;
        double tz = p[2] - zCoord;
        TileEntity o = worldObj.getTileEntity(p[0], p[1], p[2]);
        if (o instanceof TileEntityPylonBase) {
            Vec3[] om = ((TileEntityPylonBase) o).getMountPos();
            if (om != null && om.length > 0) {
                int idx = -1;
                if (o instanceof IWiredPylon) {
                    int[] oi = ((IWiredPylon) o).getLinkMounts(xCoord, yCoord, zCoord);
                    if (oi != null && oi.length > 0 && oi[0] >= 0 && oi[0] < om.length) idx = oi[0];
                }
                if (idx >= 0) {
                    tx += om[idx].xCoord - 0.5D;
                    tz += om[idx].zCoord - 0.5D;
                } else {
                    double cx = 0.0D, cz = 0.0D;
                    for (Vec3 v : om) {
                        cx += v.xCoord;
                        cz += v.zCoord;
                    }
                    tx += cx / om.length - 0.5D;
                    tz += cz / om.length - 0.5D;
                }
            }
        }
        double l = Math.sqrt(tx * tx + tz * tz);
        if (l < 1.0E-6D) return null;
        return new double[] { tx / l, tz / l };
    }
}
