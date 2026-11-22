package com.sovietindustry.models;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.render.LepGeo;
import com.sovietindustry.tileentities.Substation330;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/**
 * Модель трансформаторної підстанції 330 кВ 6x7x8. Той самий портал (ґратчасті стійки й траверси), що й
 * на підстанції 110 кВ, але на три фази, та трифазний силовий трансформатор за референсом: прямокутний бак,
 * дві батареї радіаторів, горизонтальний циліндричний консерватор на стійках, три високі нахилені вводи ВН.
 *
 * Координати центровані по плиті: центр (0, 0, 0), x' -3..+3 (ширина 6), z -3.5..+3.5 (довжина 7), y 0..8.
 * Портал входу на z = -3, три кабельні виводи (помаранчеві площадки 4x4) на торці z = +3.5.
 * Рендерер зсуває модель на Substation330.CENTER_X, тому майстер-блок лишається в клітинці (0, 0).
 */
@SideOnly(Side.CLIENT)
public class ModelSubstation330kV {

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

    private static final double SLAB_H = Substation330.SLAB_H;

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
        // плита 6x7 (0.5 блока на тайл => шви кожні 2 блоки)
        LepGeo.boxTiled(t, 0.0D, SLAB_H * 0.5D, 0.0D, 3.0D, SLAB_H * 0.5D, 3.5D, 0.5D);
        // фундамент під трансформатор (рівно на клітинки бака 4x3)
        LepGeo.boxTiled(t, 0.0D, SLAB_H + 0.05D, 0.0D, 2.0D, 0.05D, 1.5D, 0.5D);
        // кабельний лоток уздовж виходу
        LepGeo.boxTiled(t, 0.0D, SLAB_H + 0.04D, 2.35D, 2.70D, 0.04D, 0.12D, 0.5D);
        // фундаменти стійок порталу
        for (int s = -1; s <= 1; s += 2) {
            LepGeo.boxTiled(t, s * Substation330.PORTAL_X, SLAB_H + 0.05D, Substation330.PORTAL_Z, 0.32D, 0.05D, 0.32D, 0.5D);
        }
        // тумби під вихідні стійки
        for (int lx : Substation330.PORT_LX) {
            LepGeo.boxTiled(t, Substation330.cellX(lx), SLAB_H + 0.10D, Substation330.POST_Z, 0.22D, 0.10D, 0.22D, 0.5D);
        }
    }

    // ------------------------------------------------------------ бак трансформатора

    private static void tank(Tessellator t) {
        double cy = (Substation330.TANK_BOT + Substation330.TANK_TOP) * 0.5D;
        double hy = (Substation330.TANK_TOP - Substation330.TANK_BOT) * 0.5D;
        double hx = Substation330.TANK_HX;
        double hz = Substation330.TANK_HZ;

        // основний бак і фланець кришки
        LepGeo.box(t, 0.0D, cy, 0.0D, hx, hy, hz);
        LepGeo.box(t, 0.0D, Substation330.LID_Y - 0.05D, 0.0D, hx + 0.08D, 0.05D, hz + 0.07D);
        // ребра жорсткості на торцях бака
        for (int s = -1; s <= 1; s += 2) {
            for (int j = -1; j <= 1; j += 2) {
                LepGeo.box(t, s * (hx + 0.03D), cy, j * 0.42D, 0.03D, hy - 0.08D, 0.05D);
            }
        }

        // радіаторні батареї з обох довгих боків (по 8 панелей), як на фото
        for (int s = -1; s <= 1; s += 2) {
            for (int i = 0; i < 8; i++) {
                double rx = -1.40D + i * 0.40D;
                LepGeo.box(t, rx, 1.75D, s * (hz + 0.30D), 0.07D, 1.15D, 0.28D);
            }
            // колектори радіаторів
            LepGeo.box(t, 0.0D, 2.90D, s * (hz + 0.10D), 1.50D, 0.07D, 0.09D);
            LepGeo.box(t, 0.0D, 0.65D, s * (hz + 0.10D), 1.50D, 0.07D, 0.09D);
        }

        // консерватор: горизонтальний циліндр на двох сідлових опорах (ліворуч зверху, як на фото)
        cylinderX(t, Substation330.CONS_CX, Substation330.CONS_CY, Substation330.CONS_CZ,
            Substation330.CONS_R, Substation330.CONS_HALF, 14);
        for (int s = -1; s <= 1; s += 2) {
            double sx = Substation330.CONS_CX + s * 0.85D;
            LepGeo.box(t, sx, 3.60D, Substation330.CONS_CZ, 0.08D, 0.35D, 0.30D);
        }
        // кільця-бандажі на консерваторі
        for (int s = -1; s <= 1; s += 2) {
            cylinderX(t, Substation330.CONS_CX + s * 0.40D, Substation330.CONS_CY, Substation330.CONS_CZ,
                Substation330.CONS_R + 0.03D, 0.05D, 14);
        }
        // трубопровід до бака (газове реле)
        LepGeo.beam(t, Substation330.CONS_CX - 0.15D, Substation330.CONS_CY - Substation330.CONS_R,
            Substation330.CONS_CZ - 0.15D, Substation330.CONS_CX - 0.15D, Substation330.LID_Y,
            Substation330.CONS_CZ - 0.15D, 0.09D);
    }

    /** Горизонтальний циліндр уздовж осі X (n сегментів) із торцевими кришками. */
    private static void cylinderX(Tessellator t, double cx, double cy, double cz, double r, double half, int n) {
        double x1 = cx - half, x2 = cx + half;
        for (int i = 0; i < n; i++) {
            double a0 = 2.0D * Math.PI * i / n;
            double a1 = 2.0D * Math.PI * (i + 1) / n;
            double y0 = cy + r * Math.cos(a0), z0 = cz + r * Math.sin(a0);
            double y1 = cy + r * Math.cos(a1), z1 = cz + r * Math.sin(a1);
            double am = (a0 + a1) * 0.5D;
            double v0 = (double) i / n, v1 = (double) (i + 1) / n;
            t.setNormal(0.0F, (float) Math.cos(am), (float) Math.sin(am));
            t.addVertexWithUV(x1, y0, z0, 0.0D, v0);
            t.addVertexWithUV(x2, y0, z0, 1.0D, v0);
            t.addVertexWithUV(x2, y1, z1, 1.0D, v1);
            t.addVertexWithUV(x1, y1, z1, 0.0D, v1);
            // торці: трикутні віяла (вироджені чотирикутники)
            t.setNormal(1.0F, 0.0F, 0.0F);
            t.addVertexWithUV(x2, cy, cz, 0.5D, 0.5D);
            t.addVertexWithUV(x2, y0, z0, 0.0D, v0);
            t.addVertexWithUV(x2, y1, z1, 1.0D, v1);
            t.addVertexWithUV(x2, y1, z1, 1.0D, v1);
            t.setNormal(-1.0F, 0.0F, 0.0F);
            t.addVertexWithUV(x1, cy, cz, 0.5D, 0.5D);
            t.addVertexWithUV(x1, y1, z1, 1.0D, v1);
            t.addVertexWithUV(x1, y0, z0, 0.0D, v0);
            t.addVertexWithUV(x1, y0, z0, 0.0D, v0);
        }
    }

    // ------------------------------------------------------------ сталь

    private static void steel(Tessellator t) {
        double px = Substation330.PORTAL_X;
        double pz = Substation330.PORTAL_Z;

        // портал входу 330 кВ: дві ґратчасті стійки й два яруси траверс (як на підстанції 110 кВ)
        latticeColumn(t, -px, pz, Substation330.PORTAL_COLUMN_H);
        latticeColumn(t, px, pz, Substation330.PORTAL_COLUMN_H);
        double beamHalf = px + 0.10D;
        for (double top : new double[] { Substation330.TIER_TOP, Substation330.TIER_LOW }) {
            double low = top - Substation330.TIER_DEPTH;
            LepGeo.beam(t, -beamHalf, top, pz, beamHalf, top, pz, 0.11D);
            LepGeo.beam(t, -beamHalf, low, pz, beamHalf, low, pz, 0.07D);
            int segs = 8;
            double step = 2.0D * beamHalf / segs;
            for (int j = 0; j < segs; j++) {
                boolean up = (j & 1) == 0;
                LepGeo.beam(t, -beamHalf + j * step, up ? low : top, pz,
                    -beamHalf + (j + 1) * step, up ? top : low, pz, 0.045D);
            }
        }
        // розкоси між ярусами
        LepGeo.beam(t, -px, Substation330.TIER_LOW - Substation330.TIER_DEPTH, pz,
            -px + 0.50D, Substation330.TIER_TOP - Substation330.TIER_DEPTH, pz, 0.05D);
        LepGeo.beam(t, px, Substation330.TIER_LOW - Substation330.TIER_DEPTH, pz,
            px - 0.50D, Substation330.TIER_TOP - Substation330.TIER_DEPTH, pz, 0.05D);

        // підвіски трьох фаз на нижньому поясі верхнього ярусу (сюди чіпляються анкерні гірлянди з дротами)
        double low = Substation330.TIER_TOP - Substation330.TIER_DEPTH;
        for (double fx : Substation330.PHASE_X) {
            LepGeo.beam(t, fx, low, pz, fx, Substation330.MOUNT_Y, pz, 0.07D);
        }

        // декоративні спуски від порталу до трьох вводів ВН (як шлейфи на фото)
        for (int i = 0; i < 3; i++) {
            double tx = Substation330.HV_X[i] * Substation330.HV_TILT;
            cable(t, Substation330.PHASE_X[i], Substation330.MOUNT_Y, pz,
                tx, Substation330.HV_TOP_Y + 0.12D, Substation330.HV_Z, 0.40D);
            // металевий наконечник на верхівці вводу ВН
            LepGeo.box(t, tx, Substation330.HV_TOP_Y + 0.12D, Substation330.HV_Z, 0.09D, 0.07D, 0.09D);
        }

        // шини від вводів нижньої сторони до вихідних стійок (по одній на кожен кабельний порт)
        for (int i = 0; i < 3; i++) {
            double postX = Substation330.cellX(Substation330.PORT_LX[i]);
            cable(t, Substation330.LV_X[i], Substation330.LV_TOP_Y + 0.05D, Substation330.LV_Z,
                postX, 2.85D, Substation330.POST_Z, 0.45D);
            LepGeo.box(t, Substation330.LV_X[i], Substation330.LV_TOP_Y + 0.07D, Substation330.LV_Z, 0.07D, 0.05D, 0.07D);
            // стійка, кабельна муфта на опорному ізоляторі й спуск до помаранчевої площадки на торці плити
            LepGeo.box(t, postX, SLAB_H + 0.95D, Substation330.POST_Z, 0.07D, 0.95D, 0.07D);
            LepGeo.beam(t, postX, 0.55D, Substation330.POST_Z, postX, 0.14D, Substation330.LENGTH_END - 0.02D, 0.10D);
        }
    }

    /** Провисаючий декоративний кабель (парабола з 8 відрізків). */
    private static void cable(Tessellator t, double x1, double y1, double z1,
                              double x2, double y2, double z2, double sag) {
        int n = 8;
        double px = x1, py = y1, pz = z1;
        for (int i = 1; i <= n; i++) {
            double f = (double) i / n;
            double cx = x1 + (x2 - x1) * f;
            double cy = y1 + (y2 - y1) * f - sag * 4.0D * f * (1.0D - f);
            double cz = z1 + (z2 - z1) * f;
            LepGeo.beam(t, px, py, pz, cx, cy, cz, 0.035D);
            px = cx; py = cy; pz = cz;
        }
    }

    // ------------------------------------------------------------ фарфор

    /** Ребристий фарфоровий ізолятор: серцевина плюс диски-юбки по висоті (юбки звужуються до верхівки). */
    private static void bushing(Tessellator t, double bx, double by, double bz,
                                double tx, double ty, double tz, double core, double skirt, int discs) {
        LepGeo.beam(t, bx, by, bz, tx, ty, tz, core);
        for (int k = 1; k <= discs; k++) {
            double f = (double) k / (discs + 1);
            double s = skirt * (1.0D - 0.35D * f);
            LepGeo.box(t, bx + (tx - bx) * f, by + (ty - by) * f, bz + (tz - bz) * f, s, 0.025D, s);
        }
    }

    private static void porcelain(Tessellator t) {
        // три високі вводи ВН 330 кВ, крайні нахилені назовні
        for (int i = 0; i < 3; i++) {
            double bx = Substation330.HV_X[i];
            bushing(t, bx, Substation330.LID_Y, Substation330.HV_Z,
                bx * Substation330.HV_TILT, Substation330.HV_TOP_Y, Substation330.HV_Z, 0.20D, 0.17D, 10);
        }
        // три нижчі вводи для виходу
        for (int i = 0; i < 3; i++) {
            double bx = Substation330.LV_X[i];
            bushing(t, bx, Substation330.LID_Y, Substation330.LV_Z, bx, Substation330.LV_TOP_Y, Substation330.LV_Z,
                0.14D, 0.11D, 4);
        }
        // опорні ізолятори вихідних стійок
        for (int lx : Substation330.PORT_LX) {
            double px = Substation330.cellX(lx);
            bushing(t, px, 2.00D, Substation330.POST_Z, px, 2.85D, Substation330.POST_Z, 0.15D, 0.11D, 3);
        }
    }

    // ------------------------------------------------------------ шафи

    private static void cabinets(Tessellator t) {
        // шафа керування на торці бака
        LepGeo.box(t, 1.85D, SLAB_H + 0.75D, 0.0D, 0.15D, 0.65D, 0.35D);
    }

    // ------------------------------------------------------------ помаранчеві виводи під кабелі NTM

    private static void ports(Tessellator t) {
        // 4x4 пікселі = 0.25 x 0.25 блока на торці плити z = +3.5 (там нод NTM приймає кабель)
        for (int lx : Substation330.PORT_LX) {
            padZ(t, Substation330.LENGTH_END + 0.003D, Substation330.cellX(lx), SLAB_H * 0.5D, 0.125D);
        }
    }

    /** Площадка 0.25x0.25 на торцевій грані +Z, повністю накрита текстурою 4x4. */
    private static void padZ(Tessellator t, double z, double cx, double cy, double half) {
        double x1 = cx - half, x2 = cx + half, y1 = cy - half, y2 = cy + half;
        t.setNormal(0.0F, 0.0F, 1.0F);
        t.addVertexWithUV(x2, y1, z, 0.0D, 1.0D);
        t.addVertexWithUV(x2, y2, z, 0.0D, 0.0D);
        t.addVertexWithUV(x1, y2, z, 1.0D, 0.0D);
        t.addVertexWithUV(x1, y1, z, 1.0D, 1.0D);
    }

    /** Металева ґратчаста стійка порталу в точці (cx, cz) висотою h над плитою. */
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
