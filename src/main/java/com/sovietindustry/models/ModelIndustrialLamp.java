package com.sovietindustry.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelIndustrialLamp extends ModelBase {
    public ModelRenderer rod;
    public ModelRenderer ballast;
    public ModelRenderer bellTop;
    public ModelRenderer bellBottom;
    public ModelRenderer bulb;
    public ModelRenderer cageX;
    public ModelRenderer cageZ;

    public ModelIndustrialLamp() {
        textureWidth = 64;
        textureHeight = 64;

        // 1. Стрижень кріплення до стелі
        rod = new ModelRenderer(this, 0, 0);
        rod.addBox(-0.5F, -8.0F, -0.5F, 1, 4, 1);
        rod.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 2. Верхній циліндричний пускорегулювальний апарат (баласт)
        ballast = new ModelRenderer(this, 0, 6);
        ballast.addBox(-2.5F, -4.0F, -2.5F, 5, 3, 5);
        ballast.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 3. Конічний плафон (РСП «дзвін»)
        bellTop = new ModelRenderer(this, 0, 15);
        bellTop.addBox(-4.0F, -1.0F, -4.0F, 8, 2, 8);
        bellTop.setRotationPoint(0.0F, 0.0F, 0.0F);

        bellBottom = new ModelRenderer(this, 0, 26);
        bellBottom.addBox(-6.0F, 1.0F, -6.0F, 12, 4, 12);
        bellBottom.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 4. Яскрава внутрішня лампа
        bulb = new ModelRenderer(this, 34, 0);
        bulb.addBox(-1.5F, 2.0F, -1.5F, 3, 3, 3);
        bulb.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 5. Захисна металева решітка знизу
        cageX = new ModelRenderer(this, 0, 44);
        cageX.addBox(-5.5F, 5.0F, -0.5F, 11, 1, 1);
        cageX.setRotationPoint(0.0F, 0.0F, 0.0F);

        cageZ = new ModelRenderer(this, 0, 44);
        cageZ.addBox(-0.5F, 5.0F, -5.5F, 1, 1, 11);
        cageZ.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    public void renderModel() {
        float f5 = 0.0625F;
        rod.render(f5);
        ballast.render(f5);
        bellTop.render(f5);
        bellBottom.render(f5);
        bulb.render(f5);
        cageX.render(f5);
        cageZ.render(f5);
    }
}
