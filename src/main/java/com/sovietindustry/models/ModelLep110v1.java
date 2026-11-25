package com.sovietindustry.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 3D модель одноланцюгової проміжної залізобетонної опори ЛЕП 110 кВ (тип ПБ110-1 / СК-22).
 * Висота: 13 блоків (208 пікселів).
 * Нижня траверса: асиметрична (зліва 2.3 м / 37 px, справа 3.9 м / 62 px).
 * Верхня траверса: одностороння (справа 2.3 м / 37 px).
 * 3 підвісні гірлянди скляних ізоляторів ПС-70 з затискачами для проводів.
 * Вершина: грозозахисний шпиль (тросостійка) з тримачем троса на висоті 13 блоків.
 */
@SideOnly(Side.CLIENT)
public class ModelLep110v1 extends ModelBase {

    // Фундамент та стійка СК-22
    public ModelRenderer foundationBase;
    public ModelRenderer foundationFlange;
    public ModelRenderer poleSection1;
    public ModelRenderer poleSection2;
    public ModelRenderer poleSection3;
    public ModelRenderer poleSection4;
    public ModelRenderer poleSection5;
    public ModelRenderer groundSpire;
    public ModelRenderer groundHook;

    // Нижня траверса (15.0 м / ~9 блоків)
    public ModelRenderer armLowerMain;
    public ModelRenderer tieLowerLeft;
    public ModelRenderer tieLowerRight;

    // Верхня траверса (19.0 м / ~11 блоків)
    public ModelRenderer armUpperMain;
    public ModelRenderer tieUpperRight;

    // Гірлянди ізоляторів
    public ModelRenderer insulatorLowerLeft;
    public ModelRenderer insulatorLowerRight;
    public ModelRenderer insulatorUpperRight;

    public ModelLep110v1() {
        textureWidth = 128;
        textureHeight = 128;

        // 1. Фундамент та опорний фланець (y = 0..-10)
        foundationBase = new ModelRenderer(this, 0, 48);
        foundationBase.addBox(-7.0F, -8.0F, -7.0F, 14, 8, 14);
        foundationBase.setRotationPoint(0.0F, 0.0F, 0.0F);

        foundationFlange = new ModelRenderer(this, 0, 64);
        foundationFlange.addBox(-6.0F, -10.0F, -6.0F, 12, 2, 12);
        foundationFlange.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 2. Стовбур залізобетонної центрифугованої стійки СК-22 з плавним звуженням
        // Секція 1: 10x40x10 (y: -10..-50)
        poleSection1 = new ModelRenderer(this, 0, 0);
        poleSection1.addBox(-5.0F, -50.0F, -5.0F, 10, 40, 10);
        poleSection1.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Секція 2: 9x48x9 (y: -50..-98)
        poleSection2 = new ModelRenderer(this, 20, 0);
        poleSection2.addBox(-4.5F, -98.0F, -4.5F, 9, 48, 9);
        poleSection2.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Секція 3: 8x44x8 (y: -98..-142)
        poleSection3 = new ModelRenderer(this, 0, 0);
        poleSection3.addBox(-4.0F, -142.0F, -4.0F, 8, 44, 8);
        poleSection3.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Секція 4: 7x36x7 (y: -142..-178)
        poleSection4 = new ModelRenderer(this, 20, 0);
        poleSection4.addBox(-3.5F, -178.0F, -3.5F, 7, 36, 7);
        poleSection4.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Секція 5: 6x20x6 (y: -178..-198)
        poleSection5 = new ModelRenderer(this, 0, 0);
        poleSection5.addBox(-3.0F, -198.0F, -3.0F, 6, 20, 6);
        poleSection5.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Грозозахисний шпиль (шпиль з круглої сталі діаметром 50 мм) (y: -198..-208)
        groundSpire = new ModelRenderer(this, 96, 48);
        groundSpire.addBox(-1.5F, -212.0F, -1.5F, 3, 14, 3);
        groundSpire.setRotationPoint(0.0F, 0.0F, 0.0F);

        groundHook = new ModelRenderer(this, 96, 48);
        groundHook.addBox(-3.0F, -212.0F, -1.0F, 6, 2, 2);
        groundHook.setRotationPoint(0.0F, 0.0F, 0.0F);

        // 3. Нижня траверса (y ~ -140)
        // Основна балка: зліва 37 px (-37..0), справа 62 px (0..62). Загальна довжина 99 px.
        armLowerMain = new ModelRenderer(this, 0, 80);
        armLowerMain.addBox(-37.0F, -141.0F, -1.5F, 99, 3, 3);
        armLowerMain.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Верхня тяга зліва (йде від лівого краю траверси вгору до стійки)
        tieLowerLeft = new ModelRenderer(this, 64, 0);
        tieLowerLeft.setRotationPoint(-37.0F, -140.0F, 0.0F);
        tieLowerLeft.rotateAngleZ = 0.40F;
        tieLowerLeft.addBox(0.0F, -1.0F, -1.0F, 36, 2, 2);

        // Верхня тяга справа (йде від правого краю траверси вгору до стійки)
        tieLowerRight = new ModelRenderer(this, 64, 0);
        tieLowerRight.setRotationPoint(62.0F, -140.0F, 0.0F);
        tieLowerRight.rotateAngleZ = -0.30F;
        tieLowerRight.addBox(-60.0F, -1.0F, -1.0F, 60, 2, 2);

        // 4. Верхня траверса (y ~ -176)
        // Одностороння консоль праворуч: довжина 37 px (0..37)
        armUpperMain = new ModelRenderer(this, 0, 80);
        armUpperMain.addBox(0.0F, -177.0F, -1.5F, 37, 3, 3);
        armUpperMain.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Верхня тяга верхньої траверси
        tieUpperRight = new ModelRenderer(this, 64, 0);
        tieUpperRight.setRotationPoint(37.0F, -176.0F, 0.0F);
        tieUpperRight.rotateAngleZ = -0.44F;
        tieUpperRight.addBox(-36.0F, -1.0F, -1.0F, 36, 2, 2);

        // 5. Гірлянди ізоляторів
        // Нижня ліва (-37, -138)
        insulatorLowerLeft = buildInsulatorString(-37.0F, -138.0F, 0.0F);

        // Нижня права (+62, -138)
        insulatorLowerRight = buildInsulatorString(62.0F, -138.0F, 0.0F);

        // Верхня права (+37, -174)
        insulatorUpperRight = buildInsulatorString(37.0F, -174.0F, 0.0F);
    }

