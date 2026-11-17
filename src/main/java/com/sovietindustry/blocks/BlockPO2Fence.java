package com.sovietindustry.blocks;

import com.sovietindustry.tileentities.TileEntityPO2Fence;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPO2Fence extends BlockContainer {

    public BlockPO2Fence() {
        super(Material.rock);
        setHardness(2.5F);
        setResistance(15.0F);
        setStepSound(Block.soundTypeStone);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityPO2Fence();
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
        return -1; // Кастомний 3D рендер
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        // Тонка плита паркану (замість товстого куба)
        if ((meta & 1) == 0) {
            // Орієнтація X (плита йде зі сходу на захід)
            setBlockBounds(0.0F, 0.0F, 0.35F, 1.0F, 1.5F, 0.65F);
        } else {
            // Орієнтація Z (плита йде з півночі на південь)
            setBlockBounds(0.35F, 0.0F, 0.0F, 0.65F, 1.5F, 1.0F);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
        int l = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        // 0 і 2 = поздовжньо, 1 і 3 = поперечно
        int meta = (l == 0 || l == 2) ? 0 : 1;
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }
}
