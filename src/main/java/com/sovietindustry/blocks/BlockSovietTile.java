package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockSovietTile extends Block {
    public BlockSovietTile() {
        super(Material.rock);
        setHardness(1.5F);
        setResistance(5.0F);
        setStepSound(Block.soundTypeStone);
    }
}
