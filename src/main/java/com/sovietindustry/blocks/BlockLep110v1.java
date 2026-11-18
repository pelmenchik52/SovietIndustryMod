package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.tileentities.TileEntityLep110v1;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Опора ЛЕП 110 кВ одноланцюгова (3-контактна + грозозахисний трос).
 * Залізобетонна опора на базі центрифугованої стійки СК-22.
 * Мультиблок висотою 13 блоків з кроком повороту 30° та повною твердою колізією.
 */
public class BlockLep110v1 extends BlockContainer {
    public static final int TOWER_HEIGHT = 13;

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
        return -1; // Рендериться через TESR RenderLep110v1
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
        if (!world.doesBlockHaveSolidTopSurface(world, x, y - 1, z)) {
            return false;
        }
        // Перевіряємо вільний простір на 12 блоків вгору для всієї висоти опори
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
        // Крок повороту на 30 градусів за поглядом гравця (12 напрямків)
        float yaw = placer.rotationYaw;
        float snapped = Math.round(yaw / 30.0F) * 30.0F;
        while (snapped < 0.0F) snapped += 360.0F;
        while (snapped >= 360.0F) snapped -= 360.0F;

        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityLep110v1) {
            ((TileEntityLep110v1) te).setAngle(snapped);
        }

        // Спавнимо 12 структурних блоків колізії вгору
        for (int dy = 1; dy < TOWER_HEIGHT; dy++) {
            world.setBlock(x, y + dy, z, ModBlocks.lep110Dummy, dy, 3);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        // Знищуємо всі верхні сегменти колізії опори
        for (int dy = 1; dy < TOWER_HEIGHT; dy++) {
            if (world.getBlock(x, y + dy, z) == ModBlocks.lep110Dummy) {
                world.setBlockToAir(x, y + dy, z);
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        blockIcon = reg.registerIcon("sovietindustry:lep110v1");
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemStack(this);
    }
}
