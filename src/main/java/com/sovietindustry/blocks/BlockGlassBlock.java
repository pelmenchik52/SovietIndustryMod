package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGlassBlock extends Block {
    public BlockGlassBlock() {
        super(Material.glass);
        setHardness(0.8F);
        setResistance(3.0F);
        setStepSound(Block.soundTypeGlass);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderBlockPass() {
        return 1; // Alpha/translucent pass
    }
}
