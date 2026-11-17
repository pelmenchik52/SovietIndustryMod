package com.sovietindustry.blocks;

import com.sovietindustry.tileentities.TileEntityKnifeSwitch;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockKnifeSwitch extends BlockContainer {

    public BlockKnifeSwitch() {
        super(Material.circuits);
        setHardness(0.5F);
        setResistance(2.0F);
        setStepSound(Block.soundTypeMetal);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityKnifeSwitch();
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
        int dir = (meta >> 1) & 3;

        // Точний хітбокс рубильника 1-в-1 з моделлю (висота y: 0.125F .. 0.875F)
        if (dir == 0) { // Прикріплений до південної стіни (Z = 1.0)
            setBlockBounds(0.3125F, 0.125F, 0.75F, 0.6875F, 0.875F, 1.0F);
        } else if (dir == 1) { // Прикріплений до північної стіни (Z = 0.0)
            setBlockBounds(0.3125F, 0.125F, 0.0F, 0.6875F, 0.875F, 0.25F);
        } else if (dir == 2) { // Прикріплений до східної стіни (X = 1.0)
            setBlockBounds(0.75F, 0.125F, 0.3125F, 1.0F, 0.875F, 0.6875F);
        } else { // Прикріплений до західної стіни (X = 0.0)
            setBlockBounds(0.0F, 0.125F, 0.3125F, 0.25F, 0.875F, 0.6875F);
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
        int dir = 0;
        if (l == 0) dir = 0; // Південь
        if (l == 1) dir = 3; // Захід
        if (l == 2) dir = 1; // Північ
        if (l == 3) dir = 2; // Схід
        world.setBlockMetadataWithNotify(x, y, z, (dir << 1), 2);
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return (world.getBlockMetadata(x, y, z) & 1) == 1 ? 15 : 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
        return (world.getBlockMetadata(x, y, z) & 1) == 1 ? 15 : 0;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            int meta = world.getBlockMetadata(x, y, z);
            int newMeta = meta ^ 1;
            world.setBlockMetadataWithNotify(x, y, z, newMeta, 3);
            world.playSoundEffect((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "random.click", 0.8F, (newMeta & 1) == 1 ? 0.6F : 0.5F);
            world.notifyBlocksOfNeighborChange(x, y, z, this);
        }
        return true;
    }
}
