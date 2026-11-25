package com.sovietindustry.blocks;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.items.ItemWireCoil;
import com.sovietindustry.tileentities.Portal750;
import com.sovietindustry.tileentities.TileEntityLepDummy;
import com.sovietindustry.tileentities.TileEntityLepPylon;
import com.sovietindustry.tileentities.TileEntityPortal750;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Окремий портал 750 кВ: мультиблок 13 (ширина) x 1 x 23 (висота), див. Portal750.
 * Приймає дроти від анкерно-кутових опор 750 кВ, підстанції 750 кВ та інших порталів, до TileEntityPortal750.MAX_LINKS.
 * Поворот кратний 90 градусам, траверса ставиться впоперек погляду гравця.
 *
 * Майстер-блок стоїть під серединою траверси. Усі інші клітинки це dummy-блоки (lep110Dummy),
 * чия колізія береться з Portal750.localBox.
 */
public class BlockPortal750 extends BlockContainer {

    public BlockPortal750() {
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
        return new TileEntityPortal750();
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

    // ------------------------------------------------------------ колізія самого майстер-блока

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        float angle = 0.0F;
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityLepPylon) angle = ((TileEntityLepPylon) te).angle;
        float[] b = Portal750.worldBounds(0, 0, 0, angle);
        setBlockBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    // ------------------------------------------------------------ розстановка

    private static boolean freeCell(World w, int x, int y, int z) {
        Block b = w.getBlock(x, y, z);
        return w.isAirBlock(x, y, z) || b.isReplaceable(w, x, y, z);
    }

    /** Точна перевірка місця для вже відомого повороту: тверда земля лише під фундаментами, вільні клітинки каркаса. */
    private static boolean canBuild(World w, int x, int y, int z, int k) {
        if (y + Portal750.HEIGHT > 256) return false;
        for (int lx = Portal750.LX_MIN; lx <= Portal750.LX_MAX; lx++) {
            for (int lz = Portal750.LZ_MIN; lz <= Portal750.LZ_MAX; lz++) {
                int[] o = Portal750.toWorld(k, lx, lz);
                int bx = x + o[0];
                int bz = z + o[1];
                boolean footing = Portal750.hasBlock(lx, 0, lz) || (lx == 0 && lz == 0);
                if (footing && !LepMultiblock.isForce() && !World.doesBlockHaveSolidTopSurface(w, bx, y - 1, bz)) {
                    return false;
                }
                for (int dy = 0; dy < Portal750.HEIGHT; dy++) {
                    if (lx == 0 && lz == 0 && dy == 0) continue;
                    if (!Portal750.hasBlock(lx, dy, lz)) continue;
                    if (!freeCell(w, bx, y + dy, bz)) return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && y + Portal750.HEIGHT <= 256;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (world.isRemote) return;

        int dir = Math.round(placer.rotationYaw / 90.0F) & 3;
        float snapped = (dir * 90.0F) % 360.0F;
        int k = Portal750.quarter(snapped);

        if (!canBuild(world, x, y, z, k)) {
            world.setBlockToAir(x, y, z);
            if (placer instanceof EntityPlayer) {
                EntityPlayer p = (EntityPlayer) placer;
                p.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Not enough space: the portal needs a 13 block wide row, solid ground under the columns and 23 free blocks of height"));
                if (!p.capabilities.isCreativeMode) {
                    ItemStack back = new ItemStack(this);
                    if (!p.inventory.addItemStackToInventory(back)) {
                        world.spawnEntityInWorld(new EntityItem(world, x + 0.5D, y + 0.5D, z + 0.5D, back));
                    }
                }
            }
            return;
        }

        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileEntityPortal750)) return;
        ((TileEntityPortal750) te).setAngle(snapped);

        for (int lx = Portal750.LX_MIN; lx <= Portal750.LX_MAX; lx++) {
            for (int lz = Portal750.LZ_MIN; lz <= Portal750.LZ_MAX; lz++) {
                int[] o = Portal750.toWorld(k, lx, lz);
                for (int dy = 0; dy < Portal750.HEIGHT; dy++) {
                    if (lx == 0 && lz == 0 && dy == 0) continue;
                    if (!Portal750.hasBlock(lx, dy, lz)) continue;
                    setDummy(world, x + o[0], y + dy, z + o[1], x, y, z);
                }
            }
        }
        LepMultiblock.ejectEntities(world, x, y, z);
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
        float angle = 0.0F;
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityPortal750) {
            TileEntityPortal750 portal = (TileEntityPortal750) te;
            angle = portal.angle;
            portal.disconnectAndSync(); // прибрати всі дроти й вузол у мережі NTM
        }

        int k = Portal750.quarter(angle);
        for (int lx = Portal750.LX_MIN; lx <= Portal750.LX_MAX; lx++) {
            for (int lz = Portal750.LZ_MIN; lz <= Portal750.LZ_MAX; lz++) {
                int[] o = Portal750.toWorld(k, lx, lz);
                for (int dy = 0; dy < Portal750.HEIGHT; dy++) {
                    if (lx == 0 && lz == 0 && dy == 0) continue;
                    int bx = x + o[0];
                    int by = y + dy;
                    int bz = z + o[1];
                    if (world.getBlock(bx, by, bz) != ModBlocks.lep110Dummy) continue;
                    // прибираємо лише власні сегменти, а не dummy-блоки сусідніх опор
                    TileEntity dt = world.getTileEntity(bx, by, bz);
                    if (dt instanceof TileEntityLepDummy) {
                        TileEntityLepDummy d = (TileEntityLepDummy) dt;
                        if (d.valid && d.mx == x && d.my == y && d.mz == z) {
                            world.setBlockToAir(bx, by, bz);
                        }
                    }
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    /** Котушка NTM тут не приймається: дроти ведуться котушкою цього мода (вона перевіряє напругу й тип опори). */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem();
        if (held != null && ItemWireCoil.isNtmWiring(held)) {
            if (!world.isRemote) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Use the Soviet Industry Wire Coil on this 750 kV portal"));
            }
            return true;
        }
        return false;
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
