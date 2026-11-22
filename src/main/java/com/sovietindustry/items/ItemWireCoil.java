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
import com.sovietindustry.tileentities.TileEntityLep110Angle;
import com.sovietindustry.tileentities.TileEntityLep110x2;
import com.sovietindustry.tileentities.TileEntityLep110x2Base;
import com.sovietindustry.tileentities.TileEntityLep330Angle;
import com.sovietindustry.tileentities.TileEntityLep330Base;
import com.sovietindustry.tileentities.TileEntityLep750Base;
import com.sovietindustry.tileentities.TileEntitySubstation330kV;
import com.sovietindustry.tileentities.TileEntityLepPylon;
import com.sovietindustry.tileentities.TileEntitySubstation110kV;
import com.sovietindustry.tileentities.TileEntitySubstationPort110kV;

/**
 * Wire Coil.
 * RMB on pylon: first click sets start, second connects (3 phases).
 * Shift+RMB on pylon: cuts all wires on this pylon.
 * RMB in air: cancels start selection.
 * Works with both this mod's pylons and NTM TRIPLE type pylons (medium pylons).
 */
public class ItemWireCoil extends Item {

    public ItemWireCoil() {
        setMaxStackSize(1);
        setUnlocalizedName("wireCoil");
        setTextureName(SovietIndustryMod.MODID + ":wire_coil");
        setCreativeTab(SovietIndustryMod.tabSoviet);
    }

    /** True for the NTM wire spool (com.hbm.items.tool.ItemWiring), matched by name to avoid a hard link. */
    public static boolean isNtmWiring(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        Class<?> c = stack.getItem().getClass();
        while (c != null && c != Item.class) {
            if (c.getName().equals("com.hbm.items.tool.ItemWiring")) return true;
            c = c.getSuperclass();
        }
        return false;
    }

    /**
     * Finds pylon TE by any multiblock block (dummy or master, ours or NTM).
     * For the substation the clicked dummy decides the target: the 110 kV (front) side leads to the
     * 3-phase port, everything else to the HV (QUAD) portal.
     */
    public static TileEntityPylonBase resolve(World world, int x, int y, int z) {
        int clickX = x, clickZ = z;
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
        if (te instanceof TileEntitySubstation110kV) {
            return ((TileEntitySubstation110kV) te).getWireTarget(clickX - x, clickZ - z);
        }
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

    /**
     * Правила для 6-контактних опор. Повертає текст помилки або null, якщо з'єднання дозволене:
     *  - 6-контактна <-> 6-контактна: усі 6 дротів;
     *  - 6-контактна АНКЕРНО-КУТОВА <-> 3-контактна анкерно-кутова: одне коло (3 дроти), тож АКО
     *    розходиться на дві 3-контактні;
     *  - 6-контактна АНКЕРНО-КУТОВА <-> портал 110 кВ підстанції: одне коло (3 дроти) з одного боку опори,
     *    друге коло лишається для лінії;
     *  - проміжна 6-контактна приймає лише інші 6-контактні опори.
     * Місткість рахується по точках кріплення (до двох дротів на точку), а не загальним лічильником.
     */
    private static String sixWireCheck(TileEntityPylonBase first, TileEntityPylonBase second) {
        boolean a = first instanceof TileEntityLep110x2Base;
        boolean b = second instanceof TileEntityLep110x2Base;
        if (!a && !b) return null;
        if (a && b) {
            if (!((TileEntityLep110x2Base) first).canAcceptLink(second)
                || !((TileEntityLep110x2Base) second).canAcceptLink(first)) {
                return "No free contacts left on a 6-wire tower (max two wires per contact)";
            }
            return null;
        }
        TileEntityLep110x2Base six = (TileEntityLep110x2Base) (a ? first : second);
        TileEntityPylonBase other = a ? second : first;
        if (six instanceof TileEntityLep110x2) {
            return "An intermediate 6-wire tower connects only to other 6-wire towers";
        }
        if (!(other instanceof TileEntityLep110Angle) && !(other instanceof TileEntitySubstationPort110kV)) {
            return "A 6-wire angle tower can only connect to 3-wire angle towers or a substation 110 kV port";
        }
        if (!six.canAcceptLink(other)) {
            return "No free 3-wire circuit left on this 6-wire tower";
        }
        return null;
    }

    /** Опори рівня 330 кВ: лінійні опори 330 кВ і підстанція 330 кВ. */
    private static boolean is330(TileEntityPylonBase t) {
        return t instanceof TileEntityLep330Base || t instanceof TileEntitySubstation330kV;
    }

    /** Опори рівня 750 кВ: лінійні опори 750 кВ. */
    private static boolean is750(TileEntityPylonBase t) {
        return t instanceof TileEntityLep750Base;
    }

    /**
     * Правила підстанції 330 кВ: вхід приймає дріт ТІЛЬКИ від анкерно-кутової опори 330 кВ (У330-3),
     * і лише один прольот. Повертає текст помилки або null, якщо з'єднання дозволене.
     */
    private static String substation330Check(TileEntityPylonBase first, TileEntityPylonBase second) {
        boolean a = first instanceof TileEntitySubstation330kV;
        boolean b = second instanceof TileEntitySubstation330kV;
        if (!a && !b) return null;
        TileEntitySubstation330kV sub = (TileEntitySubstation330kV) (a ? first : second);
        TileEntityPylonBase other = a ? second : first;
        if (!(other instanceof TileEntityLep330Angle)) {
            return "The 330 kV substation accepts a wire only from a 330 kV angle tower (U330-3)";
        }
        if (!sub.getLinks().isEmpty()) {
            return "The 330 kV substation input is already connected";
        }
        return null;
    }

    /**
     * Обмеження довжини прольоту за типом опори: 110 кВ 3-контактна 75, 110 кВ 6-контактна 125, 330 кВ 200 блоків.
     * Для пари береться менший ліміт (6-контактна анкерна з 3-контактною анкерною, значить 75).
     * Повертає текст помилки або null.
     */
    private static String spanCheck(TileEntityPylonBase first, TileEntityPylonBase second) {
        double limit = Double.MAX_VALUE;
        if (first instanceof TileEntityLepPylon) limit = Math.min(limit, ((TileEntityLepPylon) first).getMaxWireLength());
        if (second instanceof TileEntityLepPylon) limit = Math.min(limit, ((TileEntityLepPylon) second).getMaxWireLength());
        if (limit == Double.MAX_VALUE) return null;
        double dx = first.xCoord - second.xCoord;
        double dy = first.yCoord - second.yCoord;
        double dz = first.zCoord - second.zCoord;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > limit) {
            return "Span too long: " + (int) Math.ceil(dist) + " blocks, maximum for this pair is " + (int) limit;
        }
        return null;
    }

