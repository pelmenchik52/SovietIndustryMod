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
    public static Block chimney;
    public static Block chimneyBase;
    public static Block lepPole;
    public static Block lepCrossarm;
    public static Block lep110v1;
    public static Block lep110Angle;
    public static Block lep110x2;
    public static Block lep110x2Angle;
    public static Block lep330;
    public static Block lep330Angle;
    public static Block substation110kV;
    public static Block substationPort110kV;
    public static Block substation330kV;
    public static Block lep110Dummy;

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

        chimney = new BlockChimney()
            .setBlockName("chimney")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(chimney, ItemBlockChimney.class, "chimney");

        chimneyBase = new BlockChimneyBase()
            .setBlockName("chimneyBase")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(chimneyBase, "chimneyBase");

        lepPole = new BlockLep(BlockLep.POLE)
            .setBlockName("lepPole")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lepPole, "lepPole");

        lepCrossarm = new BlockLep(BlockLep.CROSSARM)
            .setBlockName("lepCrossarm")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lepCrossarm, "lepCrossarm");

        // Блок-заповнювач для мультиблокових опор ЛЕП
        lep110Dummy = new BlockLepDummy()
            .setBlockName("lep110Dummy");
        GameRegistry.registerBlock(lep110Dummy, "lep110Dummy");

        // Опора ЛЕП 110 кВ одноланцюгова (мультиблок 13 блоків)
        lep110v1 = new BlockLep110v1()
            .setBlockName("lep110v1")
            .setBlockTextureName(SovietIndustryMod.MODID + ":lep110v1")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep110v1, "lep110v1");

        // Опора ЛЕП 110 кВ анкерно-кутова (мультиблок 13 блоків, основа 3x3)
        lep110Angle = new BlockLep110Angle()
            .setBlockName("lep110Angle")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep110Angle, "lep110Angle");

        // 6-контактна проміжна опора 110 кВ УП110-2 (мультиблок 16 блоків, колона 1x1)
        lep110x2 = new BlockLep110x2()
            .setBlockName("lep110x2")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep110x2, "lep110x2");

        // 6-контактна анкерно-кутова опора 110 кВ (мультиблок 16 блоків, основа 3x3)
        lep110x2Angle = new BlockLep110x2Angle()
            .setBlockName("lep110x2Angle")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep110x2Angle, "lep110x2Angle");

        // П-подібна проміжна опора 330 кВ (стійки на +-3 блоки, колізія 7 блоків, висота 17)
        lep330 = new BlockLep330()
            .setBlockName("lep330")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep330, "lep330");

        // Анкерно-кутова опора 330 кВ (мультиблок, основа 5x5, висота 17)
        lep330Angle = new BlockLep330Angle()
            .setBlockName("lep330Angle")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep330Angle, "lep330Angle");

        substation110kV = new BlockSubstation110kV()
            .setBlockName("substation110kV")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(substation110kV, "substation110kV");

        substationPort110kV = new BlockSubstationPort110kV()
            .setBlockName("substationPort110kV");
        GameRegistry.registerBlock(substationPort110kV, "substationPort110kV");

        // Трансформаторна підстанція 330 кВ (мультиблок 7x6x8): вхід від анкерно-кутової 330 кВ, 3 кабельні виводи NTM
        substation330kV = new BlockSubstation330kV()
            .setBlockName("substation330kV")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(substation330kV, "substation330kV");
    }

    public static void registerRenderers() {
        // Клієнтські рендерери
    }
}
