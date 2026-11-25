package com.sovietindustry.models;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.render.LepGeo;
import com.sovietindustry.tileentities.Lep500;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/**
 * Опори 500 кВ: проміжна Λ-подібна на відтяжках та анкерно-кутова з трьох веж (angle = true).
 * Проміжна Λ-подібна опора 500 кВ на відтяжках. Каркас (нахилені ноги, пряма траверса, роги) за кресленням ПП500;
 * гірлянди по 18 тарілок, окрім внутрішніх відтяжок до центрального якоря є бічні відтяжки вперед і назад.
 */
@SideOnly(Side.CLIENT)
public class ModelLep500 {

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

    private int listSteel = -1;
    private int listAtlas = -1;
    /** Анкерно-кутова: два варіанти (шлейфи праворуч / ліворуч), різняться лише упорами на гранях веж. */
    private final boolean angle;
    private final int[] angleLists = { -1, -1 };
    /** Окрема щогла: -1 звичайна/кутова модель, 0 середня (без антенки), 1 бічна (з антенкою). Упор під шлейф на грані +X. */
    private final int singleVariant;
    private int singleList = -1;

    public ModelLep500() {
        this(false);
    }

    public ModelLep500(boolean angle) {
        this.angle = angle;
        this.singleVariant = -1;
    }

    /** Окрема анкерно-кутова щогла 500 кВ на одну фазу: variant 0 середня, 1 бічна з антенкою. */
    public ModelLep500(int singleVariant) {
        this.angle = true;
        this.singleVariant = singleVariant;
    }

    public void renderAll() {
        renderAll(1);
    }

