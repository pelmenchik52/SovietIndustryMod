package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

/** Предмет труби з підтипами: tile.chimney.0 / .1 / .2 у lang-файлі. */
public class ItemBlockChimney extends ItemBlock {

    public ItemBlockChimney(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + "." + MathHelper.clamp_int(stack.getItemDamage(), 0, BlockChimney.VARIANTS - 1);
    }
}
