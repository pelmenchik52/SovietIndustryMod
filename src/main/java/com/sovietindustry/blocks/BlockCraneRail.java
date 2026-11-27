package com.sovietindustry.blocks;

import com.sovietindustry.SovietIndustryMod;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Кранова колія: низький блок заввишки HEIGHT. Гравець ставить дві паралельні лінії таких блоків,
 * на відстані 2 * GAUGE блоків одна від одної (див. TileEntityCraneController).
 * Метадата це вісь: 0 = уздовж X, 1 = уздовж Z. Контролер сам вирівнює її після сканування.
 */
public class BlockCraneRail extends Block {

    /** Висота блока колії. Колеса крана стоять на цій висоті. */
    public static final float HEIGHT = 0.25F;

    @SideOnly(Side.CLIENT)
    private IIcon iconTopX;
    @SideOnly(Side.CLIENT)
    private IIcon iconTopZ;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockCraneRail() {
        super(Material.iron);
        setHardness(4.0F);
        setResistance(20.0F);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(SovietIndustryMod.tabSoviet);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, HEIGHT, 1.0F);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    /** Вісь за напрямом погляду: дивишся вздовж X дає 0, вздовж Z дає 1. */
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int dir = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, (dir & 1) == 1 ? 0 : 1, 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 1) return meta == 0 ? iconTopX : iconTopZ;
        return iconSide;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        iconTopX = reg.registerIcon(SovietIndustryMod.MODID + ":crane_rail_top_x");
        iconTopZ = reg.registerIcon(SovietIndustryMod.MODID + ":crane_rail_top_z");
        iconSide = reg.registerIcon(SovietIndustryMod.MODID + ":crane_rail_side");
        blockIcon = iconSide;
    }
}
