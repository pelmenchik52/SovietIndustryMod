package com.sovietindustry.tileentities;

import com.sovietindustry.api.IJumperPylon;

import net.minecraft.util.Vec3;

/** Права окрема щогла 750 кВ: тросостійка, велика консоль-прапор, шлейф іде через дві верхні підвісні гірлянди. */
public class TileEntityLep750SingleRight extends TileEntityLep750Single implements IJumperPylon {
    public TileEntityLep750SingleRight() {
    }

    @Override
    public int getVariant() {
        return RIGHT;
    }

    @Override
    public int getJumperMount() {
        return 0;
    }

    @Override
    public Vec3[][] getJumperStrings() {
        double[][][] loc = Lep750.sJumperStrings();
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
}
