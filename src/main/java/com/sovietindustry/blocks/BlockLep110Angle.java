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
import com.sovietindustry.tileentities.TileEntityLep110Angle;
import com.sovietindustry.tileentities.TileEntityLepPylon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Анкерно-кутова опора ЛЕП 110 кВ (3 фази + трос), основа 3x3, висота 13, крок повороту 30°.
 * Колізія звужується вгору (див. LepMultiblock), а не монолітний бокс.
 */
public class BlockLep110Angle extends BlockContainer {
    public static final int TOWER_HEIGHT = LepMultiblock.TOWER_HEIGHT;

    public BlockLep110Angle() {
        super(Material.iron);
        setHardness(6.0F);
        setResistance(25.0F);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 2);
        setCreativeTab(SovietIndustryMod.tabSoviet);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLep110Angle();
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
        return LepMultiblock.canPlaceAngle(world, x, y, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        float snapped = Math.round(placer.rotationYaw / 30.0F) * 30.0F;
        while (snapped < 0.0F) snapped += 360.0F;
        while (snapped >= 360.0F) snapped -= 360.0F;

        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityLepPylon) {
            ((TileEntityLepPylon) te).setAngle(snapped);
        }
        LepMultiblock.buildAngle(world, x, y, z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityLepPylon) {
            ((TileEntityLepPylon) te).disconnectAndSync();
        }
        LepMultiblock.removeAngle(world, x, y, z);
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
