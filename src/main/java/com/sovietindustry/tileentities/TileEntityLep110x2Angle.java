package com.sovietindustry.tileentities;

/**
 * Анкерно-кутова 6-контактна опора 110 кВ, основа 3x3. Натяжні гірлянди динамічні (як у 3-контактної АКО)
 * і повертаються вздовж дроту. Може розходитись на дві 3-контактні анкерні опори (по одному колу на кожну).
 */
public class TileEntityLep110x2Angle extends TileEntityLep110x2Base {
    public TileEntityLep110x2Angle() {
    }

    @Override
    protected double mountDrop() {
        return Lep110x2.TENSION_DROP;
    }

    @Override
    public boolean hasAnchorStrings() {
        return true;
    }
}
