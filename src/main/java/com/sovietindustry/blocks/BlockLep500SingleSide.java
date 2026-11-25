package com.sovietindustry.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.sovietindustry.tileentities.TileEntityLep500SingleSide;

/** Бічна окрема анкерно-кутова щогла 500 кВ з антенкою (ліва й права однакові, див. BlockLep500Single). */
public class BlockLep500SingleSide extends BlockLep500Single {

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLep500SingleSide();
    }

    @Override
    protected int towerIdx() {
        return 1;
    }
}
