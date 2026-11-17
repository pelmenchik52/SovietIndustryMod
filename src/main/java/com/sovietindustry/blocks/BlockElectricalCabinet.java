package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import com.sovietindustry.SovietIndustryMod;

public class BlockElectricalCabinet extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon iconFront;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockElectricalCabinet() {
        super(Material.iron);
        setHardness(2.5F);
        setResistance(12.0F);
        setStepSound(Block.soundTypeMetal);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.iconFront = reg.registerIcon(SovietIndustryMod.MODID + ":electrical_cabinet_front");
        this.iconSide = reg.registerIcon(SovietIndustryMod.MODID + ":electrical_cabinet_side");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 2 || side == 3) {
            return this.iconFront;
        }
        return this.iconSide;
    }
}
