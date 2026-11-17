package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import com.sovietindustry.SovietIndustryMod;

public class BlockPaintedPanel extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;

    public BlockPaintedPanel() {
        super(Material.rock);
        setHardness(1.5F);
        setResistance(5.0F);
        setStepSound(Block.soundTypeMetal);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(SovietIndustryMod.MODID + ":panel_lower");
        this.iconTop = reg.registerIcon(SovietIndustryMod.MODID + ":panel_upper");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        // side 1 = top, 0 = bottom, 2-5 = sides
        if (side == 0 || side == 1) return this.iconTop;
        // Lower half green paint, upper half white
        return this.blockIcon;
    }
}
