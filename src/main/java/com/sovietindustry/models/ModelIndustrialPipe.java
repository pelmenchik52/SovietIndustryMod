package com.sovietindustry.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelIndustrialPipe extends ModelBase {
    public ModelRenderer pipeCore;
    public ModelRenderer pipeTop;
    public ModelRenderer pipeBottom;
    public ModelRenderer flange1;
    public ModelRenderer flange2;

    public ModelIndustrialPipe() {
        textureWidth = 64;
        textureHeight = 64;

        // Восьмигранна циліндрична труба (діаметр 10 пікселів, довжина 16)
        // Основний центральний короб
        pipeCore = new ModelRenderer(this, 0, 0);
        pipeCore.addBox(-5.0F, -5.0F, -8.0F, 10, 10, 16);
        pipeCore.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Верхній і нижній скоси для ідеальної круглої/циліндричної форми
        pipeTop = new ModelRenderer(this, 0, 26);
        pipeTop.addBox(-3.5F, -6.0F, -8.0F, 7, 1, 16);
        pipeTop.setRotationPoint(0.0F, 0.0F, 0.0F);

        pipeBottom = new ModelRenderer(this, 0, 26);
        pipeBottom.addBox(-3.5F, 5.0F, -8.0F, 7, 1, 16);
        pipeBottom.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Сталеві стяжні кільця-хомути (фланці)
        flange1 = new ModelRenderer(this, 0, 44);
        flange1.addBox(-5.5F, -5.5F, -5.0F, 11, 11, 2);
        flange1.setRotationPoint(0.0F, 0.0F, 0.0F);

        flange2 = new ModelRenderer(this, 0, 44);
        flange2.addBox(-5.5F, -5.5F, 3.0F, 11, 11, 2);
        flange2.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    public void renderModel() {
        float f5 = 0.0625F;
        pipeCore.render(f5);
        pipeTop.render(f5);
        pipeBottom.render(f5);
        flange1.render(f5);
        flange2.render(f5);
    }
}
