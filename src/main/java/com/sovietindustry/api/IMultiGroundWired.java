package com.sovietindustry.api;

import net.minecraft.util.Vec3;

/**
 * Опора з кількома грозозахисними тросами (П-подібна 330 кВ має по тросу на кожній стійці).
 * Тросові точки двох сусідніх опор зіставляються за поперечним зміщенням відносно прольоту.
 */
public interface IMultiGroundWired extends IGroundWired {
    /** Усі точки кріплення тросів відносно кута блока. */
    Vec3[] getGroundMounts();
}
