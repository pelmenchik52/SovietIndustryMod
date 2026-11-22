package com.sovietindustry.models;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.render.LepGeo;
import com.sovietindustry.tileentities.Lep750;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/**
 * Моделі опор 750 кВ, масштабовані під Minecraft (вищі за 330 кВ, гармонійні в грі).
 *
 * Проміжна (angle = false): висота 21 блок, ширина 15 блоків. $\Lambda$-подібна портальна опора на відтяжках.
 * Стійки нахилені всередину до траверси, роги з гачками грозотросів над стійками.
 * Під траверсою 3 гірлянди по 20 тарілок із квадратною рамкою 4 проводів та 6 відтяжок до землі.
 *
 * Анкерно-кутова (angle = true): висота 19 блоків, ширина ~13-14 блоків. Три окремі ґратчасті пірамідальні вежі
 * з консолями під натяжні гірлянди та консоллю шлейфу (як на Фото 2).
 */
@SideOnly(Side.CLIENT)
public class ModelLep750 {

    private static final ResourceLocation ATLAS =
        new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep110_single.png");
    private static final ResourceLocation LATTICE_STEEL =
        new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep_steel.png");
    private static final double TEX = 128.0D;

    private static final double[] STEEL = { 64.0D, 4.0D, 80.0D, 12.0D };
    private static final double[] STEEL_DARK = { 0.0D, 84.0D, 16.0D, 92.0D };
    private static final double[] GLASS = { 64.0D, 50.0D, 80.0D, 54.0D };
    private static final double[] CAP = { 96.0D, 48.0D, 112.0D, 56.0D };

