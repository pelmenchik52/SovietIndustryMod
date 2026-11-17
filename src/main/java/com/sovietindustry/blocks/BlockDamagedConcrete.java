package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockDamagedConcrete extends Block {
    public BlockDamagedConcrete() {
        super(Material.rock);
        setHardness(2.2F);
        setResistance(12.0F);
        setStepSound(Block.soundTypeStone);
    }
}
