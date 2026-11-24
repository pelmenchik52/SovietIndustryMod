package com.sovietindustry.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Предмет усіх опор ЛЕП. Додає режим «ставити не дивлячись на блоки»: клік у повітря з опорою в руці вмикає/вимикає
 * режим (прапорець у NBT стека). У режимі опора ставиться без перевірок (не потрібні тверда основа й вільне місце),
 * усе, що стоїть у її габариті, замінюється (без дропу). Звичайне ставлення працює як раніше.
 */
public class ItemBlockLep extends ItemBlock {
    public static final String TAG = "ForcePlace";

    public ItemBlockLep(Block block) {
        super(block);
    }

    public static boolean isForce(ItemStack stack) {
        return stack != null && stack.hasTagCompound() && stack.getTagCompound().getBoolean(TAG);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        boolean now = !isForce(stack);
        if (now) {
            if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setBoolean(TAG, true);
        } else {
            stack.getTagCompound().removeTag(TAG);
            // порожній тег прибираємо, щоб стеки знову склеювались
            if (stack.getTagCompound().hasNoTags()) stack.setTagCompound(null);
        }
        if (!world.isRemote) {
            player.addChatMessage(new ChatComponentText(now
                ? EnumChatFormatting.GOLD + "Force placement ON: ignores blocks and ground, replaces everything in the way"
                : EnumChatFormatting.GRAY + "Force placement OFF"));
        }
        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ) {
        if (!isForce(stack)) {
            return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
        }
        Block blk = Block.getBlockFromItem(this);
        Block clicked = world.getBlock(x, y, z);
        if (clicked == Blocks.snow_layer && (world.getBlockMetadata(x, y, z) & 7) < 1) {
            side = 1;
        } else if (clicked != Blocks.vine && clicked != Blocks.tallgrass && clicked != Blocks.deadbush
            && !clicked.isReplaceable(world, x, y, z)) {
            if (side == 0) --y;
            if (side == 1) ++y;
            if (side == 2) --z;
            if (side == 3) ++z;
            if (side == 4) --x;
            if (side == 5) ++x;
        }
        if (stack.stackSize == 0) return false;
        if (!player.canPlayerEdit(x, y, z, side, stack)) return false;
        if (y < 1 || y >= 255) return false;

        int meta = blk.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, getMetadata(stack.getItemDamage()));
        LepMultiblock.setForce(true);
        try {
            if (placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, meta)) {
                world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, blk.stepSound.func_150496_b(),
                    (blk.stepSound.getVolume() + 1.0F) / 2.0F, blk.stepSound.getPitch() * 0.8F);
                --stack.stackSize; // у творчому режимі кількість відновлює сам ItemStack.tryPlaceItemIntoWorld
            }
        } finally {
            LepMultiblock.setForce(false);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (isForce(stack)) {
            tooltip.add(EnumChatFormatting.GOLD + "Force placement: ON");
        }
        tooltip.add(EnumChatFormatting.DARK_GRAY + "Right-click in the air: toggle force placement");
    }
}
