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
import com.sovietindustry.tileentities.TileEntityLep750;
import com.sovietindustry.tileentities.TileEntityLepPylon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Проміжна Λ-подібна опора 750 кВ на відтяжках. Майстер-блок стоїть по центру між п'ятами стійок,
 * стійки — це dummy-колони на п'ятах (~8 блоків від осі), загальна ширина 24 блоки, висота 25.
 * Поворот кроком 30°.
 */
public class BlockLep750 extends BlockContainer {

    public BlockLep750() {
        super(Material.iron);
        setHardness(7.0F);
        setResistance(30.0F);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 2);
        setCreativeTab(SovietIndustryMod.tabSoviet);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLep750();
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
        return super.canPlaceBlockAt(world, x, y, z) && LepMultiblock.canPlaceP750Any(world, x, y, z);
    }

   @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        float snapped = LepMultiblock.snapAngle(placer.rotationYaw);

        float chosen = LepMultiblock.pickP750Angle(world, x, y, z, snapped);
        if (chosen < 0.0F) {
            world.setBlockToAir(x, y, z);
            if (placer instanceof EntityPlayer && !((EntityPlayer) placer).capabilities.isCreativeMode) {
                ((EntityPlayer) placer).inventory.addItemStackToInventory(new ItemStack(this));
            }
            return;
        }
        snapped = chosen;

        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityLepPylon) {
            ((TileEntityLepPylon) te).setAngle(snapped);
        }
        LepMultiblock.buildP750(world, x, y, z, snapped);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        float angle = 0.0F;
        if (te instanceof TileEntityLepPylon) {
            angle = ((TileEntityLepPylon) te).angle;
            ((TileEntityLepPylon) te).disconnectAndSync();
        }
        LepMultiblock.removeP750(world, x, y, z, angle);
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
