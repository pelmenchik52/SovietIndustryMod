package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockPDNSlab extends Block {
    public BlockPDNSlab() {
        super(Material.rock);
        setHardness(2.0F);
        setResistance(10.0F);
        setStepSound(Block.soundTypeStone);
    }
}
