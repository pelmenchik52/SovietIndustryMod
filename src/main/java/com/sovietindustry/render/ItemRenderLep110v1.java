package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.models.ModelLep110v1;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemRenderLep110v1 implements IItemRenderer {
    private static final ResourceLocation texture = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep110_single.png");
    private final ModelLep110v1 model = new ModelLep110v1();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        GL11.glPushMatrix();

        if (type == ItemRenderType.INVENTORY) {
            GL11.glTranslatef(8.0F, 15.0F, 0.0F);
            GL11.glScalef(1.2F, 1.2F, 1.2F);
            GL11.glRotatef(25.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(0.065F, -0.065F, -0.065F);
        } else if (type == ItemRenderType.EQUIPPED) {
            GL11.glTranslatef(0.5F, 0.2F, 0.5F);
            GL11.glScalef(0.08F, -0.08F, -0.08F);
        } else if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GL11.glTranslatef(0.5F, 0.3F, 0.5F);
            GL11.glRotatef(30.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(0.07F, -0.07F, -0.07F);
        } else if (type == ItemRenderType.ENTITY) {
            GL11.glTranslatef(0.0F, 0.0F, 0.0F);
            GL11.glScalef(0.08F, -0.08F, -0.08F);
        }

        model.renderAll();

        GL11.glPopMatrix();
    }
}
