package com.sovietindustry.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.sovietindustry.tileentities.TileEntityLep750SingleLeft;

/** Ліва окрема анкерно-кутова щогла 750 кВ (див. BlockLep750Single). */
public class BlockLep750SingleLeft extends BlockLep750Single {

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLep750SingleLeft();
    }

    @Override
    protected int towerIdx() {
        return -1;
    }
}
