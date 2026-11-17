package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import com.sovietindustry.SovietIndustryMod;

public class BlockRustyBarrel extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockRustyBarrel() {
        super(Material.iron);
        setHardness(2.0F);
        setResistance(8.0F);
        setStepSound(Block.soundTypeMetal);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.iconSide = reg.registerIcon(SovietIndustryMod.MODID + ":barrel_side");
        this.iconTop = reg.registerIcon(SovietIndustryMod.MODID + ":barrel_top");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 0 || side == 1) {
            return this.iconTop;
        }
        return this.iconSide;
    }
}
