package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockSlate extends Block {
    public BlockSlate() {
        super(Material.rock);
        setHardness(1.8F);
        setResistance(8.0F);
        setStepSound(Block.soundTypeStone);
    }
}
