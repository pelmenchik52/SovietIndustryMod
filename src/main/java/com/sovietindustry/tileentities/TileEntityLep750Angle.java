package com.sovietindustry.tileentities;

import com.sovietindustry.api.IStandoffPylon;

/**
 * Анкерно-кутова опора 750 кВ: три окремі ґратчасті вежі як одна структура.
 * Без проводів — лише сталеві вежі без ізоляторів. Коли дріт підключено,
 * натяжні гірлянди виносяться за межі веж кронштейнами, а шлейфи обходять
 * вежі збоку. Жоден провід не проходить крізь каркас веж!
 */
public class TileEntityLep750Angle extends TileEntityLep750Base implements IStandoffPylon {
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
    public double getStringStandoff(int mount, double dirX, double dirZ) {
        // винос кореня гірлянди назовні за каркас вежі вздовж напрямку дроту
        return Lep750.A_STANDOFF;
    }
}
