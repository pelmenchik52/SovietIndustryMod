package com.sovietindustry.tileentities;

import com.sovietindustry.api.IBundledPylon;
import com.sovietindustry.api.IMultiGroundWired;

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
