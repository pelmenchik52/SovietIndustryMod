package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import com.sovietindustry.SovietIndustryMod;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Опорна фундаментна плита димової труби котельні зі сталевим монтажним фланцем та анкерними болтами.
 */
public class BlockChimneyBase extends Block {

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockChimneyBase() {
        super(Material.rock);
        setHardness(5.0F);
        setResistance(25.0F);
        setStepSound(soundTypeStone);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(SovietIndustryMod.tabSoviet);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        iconTop = reg.registerIcon(SovietIndustryMod.MODID + ":chimney_base_top");
        iconSide = reg.registerIcon(SovietIndustryMod.MODID + ":chimney_base_side");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 1) return iconTop; // верх
        if (side == 0) return iconSide; // низ
        return iconSide; // боки
    }
}
