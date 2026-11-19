package com.sovietindustry.blocks;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.tileentities.TileEntityLepDummy;
import com.sovietindustry.tileentities.TileEntitySubstation110kV;
import com.sovietindustry.tileentities.TileEntitySubstationPort110kV;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/**
 * Трансформаторна підстанція ВРП 110 кВ (мультиблок 5x5x7).
 */
public class BlockSubstation110kV extends BlockContainer {

    public static final int HALF_SIZE = 2; // 5x5 (-2..+2)
    public static final int HEIGHT = 7;

    public BlockSubstation110kV() {
        super(Material.iron);
        setHardness(8.0F);
        setResistance(40.0F);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 2);
        setCreativeTab(SovietIndustryMod.tabSoviet);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntitySubstation110kV();
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
        for (int dx = -HALF_SIZE; dx <= HALF_SIZE; dx++) {
            for (int dz = -HALF_SIZE; dz <= HALF_SIZE; dz++) {
                if (!World.doesBlockHaveSolidTopSurface(world, x + dx, y - 1, z + dz)) {
                    return false;
                }
                for (int dy = 0; dy < HEIGHT; dy++) {
                    if (y + dy >= 256) return false;
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    Block b = world.getBlock(x + dx, y + dy, z + dz);
                    if (!world.isAirBlock(x + dx, y + dy, z + dz) && !b.isReplaceable(world, x + dx, y + dy, z + dz)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int dir = Math.round(placer.rotationYaw / 90.0F) & 3;
        float snapped = dir * 90.0F;

        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileEntitySubstation110kV)) return;
        TileEntitySubstation110kV sub = (TileEntitySubstation110kV) te;
        sub.setAngle(snapped);

        int[] portOff = sub.getPort110Offset();
        int px = x + portOff[0];
        int pz = z + portOff[2];

        // Заповнюємо плиту 5x5 та габарити обладнання
        for (int dx = -HALF_SIZE; dx <= HALF_SIZE; dx++) {
            for (int dz = -HALF_SIZE; dz <= HALF_SIZE; dz++) {
                int bx = x + dx;
                int bz = z + dz;

                if (bx == x && bz == z) {
                    // Центральний майстер-блок вже стоїть
                } else if (bx == px && bz == pz) {
                    // Ставимо окремий вузол-порт для 3-фазної ЛЕП 110 кВ
                    world.setBlock(bx, y, bz, ModBlocks.substationPort110kV, 0, 2);
                    TileEntity pte = world.getTileEntity(bx, y, bz);
                    if (pte instanceof TileEntitySubstationPort110kV) {
                        ((TileEntitySubstationPort110kV) pte).setAngle(snapped);
                    }
                } else {
                    setDummy(world, bx, y, bz, x, y, z);
                }

                // Верхні габаритні dummy-блоки: бак і вводи в центрі, стійки порталів по кутах.
                // Портал ВН (локально z < 0) вищий (7 блоків), портал 110 кВ (z > 0) нижчий (5 блоків).
                double[] local = sub.toLocalOffset(dx, dz);
                boolean isCenterEquip = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
                boolean isPortalPillar = (Math.abs(dx) == 2 && Math.abs(dz) == 2);
                int maxH = isPortalPillar ? (local[1] < 0.0D ? 7 : 5) : (isCenterEquip ? 4 : 1);
                for (int dy = 1; dy < maxH; dy++) {
                    setDummy(world, bx, y + dy, bz, x, y, z);
                }
            }
        }
    }

    private static void setDummy(World w, int x, int y, int z, int mx, int my, int mz) {
        w.setBlock(x, y, z, ModBlocks.lep110Dummy, 0, 2);
        TileEntity te = w.getTileEntity(x, y, z);
        if (te instanceof TileEntityLepDummy) {
            ((TileEntityLepDummy) te).setMaster(mx, my, mz);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntitySubstation110kV) {
            ((TileEntitySubstation110kV) te).disconnectAndSync();
        }

        for (int dx = -HALF_SIZE; dx <= HALF_SIZE; dx++) {
            for (int dz = -HALF_SIZE; dz <= HALF_SIZE; dz++) {
                for (int dy = 0; dy < HEIGHT; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    int bx = x + dx;
                    int by = y + dy;
                    int bz = z + dz;
                    Block b = world.getBlock(bx, by, bz);
                    if (b == ModBlocks.substationPort110kV) {
                        TileEntity pte = world.getTileEntity(bx, by, bz);
                        if (pte instanceof TileEntitySubstationPort110kV) {
                            ((TileEntitySubstationPort110kV) pte).disconnectAndSync();
                        }
                        world.setBlockToAir(bx, by, bz);
                    } else if (b == ModBlocks.lep110Dummy) {
                        world.setBlockToAir(bx, by, bz);
                    }
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        blockIcon = reg.registerIcon("sovietindustry:pdn_slab");
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemStack(this);
    }
}