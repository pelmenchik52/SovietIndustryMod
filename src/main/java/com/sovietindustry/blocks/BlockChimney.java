package com.sovietindustry.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.sovietindustry.SovietIndustryMod;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Димова труба котельні.
 * Підтримує вертикальне та горизонтальне розміщення (осі Y, X, Z).
 * Три стани іржі (нижні 2 біти метаданих):
 * 0 - нова (гладка сталь), 1 - трохи іржава, 2 - сильно іржава.
 * Біти 2..3 відповідають за вісь: 0 = Y, 1 = X, 2 = Z.
 */
public class BlockChimney extends Block {
    public static final int VARIANTS = 3;

    public static int renderId = 0;

    public IIcon[] icons = new IIcon[VARIANTS];

    public BlockChimney() {
        super(Material.iron);
        setHardness(4.0F);
        setResistance(12.0F);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 0);
        setBlockBounds(1.5F / 16F, 0F, 1.5F / 16F, 14.5F / 16F, 1F, 14.5F / 16F);
    }

    @Override
    public int getRenderType() {
        return renderId;
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
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        int rust = meta & 3;
        int axis = 0;
        if (side == 0 || side == 1) {
            axis = 0; // Вертикальна (Y)
        } else if (side == 4 || side == 5) {
            axis = 1; // Горизонтальна вздовж X
        } else if (side == 2 || side == 3) {
            axis = 2; // Горизонтальна вздовж Z
        }
        return rust | (axis << 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        for (int i = 0; i < VARIANTS; i++) {
            icons[i] = reg.registerIcon(SovietIndustryMod.MODID + ":chimney_" + i);
        }
        blockIcon = icons[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return icons[MathHelper.clamp_int(meta & 3, 0, VARIANTS - 1)];
    }

    @Override
    public int damageDropped(int meta) {
        return meta & 3;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < VARIANTS; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int axis = (world.getBlockMetadata(x, y, z) >> 2) & 3;
        if (axis == 0) { // Y
            setBlockBounds(1.5F / 16F, 0F, 1.5F / 16F, 14.5F / 16F, 1F, 14.5F / 16F);
        } else if (axis == 1) { // X
            setBlockBounds(0F, 1.5F / 16F, 1.5F / 16F, 1F, 14.5F / 16F, 14.5F / 16F);
        } else { // Z
            setBlockBounds(1.5F / 16F, 1.5F / 16F, 0F, 14.5F / 16F, 14.5F / 16F, 1F);
        }
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(1.5F / 16F, 0F, 1.5F / 16F, 14.5F / 16F, 1F, 14.5F / 16F);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity) {
        setBlockBoundsBasedOnState(world, x, y, z);
        super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
    }
}
