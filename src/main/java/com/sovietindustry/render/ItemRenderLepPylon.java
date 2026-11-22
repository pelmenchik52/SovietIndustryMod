package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.models.ModelLep110Angle;
import com.sovietindustry.models.ModelLep110v1;
import com.sovietindustry.models.ModelLep110x2;
import com.sovietindustry.models.ModelLep330;
import com.sovietindustry.models.ModelLep750;
import com.sovietindustry.models.ModelSubstation110kV;
import com.sovietindustry.models.ModelSubstation330kV;

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
    public static final int KIND_X2 = 3;
    public static final int KIND_X2_ANGLE = 4;
    public static final int KIND_330 = 5;
    public static final int KIND_330_ANGLE = 6;
    public static final int KIND_SUBSTATION_330 = 7;
    public static final int KIND_750 = 8;
    public static final int KIND_750_ANGLE = 9;

    private final ModelSubstation330kV modelSubstation330 = new ModelSubstation330kV();

    private final ModelLep330 model330 = new ModelLep330(false);
    private final ModelLep330 model330Angle = new ModelLep330(true);

    private final ModelLep750 model750 = new ModelLep750(false);
    private final ModelLep750 model750Angle = new ModelLep750(true);

    private final ModelLep110x2 modelX2 = new ModelLep110x2(false);
    private final ModelLep110x2 modelX2Angle = new ModelLep110x2(true);
    private int x2Kind = 0;
    private final boolean isAngle;
    private final boolean isSubstation;
    private final ModelSubstation110kV modelSubstation = new ModelSubstation110kV();

    public ItemRenderLepPylon(boolean isAngle) {
        this(isAngle, false);
    }

    /** isSubstation = true малює трансформаторну підстанцію (isAngle тоді ігнорується). */
    public ItemRenderLepPylon(boolean isAngle, boolean isSubstation) {
        this.isAngle = isAngle;
        this.isSubstation = isSubstation;
    }

    /** Для 6-контактних опор: KIND_X2 (проміжна) або KIND_X2_ANGLE (анкерно-кутова). */
    public ItemRenderLepPylon(int kind) {
        this(false, false);
        this.x2Kind = kind;
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

        if (x2Kind == KIND_SUBSTATION_330) {
            applySubstation330Transform(type);
            GL11.glDisable(GL11.GL_CULL_FACE);
            modelSubstation330.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glPopMatrix();
            return;
        }

        if (x2Kind == KIND_750 || x2Kind == KIND_750_ANGLE) {
            apply750Transform(type);
            GL11.glDisable(GL11.GL_CULL_FACE);
            (x2Kind == KIND_750_ANGLE ? model750Angle : model750).renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glPopMatrix();
            return;
        }

        if (x2Kind == KIND_330 || x2Kind == KIND_330_ANGLE) {
            apply330Transform(type);
            GL11.glDisable(GL11.GL_CULL_FACE);
            (x2Kind == KIND_330_ANGLE ? model330Angle : model330).renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glPopMatrix();
            return;
        }

        if (x2Kind != 0) {
            applyX2Transform(type);
            GL11.glDisable(GL11.GL_CULL_FACE);
            (x2Kind == KIND_X2_ANGLE ? modelX2Angle : modelX2).renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glPopMatrix();
            return;
        }

        if (isSubstation) {
            applySubstationTransform(type);
            modelSubstation.renderAll();
            GL11.glPopMatrix();
            return;
        }

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

    /** Підстанція ~5x5 блоків при висоті ~7: ставимо під кутом, щоб в іконці було видно портали та бак. */
    private static void applySubstationTransform(ItemRenderType type) {
        switch (type) {
            case INVENTORY:
                GL11.glTranslatef(0.0F, -0.50F, 0.0F);
                GL11.glScalef(0.115F, 0.115F, 0.115F);
                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(35.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EQUIPPED:
                GL11.glTranslatef(0.5F, 0.15F, 0.5F);
                GL11.glScalef(0.09F, 0.09F, 0.09F);
                GL11.glRotatef(35.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EQUIPPED_FIRST_PERSON:
                GL11.glTranslatef(0.55F, 0.05F, 0.45F);
                GL11.glScalef(0.08F, 0.08F, 0.08F);
                GL11.glRotatef(-35.0F, 0.0F, 1.0F, 0.0F);
                break;
            case ENTITY:
                GL11.glTranslatef(0.0F, -0.25F, 0.0F);
                GL11.glScalef(0.07F, 0.07F, 0.07F);
                break;
            default:
                GL11.glScalef(0.07F, 0.07F, 0.07F);
                break;
        }
    }

    /** Підстанція 330 кВ 7x6x8 (модель центрована по плиті): трохи менший масштаб, ніж у підстанції 110 кВ. */
    private static void applySubstation330Transform(ItemRenderType type) {
        switch (type) {
            case INVENTORY:
                GL11.glTranslatef(0.0F, -0.50F, 0.0F);
                GL11.glScalef(0.092F, 0.092F, 0.092F);
                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(35.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EQUIPPED:
                GL11.glTranslatef(0.5F, 0.15F, 0.5F);
                GL11.glScalef(0.075F, 0.075F, 0.075F);
                GL11.glRotatef(35.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EQUIPPED_FIRST_PERSON:
                GL11.glTranslatef(0.55F, 0.05F, 0.45F);
                GL11.glScalef(0.068F, 0.068F, 0.068F);
                GL11.glRotatef(-35.0F, 0.0F, 1.0F, 0.0F);
                break;
            case ENTITY:
                GL11.glTranslatef(0.0F, -0.25F, 0.0F);
                GL11.glScalef(0.058F, 0.058F, 0.058F);
                break;
            default:
                GL11.glScalef(0.058F, 0.058F, 0.058F);
                break;
        }
    }

    /** Опори 750 кВ: висота 25 (проміжна) / 19 (анкерна), ширина 24-30 блоків. */
    private static void apply750Transform(ItemRenderType type) {
        switch (type) {
            case INVENTORY:
                GL11.glTranslatef(0.0F, -0.48F, 0.0F);
                GL11.glScalef(0.024F, 0.024F, 0.024F);
                GL11.glRotatef(25.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EQUIPPED:
                GL11.glTranslatef(0.5F, 0.15F, 0.5F);
                GL11.glScalef(0.032F, 0.032F, 0.032F);
                GL11.glRotatef(35.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EQUIPPED_FIRST_PERSON:
                GL11.glTranslatef(0.55F, 0.05F, 0.45F);
                GL11.glScalef(0.028F, 0.028F, 0.028F);
                GL11.glRotatef(-35.0F, 0.0F, 1.0F, 0.0F);
                break;
            case ENTITY:
                GL11.glTranslatef(0.0F, -0.25F, 0.0F);
                GL11.glScalef(0.025F, 0.025F, 0.025F);
                break;
            default:
                GL11.glScalef(0.025F, 0.025F, 0.025F);
                break;
        }
    }

    /** Опори 330 кВ: висота 17, ширина до 12 блоків (П-опора), тому масштаб ще менший. */
    private static void apply330Transform(ItemRenderType type) {
        switch (type) {
            case INVENTORY:
                GL11.glTranslatef(0.0F, -0.52F, 0.0F);
                GL11.glScalef(0.055F, 0.055F, 0.055F);
                GL11.glRotatef(25.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EQUIPPED:
                GL11.glTranslatef(0.5F, 0.15F, 0.5F);
                GL11.glScalef(0.07F, 0.07F, 0.07F);
                GL11.glRotatef(35.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EQUIPPED_FIRST_PERSON:
                GL11.glTranslatef(0.55F, 0.05F, 0.45F);
                GL11.glScalef(0.064F, 0.064F, 0.064F);
                GL11.glRotatef(-35.0F, 0.0F, 1.0F, 0.0F);
                break;
            case ENTITY:
                GL11.glTranslatef(0.0F, -0.25F, 0.0F);
                GL11.glScalef(0.058F, 0.058F, 0.058F);
                break;
            default:
                GL11.glScalef(0.058F, 0.058F, 0.058F);
                break;
        }
    }

    /** 6-контактні опори мають висоту ~16.5 блока й ширину 5, тому масштаб менший, ніж у 3-контактних. */
    private static void applyX2Transform(ItemRenderType type) {
        switch (type) {
            case INVENTORY:
                GL11.glTranslatef(0.0F, -0.58F, 0.0F);
                GL11.glScalef(0.068F, 0.068F, 0.068F);
                GL11.glRotatef(25.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EQUIPPED:
                GL11.glTranslatef(0.5F, 0.15F, 0.5F);
                GL11.glScalef(0.088F, 0.088F, 0.088F);
                GL11.glRotatef(35.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EQUIPPED_FIRST_PERSON:
                GL11.glTranslatef(0.55F, 0.05F, 0.45F);
                GL11.glScalef(0.08F, 0.08F, 0.08F);
                GL11.glRotatef(-35.0F, 0.0F, 1.0F, 0.0F);
                break;
            case ENTITY:
                GL11.glTranslatef(0.0F, -0.25F, 0.0F);
                GL11.glScalef(0.072F, 0.072F, 0.072F);
                break;
            default:
                GL11.glScalef(0.072F, 0.072F, 0.072F);
                break;
        }
    }
}
