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
        if (mount < 0 || mount > 1) return null;
        double[][] loc = { Lep750.aSupportAttach(mount), Lep750.aSupportEnd(mount) };
        Vec3[] r = new Vec3[2];
        for (int i = 0; i < 2; i++) {
            double[] w = toWorldOffset(loc[i][0], loc[i][1], loc[i][2]);
            r[i] = Vec3.createVectorHelper(0.5D + w[0], w[1], 0.5D + w[2]);
        }
        return r;
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
