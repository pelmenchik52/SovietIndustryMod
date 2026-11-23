package com.sovietindustry.tileentities;

import com.sovietindustry.api.IJumperPylon;
import com.sovietindustry.api.ISideSupportPylon;
import com.sovietindustry.api.IStandoffPylon;

import net.minecraft.util.Vec3;

/**
 * Анкерно-кутова опора 750 кВ: три окремі ґратчасті вежі як одна структура.
 * Без проводів — лише сталевий каркас. Коли дріт підключено, з'являються натяжні гірлянди,
 * шлейфи двох перших фаз обходять вежі збоку, а третя фаза проходить через рамку правої вежі
 * на підвісних гірляндах (IJumperPylon).
 */
public class TileEntityLep750Angle extends TileEntityLep750Base implements IStandoffPylon, IJumperPylon, ISideSupportPylon {
    private static final double[][] GROUND = {
        { -Lep750.A_TOWER_X, Lep750.A_GROUND_Y, 0.0D },
        {  Lep750.A_TOWER_X, Lep750.A_GROUND_Y, 0.0D }
    };

    public TileEntityLep750Angle() {
    }

    @Override
    protected double[][] getLocalMounts() {
        return Lep750.aMounts();
    }

    @Override
    protected double[][] getLocalGround() {
        return GROUND;
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
    public Vec3[] getSideSupport(int mount) {
        double[] a = Lep750.aSupportAttach(mount);
        double[] b = Lep750.aSupportEnd(mount);
        if (a == null || b == null) return null;
        double[] wa = toWorldOffset(a[0], a[1], a[2]);
        double[] wb = toWorldOffset(b[0], b[1], b[2]);
        return new Vec3[] {
            Vec3.createVectorHelper(0.5D + wa[0], wa[1], 0.5D + wa[2]),
            Vec3.createVectorHelper(0.5D + wb[0], wb[1], 0.5D + wb[2])
        };
    }

    @Override
    public int getJumperMount() {
        return Lep750.A_JUMPER_MOUNT;
    }

    @Override
    public Vec3[][] getJumperStrings() {
        double[][][] loc = Lep750.aJumperStrings();
        Vec3[][] r = new Vec3[loc.length][2];
        for (int i = 0; i < loc.length; i++) {
            for (int j = 0; j < 2; j++) {
                double[] w = toWorldOffset(loc[i][j][0], loc[i][j][1], loc[i][j][2]);
                r[i][j] = Vec3.createVectorHelper(0.5D + w[0], w[1], 0.5D + w[2]);
            }
        }
        return r;
    }
    public Vec3[][] getLeftJumperStrings() {
        double[][][] loc = Lep750.aLeftJumperStrings();
        Vec3[][] r = new Vec3[loc.length][2];
        for (int i = 0; i < loc.length; i++) {
            for (int j = 0; j < 2; j++) {
                double[] w = toWorldOffset(loc[i][j][0], loc[i][j][1], loc[i][j][2]);
                r[i][j] = Vec3.createVectorHelper(0.5D + w[0], w[1], 0.5D + w[2]);
            }
        }
        return r;
    }

    @Override
    public double[] getJumperAxis() {
        double[] a = toWorldOffset(1.0D, 0.0D, 0.0D);
        return new double[] { a[0], 0.0D, a[2] };
    }

    @Override
    public double getStringStandoff(int mount, double dirX, double dirZ) {
        // винос кореня гірлянди назовні за каркас вежі вздовж напрямку дроту
        return Lep750.A_STANDOFF;
    }
    
}
