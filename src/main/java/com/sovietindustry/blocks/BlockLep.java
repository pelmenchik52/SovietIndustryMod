package com.sovietindustry.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Опора ЛЕП СВ-95. Один клас, два типи:
 *  POLE     - звичайний сегмент залізобетонної стійки (ставиться стовпчиком вгору)
 *  CROSSARM - верхній сегмент: стійка + траверса + 3 гірлянди ізоляторів
 *
 * metadata (лише для CROSSARM): 0 = траверса вздовж осі X, 1 = вздовж осі Z.
 * Рендер: LepRenderer (ISimpleBlockRenderingHandler), renderId виставляється у ClientProxy.
 */
public class BlockLep extends Block {
    public static final int POLE = 0;
    public static final int CROSSARM = 1;

    /** Заповнюється у ClientProxy через RenderingRegistry.getNextAvailableRenderId(). */
    public static int renderId = 0;

    private final int type;

    public IIcon iconSide;
    public IIcon iconTop;
    public IIcon iconArm;
    public IIcon iconInsulator;

    public BlockLep(int type) {
        super(Material.rock);
        this.type = type;
        setHardness(3.0F);
        setResistance(10.0F);
        setStepSound(soundTypeStone);
        setCreativeTab(CreativeTabs.tabDecorations); // заміни на свою вкладку
    }

    public int getType() {
        return type;
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
        iconSide = reg.registerIcon("sovietindustry:lep_pole_side");
        iconTop = reg.registerIcon("sovietindustry:lep_pole_top");
        iconArm = reg.registerIcon("sovietindustry:lep_arm");
        iconInsulator = reg.registerIcon("sovietindustry:lep_insulator");
        blockIcon = iconSide;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return (side == 0 || side == 1) ? iconTop : iconSide;
    }

    // ------------------------------------------------------------ розміщення
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (type == CROSSARM) {
            int facing = MathHelper.floor_double((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            // гравець дивиться вздовж Z (0,2) -> траверса вздовж X (meta 0); вздовж X (1,3) -> вздовж Z (meta 1)
            world.setBlockMetadataWithNotify(x, y, z, facing & 1, 2);
        }
    }

    // ------------------------------------------------------------ форма
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        if (type == CROSSARM) {
            if ((world.getBlockMetadata(x, y, z) & 1) == 0) {
                setBlockBounds(0F, 0F, 5F / 16F, 1F, 1F, 11F / 16F);
            } else {
                setBlockBounds(5F / 16F, 0F, 0F, 11F / 16F, 1F, 1F);
            }
        } else {
            setBlockBounds(5F / 16F, 0F, 5F / 16F, 11F / 16F, 1F, 11F / 16F);
        }
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(0F, 0F, 0F, 1F, 1F, 1F);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity) {
        // стійка
        setBlockBounds(5F / 16F, 0F, 5F / 16F, 11F / 16F, 1F, 11F / 16F);
        super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);

        // траверса (ізолятори без колізії)
        if (type == CROSSARM) {
            if ((world.getBlockMetadata(x, y, z) & 1) == 0) {
                setBlockBounds(0F, 10F / 16F, 6F / 16F, 1F, 12F / 16F, 10F / 16F);
            } else {
                setBlockBounds(6F / 16F, 10F / 16F, 0F, 10F / 16F, 12F / 16F, 1F);
            }
            super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
        }
        setBlockBoundsBasedOnState(world, x, y, z);
    }

    // ------------------------------------------------------------ для майбутніх проводів
    /**
     * Точки кріплення проводів: низ кожної з трьох гірлянд ізоляторів,
     * у локальних координатах блока (0..1). Додай x,y,z блока, щоб отримати світові.
     */
    public static double[][] getWireAnchors(int meta) {
        boolean alongX = (meta & 1) == 0;
        double[] along = { 2.0 / 16.0, 8.0 / 16.0, 14.0 / 16.0 };
        double[][] r = new double[3][];
        for (int i = 0; i < 3; i++) {
            r[i] = alongX ? new double[] { along[i], 3.0 / 16.0, 0.5 }
                          : new double[] { 0.5, 3.0 / 16.0, along[i] };
        }
        return r;
    }
}
