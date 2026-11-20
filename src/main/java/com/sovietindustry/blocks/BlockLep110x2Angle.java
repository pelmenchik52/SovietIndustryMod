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
import com.sovietindustry.tileentities.TileEntityLep110x2;
import com.sovietindustry.tileentities.TileEntityLep110x2Angle;
import com.sovietindustry.tileentities.TileEntityLepPylon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 6-контактна анкерно-кутова опора ЛЕП 110 кВ, мультиблок 16 блоків, основа 3x3, крок повороту 30°.
 * Колізія звужується вгору (LepMultiblock.x2AngleHalf). Може розходитись на дві 3-контактні АКО.
 */
public class BlockLep110x2Angle extends BlockContainer {

    public BlockLep110x2Angle() {
        super(Material.iron);
        setHardness(6.0F);
        setResistance(25.0F);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 2);
        setCreativeTab(SovietIndustryMod.tabSoviet);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLep110x2Angle();
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
        return LepMultiblock.canPlaceX2Angle(world, x, y, z);
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
        LepMultiblock.buildX2Angle(world, x, y, z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityLepPylon) {
            ((TileEntityLepPylon) te).disconnectAndSync();
        }
        LepMultiblock.removeX2Angle(world, x, y, z);
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
