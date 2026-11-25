package com.sovietindustry.tileentities;

import com.sovietindustry.api.IBundledPylon;
import com.sovietindustry.api.IMultiGroundWired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

/**
 * База опор 500 кВ. Для NTM це звичайний вузол ConnectionType.TRIPLE (3 фази, успадковано від TileEntityLepPylon).
 * Окремий клас від TileEntityLep750Base, щоб ItemWireCoil не плутав рівні напруги.
 * Кожна фаза це розщеплена фаза з 4 проводів квадратом (як у 750 кВ), на вершині два грозозахисні троси.
 */
public abstract class TileEntityLep500Base extends TileEntityLepPylon implements IBundledPylon, IMultiGroundWired {

    public static final double MAX_SPAN_500 = Lep500.MAX_SPAN_500;

    /** Локальні координати точок кріплення тросів [x, y, z]. */
    protected abstract double[][] getLocalGround();

    @Override
    public double getMaxWireLength() {
        return MAX_SPAN_500;
    }

    @Override
    public Vec3[] getGroundMounts() {
        double[][] g = getLocalGround();
        Vec3[] r = new Vec3[g.length];
        for (int i = 0; i < g.length; i++) {
            double[] w = toWorldOffset(g[i][0], g[i][1], g[i][2]);
            r[i] = Vec3.createVectorHelper(0.5D + w[0], w[1], 0.5D + w[2]);
        }
        return r;
    }

    @Override
    public Vec3 getGroundMount() {
        return getGroundMounts()[0];
    }

    // ------------------------------------------------------------ пучок

    @Override
    public int getStringsPerMount() {
        return Lep500.STRINGS;
    }

    @Override
    public double getStringSpacing() {
        return Lep500.STRING_SPACING;
    }

    @Override
    public int getWiresPerString() {
        return Lep500.WIRES_PER_STRING;
    }

    @Override
    public double getWireSpacing() {
        return Lep500.WIRE_SPACING;
    }

    /** Підвісна опора: вісь пучка це вісь траверси (локальна X у світових координатах). */
    @Override
    public double[] getBundleAxis(double spanX, double spanZ) {
        if (hasAnchorStrings()) {
            // натяжні гірлянди йдуть уздовж прольоту, пучок розсувається поперек нього
            double l = Math.sqrt(spanX * spanX + spanZ * spanZ);
            if (l < 1.0E-6D) return new double[] { 1.0D, 0.0D, 0.0D };
            return new double[] { -spanZ / l, 0.0D, spanX / l };
        }
        double[] a = toWorldOffset(1.0D, 0.0D, 0.0D);
        return new double[] { a[0], 0.0D, a[2] };
    }

    // ------------------------------------------------------------ окремі щогли (одна фаза на щоглу)

    private boolean isSingleAt(int x, int y, int z) {
        return worldObj != null && worldObj.getTileEntity(x, y, z) instanceof TileEntityLep500Single;
    }

    /** +1 / -1: з якого боку опори (вздовж її локальної осі Z) лежить сусід. */
    public int sideOf(int x, int z) {
        double[] az = toWorldOffset(0.0D, 0.0D, 1.0D);
        double d = (x - xCoord) * az[0] + (z - zCoord) * az[2];
        return d >= 0.0D ? 1 : -1;
    }

    public int singlesOnSide(int side) {
        int n = 0;
        for (int[] p : getLinks()) {
            if (isSingleAt(p[0], p[1], p[2]) && sideOf(p[0], p[2]) == side) n++;
        }
        return n;
    }

    public int normalLinkCount() {
        int n = 0;
        for (int[] p : getLinks()) {
            if (!isSingleAt(p[0], p[1], p[2])) n++;
        }
        return n;
    }

    /** Скільки боків опори зайнято окремими щоглами (три щогли з одного боку це одна «сторона»). */
    public int sidesUsedBySingles() {
        return (singlesOnSide(1) > 0 ? 1 : 0) + (singlesOnSide(-1) > 0 ? 1 : 0);
    }

    /** До окремої щогли йде дріт лише від однієї з наших точок кріплення (за положенням щогли поперек лінії). */
    @Override
    public int[] getLinkMounts(int x, int y, int z) {
        if (!isSingleAt(x, y, z)) return null;
        return new int[] { singleMount(x, y, z) };
    }

    private int singleMount(int x, int y, int z) {
        int side = sideOf(x, z);
        double[] ax = toWorldOffset(1.0D, 0.0D, 0.0D);
        List<double[]> grp = new ArrayList<double[]>();
        for (int[] p : getLinks()) {
            if (!isSingleAt(p[0], p[1], p[2]) || sideOf(p[0], p[2]) != side) continue;
            double lat = (p[0] - xCoord) * ax[0] + (p[2] - zCoord) * ax[2];
            grp.add(new double[] { lat, p[0], p[1], p[2] });
        }
        Collections.sort(grp, new Comparator<double[]>() {
            @Override
            public int compare(double[] a, double[] b) {
                return Double.compare(a[0], b[0]);
            }
        });
        int k = grp.size();
        int idx = 0;
        for (int i = 0; i < k; i++) {
            double[] g = grp.get(i);
            if ((int) g[1] == x && (int) g[2] == y && (int) g[3] == z) idx = i;
        }
        double[][] lm = getLocalMounts();
        int n = lm.length;
        if (k >= n) return Math.min(idx, n - 1);
        int[] best = null;
        double bestCost = Double.MAX_VALUE;
        for (int mask = 1; mask < (1 << n); mask++) {
            if (Integer.bitCount(mask) != k) continue;
            int[] pick = new int[k];
            int c = 0;
            for (int j = 0; j < n; j++) {
                if ((mask & (1 << j)) != 0) pick[c++] = j;
            }
            double cost = 0.0D;
            for (int i = 0; i < k; i++) cost += Math.abs(grp.get(i)[0] - lm[pick[i]][0]);
            if (cost < bestCost) {
                bestCost = cost;
                best = pick;
            }
        }
        return best == null ? 0 : best[idx];
    }

    // ------------------------------------------------------------ рамка рендера

    @Override
    protected double renderHeight() {
        return 36.0D;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        double minX = xCoord - 40.0D, minY = yCoord - 1.0D, minZ = zCoord - 40.0D;
        double maxX = xCoord + 41.0D, maxY = yCoord + renderHeight(), maxZ = zCoord + 41.0D;
        for (int[] p : getLinks()) {
            minX = Math.min(minX, p[0] - 40.0D);
            minY = Math.min(minY, p[1] - 1.0D);
            minZ = Math.min(minZ, p[2] - 40.0D);
            maxX = Math.max(maxX, p[0] + 41.0D);
            maxY = Math.max(maxY, p[1] + 36.0D);
            maxZ = Math.max(maxZ, p[2] + 41.0D);
        }
        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
