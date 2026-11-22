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
 * Моделі опор 750 кВ:
 * - Проміжна (angle = false): ПП750 на відтяжках, висота ~25.4 блока, ширина траверси 24 блоки,
 *   подвійні гірлянди по 20 тарілок із квадратною рамкою 4 проводів та X-відтяжками.
 * - Анкерно-кутова (angle = true): У750 з трьох окремих пірамідальних ґратчастих веж
 *   (ширина ~30 блоків, висота ~20.6 блока) із кронштейнами та бічною консоллю шлейфу.
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

    // ------------------------------------------------------------ проміжна опора ПП750: сталь

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
        int n = 14;
        double[] ys = new double[n + 1];
        double[] cx = new double[n + 1];
        double[] hw = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            ys[i] = Lep750.LEG_TOP_Y * i / n;
            cx[i] = side * Lep750.legCentreX(ys[i]);
            hw[i] = Lep750.legHalf(ys[i]);
        }
        lattice(t, ys, cx, hw, hw, 0.13D, 0.065D, 0.05D, 3);
    }

    private void horn(Tessellator t, int side) {
        double y0 = Lep750.LEG_TOP_Y;
        double y1 = Lep750.HORN_TOP_Y;
        int n = 6;
        double[] ys = new double[n];
        double[] cx = new double[n];
        double[] hw = new double[n];
        for (int i = 0; i < n; i++) {
            ys[i] = y0 + (y1 - y0) * i / (n - 1);
            cx[i] = side * Lep750.hornCentreX(ys[i]);
            hw[i] = Lep750.hornHalf(ys[i]);
        }
        lattice(t, ys, cx, hw, hw, 0.10D, 0.055D, 0.04D, 0);
        // шпиль громовідводу над рогом
        lb(t, side * Lep750.HORN_TOP_X, y1 - 0.05D, 0.0D, side * Lep750.HORN_TOP_X, y1 + 0.60D, 0.0D, 0.055D);
    }

    private void foot(Tessellator t, int side) {
        double x = side * Lep750.LEG_BASE_X;
        LepGeo.box(t, x, 0.18D, 0.0D, 1.05D, 0.18D, 1.05D); // бетонна плита
        LepGeo.box(t, x, 0.44D, 0.0D, 0.56D, 0.10D, 0.56D); // п'ята
        LepGeo.box(t, x, 0.68D, 0.0D, 0.30D, 0.14D, 0.30D); // шарнірний вузол
    }

    /** Траверса шириною 24 блоки: ферма з підвищенням над стійками та звуженням до кінців і центру (ПП750). */
    private void crossarm(Tessellator t) {
        double y0 = Lep750.ARM_Y;
        double half = Lep750.ARM_HALF; // 12.0D
        double legX = Lep750.LEG_TOP_X; // 5.6D
        int panels = 16;
        double[] xs = new double[panels + 1];
        double[] zh = new double[panels + 1];
        double[] yt = new double[panels + 1];
        for (int p = 0; p <= panels; p++) {
            xs[p] = -half + 2.0D * half * p / panels;
            double ax = Math.abs(xs[p]);
            // профіль висоти траверси: максимум над стійками (~1.45), плавне звуження до країв (~0.55) і центру (~0.95)
            double peakFactor;
            if (ax <= legX) {
                peakFactor = 0.45D + 0.55D * (ax / legX);
            } else {
                peakFactor = 1.0D - 0.85D * ((ax - legX) / (half - legX));
            }
            zh[p] = 0.28D + 0.26D * peakFactor;
            yt[p] = y0 + 0.55D + 0.95D * peakFactor;
        }
        for (int z = -1; z <= 1; z += 2) {
            for (int p = 0; p < panels; p++) {
                lb(t, xs[p], y0, z * zh[p], xs[p + 1], y0, z * zh[p + 1], 0.11D);
                lb(t, xs[p], yt[p], z * zh[p], xs[p + 1], yt[p + 1], z * zh[p + 1], 0.10D);
                if (p < panels / 2) {
                    lb(t, xs[p], y0, z * zh[p], xs[p + 1], yt[p + 1], z * zh[p + 1], 0.06D);
                } else {
                    lb(t, xs[p], yt[p], z * zh[p], xs[p + 1], y0, z * zh[p + 1], 0.06D);
                }
            }
            for (int p = 0; p <= panels; p++) {
                lb(t, xs[p], y0, z * zh[p], xs[p], yt[p], z * zh[p], 0.07D);
            }
        }
        for (int p = 0; p <= panels; p++) {
            lb(t, xs[p], y0, -zh[p], xs[p], y0, zh[p], 0.07D);
            lb(t, xs[p], yt[p], -zh[p], xs[p], yt[p], zh[p], 0.06D);
        }
        // вушка під подвійні гірлянди
        double halfStr = Lep750.STRING_SPACING * 0.5D;
        for (double px : Lep750.PHASE_X) {
            for (int s = -1; s <= 1; s += 2) {
                LepGeo.box(t, px + s * halfStr, y0 - 0.07D, 0.0D, 0.10D, 0.07D, 0.10D);
            }
        }
    }

    private void guyAnchors(Tessellator t, int side) {
        for (int z = -1; z <= 1; z += 2) {
            LepGeo.box(t, side * Lep750.GUY_ANCHOR_X, 0.14D, z * Lep750.GUY_ANCHOR_Z, 0.45D, 0.14D, 0.45D);
        }
    }

    // ------------------------------------------------------------ проміжна опора: подвійні гірлянди, гачки, відтяжки

    private void buildPortalAtlas(Tessellator t) {
        for (double px : Lep750.PHASE_X) {
            phaseString(t, px);
        }
        for (int side = -1; side <= 1; side += 2) {
            groundHook(t, side);
            guys(t, side);
        }
    }

    /** Подвійна гірлянда по 20 тарілок, спільне коромисло й квадратна рамка на 4 проводи (як на Фото 2). */
    private void phaseString(Tessellator t, double px) {
        double top = Lep750.STRING_TOP;
        double lastDisc = Lep750.discY(Lep750.DISCS - 1);
        double rodTop = top - 0.12D;
        double rodBottom = lastDisc - 0.18D;
        double halfStr = Lep750.STRING_SPACING * 0.5D;

        for (int s = -1; s <= 1; s += 2) {
            double sx = px + s * halfStr;
            sb(t, sx, Lep750.ARM_Y, 0.0D, sx, top - 0.04D, 0.0D, 0.08D);
            LepGeo.boxUV(t, sx, top - 0.07D, 0.0D, 0.09D, 0.06D, 0.09D, STEEL, TEX);
            LepGeo.beamUV(t, sx, rodTop, 0.0D, sx, Lep750.YOKE_Y, 0.0D, 0.055D, CAP, TEX);
            for (int k = 0; k < Lep750.DISCS; k++) {
                LepGeo.boxUV(t, sx, Lep750.discY(k), 0.0D, 0.19D, 0.03D, 0.19D, GLASS, TEX);
            }
            LepGeo.beamUV(t, sx, rodTop, 0.0D, sx, rodTop - 0.12D, 0.0D, 0.11D, CAP, TEX);
            LepGeo.beamUV(t, sx, rodBottom + 0.12D, 0.0D, sx, rodBottom, 0.0D, 0.11D, CAP, TEX);
        }

        // нижнє коромисло, що об'єднує обидві гірлянди, та підвіски до квадратної рамки
        double h = Lep750.BUNDLE_HALF;
        double cy = Lep750.MOUNT_Y;
        LepGeo.boxUV(t, px, Lep750.YOKE_Y, 0.0D, halfStr + 0.14D, 0.045D, 0.06D, STEEL, TEX);
        for (int s = -1; s <= 1; s += 2) {
            sd(t, px + s * halfStr, Lep750.YOKE_Y, 0.0D, px + s * h, cy + h, 0.0D, 0.035D);
        }

        // квадратна рамка-розпірка на 4 проводи
        for (int k = 0; k < 4; k++) {
            int kn = (k + 1) & 3;
            double x0 = px + SIGN[k][0] * h, y0 = cy + SIGN[k][1] * h;
            double x1 = px + SIGN[kn][0] * h, y1 = cy + SIGN[kn][1] * h;
            sb(t, x0, y0, 0.0D, x1, y1, 0.0D, 0.04D);
        }
        sd(t, px - h, cy - h, 0.0D, px + h, cy + h, 0.0D, 0.025D);
        sd(t, px - h, cy + h, 0.0D, px + h, cy - h, 0.0D, 0.025D);
        for (int k = 0; k < 4; k++) {
            LepGeo.boxUV(t, px + SIGN[k][0] * h, cy + SIGN[k][1] * h, 0.0D, 0.075D, 0.075D, 0.11D, CAP, TEX);
        }
    }

    private void groundHook(Tessellator t, int side) {
        double tipX = side * Lep750.HORN_TOP_X;
        double hookX = side * Lep750.GROUND_X;
        double gY = Lep750.GROUND_Y;
        sb(t, tipX, Lep750.HORN_TOP_Y, 0.0D, hookX, Lep750.HORN_TOP_Y, 0.0D, 0.055D);
        sb(t, hookX, Lep750.HORN_TOP_Y, 0.0D, hookX, gY + 0.16D, 0.0D, 0.05D);
        LepGeo.boxUV(t, hookX, gY + 0.14D, 0.0D, 0.08D, 0.035D, 0.08D, CAP, TEX);
        LepGeo.boxUV(t, hookX, gY + 0.07D, 0.0D, 0.16D, 0.035D, 0.16D, GLASS, TEX);
        LepGeo.boxUV(t, hookX, gY + 0.02D, 0.0D, 0.05D, 0.035D, 0.05D, CAP, TEX);
    }

    private void guys(Tessellator t, int side) {
        double jx = side * Lep750.LEG_TOP_X;
        double jy = Lep750.LEG_TOP_Y;
        // 4 зовнішні відтяжки до бетонних анкерів
        for (int z = -1; z <= 1; z += 2) {
            guy(t, jx, jy, 0.0D, side * Lep750.GUY_ANCHOR_X, 0.35D, z * Lep750.GUY_ANCHOR_Z);
        }
        // 2 перехресні внутрішні відтяжки (X-розкіс між стійками порталу, як на Фото 2)
        double oppX = -side * (Lep750.LEG_BASE_X - 0.30D);
        sd(t, jx, jy - 0.25D, 0.0D, oppX, 0.75D, 0.0D, 0.045D);
    }

    private static void guy(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2) {
        sd(t, x1, y1, z1, x2, y2, z2, 0.05D);
        LepGeo.boxUV(t, x1, y1, z1, 0.09D, 0.09D, 0.09D, CAP, TEX);
        double f = 0.93D;
        LepGeo.boxUV(t, x1 + (x2 - x1) * f, y1 + (y2 - y1) * f, z1 + (z2 - z1) * f, 0.08D, 0.14D, 0.08D, CAP, TEX);
        LepGeo.boxUV(t, x2, y2 + 0.05D, z2, 0.12D, 0.09D, 0.12D, STEEL, TEX);
    }

    // ------------------------------------------------------------ анкерно-кутова У750: три вежі

    private void buildAngleSteel(Tessellator t) {
        for (int idx = -1; idx <= 1; idx++) {
            tower(t, idx * Lep750.A_TOWER_X, idx != 0);
        }
    }

    /** Одна ґратчаста вежа висотою ~20.6 блока з консолями під гірлянди й бічною консоллю шлейфу (Фото 1). */
    private void tower(Tessellator t, double tx, boolean groundWire) {
        double[] ys = { 0.0D, 2.5D, 5.0D, 7.5D, 10.0D, 12.5D, 15.0D, 17.5D, Lep750.A_TOP };
        int n = ys.length;
        double[] cx = new double[n];
        double[] hw = new double[n];
        for (int i = 0; i < n; i++) {
            cx[i] = tx;
            hw[i] = Lep750.angleHalf(ys[i]);
        }
        lattice(t, ys, cx, hw, hw, 0.13D, 0.065D, 0.05D, 2);

        // посилені пояси (діафрагми)
        for (double y : new double[] { 5.0D, 10.0D, 15.0D }) {
            double h = Lep750.angleHalf(y) + 0.04D;
            LepGeo.box(t, tx, y, 0.0D, h, 0.06D, h);
        }

        // масивні бетонні фундаменти в основі кожної ноги
        double hb = Lep750.angleHalf(0.0D);
        for (int k = 0; k < 4; k++) {
            LepGeo.box(t, tx + SIGN[k][0] * hb, 0.20D, SIGN[k][1] * hb, 0.32D, 0.20D, 0.32D);
        }

        // консолі кріплення подвійних натяжних гірлянд спереду й ззаду
        double my = Lep750.A_MOUNT_Y;
        double halfStr = Lep750.STRING_SPACING * 0.5D;
        for (int z = -1; z <= 1; z += 2) {
            lb(t, tx, my, 0.0D, tx, my, z * Lep750.A_STANDOFF, 0.10D);
            lb(t, tx - halfStr, my, z * Lep750.A_STANDOFF, tx + halfStr, my, z * Lep750.A_STANDOFF, 0.09D);
            LepGeo.box(t, tx, my, z * Lep750.A_STANDOFF, halfStr + 0.12D, 0.09D, 0.14D);
        }

        // на крайній правій вежі — бічна трикутна консоль підтримки петлі шлейфу (як на Фото 1)
        if (tx > 1.0D) {
            double armLen = 2.8D;
            lb(t, tx, my, 0.0D, tx + armLen, my, 0.0D, 0.09D);
            lb(t, tx, my + 1.25D, 0.0D, tx + armLen, my, 0.0D, 0.065D);
            lb(t, tx + armLen * 0.5D, my, 0.0D, tx + armLen * 0.5D, my + 0.62D, 0.0D, 0.05D);
            LepGeo.box(t, tx + armLen, my, 0.0D, 0.10D, 0.10D, 0.10D);
            // підвісний ізолятор шлейфу, що звисає вниз із кінця консолі
            lb(t, tx + armLen, my, 0.0D, tx + armLen, my - 1.85D, 0.0D, 0.055D);
            for (int k = 0; k < 10; k++) {
                double dy = my - 0.25D - k * 0.14D;
                LepGeo.box(t, tx + armLen, dy, 0.0D, 0.16D, 0.025D, 0.16D);
            }
        }

        // громовідвід: високий шпиль над верхівкою кожної вежі
        lb(t, tx, Lep750.A_TOP - 0.2D, 0.0D, tx, Lep750.A_TOP + 0.65D, 0.0D, 0.055D);
        if (groundWire) {
            LepGeo.box(t, tx, Lep750.A_GROUND_Y, 0.0D, 0.12D, 0.06D, 0.12D);
        }
    }
}