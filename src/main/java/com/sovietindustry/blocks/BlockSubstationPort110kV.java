package com.sovietindustry.blocks;

import java.util.Random;

import com.sovietindustry.tileentities.TileEntitySubstationPort110kV;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/**
 * Допоміжний блок-вузол виходу 110 кВ на плиті підстанції (приймає кліки ItemWireCoil).
 */
public class BlockSubstationPort110kV extends BlockContainer {

    public BlockSubstationPort110kV() {
        super(Material.iron);
        setHardness(8.0F);
        setResistance(40.0F);
        setStepSound(soundTypeMetal);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntitySubstationPort110kV();
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
    public Item getItemDropped(int meta, Random rand, int fortune) {
        return null;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        // Знаходимо майстер-блок підстанції в радіусі 2 блоків і ламаємо його з дропом
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (world.getBlock(x + dx, y, z + dz) == ModBlocks.substation110kV) {
                    world.func_147480_a(x + dx, y, z + dz, true);
                    break;
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemStack(ModBlocks.substation110kV);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        blockIcon = reg.registerIcon("sovietindustry:pdn_slab");
    }
}