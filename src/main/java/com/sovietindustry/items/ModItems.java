package com.sovietindustry.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class ModItems {
    public static Item wireCoil;
    public static Item craneRemote;

    public static void init() {
        wireCoil = new ItemWireCoil();
        GameRegistry.registerItem(wireCoil, "wireCoil");

        craneRemote = new ItemCraneRemote();
        GameRegistry.registerItem(craneRemote, "craneRemote");
    }
}
