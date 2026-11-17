package com.sovietindustry.blocks;

import com.sovietindustry.SovietIndustryMod;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class ModBlocks {

    // Базові блоки
    public static Block pdnSlab;
    public static Block po2Fence;
    public static Block sovietTileWhite;
    public static Block sovietTileGreen;
    public static Block sovietTileBlue;
    public static Block sovietTileGrey;
    public static Block paintedPanel;
    public static Block silicateBrick;

    // Нові будівельні блоки та конструкції
    public static Block glassBlock;
    public static Block slate;
    public static Block corrugatedIron;
    public static Block damagedConcrete;
    public static Block po2Damaged;

    // Електрика та інженерія
    public static Block knifeSwitch;
    public static Block electricalCabinet;
    public static Block industrialLamp;
    public static Block industrialPipe;

    // Пропси та декор
    public static Block rustyBarrel;
    public static Block woodenCrate;
    public static Block warningSign;

    public static void init() {
        // Базові блоки
        pdnSlab = new BlockPDNSlab()
            .setBlockName("pdnSlab")
            .setBlockTextureName(SovietIndustryMod.MODID + ":pdn_slab")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(pdnSlab, "pdnSlab");

        po2Fence = new BlockPO2Fence()
            .setBlockName("po2Fence")
            .setBlockTextureName(SovietIndustryMod.MODID + ":po2_fence")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(po2Fence, "po2Fence");

        sovietTileWhite = new BlockSovietTile()
            .setBlockName("sovietTileWhite")
            .setBlockTextureName(SovietIndustryMod.MODID + ":tile_white")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(sovietTileWhite, "sovietTileWhite");

        sovietTileGreen = new BlockSovietTile()
            .setBlockName("sovietTileGreen")
            .setBlockTextureName(SovietIndustryMod.MODID + ":tile_green")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(sovietTileGreen, "sovietTileGreen");

        sovietTileBlue = new BlockSovietTile()
            .setBlockName("sovietTileBlue")
            .setBlockTextureName(SovietIndustryMod.MODID + ":tile_blue")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(sovietTileBlue, "sovietTileBlue");

        sovietTileGrey = new BlockSovietTile()
            .setBlockName("sovietTileGrey")
            .setBlockTextureName(SovietIndustryMod.MODID + ":tile_grey")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(sovietTileGrey, "sovietTileGrey");

        paintedPanel = new BlockPaintedPanel()
            .setBlockName("paintedPanel")
            .setBlockTextureName(SovietIndustryMod.MODID + ":panel_lower")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(paintedPanel, "paintedPanel");

        silicateBrick = new BlockSilicateBrick()
            .setBlockName("silicateBrick")
            .setBlockTextureName(SovietIndustryMod.MODID + ":silicate_brick")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(silicateBrick, "silicateBrick");

        // Нові будівельні блоки
        glassBlock = new BlockGlassBlock()
            .setBlockName("glassBlock")
            .setBlockTextureName(SovietIndustryMod.MODID + ":glass_block")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(glassBlock, "glassBlock");

        slate = new BlockSlate()
            .setBlockName("slate")
            .setBlockTextureName(SovietIndustryMod.MODID + ":slate")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(slate, "slate");

        corrugatedIron = new BlockCorrugatedIron()
            .setBlockName("corrugatedIron")
            .setBlockTextureName(SovietIndustryMod.MODID + ":corrugated_iron")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(corrugatedIron, "corrugatedIron");

        damagedConcrete = new BlockDamagedConcrete()
            .setBlockName("damagedConcrete")
            .setBlockTextureName(SovietIndustryMod.MODID + ":damaged_concrete")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(damagedConcrete, "damagedConcrete");

        po2Damaged = new BlockPO2Damaged()
            .setBlockName("po2Damaged")
            .setBlockTextureName(SovietIndustryMod.MODID + ":po2_damaged")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(po2Damaged, "po2Damaged");

        // Електрика та інженерія
        knifeSwitch = new BlockKnifeSwitch()
            .setBlockName("knifeSwitch")
            .setBlockTextureName(SovietIndustryMod.MODID + ":knife_switch_off")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(knifeSwitch, "knifeSwitch");

        electricalCabinet = new BlockElectricalCabinet()
            .setBlockName("electricalCabinet")
            .setBlockTextureName(SovietIndustryMod.MODID + ":electrical_cabinet_front")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(electricalCabinet, "electricalCabinet");

        industrialLamp = new BlockIndustrialLamp()
            .setBlockName("industrialLamp")
            .setBlockTextureName(SovietIndustryMod.MODID + ":industrial_lamp")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(industrialLamp, "industrialLamp");

        industrialPipe = new BlockIndustrialPipe()
            .setBlockName("industrialPipe")
            .setBlockTextureName(SovietIndustryMod.MODID + ":industrial_pipe")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(industrialPipe, "industrialPipe");

        // Пропси та декор
        rustyBarrel = new BlockRustyBarrel()
            .setBlockName("rustyBarrel")
            .setBlockTextureName(SovietIndustryMod.MODID + ":barrel_side")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(rustyBarrel, "rustyBarrel");

        woodenCrate = new BlockWoodenCrate()
            .setBlockName("woodenCrate")
            .setBlockTextureName(SovietIndustryMod.MODID + ":wooden_crate")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(woodenCrate, "woodenCrate");

        warningSign = new BlockWarningSign()
            .setBlockName("warningSign")
            .setBlockTextureName(SovietIndustryMod.MODID + ":warning_sign")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(warningSign, "warningSign");
    }

    public static void registerRenderers() {
        // Клієнтські рендерери
    }
}
