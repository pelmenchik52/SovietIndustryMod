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
    private final boolean single;
    /** Для окремої щогли: -1 ліва, 0 середня, +1 права (форма вежі як у складі АКО). */
    private final int variant;
    private int listSteel = -1;
    private int listAtlas = -1;

    public ModelLep750(boolean angle) {
        this(angle, false);
    }

    /** single = одна окрема щогла (середня вежа анкерно-кутової, без упору під шлейф сусідньої). */
    public ModelLep750(boolean angle, boolean single) {
        this(angle, single, 0);
    }

    public ModelLep750(boolean angle, boolean single, int variant) {
        this.angle = angle;
        this.single = single;
        this.variant = variant;
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

    // ------------------------------------------------------------ проміжна опора ПП750 (Креслення 1)

    private void buildPortalSteel(Tessellator t) {
        for (int side = -1; side <= 1; side += 2) {
            leg(t, side);
            horns(t, side);
            foot(t, side);
        }
        crossarm(t);
    }

    /** Λ-подібна нога з конічним звуженням до шарніра під траверсою та розширенням біля фундаменту. */
    /** Λ-подібна нога однакової ширини по всій довжині з гострим трикутним звуженням біля фундаменту. */
    private void leg(Tessellator t, int side) {
        int n = 15;
        double[] ys = new double[n + 1];
        double[] cx = new double[n + 1];
        double[] hw = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            ys[i] = Lep750.LEG_TOP_Y * i / n;
            cx[i] = side * Lep750.legCentreX(ys[i]);
            hw[i] = Lep750.legHalf(ys[i]);
        }
        lattice(t, ys, cx, hw, hw, 0.15D, 0.07D, 0.055D, 2);
        // вузол-«п'ята» у вершині ноги, куди сходяться пояси траверси
        LepGeo.box(t, side * Lep750.LEG_TOP_X, Lep750.LEG_TOP_Y, 0.0D, 0.55D, 0.10D, 0.55D);
    }

    private void foot(Tessellator t, int side) {
        double x = side * Lep750.LEG_BASE_X;
        // Опорний шарнірний башмак під трикутним вістрям ноги
        LepGeo.box(t, x, 0.15D, 0.0D, 0.65D, 0.15D, 0.65D);
    }

    /** Похилий ріг тросостійки з горизонтальним козирком назовні (як на кресленні ПП750). */
    private void horns(Tessellator t, int side) {
        double y0 = Lep750.ARM_TOP_LEG_Y - 0.15D;
        double y1 = Lep750.HORN_TOP_Y;
        int n = 6;
        double[] ys = new double[n];
        double[] cx = new double[n];
        double[] hw = new double[n];
        for (int i = 0; i < n; i++) {
            double f = (double) i / (n - 1);
            ys[i] = y0 + (y1 - y0) * f;
            cx[i] = side * (Lep750.LEG_TOP_X + Lep750.HORN_SPREAD * f);
            hw[i] = 0.42D + (0.20D - 0.42D) * f;
        }
        lattice(t, ys, cx, hw, hw, 0.13D, 0.065D, 0.05D, 2);

        // Горизонтальний козирок на вершині рога, винесений назовні (як на кресленні ПП750)
        double topX = cx[n - 1];
        double tipX = side * Lep750.GROUND_X;
        double botY = y1 - 0.45D;
        for (int z = -1; z <= 1; z += 2) {
            double zw = 0.18D;
            lb(t, topX - side * 0.2D, y1, z * zw, tipX, y1, z * 0.06D, 0.11D);
            lb(t, topX + side * 0.15D, botY, z * zw, tipX, y1, z * 0.06D, 0.09D);
            lb(t, topX, y1, z * zw, topX + side * 0.15D, botY, z * zw, 0.07D);
        }
        lb(t, tipX, y1, -0.25D, tipX, y1, 0.25D, 0.09D);
    }

    /**
     * Траверса ПП750: двоскатний верхній пояс (підйом до центру) та V-подібний нижній пояс,
     * що спирається на вершини обох ніг і сходиться в центрі та на гострих консолях.
     */
    private void crossarm(Tessellator t) {
        double half = Lep750.ARM_HALF;
        int panels = 20;
        double[] xs = new double[panels + 1];
        double[] yt = new double[panels + 1];
        double[] yb = new double[panels + 1];
        double[] zh = new double[panels + 1];
        for (int p = 0; p <= panels; p++) {
            xs[p] = -half + 2.0D * half * p / panels;
            yt[p] = Lep750.armTopY(xs[p]);
            yb[p] = Lep750.armBottomY(xs[p]);
            zh[p] = 0.22D + 0.48D * Lep750.armTri(xs[p]);
        }

        for (int z = -1; z <= 1; z += 2) {
            for (int p = 0; p < panels; p++) {
                lb(t, xs[p], yt[p], z * zh[p], xs[p + 1], yt[p + 1], z * zh[p + 1], 0.13D);
                lb(t, xs[p], yb[p], z * zh[p], xs[p + 1], yb[p + 1], z * zh[p + 1], 0.13D);
                if (p % 2 == 0) {
                    lb(t, xs[p], yt[p], z * zh[p], xs[p + 1], yb[p + 1], z * zh[p + 1], 0.06D);
                } else {
                    lb(t, xs[p], yb[p], z * zh[p], xs[p + 1], yt[p + 1], z * zh[p + 1], 0.06D);
                }
            }
            for (int p = 1; p < panels; p++) {
                lb(t, xs[p], yb[p], z * zh[p], xs[p], yt[p], z * zh[p], 0.07D);
            }
        }
        // Центральна вертикальна стійка симетрії траверси
        lb(t, 0.0D, Lep750.ARM_Y, -0.25D, 0.0D, Lep750.ARM_TOP_MID_Y, 0.25D, 0.11D);

        for (int p = 1; p < panels; p++) {
            lb(t, xs[p], yb[p], -zh[p], xs[p], yb[p], zh[p], 0.07D);
            lb(t, xs[p], yt[p], -zh[p], xs[p], yt[p], zh[p], 0.065D);
            if (p % 2 == 0 && p < panels - 1) {
                lb(t, xs[p], yt[p], -zh[p], xs[p + 1], yt[p + 1], zh[p + 1], 0.045D);
                lb(t, xs[p], yt[p], zh[p], xs[p + 1], yt[p + 1], -zh[p + 1], 0.045D);
            }
        }

        // Поздовжні кронштейни під подвійні гірлянди на кожній фазі
        double halfZ = Lep750.STRING_SPACING * 0.5D;
        for (double px : Lep750.PHASE_X) {
            double by = Lep750.armBottomY(px);
            lb(t, px, by - 0.04D, -halfZ, px, by - 0.04D, halfZ, 0.10D);
            for (int sz = -1; sz <= 1; sz += 2) {
                LepGeo.box(t, px, by - 0.08D, sz * halfZ, 0.10D, 0.07D, 0.10D);
            }
        }
    }

    // ------------------------------------------------------------ проміжна опора: гірлянди, відтяжки

    private void buildPortalAtlas(Tessellator t) {
        for (double px : Lep750.PHASE_X) {
            phaseString(t, px);
        }
        for (int side = -1; side <= 1; side += 2) {
            groundHook(t, side);
            // Внутрішні відтяжки до центрального фундаментного якоря (як на кресленні ПП750)
            sd(t, side * Lep750.LEG_TOP_X, Lep750.LEG_TOP_Y - 0.15D, 0.0D, 0.0D, 0.35D, 0.0D, 0.055D);
            LepGeo.boxUV(t, side * Lep750.LEG_TOP_X, Lep750.LEG_TOP_Y - 0.15D, 0.0D, 0.10D, 0.10D, 0.10D, CAP, TEX);
        }
        LepGeo.boxUV(t, 0.0D, 0.22D, 0.0D, 0.32D, 0.22D, 0.32D, STEEL, TEX);
    }

    private void phaseString(Tessellator t, double px) {
        double top = Lep750.STRING_TOP;
        double lastDisc = Lep750.discY(Lep750.DISCS - 1);
        double rodTop = top - 0.12D;
        double rodBottom = lastDisc - 0.16D;
        double halfZ = Lep750.STRING_SPACING * 0.5D;
        double by = Lep750.armBottomY(px);

        for (int sz = -1; sz <= 1; sz += 2) {
            double zPos = sz * halfZ;
            sb(t, px, by - 0.04D, zPos, px, top - 0.04D, zPos, 0.08D);
            LepGeo.boxUV(t, px, top - 0.07D, zPos, 0.09D, 0.06D, 0.09D, STEEL, TEX);
            LepGeo.beamUV(t, px, rodTop, zPos, px, Lep750.YOKE_Y, zPos, 0.055D, CAP, TEX);
            for (int k = 0; k < Lep750.DISCS; k++) {
                LepGeo.boxUV(t, px, Lep750.discY(k), zPos, 0.19D, 0.03D, 0.19D, GLASS, TEX);
            }
            LepGeo.beamUV(t, px, rodTop, zPos, px, rodTop - 0.12D, zPos, 0.11D, CAP, TEX);
            LepGeo.beamUV(t, px, rodBottom + 0.12D, zPos, px, rodBottom, zPos, 0.11D, CAP, TEX);
        }

        double cy = Lep750.MOUNT_Y;
        double h = Lep750.BUNDLE_HALF;
        LepGeo.boxUV(t, px, Lep750.YOKE_Y, 0.0D, 0.09D, 0.065D, halfZ + 0.16D, STEEL, TEX);
        sd(t, px, Lep750.YOKE_Y - 0.08D, -halfZ, px, Lep750.YOKE_Y - 0.08D, halfZ, 0.045D);
        sb(t, px, Lep750.YOKE_Y, 0.0D, px, cy + h, 0.0D, 0.06D);

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

    /** Затискач грозотроса на кінці козирка рога. */
    private void groundHook(Tessellator t, int side) {
        double hookX = side * Lep750.GROUND_X;
        double gY = Lep750.GROUND_Y;
        sb(t, hookX, Lep750.HORN_TOP_Y, 0.0D, hookX, gY + 0.10D, 0.0D, 0.065D);
        LepGeo.boxUV(t, hookX, gY + 0.10D, 0.0D, 0.10D, 0.04D, 0.10D, CAP, TEX);
        LepGeo.boxUV(t, hookX, gY + 0.03D, 0.0D, 0.18D, 0.035D, 0.18D, GLASS, TEX);
        LepGeo.boxUV(t, hookX, gY - 0.03D, 0.0D, 0.065D, 0.035D, 0.065D, CAP, TEX);
    }

    // ------------------------------------------------------------ анкерно-кутова УС750-1 (Креслення 2)

    private void buildAngleSteel(Tessellator t) {
        if (single) {
            tower(t, variant, 0.0D);
            return;
        }
        for (int idx = -1; idx <= 1; idx++) {
            tower(t, idx, idx * Lep750.A_TOWER_X);
        }
    }

    /** idx задає форму вежі (-1 ліва, 0 середня, +1 права), tx її зміщення по X (в окремої щогли 0). */
    private void tower(Tessellator t, int idx, double tx) {
        boolean isSideTower = (idx != 0);

        // 1. Нижня пірамідальна секція (0 .. A_NECK_Y) з характерним портальним вирізом внизу (як на кресленні УС750-1)
        int nLow = 11;
        double[] ysLow = new double[nLow];
        double[] cxLow = new double[nLow];
        double[] hwLow = new double[nLow];
        for (int i = 0; i < nLow; i++) {
            ysLow[i] = Lep750.A_NECK_Y * i / (nLow - 1);
            cxLow[i] = tx;
            hwLow[i] = Lep750.angleHalf(ysLow[i]);
        }
        lattice(t, ysLow, cxLow, hwLow, hwLow, 0.18D, 0.085D, 0.07D, 3);

        // Внутрішні розкоси біля основи вежі (портальний виріз під основою, як на УС750-1)
        double h0 = Lep750.angleHalf(0.0D);
        double h2 = Lep750.angleHalf(ysLow[2]);
        for (int z = -1; z <= 1; z += 2) {
            lb(t, tx - h0, 0.0D, z * h0, tx, ysLow[2], z * h2, 0.11D);
            lb(t, tx + h0, 0.0D, z * h0, tx, ysLow[2], z * h2, 0.11D);
        }

        // Пояс-«комір» на переломі (15.5 блоків)
        double nh = Lep750.A_NECK_HALF + 0.12D;
        for (int k = 0; k < 4; k++) {
            lb(t, tx + SIGN[k][0] * nh, Lep750.A_NECK_Y, SIGN[k][1] * nh,
                  tx + SIGN[(k + 1) & 3][0] * nh, Lep750.A_NECK_Y, SIGN[(k + 1) & 3][1] * nh, 0.13D);
        }

        // Фундаментні башмаки по 4 кутах вежі
        for (int k = 0; k < 4; k++) {
            LepGeo.box(t, tx + SIGN[k][0] * h0, 0.25D, SIGN[k][1] * h0, 0.45D, 0.25D, 0.45D);
        }

        // Консолі під натяжні гірлянди (вперед і назад вздовж лінії)
        double my = Lep750.A_MOUNT_Y;
        for (int z = -1; z <= 1; z += 2) {
            double so = Lep750.A_STANDOFF;
            lb(t, tx, my, 0.0D, tx, my, z * so, 0.12D);
            lb(t, tx, my - 0.95D, 0.0D, tx, my, z * so, 0.08D);
            lb(t, tx - 0.42D, my, z * so, tx + 0.42D, my, z * so, 0.11D);
            LepGeo.box(t, tx, my, z * so, 0.22D, 0.09D, 0.12D);
        }

        // Упор під горизонтальну допоміжну гірлянду шлейфу (на центральній та правій вежах)
        if (idx >= 0 && !single) {
            double sh = Lep750.angleHalf(my);
            lb(t, tx - sh, my, 0.0D, tx - sh - Lep750.A_SUP_STUB, my, 0.0D, 0.11D);
            lb(t, tx - sh, my - 0.7D, 0.0D, tx - sh - Lep750.A_SUP_STUB, my, 0.0D, 0.065D);
            LepGeo.box(t, tx - sh - Lep750.A_SUP_STUB, my, 0.0D, 0.09D, 0.12D, 0.12D);
        }

        // Окрема щогла (ліва/середня): власний упор на +X грані під допоміжну гірлянду шлейфу
        if (single && idx != 1) {
            double sh = Lep750.angleHalf(my);
            lb(t, tx + sh, my, 0.0D, tx + sh + Lep750.A_SUP_STUB, my, 0.0D, 0.11D);
            lb(t, tx + sh, my - 0.7D, 0.0D, tx + sh + Lep750.A_SUP_STUB, my, 0.0D, 0.065D);
            LepGeo.box(t, tx + sh + Lep750.A_SUP_STUB, my, 0.0D, 0.09D, 0.12D, 0.12D);
        }

        // 2. Верхня частина: у середньої вежі (idx == 0) плоска голівка без високої тросостійки!
        if (!isSideTower) {
            double[] ysMid = { Lep750.A_NECK_Y, Lep750.A_MID_TOP };
            double[] cxMid = { tx, tx };
            double[] hwMid = { Lep750.A_NECK_HALF, Lep750.A_NECK_HALF * 0.92D };
            lattice(t, ysMid, cxMid, hwMid, hwMid, 0.15D, 0.08D, 0.06D, 0);
            LepGeo.box(t, tx, Lep750.A_MID_TOP + 0.08D, 0.0D, Lep750.A_NECK_HALF + 0.12D, 0.10D, Lep750.A_NECK_HALF + 0.12D);
            return;
        }

        // 3. Стрімка верхня тросостійка лише на лівій (-1) та правій (+1) вежах (як на УС750-1)
        int nTop = 6;
        double[] ysTop = new double[nTop];
        double[] cxTop = new double[nTop];
        double[] hwTop = new double[nTop];
        for (int i = 0; i < nTop; i++) {
            ysTop[i] = Lep750.A_NECK_Y + (Lep750.A_TOP - Lep750.A_NECK_Y) * i / (nTop - 1);
            cxTop[i] = tx;
            hwTop[i] = Lep750.angleHalf(ysTop[i]);
        }
        lattice(t, ysTop, cxTop, hwTop, hwTop, 0.13D, 0.065D, 0.05D, 2);

        // Оголовок вежі й затискач троса
        double top = Lep750.A_TOP;
        LepGeo.box(t, tx, top, 0.0D, 0.42D, 0.09D, 0.42D);
        lb(t, tx, top, 0.0D, tx, Lep750.A_GROUND_Y, 0.0D, 0.08D);
        LepGeo.box(t, tx, Lep750.A_GROUND_Y, 0.0D, 0.12D, 0.06D, 0.12D);

        // 4. Трикутні консолі вправо на лівій (мала 3850 мм) та правій (велика 10500 мм) вежах (як на кресленні УС750-1)
        double w0 = Lep750.A_TOP_HALF;
        if (idx == -1) {
            flag(t, tx, top, Lep750.A_LEFT_ARM_LEN, Lep750.A_LEFT_ARM_DEPTH, w0, 0.18D);
        } else if (idx == 1) {
            flag(t, tx, Lep750.A_ARM_Y, Lep750.A_ARM_LEN, Lep750.A_ARM_DEPTH, w0, 0.22D);
            double ex = tx + Lep750.A_ARM_LEN;
            for (int z = -1; z <= 1; z += 2) {
                LepGeo.box(t, ex, Lep750.A_ARM_Y - 0.10D, z * Lep750.A_JUMP_Z, 0.08D, 0.06D, 0.08D);
            }
            lb(t, ex, Lep750.A_ARM_Y - 0.10D, -Lep750.A_JUMP_Z, ex, Lep750.A_ARM_Y - 0.10D, Lep750.A_JUMP_Z, 0.075D);
        }
    }

    /** Ґратчастий прямокутний трикутник консолі, спрямований вправо (+X). */
    private void flag(Tessellator t, double tx, double yTop, double len, double depth, double w0, double w1) {
        int n = 7;
        double[] xs = new double[n + 1], yb = new double[n + 1], w = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            double f = (double) i / n;
            xs[i] = tx + len * f;
            yb[i] = yTop - depth * (1.0D - f);
            w[i] = w0 + (w1 - w0) * f;
        }
        for (int z = -1; z <= 1; z += 2) {
            for (int i = 0; i < n; i++) {
                lb(t, xs[i], yTop, z * w[i], xs[i + 1], yTop, z * w[i + 1], 0.11D);
                lb(t, xs[i], yb[i], z * w[i], xs[i + 1], yb[i + 1], z * w[i + 1], 0.10D);
                if (i % 2 == 0) {
                    lb(t, xs[i], yb[i], z * w[i], xs[i + 1], yTop, z * w[i + 1], 0.045D);
                } else {
                    lb(t, xs[i], yTop, z * w[i], xs[i + 1], yb[i + 1], z * w[i + 1], 0.045D);
                }
            }
            for (int i = 0; i < n; i++) {
                lb(t, xs[i], yb[i], z * w[i], xs[i], yTop, z * w[i], 0.06D);
            }
        }
        for (int i = 0; i < n; i++) {
            lb(t, xs[i], yTop, -w[i], xs[i], yTop, w[i], 0.055D);
            lb(t, xs[i], yb[i], -w[i], xs[i], yb[i], w[i], 0.055D);
        }
    }
}