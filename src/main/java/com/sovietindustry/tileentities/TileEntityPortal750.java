package com.sovietindustry.tileentities;

import com.sovietindustry.api.IBundledPylon;

/**
 * Окремий портал 750 кВ (мультиблок 13x1x23, див. Portal750): та сама траверса з трьома фазами, що й на вході
 * підстанції, але як самостійний блок і з багатьма дротами.
 *
 * Фази це ConnectionType.TRIPLE, кожна розщеплена (2 гірлянди по 4 дроти), тож портал сумісний з анкерно-кутовою
 * опорою 750 кВ, підстанцією 750 кВ та іншими порталами. Звичайні опори тримають лише вхід і вихід, а портал
 * працює як шина: до нього можна підвести до MAX_LINKS дротів у різні боки. Натяжні гірлянди сам повертає
 * LepWireRenderer уздовж кожного проводу.
 */
public class TileEntityPortal750 extends TileEntityLepPylon implements IBundledPylon {

    /** Скільки прольотів (дротів до різних опор) приймає один портал. */
    public static final int MAX_LINKS = 12;

    public static final double MAX_SPAN_750 = Lep750.MAX_SPAN_750;

    public TileEntityPortal750() {
    }

    // ------------------------------------------------------------ геометрія

    @Override
    protected double[][] getLocalMounts() {
        return Portal750.mounts();
    }

    @Override
    public boolean hasAnchorStrings() {
        return true;
    }

    @Override
    public double getStringLength() {
        return Portal750.STRING_LEN;
    }

    @Override
    public double getMaxWireLength() {
        return MAX_SPAN_750;
    }

    /** true, коли портал уже тримає максимум прольотів. */
    public boolean isFull() {
        return getLinks().size() >= MAX_LINKS;
    }

    // ------------------------------------------------------------ пучок (такий самий, як у анкерної опори 750 кВ)

    @Override
    public int getStringsPerMount() {
        return Lep750.STRINGS;
    }

    @Override
    public double getStringSpacing() {
        return Lep750.STRING_SPACING;
    }

    @Override
    public int getWiresPerString() {
        return Lep750.WIRES_PER_STRING;
    }

    @Override
    public double getWireSpacing() {
        return Lep750.WIRE_SPACING;
    }

    /** Гірлянди анкерні: вісь пучка перпендикулярна до прольоту. */
    @Override
    public double[] getBundleAxis(double spanX, double spanZ) {
        double l = Math.sqrt(spanX * spanX + spanZ * spanZ);
        if (l < 1.0E-6D) return new double[] { 1.0D, 0.0D, 0.0D };
        return new double[] { -spanZ / l, 0.0D, spanX / l };
    }

    @Override
    protected double renderHeight() {
        return 25.0D;
    }
}
