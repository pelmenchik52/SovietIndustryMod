package com.sovietindustry.items;

import java.util.List;

import com.sovietindustry.SovietIndustryMod;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Пульт керування портальним краном.
 * ПКМ по контролеру: прив'язати пульт до цього крана.
 * ПКМ у повітря (або по будь-якому іншому блоку): відкрити екран керування.
 */
public class ItemCraneRemote extends Item {

    public ItemCraneRemote() {
        setMaxStackSize(1);
        setUnlocalizedName("craneRemote");
        setTextureName(SovietIndustryMod.MODID + ":crane_remote");
        setCreativeTab(SovietIndustryMod.tabSoviet);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) SovietIndustryMod.proxy.openCraneRemote(stack);
        return stack;
    }

    public static void bind(ItemStack stack, int x, int y, int z, int dim) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setInteger("cx", x);
        tag.setInteger("cy", y);
        tag.setInteger("cz", z);
        tag.setInteger("cdim", dim);
    }

    public static boolean isBound(ItemStack stack) {
        NBTTagCompound tag = stack == null ? null : stack.getTagCompound();
        return tag != null && tag.hasKey("cx") && tag.hasKey("cy") && tag.hasKey("cz") && tag.hasKey("cdim");
    }

    /** Чи є в гравця пульт, прив'язаний саме до цього контролера. */
    public static boolean hasBoundRemote(EntityPlayer player, int x, int y, int z, int dim) {
        ItemStack[] inv = player.inventory.mainInventory;
        for (int i = 0; i < inv.length; i++) {
            ItemStack st = inv[i];
            if (st == null || !(st.getItem() instanceof ItemCraneRemote) || !isBound(st)) continue;
            NBTTagCompound tag = st.getTagCompound();
            if (tag.getInteger("cx") == x && tag.getInteger("cy") == y && tag.getInteger("cz") == z
                && tag.getInteger("cdim") == dim) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        if (isBound(stack)) {
            NBTTagCompound tag = stack.getTagCompound();
            list.add("Bound to: " + tag.getInteger("cx") + ", " + tag.getInteger("cy") + ", " + tag.getInteger("cz"));
        } else {
            list.add("Not bound. Right click a crane controller");
        }
    }
}
