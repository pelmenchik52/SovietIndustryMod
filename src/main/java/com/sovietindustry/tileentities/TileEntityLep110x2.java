package com.sovietindustry.tileentities;

/**
 * Проміжна (підвісна) 6-контактна опора 110 кВ УП110-2. Гірлянди висять униз і запечені в моделі,
 * дріт чіпляється до нижнього затискача кожної гірлянди. Колона 1x1 блок.
 */
public class TileEntityLep110x2 extends TileEntityLep110x2Base {
    public TileEntityLep110x2() {
    }

    @Override
    protected double mountDrop() {
        return Lep110x2.SUSPENSION_DROP;
    }
}
