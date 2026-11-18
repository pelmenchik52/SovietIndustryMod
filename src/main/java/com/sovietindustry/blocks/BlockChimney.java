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
 * Димова труба котельні. Порожня всередині, три стани іржі (metadata 0..2):
 * 0 - нова, 1 - трохи іржава, 2 - сильно іржава.
 * Рендер: ChimneyRenderer (16-гранна труба). renderId виставляється у ClientProxy.
 */
public class BlockChimney extends Block {
    public static final int VARIANTS = 3;

    /** Заповнюється у ClientProxy через RenderingRegistry.getNextAvailableRenderId(). */
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

    // ------------------------------------------------------------ вигляд
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
        return icons[MathHelper.clamp_int(meta, 0, VARIANTS - 1)];
    }

    // ------------------------------------------------------------ предмет / творчий режим
    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < VARIANTS; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    // ------------------------------------------------------------ форма
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setBlockBounds(1.5F / 16F, 0F, 1.5F / 16F, 14.5F / 16F, 1F, 14.5F / 16F);
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(1.5F / 16F, 0F, 1.5F / 16F, 14.5F / 16F, 1F, 14.5F / 16F);
    }

    /** Колізія тільки по стінках: всередині труби порожньо, у неї можна впасти зверху. */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity) {
        float a = 1.5F / 16F, b = 3.0F / 16F, c = 13.0F / 16F, d = 14.5F / 16F;
        setBlockBounds(a, 0F, a, b, 1F, d); // західна стінка
        super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
        setBlockBounds(c, 0F, a, d, 1F, d); // східна
        super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
        setBlockBounds(b, 0F, a, c, 1F, b); // північна
        super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
        setBlockBounds(b, 0F, c, c, 1F, d); // південна
        super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
        setBlockBounds(a, 0F, a, d, 1F, d); // повернути рамку виділення
    }
}
