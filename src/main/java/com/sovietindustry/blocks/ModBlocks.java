package com.sovietindustry.blocks;

import com.sovietindustry.SovietIndustryMod;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class ModBlocks {

    // Будівельні блоки
    public static Block po2Fence;
    public static Block glassBlock;
    public static Block slate;
    public static Block industrialPipe;

    // ЛЕП і підстанції
    public static Block lepPole;
    public static Block lepCrossarm;
    public static Block lep110v1;
    public static Block lep110Angle;
    public static Block lep110x2;
    public static Block lep110x2Angle;
    public static Block lep330;
    public static Block lep330Angle;
    public static Block lep500;
    public static Block lep500Angle;
    public static Block lep500Single;
    public static Block lep500SingleSide;
    public static Block lep750;
    public static Block lep750Angle;
    public static Block lep750Single;
    public static Block lep750SingleLeft;
    public static Block lep750SingleRight;
    public static Block substation110kV;
    public static Block substationPort110kV;
    public static Block substation330kV;
    public static Block substation500kV;
    public static Block substation750kV;
    public static Block portal750;
    public static Block lep110Dummy;

    public static void init() {
        // Будівельні блоки
        po2Fence = new BlockPO2Fence()
            .setBlockName("po2Fence")
            .setBlockTextureName(SovietIndustryMod.MODID + ":po2_fence")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(po2Fence, "po2Fence");

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

        industrialPipe = new BlockIndustrialPipe()
            .setBlockName("industrialPipe")
            .setBlockTextureName(SovietIndustryMod.MODID + ":industrial_pipe")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(industrialPipe, "industrialPipe");

        // Старі стовп і траверса
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
        GameRegistry.registerBlock(lep110v1, ItemBlockLep.class, "lep110v1");

        // Опора ЛЕП 110 кВ анкерно-кутова (мультиблок 13 блоків, основа 3x3)
        lep110Angle = new BlockLep110Angle()
            .setBlockName("lep110Angle")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep110Angle, ItemBlockLep.class, "lep110Angle");

        // 6-контактна проміжна опора 110 кВ УП110-2 (мультиблок 16 блоків, колона 1x1)
        lep110x2 = new BlockLep110x2()
            .setBlockName("lep110x2")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep110x2, ItemBlockLep.class, "lep110x2");

        // 6-контактна анкерно-кутова опора 110 кВ (мультиблок 16 блоків, основа 3x3)
        lep110x2Angle = new BlockLep110x2Angle()
            .setBlockName("lep110x2Angle")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep110x2Angle, ItemBlockLep.class, "lep110x2Angle");

        // П-подібна проміжна опора 330 кВ (стійки на +-3 блоки, колізія 7 блоків, висота 17)
        lep330 = new BlockLep330()
            .setBlockName("lep330")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep330, ItemBlockLep.class, "lep330");

        // Анкерно-кутова опора 330 кВ (мультиблок, основа 5x5, висота 17)
        lep330Angle = new BlockLep330Angle()
            .setBlockName("lep330Angle")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep330Angle, ItemBlockLep.class, "lep330Angle");

        // Проміжна опора 500 кВ (габарити як у проміжної 750 кВ, гірлянди по 18 тарілок, відтяжки вперед/назад)
        lep500 = new BlockLep500()
            .setBlockName("lep500")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep500, ItemBlockLep.class, "lep500");

        // Анкерно-кутова опора 500 кВ (мультиблок, 3 однакові вежі, бічні з антенкою для грозотроса)
        lep500Angle = new BlockLep500Angle()
            .setBlockName("lep500Angle")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep500Angle, ItemBlockLep.class, "lep500Angle");

        // Окремі анкерно-кутові щогли 500 кВ (одна фаза): середня й бічна з антенкою (ліва й права однакові)
        lep500Single = new BlockLep500Single()
            .setBlockName("lep500Single")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep500Single, ItemBlockLep.class, "lep500Single");
        lep500SingleSide = new BlockLep500SingleSide()
            .setBlockName("lep500SingleSide")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep500SingleSide, ItemBlockLep.class, "lep500SingleSide");

        // Проміжна опора 750 кВ (мультиблок, ширина 9, висота 29)
        lep750 = new BlockLep750()
            .setBlockName("lep750")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep750, ItemBlockLep.class, "lep750");

        // Анкерно-кутова опора 750 кВ (мультиблок, 3 вежі як одна структура, ширина 12, висота 25)
        lep750Angle = new BlockLep750Angle()
            .setBlockName("lep750Angle")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep750Angle, ItemBlockLep.class, "lep750Angle");

        // Окрема анкерно-кутова щогла 750 кВ (одна фаза; три такі замінюють анкерно-кутову опору, наприклад у горах)
        lep750Single = new BlockLep750Single()
            .setBlockName("lep750Single")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep750Single, ItemBlockLep.class, "lep750Single");

        // Ліва й права окремі щогли (тросостійка, консоль): разом із середньою складають повну анкерно-кутову опору з трьох щогл
        lep750SingleLeft = new BlockLep750SingleLeft()
            .setBlockName("lep750SingleLeft")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep750SingleLeft, ItemBlockLep.class, "lep750SingleLeft");
        lep750SingleRight = new BlockLep750SingleRight()
            .setBlockName("lep750SingleRight")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(lep750SingleRight, ItemBlockLep.class, "lep750SingleRight");

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

        // Трансформаторна підстанція 500 кВ (мультиблок 13x13x23, така сама, як 750 кВ): вхід лише від анкерно-кутової 500 кВ, 4 кабельні виводи NTM
        substation500kV = new BlockSubstation500kV()
            .setBlockName("substation500kV")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(substation500kV, "substation500kV");

        // Трансформаторна підстанція 750 кВ (мультиблок 9x9x15): вхід лише від анкерно-кутової 750 кВ, 4 кабельні виводи NTM
        substation750kV = new BlockSubstation750kV()
            .setBlockName("substation750kV")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(substation750kV, "substation750kV");

        // Окремий портал 750 кВ (мультиблок 13x1x23): шина, що приймає багато дротів від опор 750 кВ
        portal750 = new BlockPortal750()
            .setBlockName("portal750")
            .setCreativeTab(SovietIndustryMod.tabSoviet);
        GameRegistry.registerBlock(portal750, "portal750");
    }

    public static void registerRenderers() {
        // Клієнтські рендерери
    }
}
