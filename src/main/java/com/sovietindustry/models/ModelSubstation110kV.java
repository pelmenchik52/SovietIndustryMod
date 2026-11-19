package com.sovietindustry.models;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.render.LepGeo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;

@SideOnly(Side.CLIENT)
public class ModelSubstation110kV {

    private int displayList = -1;

    public void renderAll() {
        if (displayList < 0) {
            displayList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(displayList, GL11.GL_COMPILE);
            build();
            GL11.glEndList();
        }
        GL11.glCallList(displayList);
    }

    private void build() {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();

        // 1. Бетонна основа ВРП 5x5 (висота 0.25 блока) та кабельні лотки по периметру
        LepGeo.box(t, 0.0D, 0.12D, 0.0D, 2.48D, 0.12D, 2.48D);
        LepGeo.box(t, 0.0D, 0.30D, -2.35D, 2.48D, 0.08D, 0.12D);
        LepGeo.box(t, 0.0D, 0.30D,  2.35D, 2.48D, 0.08D, 0.12D);

        // 2. Силовий трансформатор по центру (бак + радіатори + розширювальний бачок)
        LepGeo.box(t, 0.0D, 1.35D, 0.0D, 1.05D, 1.05D, 0.78D); // основний бак
        LepGeo.box(t, 0.0D, 2.50D, 0.0D, 1.12D, 0.10D, 0.85D); // верхній фланець кришки
        LepGeo.box(t, 0.0D, 3.05D, -0.45D, 0.85D, 0.24D, 0.24D); // розширювальний бак (консерватор)
        LepGeo.beam(t, -0.55D, 2.55D, -0.45D, -0.55D, 2.85D, -0.45D, 0.07D);
        LepGeo.beam(t,  0.55D, 2.55D, -0.45D,  0.55D, 2.85D, -0.45D, 0.07D);

        // Радіаторні батареї охолодження з обох боків бака
        for (int i = -3; i <= 3; i++) {
            double rx = i * 0.26D;
            LepGeo.box(t, rx, 1.35D, -0.95D, 0.06D, 0.85D, 0.16D);
            LepGeo.box(t, rx, 1.35D,  0.95D, 0.06D, 0.85D, 0.16D);
        }

        // Високовольтні вводи на кришці трансформатора (3 на сторону 110 кВ + 4 на сторону ВН)
        for (int i = -1; i <= 1; i++) {
            double ix = i * 0.65D;
            LepGeo.beam(t, ix, 2.55D, 0.45D, ix * 1.3D, 3.55D, 0.85D, 0.11D);
        }
        double[] hvX = { -0.75D, -0.25D, 0.25D, 0.75D };
        for (double hx : hvX) {
            LepGeo.beam(t, hx, 2.55D, -0.35D, hx * 1.4D, 3.75D, -0.85D, 0.12D);
        }

        // 3. Високий портал ВН (для 4-провідної ЛЕП NTM, z = -1.95, висота траверси 6.45)
        latticeColumn(t, -2.05D, -1.95D, 6.65D);
        latticeColumn(t,  2.05D, -1.95D, 6.65D);
        // Ґратчаста траверса ВН
        LepGeo.beam(t, -2.15D, 6.55D, -1.95D, 2.15D, 6.55D, -1.95D, 0.11D);
        LepGeo.beam(t, -2.15D, 6.15D, -1.95D, 2.15D, 6.15D, -1.95D, 0.08D);
        for (int j = -4; j < 4; j++) {
            double x0 = j * 0.50D;
            double x1 = (j + 1) * 0.50D;
            LepGeo.beam(t, x0, 6.15D, -1.95D, x1, 6.55D, -1.95D, 0.05D);
        }

        // 4. Вихідний портал 110 кВ (для 3-фазної ЛЕП 110 кВ, z = +2.15, висота траверси 4.55)
        latticeColumn(t, -1.85D, 2.15D, 4.75D);
        latticeColumn(t,  1.85D, 2.15D, 4.75D);
        LepGeo.beam(t, -1.95D, 4.65D, 2.15D, 1.95D, 4.65D, 2.15D, 0.10D);
        LepGeo.beam(t, -1.95D, 4.30D, 2.15D, 1.95D, 4.30D, 2.15D, 0.07D);

        // Опорні ізолятори роз'єднувачів та ошиновка від трансформатора до порталів
        for (int i = -1; i <= 1; i++) {
            double px = i * 1.55D;
            LepGeo.box(t, px, 1.15D, 1.55D, 0.18D, 0.90D, 0.18D); // стійка роз'єднувача
            LepGeo.beam(t, px, 2.05D, 1.55D, px, 2.95D, 1.55D, 0.12D); // колонка ізолятора
            LepGeo.beam(t, i * 0.85D, 3.55D, 0.85D, px, 2.95D, 1.55D, 0.05D); // шина від транса
            LepGeo.beam(t, px, 2.95D, 1.55D, px, 4.45D, 2.15D, 0.05D); // спуск до порталу 110 кВ
        }

        // 5. Два кабельні вводи/виводи збоку (лівий та правий борти плити 5x5)
        LepGeo.box(t, -2.22D, 0.95D, 0.0D, 0.24D, 0.70D, 0.45D); // ліва кабельна шафа
        LepGeo.box(t,  2.22D, 0.95D, 0.0D, 0.24D, 0.70D, 0.45D); // права кабельна шафа
        LepGeo.beam(t, -2.22D, 1.65D, 0.0D, -1.05D, 2.10D, 0.0D, 0.06D); // кабельний ввід у бак
        LepGeo.beam(t,  2.22D, 1.65D, 0.0D,  1.05D, 2.10D, 0.0D, 0.06D);

        t.draw();
    }

    /** Металева ґратчаста стійка порталу в точці (cx, cz) висотою h. */
    private static void latticeColumn(Tessellator t, double cx, double cz, double h) {
        double hw = 0.18D;
        int steps = (int) Math.round(h);
        double dh = h / steps;
        for (int s = 0; s < steps; s++) {
            double y0 = 0.25D + s * dh;
            double y1 = 0.25D + (s + 1) * dh;
            LepGeo.beam(t, cx - hw, y0, cz - hw, cx - hw, y1, cz - hw, 0.06D);
            LepGeo.beam(t, cx + hw, y0, cz - hw, cx + hw, y1, cz - hw, 0.06D);
            LepGeo.beam(t, cx + hw, y0, cz + hw, cx + hw, y1, cz + hw, 0.06D);
            LepGeo.beam(t, cx - hw, y0, cz + hw, cx - hw, y1, cz + hw, 0.06D);
            LepGeo.beam(t, cx - hw, y0, cz - hw, cx + hw, y1, cz - hw, 0.04D);
            LepGeo.beam(t, cx - hw, y0, cz + hw, cx + hw, y1, cz + hw, 0.04D);
        }
    }
}