    private static final int[][] SIGN = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };

    private final boolean angle;
    private int listSteel = -1;
    private int listAtlas = -1;

    public ModelLep750(boolean angle) {
        this.angle = angle;
    }

    public void renderAll() {
        if (listSteel < 0) {
            listSteel = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(listSteel, GL11.GL_COMPILE);
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            if (angle) {
                buildAngleSteel(t);
            } else {
                buildPortalSteel(t);
            }
            t.draw();
            GL11.glEndList();
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(LATTICE_STEEL);
        GL11.glCallList(listSteel);

        if (angle) return;
        if (listAtlas < 0) {
            listAtlas = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(listAtlas, GL11.GL_COMPILE);
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            buildPortalAtlas(t);
            t.draw();
            GL11.glEndList();
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(ATLAS);
        GL11.glCallList(listAtlas);
    }

    // ------------------------------------------------------------ допоміжні

    private static void lb(Tessellator t, double[] a, double[] b, double th) {
        LepGeo.beam(t, a[0], a[1], a[2], b[0], b[1], b[2], th);
    }

    private static void lb(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2, double th) {
        LepGeo.beam(t, x1, y1, z1, x2, y2, z2, th);
    }

    private static void sb(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2, double th) {
        LepGeo.beamUV(t, x1, y1, z1, x2, y2, z2, th, STEEL, TEX);
    }

    private static void sd(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2, double th) {
        LepGeo.beamUV(t, x1, y1, z1, x2, y2, z2, th, STEEL_DARK, TEX);
    }

    private static void lattice(Tessellator t, double[] ys, double[] cx, double[] hw, double[] hd,
                                double legT, double barT, double diagT, int crossEvery) {
        int n = ys.length;
        double[][][] c = new double[n][4][];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < 4; k++) {
                c[i][k] = new double[] { cx[i] + SIGN[k][0] * hw[i], ys[i], SIGN[k][1] * hd[i] };
            }
        }
        for (int i = 0; i < n - 1; i++) {
            for (int k = 0; k < 4; k++) {
                lb(t, c[i][k], c[i + 1][k], legT);
            }
        }
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < 4; k++) {
                lb(t, c[i][k], c[i][(k + 1) & 3], barT);
            }
            if (crossEvery > 0 && i > 0 && i < n - 1 && i % crossEvery == 0) {
                lb(t, c[i][0], c[i][2], diagT);
                lb(t, c[i][1], c[i][3], diagT);
            }
        }
        for (int i = 0; i < n - 1; i++) {
            for (int k = 0; k < 4; k++) {
                int k1 = (k + 1) & 3;
                lb(t, c[i][k], c[i + 1][k1], diagT);
                lb(t, c[i][k1], c[i + 1][k], diagT);
            }
        }
    }

    // ------------------------------------------------------------ проміжна опора: сталь

    private void buildPortalSteel(Tessellator t) {
        for (int side = -1; side <= 1; side += 2) {
            leg(t, side);
            horn(t, side);
            foot(t, side);
        }
        crossarm(t);
        for (int side = -1; side <= 1; side += 2) {
            guyAnchors(t, side);
        }
    }

    private void leg(Tessellator t, int side) {
        int n = 10;
        double[] ys = new double[n + 1];
        double[] cx = new double[n + 1];
        double[] hw = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            ys[i] = Lep750.LEG_TOP_Y * i / n;
            cx[i] = side * Lep750.legCentreX(ys[i]);
            hw[i] = Lep750.legHalf(ys[i]);
        }
        lattice(t, ys, cx, hw, hw, 0.11D, 0.055D, 0.045D, 3);
    }

    private void horn(Tessellator t, int side) {
        double y0 = Lep750.LEG_TOP_Y;
        double y1 = Lep750.HORN_TOP_Y;
        int n = 5;
        double[] ys = new double[n];
        double[] cx = new double[n];
        double[] hw = new double[n];
        for (int i = 0; i < n; i++) {
            ys[i] = y0 + (y1 - y0) * i / (n - 1);
            cx[i] = side * Lep750.hornCentreX(ys[i]);
            hw[i] = Lep750.hornHalf(ys[i]);
        }
        lattice(t, ys, cx, hw, hw, 0.085D, 0.045D, 0.035D, 0);
        // вістря громовідводу над рогом
        lb(t, side * Lep750.HORN_TOP_X, y1 - 0.05D, 0.0D, side * Lep750.HORN_TOP_X, y1 + 0.50D, 0.0D, 0.05D);
    }

    private void foot(Tessellator t, int side) {
        double x = side * Lep750.LEG_BASE_X;
        LepGeo.box(t, x, 0.16D, 0.0D, 0.85D, 0.16D, 0.85D);          // бетонна плита
        LepGeo.box(t, x, 0.38D, 0.0D, 0.46D, 0.08D, 0.46D);          // п'ята
        LepGeo.box(t, x, 0.58D, 0.0D, 0.24D, 0.12D, 0.24D);          // шарнір
    }

    /** Траверса: ширина 15 блоків, ферма Warren із плавним центром, як на Фото 1. */
    private void crossarm(Tessellator t) {
        double y0 = Lep750.ARM_Y;
        double half = Lep750.ARM_HALF; // 7.5D
        int panels = 10;
        double[] xs = new double[panels + 1];
        double[] zh = new double[panels + 1];
        double[] yt = new double[panels + 1];
        for (int p = 0; p <= panels; p++) {
            xs[p] = -half + 2.0D * half * p / panels;
            double f = 1.0D - Math.abs(xs[p]) / half;
            zh[p] = 0.28D + 0.18D * f;
            yt[p] = y0 + 0.75D + 0.35D * f;
        }
        for (int z = -1; z <= 1; z += 2) {
            for (int p = 0; p < panels; p++) {
                lb(t, xs[p], y0, z * zh[p], xs[p + 1], y0, z * zh[p + 1], 0.09D);
                lb(t, xs[p], yt[p], z * zh[p], xs[p + 1], yt[p + 1], z * zh[p + 1], 0.08D);
                if (p < panels / 2) {
                    lb(t, xs[p], y0, z * zh[p], xs[p + 1], yt[p + 1], z * zh[p + 1], 0.05D);
                } else {
                    lb(t, xs[p], yt[p], z * zh[p], xs[p + 1], y0, z * zh[p + 1], 0.05D);
                }
            }
            for (int p = 0; p <= panels; p++) {
                lb(t, xs[p], y0, z * zh[p], xs[p], yt[p], z * zh[p], 0.06D);
            }
        }
        for (int p = 0; p <= panels; p++) {
            lb(t, xs[p], y0, -zh[p], xs[p], y0, zh[p], 0.06D);
            lb(t, xs[p], yt[p], -zh[p], xs[p], yt[p], zh[p], 0.05D);
        }
        // вушка під гірлянди
        for (double px : Lep750.PHASE_X) {
            LepGeo.box(t, px, y0 - 0.06D, 0.0D, 0.10D, 0.06D, 0.10D);
        }
    }

    private void guyAnchors(Tessellator t, int side) {
        for (int z = -1; z <= 1; z += 2) {
            LepGeo.box(t, side * Lep750.GUY_ANCHOR_X, 0.12D, z * Lep750.GUY_ANCHOR_Z, 0.38D, 0.12D, 0.38D);
        }
    }

    // ------------------------------------------------------------ проміжна опора: гірлянди, гачки, відтяжки

    private void buildPortalAtlas(Tessellator t) {
        for (double px : Lep750.PHASE_X) {
            phaseString(t, px);
        }
        for (int side = -1; side <= 1; side += 2) {
            groundHook(t, side);
            guys(t, side);
        }
    }

    /** Гірлянда з 20 тарілок, ярмо й квадратна рамка, до кутів якої чіпляються 4 проводи. */
    private void phaseString(Tessellator t, double px) {
        double top = Lep750.STRING_TOP;
        double lastDisc = Lep750.discY(Lep750.DISCS - 1);
        double rodTop = top - 0.10D;
        double rodBottom = lastDisc - 0.15D;

        sb(t, px, Lep750.ARM_Y, 0.0D, px, top - 0.04D, 0.0D, 0.08D);
        LepGeo.boxUV(t, px, top - 0.06D, 0.0D, 0.09D, 0.05D, 0.09D, STEEL, TEX);
        LepGeo.beamUV(t, px, rodTop, 0.0D, px, Lep750.YOKE_Y, 0.0D, 0.055D, CAP, TEX);
        for (int k = 0; k < Lep750.DISCS; k++) {
            LepGeo.boxUV(t, px, Lep750.discY(k), 0.0D, 0.18D, 0.025D, 0.18D, GLASS, TEX);
        }
        LepGeo.beamUV(t, px, rodTop, 0.0D, px, rodTop - 0.10D, 0.0D, 0.11D, CAP, TEX);
        LepGeo.beamUV(t, px, rodBottom + 0.10D, 0.0D, px, rodBottom, 0.0D, 0.11D, CAP, TEX);

        // ярмо й підвіски до верхніх кутів квадрата
        double h = Lep750.BUNDLE_HALF; // 0.20D
        double cy = Lep750.MOUNT_Y;
        LepGeo.boxUV(t, px, Lep750.YOKE_Y, 0.0D, 0.28D, 0.035D, 0.05D, STEEL, TEX);
        for (int s = -1; s <= 1; s += 2) {
            sd(t, px + s * 0.24D, Lep750.YOKE_Y, 0.0D, px + s * h, cy + h, 0.0D, 0.03D);
        }

        // квадратна рамка, хрестовина й затискачі в кутах
        for (int k = 0; k < 4; k++) {
            int kn = (k + 1) & 3;
            double x0 = px + SIGN[k][0] * h, y0 = cy + SIGN[k][1] * h;
            double x1 = px + SIGN[kn][0] * h, y1 = cy + SIGN[kn][1] * h;
            sb(t, x0, y0, 0.0D, x1, y1, 0.0D, 0.035D);
        }
        sd(t, px - h, cy - h, 0.0D, px + h, cy + h, 0.0D, 0.02D);
        sd(t, px - h, cy + h, 0.0D, px + h, cy - h, 0.0D, 0.02D);
        for (int k = 0; k < 4; k++) {
            LepGeo.boxUV(t, px + SIGN[k][0] * h, cy + SIGN[k][1] * h, 0.0D, 0.065D, 0.065D, 0.10D, CAP, TEX);
        }
    }

    private void groundHook(Tessellator t, int side) {
        double tipX = side * Lep750.HORN_TOP_X;
        double hookX = side * Lep750.GROUND_X;
        double gY = Lep750.GROUND_Y;
        sb(t, tipX, Lep750.HORN_TOP_Y, 0.0D, hookX, Lep750.HORN_TOP_Y, 0.0D, 0.05D);
        sb(t, hookX, Lep750.HORN_TOP_Y, 0.0D, hookX, gY + 0.14D, 0.0D, 0.045D);
        LepGeo.boxUV(t, hookX, gY + 0.12D, 0.0D, 0.07D, 0.03D, 0.07D, CAP, TEX);
        LepGeo.boxUV(t, hookX, gY + 0.06D, 0.0D, 0.15D, 0.03D, 0.15D, GLASS, TEX);
        LepGeo.boxUV(t, hookX, gY + 0.02D, 0.0D, 0.045D, 0.03D, 0.045D, CAP, TEX);
    }

    private void guys(Tessellator t, int side) {
        double jx = side * Lep750.LEG_TOP_X;
        double jy = Lep750.LEG_TOP_Y;
        // Зовнішні відтяжки вперед і назад до бетонних анкерів (як на Фото 1 та 3)
        for (int z = -1; z <= 1; z += 2) {
            guy(t, jx, jy, 0.0D, side * Lep750.GUY_ANCHOR_X, 0.30D, z * Lep750.GUY_ANCHOR_Z);
        }
        // Внутрішня перехресна відтяжка до основи протилежної стійки (утворює X-розкіс порталу)
        double oppX = -side * (Lep750.LEG_BASE_X - 0.25D);
        sd(t, jx, jy - 0.2D, 0.0D, oppX, 0.65D, 0.0D, 0.04D);
    }

    private static void guy(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2) {
        sd(t, x1, y1, z1, x2, y2, z2, 0.045D);
        LepGeo.boxUV(t, x1, y1, z1, 0.08D, 0.08D, 0.08D, CAP, TEX);
        double f = 0.93D;
        LepGeo.boxUV(t, x1 + (x2 - x1) * f, y1 + (y2 - y1) * f, z1 + (z2 - z1) * f, 0.07D, 0.12D, 0.07D, CAP, TEX);
        LepGeo.boxUV(t, x2, y2 + 0.04D, z2, 0.10D, 0.08D, 0.10D, STEEL, TEX);
    }

    // ------------------------------------------------------------ анкерно-кутова: три вежі

    private void buildAngleSteel(Tessellator t) {
        for (int idx = -1; idx <= 1; idx++) {
            tower(t, idx * Lep750.A_TOWER_X, idx != 0);
        }
    }

    /** Одна ґратчаста вежа висотою 19 блоків із консолями під гірлянди й шлейф (як на Фото 2). */
    private void tower(Tessellator t, double tx, boolean groundWire) {
        double[] ys = { 0.0D, 2.5D, 5.0D, 7.5D, 10.0D, 12.5D, 14.5D, 16.5D, 18.0D, Lep750.A_TOP };
        int n = ys.length;
        double[] cx = new double[n];
        double[] hw = new double[n];
        for (int i = 0; i < n; i++) {
            cx[i] = tx;
            hw[i] = Lep750.angleHalf(ys[i]);
        }
        lattice(t, ys, cx, hw, hw, 0.11D, 0.055D, 0.045D, 3);

        // посилені пояси (діафрагми)
        for (double y : new double[] { 5.0D, 12.5D }) {
            double h = Lep750.angleHalf(y) + 0.03D;
            LepGeo.box(t, tx, y, 0.0D, h, 0.05D, h);
        }

        // башмаки в основі
        double hb = Lep750.angleHalf(0.0D);
        for (int k = 0; k < 4; k++) {
            LepGeo.box(t, tx + SIGN[k][0] * hb, 0.16D, SIGN[k][1] * hb, 0.24D, 0.16D, 0.24D);
        }

        // консолі/кронштейни кріплення натяжних гірлянд спереду й ззаду на голівці
        double my = Lep750.A_MOUNT_Y;
        for (int z = -1; z <= 1; z += 2) {
            lb(t, tx, my, 0.0D, tx, my, z * Lep750.A_STANDOFF, 0.08D);
            LepGeo.box(t, tx, my, z * Lep750.A_STANDOFF, 0.12D, 0.08D, 0.12D);
        }

        // на крайній правій вежі — бічна консоль підтримки петлі шлейфу (як на Фото 2)
        if (tx > 1.0D) {
            double armLen = 1.6D;
            lb(t, tx, my, 0.0D, tx + armLen, my, 0.0D, 0.07D);
            lb(t, tx, my + 0.8D, 0.0D, tx + armLen, my, 0.0D, 0.05D);
            LepGeo.box(t, tx + armLen, my, 0.0D, 0.08D, 0.08D, 0.08D);
            // ізолятор шлейфу, що звисає вниз із консолі
            lb(t, tx + armLen, my, 0.0D, tx + armLen, my - 1.2D, 0.0D, 0.04D);
        }

        // громовідвід: шпиль над верхівкою
        lb(t, tx, Lep750.A_TOP - 0.2D, 0.0D, tx, Lep750.A_TOP + 0.45D, 0.0D, 0.05D);
        if (groundWire) {
            LepGeo.box(t, tx, Lep750.A_GROUND_Y, 0.0D, 0.10D, 0.05D, 0.10D);
        }
    }
}
