package com.sovietindustry.tileentities;

import com.sovietindustry.api.IJumperPylon;

import net.minecraft.util.Vec3;

/**
 * Анкерно-кутова опора 330 кВ. Один грозотрос іде через меншу верхню консоль,
 * другий — через середину верхівки опори.
 */
public class TileEntityLep330Angle extends TileEntityLep330Base implements IJumperPylon {
    private static final double[][] GROUND = {
        { Lep330.A_UPPER_SHORT_LEN, Lep330.A_UPPER_Y - 0.15D, 0.0D },
        { 0.0D,                     Lep330.A_GROUND_Y + 0.15D, 0.0D }
    };

    public TileEntityLep330Angle() {
    }

    @Override
    protected double[][] getLocalMounts() {
        return Lep330.aMounts();
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
        return Lep330.A_STRING_LEN;
    }

    // ------------------------------------------------------------ роздвоєні гірлянди

    @Override
    public int getStringsPerMount() {
        return Lep330.A_STRINGS;
    }

    @Override
    public double getStringSpacing() {
        return Lep330.A_STRING_SPACING;
    }

    @Override
    public int getWiresPerString() {
        return Lep330.A_WIRES_PER_STRING;
    }

    @Override
    public double getWireSpacing() {
        return Lep330.A_WIRE_SPACING;
    }

    // ------------------------------------------------------------ верхні гірлянди й шлейф

    @Override
    public int getJumperMount() {
        return Lep330.A_JUMPER_MOUNT;
    }

    @Override
    public Vec3[][] getJumperStrings() {
        double[][][] loc = Lep330.aJumperStrings();
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
