package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockCorrugatedIron extends Block {
    public BlockCorrugatedIron() {
        super(Material.iron);
        setHardness(3.0F);
        setResistance(15.0F);
        setStepSound(Block.soundTypeMetal);
    }
}
