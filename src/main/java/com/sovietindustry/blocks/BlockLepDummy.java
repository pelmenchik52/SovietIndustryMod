package com.sovietindustry.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Блок-структура (даммі) для створення реальної твердої колізії вздовж стовбура опори ЛЕП (висота 13 блоків).
 * Метадані зберігають зміщення dy (1..12) відносно майстер-блока основи.
 * Ламання будь-якого сегмента опори коректно демонтує всю конструкцію та дропає 1 предмет.
 */
public class BlockLepDummy extends Block {

    public BlockLepDummy() {
        super(Material.rock);
        setHardness(4.0F);
        setResistance(15.0F);
        setStepSound(soundTypeStone);
        setHarvestLevel("pickaxe", 1);
        setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
    }

    @Override
    public int getRenderType() {
        return -1; // Невидимий - всю модель опори рендерить TESR майстер-блока
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
    public Item getItemDropped(int meta, Random rand, int fortune) {
        return null; // Дропає тільки майстер-блок
    }

    @Override
    public int quantityDropped(Random rand) {
        return 0;
    }

    @Override
    public int getMobilityFlag() {
        return 2; // Поршні не можуть зрушити сегмент опори
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int dy = world.getBlockMetadata(x, y, z);
        float r;
        if (dy <= 3) {
            r = 4.5F / 16.0F; // 9 px
        } else if (dy <= 6) {
            r = 4.0F / 16.0F; // 8 px
        } else if (dy <= 9) {
            r = 3.5F / 16.0F; // 7 px
        } else {
            r = 3.0F / 16.0F; // 6 px
        }
        setBlockBounds(0.5F - r, 0.0F, 0.5F - r, 0.5F + r, 1.0F, 0.5F + r);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
        int dy = world.getBlockMetadata(x, y, z);
        int masterY = y - dy;
        // Якщо майстер-блок зруйновано - самознищуємося
        if (world.getBlock(x, masterY, z) != ModBlocks.lep110v1) {
            world.setBlockToAir(x, y, z);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        int dy = meta;
        int masterY = y - dy;
        // Якщо гравець зламав цей фіктивний блок, ламаємо майстер-блок зі спавном дропу
        if (world.getBlock(x, masterY, z) == ModBlocks.lep110v1) {
            world.func_147480_a(x, masterY, z, true);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemStack(ModBlocks.lep110v1);
    }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister reg) {
        blockIcon = reg.registerIcon("sovietindustry:pdn_slab");
    }
}
