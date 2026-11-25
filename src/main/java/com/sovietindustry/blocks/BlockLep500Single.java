package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.tileentities.TileEntityLep500Single;
import com.sovietindustry.tileentities.TileEntityLepPylon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Окрема анкерно-кутова щогла 500 кВ на одну фазу (середня, без антенки). Три такі щогли (або середня й дві бічні) замінюють одну анкерно-кутову опору,
 * коли її не можна поставити на рівний майданчик (гори): кожна щогла ставиться окремо, на своїй висоті,
 * клітинки основи, зайняті схилом, пропускаються.
 */
public class BlockLep500Single extends BlockContainer {

    public BlockLep500Single() {
        super(Material.iron);
        setHardness(7.0F);
        setResistance(30.0F);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 2);
        setCreativeTab(SovietIndustryMod.tabSoviet);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLep500Single();
    }

    /** Форма вежі, за якою рахується висота колізії: 0 середня (низька, без антенки), 1 бічна з антенкою. */
    protected int towerIdx() {
        return 0;
    }

    @Override
    public int getRenderType() {
        return -1;
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
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        float[] b = LepMultiblock.bounds(this, 0, 0, 0);
        setBlockBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && LepMultiblock.canPlaceS500(world, x, y, z, towerIdx());
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        float snapped = LepMultiblock.snapAngle(placer.rotationYaw);

        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityLepPylon) {
            ((TileEntityLepPylon) te).setAngle(snapped);
        }
        LepMultiblock.buildS500(world, x, y, z, snapped, towerIdx());
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        float angle = 0.0F;
        if (te instanceof TileEntityLepPylon) {
            angle = ((TileEntityLepPylon) te).angle;
            ((TileEntityLepPylon) te).disconnectAndSync();
        }
        LepMultiblock.removeS500(world, x, y, z, angle, towerIdx());
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        blockIcon = reg.registerIcon("sovietindustry:lep110v1");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return blockIcon;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemStack(this);
    }
}
