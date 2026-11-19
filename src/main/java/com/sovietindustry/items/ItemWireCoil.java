package com.sovietindustry.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.network.TileEntityPylonBase;
import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.blocks.ModBlocks;
import com.sovietindustry.tileentities.TileEntityLepDummy;
import com.sovietindustry.tileentities.TileEntityLepPylon;

/**
 * Котушка проводу.
 *  ПКМ по опорі: перший клік запам'ятовує початок, другий з'єднує (3 фази).
 *  Shift+ПКМ по опорі: зрізає всі дроти цієї опори.
 *  ПКМ у повітря: скасовує вибір початку.
 * Працює і з опорами цього мода, і з опорами NTM типу TRIPLE (середні пілони).
 */
public class ItemWireCoil extends Item {

    public ItemWireCoil() {
        setMaxStackSize(1);
        setUnlocalizedName("wireCoil");
        setTextureName(SovietIndustryMod.MODID + ":wire_coil");
        setCreativeTab(SovietIndustryMod.tabSoviet);
    }

    /** Знаходить TE опори за будь-яким блоком мультиблока (dummy чи майстер, наш чи NTM). */
    public static TileEntityPylonBase resolve(World world, int x, int y, int z) {
        Block b = world.getBlock(x, y, z);
        if (b == ModBlocks.lep110Dummy) {
            TileEntity d = world.getTileEntity(x, y, z);
            if (d instanceof TileEntityLepDummy && ((TileEntityLepDummy) d).valid) {
                TileEntityLepDummy dd = (TileEntityLepDummy) d;
                x = dd.mx;
                y = dd.my;
                z = dd.mz;
            } else {
                return null;
            }
        }
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityPylonBase) {
            return (TileEntityPylonBase) te;
        }
        Block blk = world.getBlock(x, y, z);
        if (blk instanceof BlockDummyable) {
            int[] core = ((BlockDummyable) blk).findCore(world, x, y, z);
            if (core != null) {
                TileEntity ct = world.getTileEntity(core[0], core[1], core[2]);
                if (ct instanceof TileEntityPylonBase) return (TileEntityPylonBase) ct;
            }
        }
        return null;
    }

    private static void msg(EntityPlayer p, EnumChatFormatting color, String text) {
        p.addChatMessage(new ChatComponentText(color + text));
    }

    private static void clear(ItemStack stack) {
        if (stack.getTagCompound() != null) {
            stack.getTagCompound().removeTag("sx");
            stack.getTagCompound().removeTag("sy");
            stack.getTagCompound().removeTag("sz");
        }
    }

    private static boolean linked(TileEntityPylonBase a, TileEntityPylonBase b) {
        for (Object o : a.getConnected()) {
            int[] p = (int[]) o;
            if (p[0] == b.xCoord && p[1] == b.yCoord && p[2] == b.zCoord) return true;
        }
        return false;
    }

    private static boolean full(TileEntityPylonBase t) {
        return t instanceof TileEntityLepPylon
            && t.getConnected().size() >= TileEntityLepPylon.MAX_LINE_CONNECTIONS;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                             int side, float hx, float hy, float hz) {
        if (world.isRemote) return true;

        TileEntityPylonBase pylon = resolve(world, x, y, z);

        if (player.isSneaking()) {
            if (pylon instanceof TileEntityLepPylon) {
                ((TileEntityLepPylon) pylon).disconnectAndSync();
                msg(player, EnumChatFormatting.YELLOW, "Усі проводи цієї опори зрізано");
            } else if (pylon != null) {
                pylon.disconnectAll();
                msg(player, EnumChatFormatting.YELLOW, "Усі проводи цієї опори зрізано");
            }
            clear(stack);
            return true;
        }

        if (pylon == null) {
            msg(player, EnumChatFormatting.RED, "Це не опора ЛЕП");
            return true;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        if (!tag.hasKey("sx")) {
            tag.setInteger("sx", pylon.xCoord);
            tag.setInteger("sy", pylon.yCoord);
            tag.setInteger("sz", pylon.zCoord);
            msg(player, EnumChatFormatting.GREEN, "Початок проводу: " + pylon.xCoord + " " + pylon.yCoord + " " + pylon.zCoord);
            return true;
        }

        int sx = tag.getInteger("sx"), sy = tag.getInteger("sy"), sz = tag.getInteger("sz");
        clear(stack);

        TileEntity ft = world.blockExists(sx, sy, sz) ? world.getTileEntity(sx, sy, sz) : null;
        if (!(ft instanceof TileEntityPylonBase)) {
            msg(player, EnumChatFormatting.RED, "Початкова опора зникла або не завантажена");
            return true;
        }
        TileEntityPylonBase first = (TileEntityPylonBase) ft;
        TileEntityPylonBase second = pylon;

        if (first == second) {
            msg(player, EnumChatFormatting.RED, "Не можна з'єднати опору саму з собою");
            return true;
        }
        if (first.getConnectionType() != second.getConnectionType()) {
            msg(player, EnumChatFormatting.RED, "Опори різного типу (кількість фаз не збігається)");
            return true;
        }
        if (TileEntityPylonBase.canConnect(first, second) != 0) {
            msg(player, EnumChatFormatting.RED, "Завелика відстань між опорами");
            return true;
        }
        if (linked(first, second)) {
            msg(player, EnumChatFormatting.RED, "Ці опори вже з'єднані");
            return true;
        }
        if (full(first) || full(second)) {
            msg(player, EnumChatFormatting.RED, "На опорі вже є два проводи (вхід і вихід)");
            return true;
        }

        first.addConnection(second.xCoord, second.yCoord, second.zCoord);
        second.addConnection(first.xCoord, first.yCoord, first.zCoord);
        world.markBlockForUpdate(first.xCoord, first.yCoord, first.zCoord);
        world.markBlockForUpdate(second.xCoord, second.yCoord, second.zCoord);

        msg(player, EnumChatFormatting.GREEN, "Опори з'єднано");
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && stack.getTagCompound() != null && stack.getTagCompound().hasKey("sx")) {
            clear(stack);
            msg(player, EnumChatFormatting.GRAY, "Вибір початку скасовано");
        }
        return stack;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv) {
        list.add("ПКМ по опорі: початок, потім кінець проводу");
        list.add("Shift+ПКМ по опорі: зрізати всі проводи");
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("sx")) {
            list.add("Початок: " + tag.getInteger("sx") + " " + tag.getInteger("sy") + " " + tag.getInteger("sz"));
        }
    }
}
