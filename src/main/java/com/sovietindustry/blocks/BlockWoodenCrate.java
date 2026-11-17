package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockWoodenCrate extends Block {
    public BlockWoodenCrate() {
        super(Material.wood);
        setHardness(1.5F);
        setResistance(5.0F);
        setStepSound(Block.soundTypeWood);
    }
}
