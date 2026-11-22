package com.sovietindustry.tileentities;

/**
 * Проміжна Λ-подібна опора 750 кВ на відтяжках. Підвісні гірлянди по 20 тарілок
 * із квадратною рамкою 4 проводів запечені в моделі, дроти чіпляються до затискачів рамки.
 * Два грозозахисні троси на рогах над стійками.
 */
public class TileEntityLep750 extends TileEntityLep750Base {
    private static final double[][] GROUND = {
        { -Lep750.GROUND_X, Lep750.GROUND_Y, 0.0D },
        {  Lep750.GROUND_X, Lep750.GROUND_Y, 0.0D }
    };

    public TileEntityLep750() {
    }

    @Override
    protected double[][] getLocalMounts() {
        return Lep750.pMounts();
    }

    @Override
    protected double[][] getLocalGround() {
        return GROUND;
    }
}
