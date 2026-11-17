package com.sovietindustry.blocks;

import com.sovietindustry.tileentities.TileEntityIndustrialLamp;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockIndustrialLamp extends BlockContainer {

    public BlockIndustrialLamp() {
        super(Material.glass);
        setHardness(0.5F);
        setResistance(2.0F);
        setLightLevel(1.0F); // Максимальне світло (рівень 15)
        setStepSound(Block.soundTypeGlass);
        // Акуратний хітбокс підвісної лампи
        setBlockBounds(0.125F, 0.125F, 0.125F, 0.875F, 1.0F, 0.875F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityIndustrialLamp();
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
    public int getRenderType() {
        return -1; // 3D модель через TESR
    }
}
