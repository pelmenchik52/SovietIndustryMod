package com.sovietindustry.tileentities;

/**
 * Проміжна Λ-подібна опора 500 кВ на відтяжках. Підвісні гірлянди по 18 тарілок із квадратною рамкою
 * 4 проводів запечені в моделі, дроти чіпляються до затискачів рамки. Два грозозахисні троси на рогах над стійками.
 */
public class TileEntityLep500 extends TileEntityLep500Base {
    private static final double[][] GROUND = {
        { -Lep500.GROUND_X, Lep500.GROUND_Y, 0.0D },
        {  Lep500.GROUND_X, Lep500.GROUND_Y, 0.0D }
    };

    public TileEntityLep500() {
    }

    @Override
    protected double[][] getLocalMounts() {
        return Lep500.pMounts();
    }

    @Override
    protected double[][] getLocalGround() {
        return GROUND;
    }
}
