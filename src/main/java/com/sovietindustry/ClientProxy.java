package com.sovietindustry;

import com.sovietindustry.blocks.BlockChimney;
import com.sovietindustry.blocks.BlockLep;
import com.sovietindustry.blocks.ModBlocks;
import com.sovietindustry.render.ChimneyRenderer;
import com.sovietindustry.render.LepRenderer;
import com.sovietindustry.render.RenderIndustrialLamp;
import com.sovietindustry.render.RenderIndustrialPipe;
import com.sovietindustry.render.RenderKnifeSwitch;
import com.sovietindustry.render.RenderLep110v1;
import com.sovietindustry.render.RenderPO2Fence;
import com.sovietindustry.tileentities.TileEntityIndustrialLamp;
import com.sovietindustry.tileentities.TileEntityIndustrialPipe;
import com.sovietindustry.tileentities.TileEntityKnifeSwitch;
import com.sovietindustry.tileentities.TileEntityLep110v1;
import com.sovietindustry.tileentities.TileEntityPO2Fence;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void registerRenderers() {
        super.registerRenderers();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityKnifeSwitch.class, new RenderKnifeSwitch());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPO2Fence.class, new RenderPO2Fence());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityIndustrialLamp.class, new RenderIndustrialLamp());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityIndustrialPipe.class, new RenderIndustrialPipe());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep110v1.class, new RenderLep110v1());

        BlockLep.renderId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new LepRenderer());

        BlockChimney.renderId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new ChimneyRenderer());
    }
}