    /** dir (+1 або -1) обирає, з якого боку веж стоять упори під допоміжні гірлянди шлейфів (лише для анкерно-кутової). */
    public void renderAll(int dir) {
        if (singleVariant >= 0) {
            if (singleList < 0) {
                singleList = GLAllocation.generateDisplayLists(1);
                GL11.glNewList(singleList, GL11.GL_COMPILE);
                Tessellator t = Tessellator.instance;
                t.startDrawingQuads();
                // dir = -1 ставить упор під допоміжну гірлянду на грань +X (див. Lep500.sSupportAttach)
                tower(t, singleVariant == 0 ? 0 : 1, 0.0D, -1, true);
                t.draw();
                GL11.glEndList();
            }
            Minecraft.getMinecraft().renderEngine.bindTexture(LATTICE_STEEL);
            GL11.glCallList(singleList);
            return;
        }
        if (angle) {
            int k = dir < 0 ? 0 : 1;
            if (angleLists[k] < 0) {
                angleLists[k] = GLAllocation.generateDisplayLists(1);
                GL11.glNewList(angleLists[k], GL11.GL_COMPILE);
                Tessellator t = Tessellator.instance;
                t.startDrawingQuads();
                for (int idx = -1; idx <= 1; idx++) {
                    tower(t, idx, idx * Lep500.A_TOWER_X, dir < 0 ? -1 : 1);
                }
                t.draw();
                GL11.glEndList();
            }
            Minecraft.getMinecraft().renderEngine.bindTexture(LATTICE_STEEL);
            GL11.glCallList(angleLists[k]);
            return;
        }
        if (listSteel < 0) {
            listSteel = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(listSteel, GL11.GL_COMPILE);
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            buildPortalSteel(t);
            t.draw();
            GL11.glEndList();
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(LATTICE_STEEL);
        GL11.glCallList(listSteel);

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

    // ------------------------------------------------------------ каркас опори ПП500

    private void buildPortalSteel(Tessellator t) {
        for (int side = -1; side <= 1; side += 2) {
            leg(t, side);
            horns(t, side);
            foot(t, side);
        }
        crossarm(t);
    }

    /** Λ-подібна нога однакової ширини по всій довжині з гострим трикутним звуженням біля фундаменту. */
    private void leg(Tessellator t, int side) {
        int n = 15;
        double[] ys = new double[n + 1];
        double[] cx = new double[n + 1];
        double[] hw = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            ys[i] = Lep500.LEG_TOP_Y * i / n;
            cx[i] = side * Lep500.legCentreX(ys[i]);
            hw[i] = Lep500.legHalf(ys[i]);
        }
        lattice(t, ys, cx, hw, hw, 0.15D, 0.07D, 0.055D, 2);
        // вузол-«п'ята» у вершині ноги, куди сходяться пояси траверси
        LepGeo.box(t, side * Lep500.LEG_TOP_X, Lep500.LEG_TOP_Y, 0.0D, 0.55D, 0.10D, 0.55D);
    }

    private void foot(Tessellator t, int side) {
        double x = side * Lep500.LEG_BASE_X;
        // Опорний шарнірний башмак під трикутним вістрям ноги
        LepGeo.box(t, x, 0.15D, 0.0D, 0.65D, 0.15D, 0.65D);
    }

    /** Похилий ріг тросостійки з горизонтальним козирком назовні (як на кресленні ПП500). */
    private void horns(Tessellator t, int side) {
        double y0 = Lep500.ARM_TOP_MID_Y - 0.15D;
        double y1 = Lep500.HORN_TOP_Y;
        int n = 6;
        double[] ys = new double[n];
        double[] cx = new double[n];
        double[] hw = new double[n];
        for (int i = 0; i < n; i++) {
            double f = (double) i / (n - 1);
            ys[i] = y0 + (y1 - y0) * f;
            cx[i] = side * (Lep500.LEG_TOP_X + Lep500.HORN_SPREAD * f);
            hw[i] = 0.38D + (0.18D - 0.38D) * f;
        }
        lattice(t, ys, cx, hw, hw, 0.13D, 0.065D, 0.05D, 2);

        // Горизонтальний козирок на вершині рога, винесений назовні (як на кресленні ПП500)
        double topX = cx[n - 1];
        double tipX = side * Lep500.GROUND_X;
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
     * Траверса ПП500 за кресленням: пряма горизонтальна ферма з рівним нижнім поясом, плоским верхнім над
     * проміжком між ногами та косим верхнім поясом, що сходить на вістря до кінців. Решітка з хрестових розкосів.
     */
    private void crossarm(Tessellator t) {
        double half = Lep500.ARM_HALF;
        int panels = 24;
        double[] xs = new double[panels + 1];
        double[] yt = new double[panels + 1];
        double[] yb = new double[panels + 1];
        double[] zh = new double[panels + 1];
        for (int p = 0; p <= panels; p++) {
            xs[p] = -half + 2.0D * half * p / panels;
            yt[p] = Lep500.armTopY(xs[p]);
            yb[p] = Lep500.armBottomY(xs[p]);
            zh[p] = 0.20D + 0.42D * Lep500.armTri(xs[p]);
        }

        for (int z = -1; z <= 1; z += 2) {
            for (int p = 0; p < panels; p++) {
                lb(t, xs[p], yt[p], z * zh[p], xs[p + 1], yt[p + 1], z * zh[p + 1], 0.12D);
                lb(t, xs[p], yb[p], z * zh[p], xs[p + 1], yb[p + 1], z * zh[p + 1], 0.12D);
                // хрестові розкоси в кожній панелі (X-решітка, як на кресленні)
                lb(t, xs[p], yt[p], z * zh[p], xs[p + 1], yb[p + 1], z * zh[p + 1], 0.055D);
                lb(t, xs[p], yb[p], z * zh[p], xs[p + 1], yt[p + 1], z * zh[p + 1], 0.055D);
            }
            for (int p = 0; p <= panels; p++) {
                lb(t, xs[p], yb[p], z * zh[p], xs[p], yt[p], z * zh[p], 0.07D);
            }
        }
        // Центральна вертикальна стійка симетрії траверси
        lb(t, 0.0D, Lep500.ARM_Y, -0.25D, 0.0D, Lep500.ARM_TOP_MID_Y, 0.25D, 0.10D);

        // Поперечні розпірки між двома площинами ферми
        for (int p = 1; p < panels; p++) {
            lb(t, xs[p], yb[p], -zh[p], xs[p], yb[p], zh[p], 0.065D);
            lb(t, xs[p], yt[p], -zh[p], xs[p], yt[p], zh[p], 0.060D);
        }

        // Поздовжні кронштейни під подвійні гірлянди на кожній фазі
        double halfZ = Lep500.STRING_SPACING * 0.5D;
        for (double px : Lep500.PHASE_X) {
            double by = Lep500.armBottomY(px);
            lb(t, px, by - 0.04D, -halfZ, px, by - 0.04D, halfZ, 0.10D);
            for (int sz = -1; sz <= 1; sz += 2) {
                LepGeo.box(t, px, by - 0.08D, sz * halfZ, 0.10D, 0.07D, 0.10D);
            }
        }
    }

    // ------------------------------------------------------------ гірлянди (18 тарілок), відтяжки

    private void buildPortalAtlas(Tessellator t) {
        for (double px : Lep500.PHASE_X) {
            phaseString(t, px);
        }
        for (int side = -1; side <= 1; side += 2) {
            groundHook(t, side);
            double topX = side * Lep500.LEG_TOP_X;
            double topY = Lep500.LEG_TOP_Y - 0.15D;
            // Внутрішня відтяжка до центрального фундаментного якоря (вигляд спереду: V)
            sd(t, topX, topY, 0.0D, 0.0D, 0.35D, 0.0D, 0.055D);
            LepGeo.boxUV(t, topX, topY, 0.0D, 0.10D, 0.10D, 0.10D, CAP, TEX);
            // Бічні відтяжки вперед і назад (вигляд збоку: трикутник із вершиною на верху ноги)
            for (int fz = -1; fz <= 1; fz += 2) {
                double ax = side * Lep500.GUY_ANCHOR_X;
                double az = fz * Lep500.GUY_Z;
                sd(t, topX, topY, 0.0D, ax, 0.30D, az, 0.055D);
                LepGeo.boxUV(t, ax, 0.14D, az, 0.26D, 0.14D, 0.26D, STEEL, TEX);
            }
        }
        LepGeo.boxUV(t, 0.0D, 0.22D, 0.0D, 0.32D, 0.22D, 0.32D, STEEL, TEX);
    }

    private void phaseString(Tessellator t, double px) {
        double top = Lep500.STRING_TOP;
        double lastDisc = Lep500.discY(Lep500.DISCS - 1);
        double rodTop = top - 0.12D;
        double rodBottom = lastDisc - 0.16D;
        double halfZ = Lep500.STRING_SPACING * 0.5D;
        double by = Lep500.armBottomY(px);

        for (int sz = -1; sz <= 1; sz += 2) {
            double zPos = sz * halfZ;
            sb(t, px, by - 0.04D, zPos, px, top - 0.04D, zPos, 0.08D);
            LepGeo.boxUV(t, px, top - 0.07D, zPos, 0.09D, 0.06D, 0.09D, STEEL, TEX);
            LepGeo.beamUV(t, px, rodTop, zPos, px, Lep500.YOKE_Y, zPos, 0.055D, CAP, TEX);
            for (int k = 0; k < Lep500.DISCS; k++) {
                LepGeo.boxUV(t, px, Lep500.discY(k), zPos, 0.19D, 0.03D, 0.19D, GLASS, TEX);
            }
            LepGeo.beamUV(t, px, rodTop, zPos, px, rodTop - 0.12D, zPos, 0.11D, CAP, TEX);
            LepGeo.beamUV(t, px, rodBottom + 0.12D, zPos, px, rodBottom, zPos, 0.11D, CAP, TEX);
        }

        double cy = Lep500.MOUNT_Y;
        double h = Lep500.BUNDLE_HALF;
        LepGeo.boxUV(t, px, Lep500.YOKE_Y, 0.0D, 0.09D, 0.065D, halfZ + 0.16D, STEEL, TEX);
        sd(t, px, Lep500.YOKE_Y - 0.08D, -halfZ, px, Lep500.YOKE_Y - 0.08D, halfZ, 0.045D);
        sb(t, px, Lep500.YOKE_Y, 0.0D, px, cy + h, 0.0D, 0.06D);

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
        double hookX = side * Lep500.GROUND_X;
        double gY = Lep500.GROUND_Y;
        sb(t, hookX, Lep500.HORN_TOP_Y, 0.0D, hookX, gY + 0.10D, 0.0D, 0.065D);
        LepGeo.boxUV(t, hookX, gY + 0.10D, 0.0D, 0.10D, 0.04D, 0.10D, CAP, TEX);
        LepGeo.boxUV(t, hookX, gY + 0.03D, 0.0D, 0.18D, 0.035D, 0.18D, GLASS, TEX);
        LepGeo.boxUV(t, hookX, gY - 0.03D, 0.0D, 0.065D, 0.035D, 0.065D, CAP, TEX);
    }

    // ------------------------------------------------------------ анкерно-кутова 500 кВ

    /**
     * Вежа анкерно-кутової: тіло як у середньої вежі АКО 750 кВ (піраміда з порталом унизу, комір, консолі під натяжні
     * гірлянди вперед і назад, плоска голівка). Бічні вежі (idx = -1 і +1) отримують лише гостру антенку з гачком грозотроса.
     * dir задає бік упору під допоміжну гірлянду шлейфу сусідньої вежі: +1 ліва грань (шлейфи йдуть вправо), -1 права.
     */
    private void tower(Tessellator t, int idx, double tx, int dir) {
        tower(t, idx, tx, dir, false);
    }

    private void tower(Tessellator t, int idx, double tx, int dir, boolean forceStub) {
        int nLow = 11;
        double[] ysLow = new double[nLow];
        double[] cxLow = new double[nLow];
        double[] hwLow = new double[nLow];
        for (int i = 0; i < nLow; i++) {
            ysLow[i] = Lep500.A_NECK_Y * i / (nLow - 1);
            cxLow[i] = tx;
            hwLow[i] = Lep500.angleHalf(ysLow[i]);
        }
        lattice(t, ysLow, cxLow, hwLow, hwLow, 0.17D, 0.08D, 0.065D, 3);

        double h0 = Lep500.angleHalf(0.0D);
        double h2 = Lep500.angleHalf(ysLow[2]);
        for (int z = -1; z <= 1; z += 2) {
            lb(t, tx - h0, 0.0D, z * h0, tx, ysLow[2], z * h2, 0.10D);
            lb(t, tx + h0, 0.0D, z * h0, tx, ysLow[2], z * h2, 0.10D);
        }

        double nh = Lep500.A_NECK_HALF + 0.12D;
        for (int k = 0; k < 4; k++) {
            lb(t, tx + SIGN[k][0] * nh, Lep500.A_NECK_Y, SIGN[k][1] * nh,
                  tx + SIGN[(k + 1) & 3][0] * nh, Lep500.A_NECK_Y, SIGN[(k + 1) & 3][1] * nh, 0.12D);
        }
        for (int k = 0; k < 4; k++) {
            LepGeo.box(t, tx + SIGN[k][0] * h0, 0.25D, SIGN[k][1] * h0, 0.42D, 0.25D, 0.42D);
        }

        // Консолі під натяжні гірлянди (вперед і назад уздовж лінії)
        double my = Lep500.A_MOUNT_Y;
        for (int z = -1; z <= 1; z += 2) {
            double so = Lep500.A_STANDOFF;
            lb(t, tx, my, 0.0D, tx, my, z * so, 0.11D);
            lb(t, tx, my - 0.9D, 0.0D, tx, my, z * so, 0.075D);
            lb(t, tx - 0.40D, my, z * so, tx + 0.40D, my, z * so, 0.10D);
            LepGeo.box(t, tx, my, z * so, 0.21D, 0.09D, 0.12D);
        }

        // Упори під допоміжні гірлянди шлейфів: на грані, зверненій до вежі, чия фаза спирається на цю вежу.
        // dir = +1: фази лівої й середньої веж кріпляться до лівих граней середньої й правої; dir = -1 дзеркально.
        boolean hasStub = forceStub || ((dir > 0) ? idx >= 0 : idx <= 0);
        if (hasStub) {
            double sh = Lep500.angleHalf(my);
            double sg = -dir;
            lb(t, tx + sg * sh, my, 0.0D, tx + sg * (sh + Lep500.A_SUP_STUB), my, 0.0D, 0.10D);
            lb(t, tx + sg * sh, my - 0.7D, 0.0D, tx + sg * (sh + Lep500.A_SUP_STUB), my, 0.0D, 0.06D);
            LepGeo.box(t, tx + sg * (sh + Lep500.A_SUP_STUB), my, 0.0D, 0.09D, 0.12D, 0.12D);
        }

        // Плоска голівка (середня вежа закінчується нею)
        double[] ysMid = { Lep500.A_NECK_Y, Lep500.A_MID_TOP };
        double[] cxMid = { tx, tx };
        double[] hwMid = { Lep500.A_NECK_HALF, Lep500.A_NECK_HALF * 0.92D };
        lattice(t, ysMid, cxMid, hwMid, hwMid, 0.14D, 0.075D, 0.055D, 0);
        if (idx == 0) {
            LepGeo.box(t, tx, Lep500.A_MID_TOP + 0.08D, 0.0D, Lep500.A_NECK_HALF + 0.12D, 0.10D, Lep500.A_NECK_HALF + 0.12D);
            return;
        }

        // Гостра ґратчаста антенка на бічних вежах (як на фото): основа лежить на голівці й повністю її перекриває
        // (така сама півширина, як у опорної площадки), далі решітка звужується до вістря, на вістрі гачок грозотроса.
        int nTop = 9;
        double[] ysTop = new double[nTop];
        double[] cxTop = new double[nTop];
        double[] hwTop = new double[nTop];
        for (int i = 0; i < nTop; i++) {
            ysTop[i] = Lep500.A_MID_TOP + (Lep500.A_ANT_TOP - Lep500.A_MID_TOP) * i / (nTop - 1);
            cxTop[i] = tx;
            hwTop[i] = Lep500.angleHalf(ysTop[i]);
        }
        lattice(t, ysTop, cxTop, hwTop, hwTop, 0.11D, 0.05D, 0.04D, 2);
        double top = Lep500.A_ANT_TOP;
        lb(t, tx, top - 0.3D, 0.0D, tx, top + 0.15D, 0.0D, 0.07D);
        lb(t, tx, top + 0.15D, 0.0D, tx, Lep500.A_GROUND_Y, 0.0D, 0.06D);
        LepGeo.box(t, tx, Lep500.A_GROUND_Y, 0.0D, 0.10D, 0.05D, 0.10D);
    }
}
