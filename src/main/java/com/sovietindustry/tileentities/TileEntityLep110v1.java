package com.sovietindustry.tileentities;

/**
 * Проміжна (підвісна) опора 110 кВ. Гірлянди висять униз (вони запечені в моделі),
 * провід чіпляється до нижнього затискача кожної гірлянди.
 *
 * Локальні координати тут ПІСЛЯ повороту 180° навколо Z у RenderLep110v1,
 * тобто X дзеркальний до X у ModelLep110v1.
 */
public class TileEntityLep110v1 extends TileEntityLepPylon {

    private static final double[][] MOUNTS = {
        {  2.3125D, 7.53D, 0.0D },   // нижня коротка консоль (модель x = -37 px)
        { -3.875D,  7.53D, 0.0D },   // нижня довга консоль   (модель x = +62 px)
        { -2.3125D, 9.78D, 0.0D }    // верхня консоль        (модель x = +37 px)
    };

    public TileEntityLep110v1() {
    }

    @Override
    protected double[][] getLocalMounts() {
        return MOUNTS;
    }
}
