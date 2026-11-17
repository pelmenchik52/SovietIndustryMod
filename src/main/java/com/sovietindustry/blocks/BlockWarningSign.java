package com.sovietindustry.blocks;

import com.sovietindustry.SovietIndustryMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWarningSign extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon iconFront;
    @SideOnly(Side.CLIENT)
    private IIcon iconBack;

    public BlockWarningSign() {
        super(Material.iron);
        setHardness(0.8F);
        setResistance(3.0F);
        setStepSound(Block.soundTypeMetal);
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
    public int getRenderBlockPass() {
        return 1; // Альфа-канал для ідеальної прозорості навколо трикутника
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        float th = 0.0625F; // Товщина таблички всього 1 піксель!
        if (meta == 2) { // Північна стіна (прикріплена до Z=1, дивиться на Z-)
            setBlockBounds(0.0625F, 0.0625F, 1.0F - th, 0.9375F, 0.9375F, 1.0F);
        } else if (meta == 3) { // Південна стіна
            setBlockBounds(0.0625F, 0.0625F, 0.0F, 0.9375F, 0.9375F, th);
        } else if (meta == 4) { // Західна стіна
            setBlockBounds(1.0F - th, 0.0625F, 0.0625F, 1.0F, 0.9375F, 0.9375F);
        } else if (meta == 5) { // Східна стіна
            setBlockBounds(0.0F, 0.0625F, 0.0625F, th, 0.9375F, 0.9375F);
        } else {
            // На підлозі (лежача)
            setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, th, 0.9375F);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null; // Через тонку табличку можна проходити як через картину або факел
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        // side: 0=bottom, 1=top, 2=north, 3=south, 4=west, 5=east
        return side;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.iconFront = reg.registerIcon(SovietIndustryMod.MODID + ":warning_sign");
        this.iconBack = reg.registerIcon(SovietIndustryMod.MODID + ":electrical_cabinet_side");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        // Лицьова сторона таблички показує знак
        if (side == meta || (meta == 1 && side == 1) || (meta == 0 && side == 0)) {
            return this.iconFront;
        }
        return this.iconBack;
    }
}
