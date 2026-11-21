package com.sovietindustry.tileentities;

/**
 * П-подібна проміжна опора 330 кВ. Подвійні підвісні гірлянди запечені в моделі,
 * дроти чіпляються до нижнього затискача кожної гірлянди. Два грозозахисні троси по стійках.
 */
public class TileEntityLep330 extends TileEntityLep330Base {
    private static final double[][] GROUND = {
        { -Lep330.P_GROUND_X, Lep330.P_GROUND_Y, 0.0D },
        {  Lep330.P_GROUND_X, Lep330.P_GROUND_Y, 0.0D }
    };

    public TileEntityLep330() {
    }

    @Override
    protected double[][] getLocalMounts() {
        return Lep330.pMounts();
    }

    @Override
    protected double[][] getLocalGround() {
        return GROUND;
    }
}
