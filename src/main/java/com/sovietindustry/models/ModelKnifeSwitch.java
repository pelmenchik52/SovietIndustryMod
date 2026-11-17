package com.sovietindustry.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelKnifeSwitch extends ModelBase {
    public ModelRenderer basePlate;
    public ModelRenderer upperContact;
    public ModelRenderer lowerContact;
    public ModelRenderer bladeHinge;
    public ModelRenderer blade;
    public ModelRenderer handle;

    public ModelKnifeSwitch() {
        textureWidth = 32;
        textureHeight = 32;

        // Центр моделі знаходиться в (0, 0, 0) - прямо в центрі блоку!
        // 1. Керамічна основа на стіні (товщина 1 піксель, z = 7.0F..8.0F)
        basePlate = new ModelRenderer(this, 0, 0);
        basePlate.addBox(-3.0F, -6.0F, 7.0F, 6, 12, 1);
        basePlate.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 2. Верхній мідний контакт
        upperContact = new ModelRenderer(this, 14, 0);
        upperContact.addBox(-1.0F, -4.5F, 5.0F, 2, 2, 2);
        upperContact.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 3. Нижній мідний контакт
        lowerContact = new ModelRenderer(this, 14, 4);
        lowerContact.addBox(-1.0F, 2.5F, 5.0F, 2, 2, 2);
        lowerContact.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 4. Поворотний шарнір ножа (на рівні нижнього контакту)
        bladeHinge = new ModelRenderer(this, 0, 0);
        bladeHinge.setRotationPoint(0.0F, 3.5F, 5.5F);

        // 5. Лезо ножа
        blade = new ModelRenderer(this, 0, 14);
        blade.addBox(-0.5F, -7.0F, -0.5F, 1, 7, 1);
        bladeHinge.addChild(blade);

        // 6. Червона ручка на кінці ножа
        handle = new ModelRenderer(this, 4, 14);
        handle.addBox(-1.0F, -9.0F, -1.0F, 2, 2, 2);
        bladeHinge.addChild(handle);
    }

    public void renderModel(boolean isOn) {
        float f5 = 0.0625F;
        basePlate.render(f5);
        upperContact.render(f5);
        lowerContact.render(f5);

        // Якщо ON - ніж притиснутий до верхнього контакту
        // Якщо OFF - ніж відкинутий на кут ~63 градуси
        if (isOn) {
            bladeHinge.rotateAngleX = 0.0F;
        } else {
            bladeHinge.rotateAngleX = 1.1F;
        }

        bladeHinge.render(f5);
    }
}
