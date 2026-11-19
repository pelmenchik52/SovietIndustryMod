package com.sovietindustry.models;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.render.LepGeo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/**
 * Модель трансформаторної підстанції. Кожен матеріал це окремий display list зі своєю текстурою
 * (бетон, бак трансформатора, оцинкована сталь, фарфор, шафи, помаранчеві кабельні виводи).
 * Локальні координати: центр плити в (0, 0, 0), портал ВН на z = -1.95, портал 110 кВ на z = +2.15.
 */
@SideOnly(Side.CLIENT)
public class ModelSubstation110kV {

    private static final int CONCRETE = 0;
    private static final int TANK = 1;
    private static final int STEEL = 2;
    private static final int PORCELAIN = 3;
    private static final int CABINET = 4;
    private static final int PORT = 5;
    private static final int PARTS = 6;

    private static final String[] TEXTURE_NAMES = {
        "substation_concrete", "substation_tank", "substation_steel",
        "substation_porcelain", "substation_cabinet", "substation_cable_port"
    };
    private static ResourceLocation[] textures;

    /** Висота бетонної плити (рівно 4 пікселі, під помаранчевий вивід 4x4). */
    private static final double SLAB_H = 0.25D;

    private final int[] lists = new int[PARTS];
    private boolean built = false;

    private static ResourceLocation tex(int part) {
        if (textures == null) {
            textures = new ResourceLocation[PARTS];
            for (int i = 0; i < PARTS; i++) {
                textures[i] = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/" + TEXTURE_NAMES[i] + ".png");
            }
        }
        return textures[part];
    }

    /** Малює модель повністю, сам підв'язуючи текстури кожного матеріалу. */
    public void renderAll() {
        if (!built) {
            for (int part = 0; part < PARTS; part++) {
                lists[part] = GLAllocation.generateDisplayLists(1);
                GL11.glNewList(lists[part], GL11.GL_COMPILE);
                build(part);
                GL11.glEndList();
            }
            built = true;
        }
        for (int part = 0; part < PARTS; part++) {
            Minecraft.getMinecraft().renderEngine.bindTexture(tex(part));
            GL11.glCallList(lists[part]);
        }
    }

