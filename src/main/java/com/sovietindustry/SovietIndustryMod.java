package com.sovietindustry;

import com.sovietindustry.blocks.ModBlocks;
import com.sovietindustry.items.ModItems;
import com.sovietindustry.network.PacketRequestPylonSync;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.creativetab.CreativeTabs;

@Mod(modid = SovietIndustryMod.MODID, name = "Soviet Industry", version = "1.0.0", dependencies = "required-after:hbm")
public class SovietIndustryMod {
    public static final String MODID = "sovietindustry";

    public static CreativeTabs tabSoviet = new SovietIndustryTab("tabSovietIndustry");
    public static SimpleNetworkWrapper network;

    @SidedProxy(clientSide = "com.sovietindustry.ClientProxy", serverSide = "com.sovietindustry.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        network.registerMessage(PacketRequestPylonSync.Handler.class, PacketRequestPylonSync.class, 0, Side.SERVER);

        ModBlocks.init();
        ModItems.init();
        proxy.registerTileEntities();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.registerRenderers();
    }
}