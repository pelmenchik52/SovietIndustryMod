package com.sovietindustry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.creativetab.CreativeTabs;
import com.sovietindustry.blocks.ModBlocks;

@Mod(modid = SovietIndustryMod.MODID, name = "Soviet Industry", version = "1.0.0")
public class SovietIndustryMod {
    public static final String MODID = "sovietindustry";

    public static CreativeTabs tabSoviet = new SovietIndustryTab("tabSovietIndustry");

    @SidedProxy(clientSide = "com.sovietindustry.ClientProxy", serverSide = "com.sovietindustry.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModBlocks.init();
        proxy.registerTileEntities();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.registerRenderers();
    }
}