    private void build(int part) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        switch (part) {
            case CONCRETE: concrete(t); break;
            case TANK: tank(t); break;
            case STEEL: steel(t); break;
            case PORCELAIN: porcelain(t); break;
            case CABINET: cabinets(t); break;
            case PORT: ports(t); break;
            default: break;
        }
        t.draw();
    }

    // ------------------------------------------------------------ бетон

    private static void concrete(Tessellator t) {
        // плита 5x5 (0.5 блока на тайл => шви кожні 2 блоки)
        LepGeo.boxTiled(t, 0.0D, SLAB_H * 0.5D, 0.0D, 2.5D, SLAB_H * 0.5D, 2.5D, 0.5D);
        // кабельні лотки вздовж двох довгих країв
        LepGeo.boxTiled(t, 0.0D, SLAB_H + 0.04D, -2.30D, 2.30D, 0.04D, 0.12D, 0.5D);
        LepGeo.boxTiled(t, 0.0D, SLAB_H + 0.04D,  2.30D, 2.30D, 0.04D, 0.12D, 0.5D);
        // фундамент під трансформатор і опори порталів
        LepGeo.boxTiled(t, 0.0D, SLAB_H + 0.05D, 0.0D, 1.30D, 0.05D, 1.10D, 0.5D);
        LepGeo.boxTiled(t, -2.05D, SLAB_H + 0.05D, -1.95D, 0.30D, 0.05D, 0.30D, 0.5D);
        LepGeo.boxTiled(t,  2.05D, SLAB_H + 0.05D, -1.95D, 0.30D, 0.05D, 0.30D, 0.5D);
        LepGeo.boxTiled(t, -1.85D, SLAB_H + 0.05D,  2.15D, 0.30D, 0.05D, 0.30D, 0.5D);
        LepGeo.boxTiled(t,  1.85D, SLAB_H + 0.05D,  2.15D, 0.30D, 0.05D, 0.30D, 0.5D);
        // стійки роз'єднувачів стоять на бетонних тумбах
        for (int i = -1; i <= 1; i++) {
            LepGeo.boxTiled(t, i * 1.55D, SLAB_H + 0.10D, 1.55D, 0.22D, 0.10D, 0.22D, 0.5D);
        }
    }

    // ------------------------------------------------------------ бак трансформатора

    private static void tank(Tessellator t) {
        double by = SLAB_H + 0.10D;
        // основний бак (дно на фундаменті)
        LepGeo.box(t, 0.0D, by + 1.00D, 0.0D, 1.05D, 1.00D, 0.78D);
        // верхній фланець кришки
        LepGeo.box(t, 0.0D, by + 2.10D, 0.0D, 1.12D, 0.08D, 0.85D);
        // розширювальний бак (консерватор) на стійках
        LepGeo.box(t, 0.0D, 3.05D, -0.45D, 0.85D, 0.24D, 0.24D);
        LepGeo.box(t, -0.55D, 2.72D, -0.45D, 0.07D, 0.34D, 0.07D);
        LepGeo.box(t,  0.55D, 2.72D, -0.45D, 0.07D, 0.34D, 0.07D);
        // радіаторні батареї з обох боків бака
        for (int i = -3; i <= 3; i++) {
            double rx = i * 0.26D;
            LepGeo.box(t, rx, by + 1.00D, -0.95D, 0.06D, 0.85D, 0.16D);
            LepGeo.box(t, rx, by + 1.00D,  0.95D, 0.06D, 0.85D, 0.16D);
        }
        // колектори радіаторів
        LepGeo.box(t, 0.0D, by + 1.88D, -0.95D, 0.85D, 0.05D, 0.09D);
        LepGeo.box(t, 0.0D, by + 0.14D, -0.95D, 0.85D, 0.05D, 0.09D);
        LepGeo.box(t, 0.0D, by + 1.88D,  0.95D, 0.85D, 0.05D, 0.09D);
        LepGeo.box(t, 0.0D, by + 0.14D,  0.95D, 0.85D, 0.05D, 0.09D);
    }

    // ------------------------------------------------------------ сталь

    private static void steel(Tessellator t) {
        // 3. портал ВН: дві ґратчасті стійки й два яруси траверс (по одному дроту на кінці кожної)
        latticeColumn(t, -2.05D, -1.95D, 6.65D);
        latticeColumn(t,  2.05D, -1.95D, 6.65D);
        for (double y : new double[] { 6.55D, 5.55D }) {
            LepGeo.beam(t, -2.15D, y, -1.95D, 2.15D, y, -1.95D, 0.11D);
            LepGeo.beam(t, -2.15D, y - 0.38D, -1.95D, 2.15D, y - 0.38D, -1.95D, 0.07D);
            for (int j = -4; j < 4; j++) {
                LepGeo.beam(t, j * 0.50D, y - 0.38D, -1.95D, (j + 1) * 0.50D, y, -1.95D, 0.045D);
            }
        }
        // розкоси між ярусами
        LepGeo.beam(t, -2.05D, 5.55D, -1.95D, -1.70D, 6.55D, -1.95D, 0.05D);
        LepGeo.beam(t,  2.05D, 5.55D, -1.95D,  1.70D, 6.55D, -1.95D, 0.05D);

        // 4. вихідний портал 110 кВ
        latticeColumn(t, -1.85D, 2.15D, 4.75D);
        latticeColumn(t,  1.85D, 2.15D, 4.75D);
        LepGeo.beam(t, -1.95D, 4.65D, 2.15D, 1.95D, 4.65D, 2.15D, 0.10D);
        LepGeo.beam(t, -1.95D, 4.30D, 2.15D, 1.95D, 4.30D, 2.15D, 0.07D);
        for (int j = -4; j < 4; j++) {
            LepGeo.beam(t, j * 0.49D, 4.30D, 2.15D, (j + 1) * 0.49D, 4.65D, 2.15D, 0.045D);
        }

        // стійки роз'єднувачів і ошиновка від трансформатора до порталу 110 кВ
        for (int i = -1; i <= 1; i++) {
            double px = i * 1.55D;
            LepGeo.box(t, px, 1.15D, 1.55D, 0.09D, 0.85D, 0.09D);
            // ніж роз'єднувача
            LepGeo.beam(t, px - 0.35D, 3.00D, 1.55D, px + 0.30D, 3.18D, 1.55D, 0.05D);
            LepGeo.beam(t, i * 0.85D, 3.55D, 0.85D, px, 2.95D, 1.55D, 0.05D);
            LepGeo.beam(t, px, 2.95D, 1.55D, px, 4.45D, 2.15D, 0.05D);
        }

        // кабельні вводи в бак
        LepGeo.beam(t, -2.22D, 1.65D, 0.0D, -1.05D, 2.10D, 0.0D, 0.06D);
        LepGeo.beam(t,  2.22D, 1.65D, 0.0D,  1.05D, 2.10D, 0.0D, 0.06D);
    }

    // ------------------------------------------------------------ фарфор

    private static void porcelain(Tessellator t) {
        double lid = SLAB_H + 0.10D + 2.18D;
        // вводи 110 кВ (3 шт.) і ВН (4 шт.) на кришці
        for (int i = -1; i <= 1; i++) {
            double ix = i * 0.65D;
            LepGeo.beam(t, ix, lid, 0.45D, ix * 1.3D, 3.55D, 0.85D, 0.16D);
        }
        double[] hvX = { -0.75D, -0.25D, 0.25D, 0.75D };
        for (double hx : hvX) {
            LepGeo.beam(t, hx, lid, -0.35D, hx * 1.4D, 3.75D, -0.85D, 0.17D);
        }
        // опорні ізолятори роз'єднувачів
        for (int i = -1; i <= 1; i++) {
            double px = i * 1.55D;
            LepGeo.beam(t, px, 2.00D, 1.55D, px, 2.95D, 1.55D, 0.18D);
        }
    }

    // ------------------------------------------------------------ шафи

    private static void cabinets(Tessellator t) {
        LepGeo.box(t, -2.22D, SLAB_H + 0.35D, 0.0D, 0.24D, 0.35D, 0.45D);
        LepGeo.box(t,  2.22D, SLAB_H + 0.35D, 0.0D, 0.24D, 0.35D, 0.45D);
    }

    // ------------------------------------------------------------ помаранчеві виводи під кабелі NTM

    private static void ports(Tessellator t) {
        // 4x4 пікселі = 0.25 x 0.25 блока, на зовнішніх торцях плити по осі X (там нод NTM приймає кабель)
        LepGeo.padX(t,  2.503D, SLAB_H * 0.5D, 0.0D, 0.125D, true);
        LepGeo.padX(t, -2.503D, SLAB_H * 0.5D, 0.0D, 0.125D, false);
    }

    /** Металева ґратчаста стійка порталу в точці (cx, cz) висотою h. */
    private static void latticeColumn(Tessellator t, double cx, double cz, double h) {
        double hw = 0.18D;
        int steps = (int) Math.round(h);
        double dh = h / steps;
        for (int s = 0; s < steps; s++) {
            double y0 = SLAB_H + s * dh;
            double y1 = SLAB_H + (s + 1) * dh;
            LepGeo.beam(t, cx - hw, y0, cz - hw, cx - hw, y1, cz - hw, 0.06D);
            LepGeo.beam(t, cx + hw, y0, cz - hw, cx + hw, y1, cz - hw, 0.06D);
            LepGeo.beam(t, cx + hw, y0, cz + hw, cx + hw, y1, cz + hw, 0.06D);
            LepGeo.beam(t, cx - hw, y0, cz + hw, cx - hw, y1, cz + hw, 0.06D);
            LepGeo.beam(t, cx - hw, y0, cz - hw, cx + hw, y1, cz - hw, 0.04D);
            LepGeo.beam(t, cx - hw, y0, cz + hw, cx + hw, y1, cz + hw, 0.04D);
            LepGeo.beam(t, cx - hw, y0, cz - hw, cx - hw, y1, cz + hw, 0.04D);
            LepGeo.beam(t, cx + hw, y0, cz - hw, cx + hw, y1, cz + hw, 0.04D);
        }
    }
}
