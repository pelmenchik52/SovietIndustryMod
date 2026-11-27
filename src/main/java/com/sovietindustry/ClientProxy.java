package com.sovietindustry;

import com.sovietindustry.render.LepWireRenderer;
import com.sovietindustry.tileentities.TileEntityLepPylon;
import net.minecraft.tileentity.TileEntity;
import com.sovietindustry.render.RenderSubstation110kV;
import com.sovietindustry.render.RenderSubstation330kV;
import com.sovietindustry.render.RenderSubstation500kV;
import com.sovietindustry.render.RenderSubstation750kV;
import com.sovietindustry.render.RenderPortal750;
import com.sovietindustry.render.RenderCraneController;
import com.sovietindustry.tileentities.TileEntityCraneController;
import com.sovietindustry.tileentities.TileEntityPortal750;
import com.sovietindustry.tileentities.TileEntitySubstation330kV;
import com.sovietindustry.tileentities.TileEntitySubstation500kV;
import com.sovietindustry.tileentities.TileEntitySubstation750kV;
import com.sovietindustry.tileentities.TileEntitySubstation110kV;
import com.sovietindustry.tileentities.TileEntitySubstationPort110kV;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import com.sovietindustry.blocks.ModBlocks;
import com.sovietindustry.render.ItemRenderLepPylon;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import com.sovietindustry.blocks.BlockLep;
import com.sovietindustry.render.LepRenderer;
import com.sovietindustry.render.RenderIndustrialPipe;
import com.sovietindustry.render.RenderLep110Angle;
import com.sovietindustry.render.RenderLep110v1;
import com.sovietindustry.render.RenderPO2Fence;
import com.sovietindustry.tileentities.TileEntityIndustrialPipe;
import com.sovietindustry.tileentities.TileEntityLep110Angle;
import com.sovietindustry.tileentities.TileEntityLep110v1;
import com.sovietindustry.tileentities.TileEntityLep110x2;
import com.sovietindustry.tileentities.TileEntityLep110x2Angle;
import com.sovietindustry.render.RenderLep110x2;
import com.sovietindustry.render.RenderLep330;
import com.sovietindustry.render.RenderLep500;
import com.sovietindustry.render.RenderLep750;
import com.sovietindustry.tileentities.TileEntityLep330;
import com.sovietindustry.tileentities.TileEntityLep330Angle;
import com.sovietindustry.tileentities.TileEntityLep500;
import com.sovietindustry.tileentities.TileEntityLep500Angle;
import com.sovietindustry.tileentities.TileEntityLep500Single;
import com.sovietindustry.tileentities.TileEntityLep500SingleSide;
import com.sovietindustry.tileentities.TileEntityLep750;
import com.sovietindustry.tileentities.TileEntityLep750Angle;
import com.sovietindustry.tileentities.TileEntityLep750Single;
import com.sovietindustry.tileentities.TileEntityLep750SingleLeft;
import com.sovietindustry.tileentities.TileEntityLep750SingleRight;
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
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPO2Fence.class, new RenderPO2Fence());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityIndustrialPipe.class, new RenderIndustrialPipe());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep110v1.class, new RenderLep110v1());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep110Angle.class, new RenderLep110Angle());
        RenderLep110x2 x2Renderer = new RenderLep110x2();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep110x2.class, x2Renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep110x2Angle.class, x2Renderer);
        RenderLep330 l330Renderer = new RenderLep330();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep330.class, l330Renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep330Angle.class, l330Renderer);
        RenderLep500 l500Renderer = new RenderLep500();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep500.class, l500Renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep500Angle.class, l500Renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep500Single.class, l500Renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep500SingleSide.class, l500Renderer);
        RenderLep750 l750Renderer = new RenderLep750();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep750.class, l750Renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep750Angle.class, l750Renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep750Single.class, l750Renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep750SingleLeft.class, l750Renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLep750SingleRight.class, l750Renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySubstation110kV.class, new RenderSubstation110kV());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySubstation330kV.class, new RenderSubstation330kV());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySubstation750kV.class, new RenderSubstation750kV());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPortal750.class, new RenderPortal750());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCraneController.class, new RenderCraneController());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySubstation500kV.class, new RenderSubstation500kV());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySubstationPort110kV.class, new TileEntitySpecialRenderer() {
            @Override
            public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
                LepWireRenderer.render((TileEntityLepPylon) te, x, y, z);
            }
        });

        BlockLep.renderId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new LepRenderer());

        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep110v1),
            new ItemRenderLepPylon(false)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep110Angle),
            new ItemRenderLepPylon(true)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep110x2),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_X2)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep110x2Angle),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_X2_ANGLE)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep330),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_330)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep330Angle),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_330_ANGLE)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep500),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_500)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep500Angle),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_500_ANGLE)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep500Single),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_500_SINGLE)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep500SingleSide),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_500_SINGLE_SIDE)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep750),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_750)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep750Angle),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_750_ANGLE)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep750Single),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_750_SINGLE)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep750SingleLeft),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_750_SINGLE_LEFT)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.lep750SingleRight),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_750_SINGLE_RIGHT)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.substation110kV),
            new ItemRenderLepPylon(false, true)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.substation330kV),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_SUBSTATION_330)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.substation750kV),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_SUBSTATION_750)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.substation500kV),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_SUBSTATION_500)
        );
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(ModBlocks.portal750),
            new ItemRenderLepPylon(ItemRenderLepPylon.KIND_PORTAL_750)
        );
    }
}
