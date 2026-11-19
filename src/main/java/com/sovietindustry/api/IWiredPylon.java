package com.sovietindustry.api;

import java.util.List;

import net.minecraft.util.Vec3;

/**
 * Усе, що потрібно LepWireRenderer, щоб намалювати дроти й гірлянди будь-якої нашої опори
 * (лінійна опора, анкерна опора, портали підстанції).
 */
public interface IWiredPylon {

    /** Зовнішні дроти (внутрішні зв'язки підстанції сюди не входять). */
    List<int[]> getLinks();

    /** Точки кріплення відносно кута блока (як у TileEntityPylonBase). */
    Vec3[] getMountPos();

    /** true: гірлянди динамічні й повертаються вздовж проводу (вгору/вниз/убік). */
    boolean hasAnchorStrings();

    /** Довжина натяжної гірлянди в блоках. */
    double getStringLength();
}