    /**
     * Будує підвісну гірлянду скляних тарілчастих ізоляторів ПС-70. Гірлянда СУЦІЛЬНА: скоба, наскрізний стрижень,
     * 5 ланок (тарілка + дзвін), що лежать впритул одна на одній без проміжків, і нижній затискач проводу.
     * Висота всієї гірлянди, як і раніше, 18 px, тому точки кріплення проводів не змінились.
     */
    private ModelRenderer buildInsulatorString(float x, float y, float z) {
        ModelRenderer root = new ModelRenderer(this, 96, 48);
        root.setRotationPoint(x, y, z);

        // Скоба (кріплення до траверси): y 0..1
        root.addBox(-1.0F, 0.0F, -1.0F, 2, 1, 2);

        // Наскрізний стрижень від скоби до затискача: y 1..16
        root.addBox(-1.0F, 1.0F, -1.0F, 2, 15, 2);

        // 5 ланок по 3 px: тарілка скла 1 px + дзвін 2 px, y 1..16 без зазорів
        for (int i = 0; i < 5; i++) {
            float dy = 1.0F + i * 3.0F;
            root.setTextureOffset(64, 48);
            root.addBox(-2.5F, dy, -2.5F, 5, 1, 5);          // тарілка
            root.addBox(-1.5F, dy + 1.0F, -1.5F, 3, 2, 3);   // дзвін
        }

        // Нижній затискач проводу: y 16..18
        root.setTextureOffset(96, 48);
        root.addBox(-1.5F, 16.0F, -1.5F, 3, 2, 3);
        root.addBox(-3.0F, 17.0F, -0.5F, 6, 1, 1); // жолоб для дроту

        return root;
    }

    public void renderAll() {
        float f5 = 0.0625F; // 1 / 16

        // Фундамент і стійка
        foundationBase.render(f5);
        foundationFlange.render(f5);
        poleSection1.render(f5);
        poleSection2.render(f5);
        poleSection3.render(f5);
        poleSection4.render(f5);
        poleSection5.render(f5);
        groundSpire.render(f5);
        groundHook.render(f5);

        // Нижня траверса
        armLowerMain.render(f5);
        tieLowerLeft.render(f5);
        tieLowerRight.render(f5);

        // Верхня траверса
        armUpperMain.render(f5);
        tieUpperRight.render(f5);

        // Ізолятори
        insulatorLowerLeft.render(f5);
        insulatorLowerRight.render(f5);
        insulatorUpperRight.render(f5);
    }
}
