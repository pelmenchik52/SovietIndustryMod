package com.sovietindustry.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Текстура: po2_fence_model.png, 64x64.
 * UV-розкладка (u,v) кожного боксу:
 *   slab        (0, 0)   12x19x1
 *   pillars     (28, 0)  2x20x2   (обидва стовпи ділять одну ділянку)
 *   beams       (0, 22)  16x2x2   (обидві балки ділять одну ділянку)
 *   loops       (38, 0)  2x2x1    <-- FIXED: раніше було (0,0) і накладалось на slab
 *   footBase    (0, 28)  4x3x8
 *   footTop     (26, 28) 3x2x6
 */
@SideOnly(Side.CLIENT)
public class ModelPO2Fence extends ModelBase {
    public ModelRenderer slab;
    public ModelRenderer leftPillar;
    public ModelRenderer rightPillar;
    public ModelRenderer topBeam;
    public ModelRenderer bottomBeam;
    public ModelRenderer loopLeft;
    public ModelRenderer loopRight;
    public ModelRenderer footingLeftBase;
    public ModelRenderer footingLeftTop;
    public ModelRenderer footingRightBase;
    public ModelRenderer footingRightTop;

    public ModelPO2Fence() {
        textureWidth = 64;
        textureHeight = 64;

        // 1. Центральна панель з ромбиками (12 x 19 x 1)
        slab = new ModelRenderer(this, 0, 0);
        slab.addBox(-6.0F, -10.0F, -0.5F, 12, 19, 1);
        slab.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 2. Бічні стовпчики
        leftPillar = new ModelRenderer(this, 28, 0);
        leftPillar.addBox(-8.0F, -11.0F, -1.0F, 2, 20, 2);
        leftPillar.setRotationPoint(0.0F, 0.0F, 0.0F);

        rightPillar = new ModelRenderer(this, 28, 0);
        rightPillar.addBox(6.0F, -11.0F, -1.0F, 2, 20, 2);
        rightPillar.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 3. Верхня та нижня балки
        topBeam = new ModelRenderer(this, 0, 22);
        topBeam.addBox(-8.0F, -12.0F, -1.0F, 16, 2, 2);
        topBeam.setRotationPoint(0.0F, 0.0F, 0.0F);

        bottomBeam = new ModelRenderer(this, 0, 22);
        bottomBeam.addBox(-8.0F, 8.0F, -1.0F, 16, 2, 2);
        bottomBeam.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 4. Монтажні вушка (власна ділянка текстури, не перетинається зі slab)
        loopLeft = new ModelRenderer(this, 38, 0);
        loopLeft.addBox(-5.0F, -14.0F, -0.5F, 2, 2, 1);
        loopLeft.setRotationPoint(0.0F, 0.0F, 0.0F);

        loopRight = new ModelRenderer(this, 38, 0);
        loopRight.addBox(3.0F, -14.0F, -0.5F, 2, 2, 1);
        loopRight.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 5. Фундаментні стакани
        footingLeftBase = new ModelRenderer(this, 0, 28);
        footingLeftBase.addBox(-8.5F, 5.0F, -4.0F, 4, 3, 8);
        footingLeftBase.setRotationPoint(0.0F, 0.0F, 0.0F);

        footingLeftTop = new ModelRenderer(this, 26, 28);
        footingLeftTop.addBox(-8.0F, 3.0F, -3.0F, 3, 2, 6);
        footingLeftTop.setRotationPoint(0.0F, 0.0F, 0.0F);

        footingRightBase = new ModelRenderer(this, 0, 28);
        footingRightBase.addBox(4.5F, 5.0F, -4.0F, 4, 3, 8);
        footingRightBase.setRotationPoint(0.0F, 0.0F, 0.0F);

        footingRightTop = new ModelRenderer(this, 26, 28);
        footingRightTop.addBox(5.0F, 3.0F, -3.0F, 3, 2, 6);
        footingRightTop.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    public void renderModel() {
        float f5 = 0.0625F;
        slab.render(f5);
        leftPillar.render(f5);
        rightPillar.render(f5);
        topBeam.render(f5);
        bottomBeam.render(f5);
        loopLeft.render(f5);
        loopRight.render(f5);
        footingLeftBase.render(f5);
        footingLeftTop.render(f5);
        footingRightBase.render(f5);
        footingRightTop.render(f5);
    }
}
