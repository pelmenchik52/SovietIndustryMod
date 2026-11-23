package com.sovietindustry.tileentities;

import com.sovietindustry.api.ISideSupportPylon;
import com.sovietindustry.api.IStandoffPylon;

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
public class TileEntityLep750Single extends TileEntityLep750Base implements IStandoffPylon, ISideSupportPylon {
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

    /** Допоміжна гірлянда шлейфу на власному упорі щогли. Права щогла веде шлейф через верхні гірлянди, тож для неї null. */
    @Override
    public Vec3[] getSideSupport(int mount) {
        if (mount != 0 || getVariant() == RIGHT) return null;
        double[] a = Lep750.sSupportAttach();
        double[] b = Lep750.sSupportEnd();
        double[] wa = toWorldOffset(a[0], a[1], a[2]);
        double[] wb = toWorldOffset(b[0], b[1], b[2]);
        return new Vec3[] {
            Vec3.createVectorHelper(0.5D + wa[0], wa[1], 0.5D + wa[2]),
            Vec3.createVectorHelper(0.5D + wb[0], wb[1], 0.5D + wb[2])
        };
    }
}
