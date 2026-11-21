package com.sovietindustry.tileentities;

/**
 * Анкерно-кутова опора 330 кВ. Один грозотрос іде через меншу верхню консоль,
 * другий — через середину верхівки опори.
 */
public class TileEntityLep330Angle extends TileEntityLep330Base {
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
}