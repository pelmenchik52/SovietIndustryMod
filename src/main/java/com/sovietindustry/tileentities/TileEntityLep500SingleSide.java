package com.sovietindustry.tileentities;

/** Бічна окрема щогла 500 кВ (ліва й права однакові): антенка із затискачем грозотроса, шлейф на допоміжній гірлянді. */
public class TileEntityLep500SingleSide extends TileEntityLep500Single {
    public TileEntityLep500SingleSide() {
    }

    @Override
    public int getVariant() {
        return SIDE;
    }
}
