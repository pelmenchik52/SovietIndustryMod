package com.sovietindustry;

import com.sovietindustry.blocks.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SovietIndustryTab extends CreativeTabs {
    public SovietIndustryTab(String label) {
        super(label);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem() {
        return Item.getItemFromBlock(ModBlocks.slate);
    }
}
