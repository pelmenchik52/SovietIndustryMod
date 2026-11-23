package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.tileentities.TileEntityLep110v1;
import com.sovietindustry.tileentities.TileEntityLepPylon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Опора ЛЕП 110 кВ одноланцюгова (3 фази + трос), мультиблок 13 блоків, крок повороту 30°.
 */
public class BlockLep110v1 extends BlockContainer {
    public static final int TOWER_HEIGHT = LepMultiblock.TOWER_HEIGHT;

    public BlockLep110v1() {
        super(Material.rock);
        setHardness(5.0F);
        setResistance(20.0F);
        setStepSound(soundTypeStone);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(SovietIndustryMod.tabSoviet);
        setBlockBounds(1.0F / 16.0F, 0.0F, 1.0F / 16.0F, 15.0F / 16.0F, 1.0F, 15.0F / 16.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLep110v1();
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
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        if (!World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)) {
            return false;
        }
        for (int dy = 1; dy < TOWER_HEIGHT; dy++) {
            if (y + dy >= 256) return false;
            Block b = world.getBlock(x, y + dy, z);
            if (!world.isAirBlock(x, y + dy, z) && !b.isReplaceable(world, x, y + dy, z)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        float snapped = LepMultiblock.snapAngle(placer.rotationYaw);

        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityLepPylon) {
            ((TileEntityLepPylon) te).setAngle(snapped);
        }
        LepMultiblock.buildV1(world, x, y, z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityLepPylon) {
            ((TileEntityLepPylon) te).disconnectAndSync(); // прибрати дроти і вузол у мережі NTM
        }
        LepMultiblock.removeV1(world, x, y, z);
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