    private static boolean full(TileEntityPylonBase t) {
        if (t instanceof TileEntityLep110x2Base) return false; // місткість перевіряє sixWireCheck
        return t instanceof TileEntityLepPylon
            && ((TileEntityLepPylon) t).getLinks().size() >= TileEntityLepPylon.MAX_LINE_CONNECTIONS;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                             int side, float hx, float hy, float hz) {
        if (world.isRemote) return true;

        TileEntityPylonBase pylon = resolve(world, x, y, z);

        if (player.isSneaking()) {
            if (pylon instanceof TileEntityLepPylon) {
                ((TileEntityLepPylon) pylon).disconnectAndSync();
                msg(player, EnumChatFormatting.YELLOW, "All wires on this pylon have been cut");
            } else if (pylon != null) {
                pylon.disconnectAll();
                msg(player, EnumChatFormatting.YELLOW, "All wires on this pylon have been cut");
            }
            clear(stack);
            return true;
        }

        if (pylon == null) {
            msg(player, EnumChatFormatting.RED, "Not a power pylon");
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
            msg(player, EnumChatFormatting.GREEN, "Wire start: " + pylon.xCoord + " " + pylon.yCoord + " " + pylon.zCoord);
            return true;
        }

        int sx = tag.getInteger("sx"), sy = tag.getInteger("sy"), sz = tag.getInteger("sz");
        clear(stack);

        TileEntity ft = world.blockExists(sx, sy, sz) ? world.getTileEntity(sx, sy, sz) : null;
        if (!(ft instanceof TileEntityPylonBase)) {
            msg(player, EnumChatFormatting.RED, "Start pylon is missing or unloaded");
            return true;
        }
        TileEntityPylonBase first = (TileEntityPylonBase) ft;
        TileEntityPylonBase second = pylon;

        if (first == second) {
            msg(player, EnumChatFormatting.RED, "Cannot connect a pylon to itself");
            return true;
        }
        if (first instanceof TileEntityLepPylon && second instanceof TileEntityLepPylon
            && is330(first) != is330(second)) {
            msg(player, EnumChatFormatting.RED, "Voltage mismatch: 330 kV towers connect only to 330 kV towers");
            return true;
        }
        if (first instanceof TileEntityLepPylon && second instanceof TileEntityLepPylon
            && is750(first) != is750(second)) {
            msg(player, EnumChatFormatting.RED, "Voltage mismatch: 750 kV towers connect only to 750 kV towers");
            return true;
        }
        if (first.getConnectionType() != second.getConnectionType()) {
            msg(player, EnumChatFormatting.RED, "Pylon types do not match (phase count mismatch)");
            return true;
        }
        String spanErr = spanCheck(first, second);
        if (spanErr != null) {
            msg(player, EnumChatFormatting.RED, spanErr);
            return true;
        }
        if (TileEntityPylonBase.canConnect(first, second) != 0) {
            msg(player, EnumChatFormatting.RED, "Distance between pylons is too far");
            return true;
        }
        if (linked(first, second)) {
            msg(player, EnumChatFormatting.RED, "These pylons are already connected");
            return true;
        }
        String subErr = substation330Check(first, second);
        if (subErr != null) {
            msg(player, EnumChatFormatting.RED, subErr);
            return true;
        }
        String sixErr = sixWireCheck(first, second);
        if (sixErr != null) {
            msg(player, EnumChatFormatting.RED, sixErr);
            return true;
        }
        if (full(first) || full(second)) {
            msg(player, EnumChatFormatting.RED, "Pylon already has two connections (input and output)");
            return true;
        }

        first.addConnection(second.xCoord, second.yCoord, second.zCoord);
        second.addConnection(first.xCoord, first.yCoord, first.zCoord);
        world.markBlockForUpdate(first.xCoord, first.yCoord, first.zCoord);
        world.markBlockForUpdate(second.xCoord, second.yCoord, second.zCoord);

        msg(player, EnumChatFormatting.GREEN, "Pylons connected");
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && stack.getTagCompound() != null && stack.getTagCompound().hasKey("sx")) {
            clear(stack);
            msg(player, EnumChatFormatting.GRAY, "Wire start selection cancelled");
        }
        return stack;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv) {
        list.add("RMB on pylon: set start, then end of wire");
        list.add("Shift+RMB on pylon: cut all wires");
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("sx")) {
            list.add("Start: " + tag.getInteger("sx") + " " + tag.getInteger("sy") + " " + tag.getInteger("sz"));
        }
    }
}