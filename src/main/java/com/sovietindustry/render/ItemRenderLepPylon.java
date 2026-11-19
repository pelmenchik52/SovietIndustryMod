package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.models.ModelLep110Angle;
import com.sovietindustry.models.ModelLep110v1;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

@SideOnly(Side.CLIENT)
public class ItemRenderLepPylon implements IItemRenderer {

    private static final ResourceLocation TEX_V1 =
        new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep110_single.png");
    private static final ResourceLocation TEX_ANGLE =
        new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep_steel.png");

    private final ModelLep110v1 modelV1 = new ModelLep110v1();
    private final ModelLep110Angle modelAngle = new ModelLep110Angle();
    private final boolean isAngle;

    public ItemRenderLepPylon(boolean isAngle) {
        this.isAngle = isAngle;
    }

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
        GL11.glPushMatrix();

        switch (type) {
            case INVENTORY:
                // Опора висотою ~13.25 блока: зменшуємо, щоб умістилась у 1x1 слот інвентаря по висоті
                GL11.glTranslatef(0.0F, -0.58F, 0.0F);
                GL11.glScalef(0.085F, 0.085F, 0.085F);
                GL11.glRotatef(25.0F, 0.0F, 1.0F, 0.0F);
                break;

            case EQUIPPED:
                // У руці від 3-ї особи
                GL11.glTranslatef(0.5F, 0.15F, 0.5F);
                GL11.glScalef(0.11F, 0.11F, 0.11F);
                GL11.glRotatef(35.0F, 0.0F, 1.0F, 0.0F);
                break;

            case EQUIPPED_FIRST_PERSON:
                // У руці від 1-ї особи
                GL11.glTranslatef(0.55F, 0.05F, 0.45F);
                GL11.glScalef(0.10F, 0.10F, 0.10F);
                GL11.glRotatef(-35.0F, 0.0F, 1.0F, 0.0F);
                break;

            case ENTITY:
                // Викинутий предмет на землі
                GL11.glTranslatef(0.0F, -0.25F, 0.0F);
                GL11.glScalef(0.09F, 0.09F, 0.09F);
                break;

            default:
                GL11.glScalef(0.09F, 0.09F, 0.09F);
                break;
        }

        if (isAngle) {
            Minecraft.getMinecraft().renderEngine.bindTexture(TEX_ANGLE);
            GL11.glDisable(GL11.GL_CULL_FACE);
            modelAngle.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
        } else {
            Minecraft.getMinecraft().renderEngine.bindTexture(TEX_V1);
            // Для v1 зберігаємо той самий переворот осі Y, що й у RenderLep110v1
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            modelV1.renderAll();
        }

        GL11.glPopMatrix();
    }
}