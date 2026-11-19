package com.sovietindustry.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.sovietindustry.tileentities.TileEntityLepDummy;

/**
 * Невидимий блок-заповнювач опор ЛЕП. Тепер має TileEntity з координатами майстер-блока,
 * тому годиться і для стовпа, і для ширшої основи анкерної опори.
 * Колізія рахується з зміщення (dx, dy, dz) від майстра, див. LepMultiblock.bounds.
 */
public class BlockLepDummy extends BlockContainer {

    public BlockLepDummy() {
        super(Material.rock);
        setHardness(4.0F);
        setResistance(15.0F);
        setStepSound(soundTypeStone);
        setHarvestLevel("pickaxe", 1);
        setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityLepDummy();
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
    public int quantityDropped(Random rand) {
        return 0;
    }

    @Override
    public int getMobilityFlag() {
        return 2;
    }

    private TileEntityLepDummy getDummy(IBlockAccess world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityLepDummy && ((TileEntityLepDummy) te).valid) {
            return (TileEntityLepDummy) te;
        }
        return null;
    }

    private static boolean isTower(Block b) {
        return b == ModBlocks.lep110v1 || b == ModBlocks.lep110Angle || b == ModBlocks.substation110kV;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        TileEntityLepDummy d = getDummy(world, x, y, z);
        if (d == null) {
            setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
            return;
        }
        float[] b = LepMultiblock.bounds(world.getBlock(d.mx, d.my, d.mz), x - d.mx, y - d.my, z - d.mz);
        setBlockBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
    }

    /** У 1.7.10 колізія бере поточні bounds, тому виставляємо їх явно перед запитом. */
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;
        TileEntityLepDummy d = getDummy(world, x, y, z);
        // без валідного майстра (наприклад, dummy зі старого світу) самознищуємось
        if (d == null || !isTower(world.getBlock(d.mx, d.my, d.mz))) {
            world.setBlockToAir(x, y, z);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntityLepDummy d = getDummy(world, x, y, z);
        if (d != null && isTower(world.getBlock(d.mx, d.my, d.mz))) {
            // ламаємо майстер-блок із дропом; він сам приберe решту сегментів і дроти
            world.func_147480_a(d.mx, d.my, d.mz, true);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        TileEntityLepDummy d = getDummy(world, x, y, z);
        if (d != null) {
            Block m = world.getBlock(d.mx, d.my, d.mz);
            if (isTower(m)) return new ItemStack(m);
        }
        return new ItemStack(ModBlocks.lep110v1);
    }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister reg) {
        blockIcon = reg.registerIcon("sovietindustry:pdn_slab");
    }
}
