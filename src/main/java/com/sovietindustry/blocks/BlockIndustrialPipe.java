package com.sovietindustry.blocks;

import com.sovietindustry.tileentities.TileEntityIndustrialPipe;
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

public class BlockIndustrialPipe extends BlockContainer {

    public BlockIndustrialPipe() {
        super(Material.iron);
        setHardness(2.0F);
        setResistance(8.0F);
        setStepSound(Block.soundTypeMetal);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityIndustrialPipe();
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
        return -1; // 3D циліндрична модель через TESR
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        // Циліндричний хітбокс труби діаметром 11 пікселів
        if ((meta & 1) == 0) {
            setBlockBounds(0.18F, 0.18F, 0.0F, 0.82F, 0.82F, 1.0F);
        } else {
            setBlockBounds(0.0F, 0.18F, 0.18F, 1.0F, 0.82F, 0.82F);
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
        int meta = (l == 0 || l == 2) ? 0 : 1;
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }
}
