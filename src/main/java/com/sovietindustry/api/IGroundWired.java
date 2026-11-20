package com.sovietindustry.api;

import net.minecraft.util.Vec3;

/**
 * Опора з грозозахисним тросом на вершині. Трос малюється лише між двома опорами,
 * які обидві мають трос; там, де лінія закінчується (або переходить на 3-контактну опору
 * чи підстанцію), трос просто обривається.
 */
public interface IGroundWired {
    /** Точка кріплення троса відносно кута блока (як getMountPos). */
    Vec3 getGroundMount();
}
