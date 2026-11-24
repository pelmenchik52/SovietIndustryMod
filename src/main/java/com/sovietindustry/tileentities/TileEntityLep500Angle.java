package com.sovietindustry.tileentities;

import java.util.List;

import com.sovietindustry.api.IFreeLoopPylon;
import com.sovietindustry.api.ISideSupportPylon;
import com.sovietindustry.api.IStandoffPylon;

import net.minecraft.util.Vec3;

/**
 * Анкерно-кутова опора 500 кВ: три однакові ґратчасті вежі як одна структура. Ліва й права мають зверху лише
 * гостру антенку з гачком грозотроса, ніяких консолей. Шлейф кожної фази тримає допоміжна гірлянда на сусідній вежі:
 *  - якщо кут між передньою й задньою натяжними гірляндами (з боку локальної осі +X) не більший за 180 градусів,
 *    шлейфи йдуть праворуч, права вежа тримає свою дугу вільно збоку;
 *  - якщо кут більший за 180, усе дзеркально: шлейфи йдуть ліворуч, а вільна дуга лежить на лівій вежі.
 */
public class TileEntityLep500Angle extends TileEntityLep500Base implements IStandoffPylon, ISideSupportPylon, IFreeLoopPylon {

    public TileEntityLep500Angle() {
    }

    @Override
    protected double[][] getLocalMounts() {
        return Lep500.aMounts();
    }

    @Override
    protected double[][] getLocalGround() {
        return Lep500.aGround();
    }

    @Override
    public boolean hasAnchorStrings() {
        return true;
    }

    @Override
    public double getStringLength() {
        return Lep500.A_STRING_LEN;
    }

    @Override
    public double getStringStandoff(int mount, double dirX, double dirZ) {
        return Lep500.A_STANDOFF;
    }

    /**
     * +1: шлейфи йдуть у бік локальної осі +X (кут з боку +X не більший за 180), -1: дзеркально.
     * +X лежить у меншому секторі між напрямками на сусідні опори, якщо його можна розкласти як невід'ємну
     * комбінацію цих двох напрямків. Пряма лінія (180) і менше двох зв'язків дають +1.
     */
    public int getLoopDir() {
        List<int[]> links = getLinks();
        if (links == null || links.size() < 2) return 1;
        int[] p1 = links.get(0), p2 = links.get(1);
        double d1x = p1[0] - xCoord, d1z = p1[2] - zCoord;
        double d2x = p2[0] - xCoord, d2z = p2[2] - zCoord;
        double l1 = Math.sqrt(d1x * d1x + d1z * d1z), l2 = Math.sqrt(d2x * d2x + d2z * d2z);
        if (l1 < 1.0E-6D || l2 < 1.0E-6D) return 1;
        d1x /= l1; d1z /= l1; d2x /= l2; d2z /= l2;
        double[] ax = toWorldOffset(1.0D, 0.0D, 0.0D);
        double det = d1x * d2z - d1z * d2x;
        if (Math.abs(det) < 1.0E-4D) return 1;
        double a = (ax[0] * d2z - ax[2] * d2x) / det;
        double b = (d1x * ax[2] - d1z * ax[0]) / det;
        return (a >= -1.0E-6D && b >= -1.0E-6D) ? 1 : -1;
    }

    @Override
    public Vec3[] getSideSupport(int mount) {
        double[][] s = Lep500.aSupport(mount, getLoopDir());
        if (s == null) return null;
        double[] wa = toWorldOffset(s[0][0], s[0][1], s[0][2]);
        double[] wb = toWorldOffset(s[1][0], s[1][1], s[1][2]);
        return new Vec3[] {
            Vec3.createVectorHelper(0.5D + wa[0], wa[1], 0.5D + wa[2]),
            Vec3.createVectorHelper(0.5D + wb[0], wb[1], 0.5D + wb[2])
        };
    }

    @Override
    public Vec3 getFreeLoopApex(int mount) {
        int dir = getLoopDir();
        if (Lep500.aSupport(mount, dir) != null) return null;
        double[] a = Lep500.aFreeApex(mount, dir);
        double[] w = toWorldOffset(a[0], a[1], a[2]);
        return Vec3.createVectorHelper(0.5D + w[0], w[1], 0.5D + w[2]);
    }
}
