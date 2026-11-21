package com.sovietindustry.models;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.render.LepGeo;
import com.sovietindustry.tileentities.Lep330;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class ModelLep330 {

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

    public ModelLep330(boolean angle) {
        this.angle = angle;
    }

    public void renderAll() {
        if (!angle) {
            if (listAtlas < 0) {
                listAtlas = GLAllocation.generateDisplayLists(1);
                GL11.glNewList(listAtlas, GL11.GL_COMPILE);
                Tessellator t = Tessellator.instance;
                t.startDrawingQuads();
                buildPortal(t);
                t.draw();
                GL11.glEndList();
            }
            Minecraft.getMinecraft().renderEngine.bindTexture(ATLAS);
            GL11.glCallList(listAtlas);
            return;
        }

        // Анкерно-кутова опора: лише сталевий каркас. Гірлянди, верхній ізолятор і шлейф дротів
        // малює LepWireRenderer, коли підключають дріт.
        if (listSteel < 0) {
            listSteel = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(listSteel, GL11.GL_COMPILE);
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            buildLatticeSteel(t);
            t.draw();
            GL11.glEndList();
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(LATTICE_STEEL);
        GL11.glCallList(listSteel);
    }

    // ------------------------------------------------------------ допоміжні

    private static void sb(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2, double th) {
        LepGeo.beamUV(t, x1, y1, z1, x2, y2, z2, th, STEEL, TEX);
    }

    private static void sd(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2, double th) {
        LepGeo.beamUV(t, x1, y1, z1, x2, y2, z2, th, STEEL_DARK, TEX);
    }

    private static void lb(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2, double th) {
        LepGeo.beam(t, x1, y1, z1, x2, y2, z2, th);
    }

    private static void quad(Tessellator t,
                             double x0, double y0, double z0, double x1, double y1, double z1,
                             double x2, double y2, double z2, double x3, double y3, double z3,
                             double u0, double v0, double u1, double v1) {
        t.addVertexWithUV(x0, y0, z0, (u0 + 0.05D) / TEX, (v1 - 0.05D) / TEX);
        t.addVertexWithUV(x1, y1, z1, (u0 + 0.05D) / TEX, (v0 + 0.05D) / TEX);
        t.addVertexWithUV(x2, y2, z2, (u1 - 0.05D) / TEX, (v0 + 0.05D) / TEX);
        t.addVertexWithUV(x3, y3, z3, (u1 - 0.05D) / TEX, (v1 - 0.05D) / TEX);
    }

    // ------------------------------------------------------------ П-подібна опора

    private void buildPortal(Tessellator t) {
        for (int side = -1; side <= 1; side += 2) {
            double cx = side * Lep330.LEG_X;
            concretePole(t, cx, Lep330.P_CONCRETE_TOP);
            poleJoints(t, cx);
            steelPeakWithSideHook(t, side, cx);
        }
        crossBracing(t);
        beamAndAlignedGuys(t);
        for (double px : Lep330.P_PHASE_X) {
            singleStringWithRing(t, px);
        }
    }

    private void concretePole(Tessellator t, double cx, double topY) {
        for (double y0 = 0.0D; y0 < topY - 1.0E-6D; y0 += 1.0D) {
            int i = (int) y0;
            double y1 = Math.min(topY, y0 + 1.0D);
            double r0 = Lep330.pLegHalf(y0);
            double r1 = Lep330.pLegHalf(y1);
            double v = 16.0D * (i % 3);
            double vh = v + 16.0D * (y1 - y0);
            for (int k = 0; k < 4; k++) {
                int[] s0 = SIGN[k];
                int[] s1 = SIGN[(k + 1) & 3];
                double u = 16.0D * ((k + i) & 3);
                t.setNormal((s0[0] + s1[0]) * 0.5F, 0.15F, (s0[1] + s1[1]) * 0.5F);
                quad(t,
                    cx + s0[0] * r0, y0, s0[1] * r0,
                    cx + s0[0] * r1, y1, s0[1] * r1,
                    cx + s1[0] * r1, y1, s1[1] * r1,
                    cx + s1[0] * r0, y0, s1[1] * r0,
                    u, v, u + 16.0D, vh);
            }
        }
        double rt = Lep330.pLegHalf(topY);
        t.setNormal(0.0F, 1.0F, 0.0F);
        quad(t, cx - rt, topY, -rt, cx - rt, topY, rt, cx + rt, topY, rt, cx + rt, topY, -rt, 0.0D, 0.0D, 16.0D, 16.0D);

        squareBlock(t, cx, 7.0D / 16.0D, 0.0D, 8.0D / 16.0D,
            14.0D, 62.0D, 28.0D, 70.0D, 14.0D, 48.0D, 28.0D, 62.0D);
        squareBlock(t, cx, 6.0D / 16.0D, 8.0D / 16.0D, 10.0D / 16.0D,
            12.0D, 76.0D, 24.0D, 78.0D, 12.0D, 64.0D, 24.0D, 76.0D);
    }

    private static void squareBlock(Tessellator t, double cx, double h, double y0, double y1,
                                    double su0, double sv0, double su1, double sv1,
                                    double tu0, double tv0, double tu1, double tv1) {
        for (int k = 0; k < 4; k++) {
            int[] a = SIGN[k];
            int[] b = SIGN[(k + 1) & 3];
            t.setNormal((a[0] + b[0]) * 0.5F, 0.0F, (a[1] + b[1]) * 0.5F);
            quad(t,
                cx + a[0] * h, y0, a[1] * h,
                cx + a[0] * h, y1, a[1] * h,
                cx + b[0] * h, y1, b[1] * h,
                cx + b[0] * h, y0, b[1] * h,
                su0, sv0, su1, sv1);
        }
        t.setNormal(0.0F, 1.0F, 0.0F);
        quad(t, cx - h, y1, -h, cx - h, y1, h, cx + h, y1, h, cx + h, y1, -h, tu0, tv0, tu1, tv1);
    }

    private void poleJoints(Tessellator t, double cx) {
        double[] ys = { 1.55D, Lep330.P_ARM_Y - 1.55D, Lep330.P_ARM_Y, Lep330.P_CONCRETE_TOP };
        for (double y : ys) {
            double r = Lep330.pLegHalf(y) + 0.05D;
            LepGeo.boxUV(t, cx, y, 0.0D, r, 0.07D, r, STEEL, TEX);
        }
    }

    private void steelPeakWithSideHook(Tessellator t, int side, double cx) {
        double y0 = Lep330.P_CONCRETE_TOP;
        double yMid = (y0 + Lep330.P_TOP) * 0.52D;
        double yTop = Lep330.P_TOP;
        double hw = 0.22D;

        for (int k = 0; k < 4; k++) {
            double x0 = cx + SIGN[k][0] * hw;
            double z0 = SIGN[k][1] * hw;
            sb(t, x0, y0, z0, cx, yTop, 0.0D, 0.045D);
        }
        double hm = hw * 0.48D;
        for (int k = 0; k < 4; k++) {
            int kn = (k + 1) & 3;
            double xA = cx + SIGN[k][0] * hm;
            double zA = SIGN[k][1] * hm;
            double xB = cx + SIGN[kn][0] * hm;
            double zB = SIGN[kn][1] * hm;
            sd(t, xA, yMid, zA, xB, yMid, zB, 0.035D);
            sd(t, cx + SIGN[k][0] * hw, y0, SIGN[k][1] * hw, xB, yMid, zB, 0.03D);
        }

        double hookX = side * Lep330.P_GROUND_X;
        double gY = Lep330.P_GROUND_Y;
        sb(t, cx, yTop, 0.0D, hookX, yTop, 0.0D, 0.055D);
        sd(t, cx + side * hm, yMid, 0.0D, hookX, yTop, 0.0D, 0.035D);
        sb(t, hookX, yTop, 0.0D, hookX, gY + 0.16D, 0.0D, 0.045D);

        LepGeo.boxUV(t, hookX, gY + 0.13D, 0.0D, 0.07D, 0.03D, 0.07D, CAP, TEX);
        LepGeo.boxUV(t, hookX, gY + 0.07D, 0.0D, 0.16D, 0.035D, 0.16D, GLASS, TEX);
        LepGeo.boxUV(t, hookX, gY + 0.02D, 0.0D, 0.045D, 0.03D, 0.045D, CAP, TEX);
    }

    private void crossBracing(Tessellator t) {
        double yHi = Lep330.P_ARM_Y - 1.55D;
        double yLo = 1.55D;
        double xa = Lep330.LEG_X - Lep330.pLegHalf(yHi);
        double xb = Lep330.LEG_X - Lep330.pLegHalf(yLo);
        for (int z = -1; z <= 1; z += 2) {
            double zz = z * 0.06D;
            sd(t, -xa, yHi, zz, xb, yLo, zz, 0.045D);
            sd(t, xa, yHi, -zz, -xb, yLo, -zz, 0.045D);
        }
        LepGeo.boxUV(t, 0.0D, (yHi + yLo) * 0.5D, 0.0D, 0.08D, 0.08D, 0.09D, STEEL, TEX);
    }

    private void beamAndAlignedGuys(Tessellator t) {
        double y0 = Lep330.P_ARM_Y;
        double tip = Lep330.P_PHASE_X[2];
        double leg = Lep330.LEG_X;

        for (int s = -1; s <= 1; s += 2) {
            double z = s * 0.09D;
            sb(t, -tip, y0, z, tip, y0, z, 0.07D);
        }
        for (double x : new double[] { -tip, -leg, 0.0D, leg, tip }) {
            sd(t, x, y0, -0.09D, x, y0, 0.09D, 0.045D);
        }

        double yAnchor = Lep330.P_CONCRETE_TOP + 0.12D;
        double yCenterVertex = y0 + 0.42D;

        sd(t, -leg, yAnchor, 0.0D, leg, yAnchor, 0.0D, 0.024D);
        sd(t, -leg, yAnchor, 0.0D, -tip, y0 + 0.04D, 0.0D, 0.024D);
        sd(t,  leg, yAnchor, 0.0D,  tip, y0 + 0.04D, 0.0D, 0.024D);
        sd(t, -leg, yAnchor, 0.0D, 0.0D, yCenterVertex, 0.0D, 0.022D);
        sd(t,  leg, yAnchor, 0.0D, 0.0D, yCenterVertex, 0.0D, 0.022D);
        sd(t, 0.0D, yCenterVertex, 0.0D, 0.0D, y0, 0.0D, 0.03D);
    }

    private void singleStringWithRing(Tessellator t, double px) {
        double top = Lep330.P_ARM_Y;
        double bottom = Lep330.P_ARM_Y - Lep330.P_STRING_DROP;

        LepGeo.boxUV(t, px, top - 0.04D, 0.0D, 0.08D, 0.04D, 0.08D, STEEL, TEX);

        double sTop = top - 0.08D;
        double len = sTop - bottom;
        LepGeo.beamUV(t, px, sTop, 0.0D, px, bottom, 0.0D, 0.065D, CAP, TEX);
        int n = 11;
        for (int k = 0; k < n; k++) {
            double c = 0.14D + k * ((len - 0.28D) / (n - 1));
            LepGeo.boxUV(t, px, sTop - c, 0.0D, 0.15D, 0.03D, 0.15D, GLASS, TEX);
        }
        LepGeo.beamUV(t, px, sTop, 0.0D, px, sTop - 0.11D, 0.0D, 0.13D, CAP, TEX);
        LepGeo.beamUV(t, px, bottom + 0.10D, 0.0D, px, bottom, 0.0D, 0.13D, CAP, TEX);

        double ringY = bottom + 0.08D;
        double ringR = 0.26D;
        int segs = 8;
        for (int i = 0; i < segs; i++) {
            double a0 = (2.0D * Math.PI * i) / segs;
            double a1 = (2.0D * Math.PI * (i + 1)) / segs;
            sb(t,
                px + Math.cos(a0) * ringR, ringY, Math.sin(a0) * ringR,
                px + Math.cos(a1) * ringR, ringY, Math.sin(a1) * ringR,
                0.025D);
        }
        sd(t, px - ringR, ringY, 0.0D, px + ringR, ringY, 0.0D, 0.022D);
        sd(t, px, ringY, -ringR, px, ringY, ringR, 0.022D);

        double wh = Lep330.WIRE_SPACING * 0.5D;
        LepGeo.boxUV(t, px, bottom - 0.03D, 0.0D, wh + 0.07D, 0.045D, 0.04D, CAP, TEX);
        for (int w = -1; w <= 1; w += 2) {
            LepGeo.boxUV(t, px + w * wh, bottom - 0.08D, 0.0D, 0.04D, 0.05D, 0.05D, CAP, TEX);
        }
    }

    // ------------------------------------------------------------ анкерно-кутова опора (Фото)

    private double[] corner(int k, double y) {
        double hw = Lep330.angleLegHalf(y);
        return new double[] { SIGN[k & 3][0] * hw, y, SIGN[k & 3][1] * hw };
    }

    /** Металевий каркас вежі і траверс (текстура LATTICE_STEEL). */
    private void buildLatticeSteel(Tessellator t) {
        double armY = Lep330.A_ARM_Y;
        double uY = Lep330.A_UPPER_Y;
        double[] ys = { 0.0D, 1.6D, 3.2D, 4.8D, 6.4D, 8.0D, 9.6D, armY, armY + 1.05D, 13.8D, uY - 0.95D, uY, Lep330.A_TOP };
        double legT = 0.11D;
        double beamT = 0.055D;

        for (int k = 0; k < 4; k++) {
            for (int i = 0; i < ys.length - 1; i++) {
                double[] a = corner(k, ys[i]);
                double[] b = corner(k, ys[i + 1]);
                lb(t, a[0], a[1], a[2], b[0], b[1], b[2], legT);
            }
        }
        for (int i = 0; i < ys.length; i++) {
            for (int k = 0; k < 4; k++) {
                double[] a = corner(k, ys[i]);
                double[] b = corner(k + 1, ys[i]);
                lb(t, a[0], a[1], a[2], b[0], b[1], b[2], beamT);
            }
        }
        for (int i = 0; i < ys.length - 1; i++) {
            for (int k = 0; k < 4; k++) {
                double[] a0 = corner(k, ys[i]);
                double[] b0 = corner(k + 1, ys[i]);
                double[] a1 = corner(k, ys[i + 1]);
                double[] b1 = corner(k + 1, ys[i + 1]);
                lb(t, a0[0], a0[1], a0[2], b1[0], b1[1], b1[2], beamT);
                lb(t, b0[0], b0[1], b0[2], a1[0], a1[1], a1[2], beamT);
            }
        }

        // Нижній ярус: дві симетричні консолі + центральна рамка з виносними вушками
        trapezoidArm(t, -Lep330.A_ARM_LEN, armY, 1.05D, 0.70D, 0.25D, beamT);
        trapezoidArm(t,  Lep330.A_ARM_LEN, armY, 1.05D, 0.70D, 0.25D, beamT);
        centreHead(t, armY, beamT);

        // Верхній ярус (перевернутий): довга консоль вліво, менша вправо
        trapezoidArm(t, -Lep330.A_UPPER_LONG_LEN,  uY, -0.95D, 0.65D, 0.22D, beamT);
        trapezoidArm(t,  Lep330.A_UPPER_SHORT_LEN, uY, -0.75D, 0.45D, 0.16D, beamT);
        // вушко під верхні гірлянди на кінці довгої консолі (самі гірлянди динамічні)
        lb(t, -Lep330.A_UPPER_LONG_LEN, uY - 0.04D, -Lep330.A_JUMPER_Z,
              -Lep330.A_UPPER_LONG_LEN, uY - 0.04D, Lep330.A_JUMPER_Z, beamT * 1.4D);

        double hwTop = Lep330.angleLegHalf(Lep330.A_TOP);
        for (int k = 0; k < 4; k++) {
            lb(t, SIGN[k][0] * hwTop, Lep330.A_TOP, SIGN[k][1] * hwTop, 0.0D, Lep330.A_GROUND_Y + 0.15D, 0.0D, beamT);
        }

        for (int k = 0; k < 4; k++) {
            double[] c = corner(k, 0.0D);
            LepGeo.box(t, c[0], 0.16D, c[2], 0.24D, 0.16D, 0.24D);
        }
    }

    private void trapezoidArm(Tessellator t, double tipX, double baseY, double slopeDeltaY,
                              double rootHalfZ, double tipHalfZ, double beamT) {
        double sgn = Math.signum(tipX);
        double rootX = sgn * Math.max(0.15D, Lep330.angleLegHalf(baseY));
        double rootSlopedY = baseY + slopeDeltaY;
        double tipSlopedY = baseY + Math.signum(slopeDeltaY) * 0.12D;
        int n = 4;

        for (int s = -1; s <= 1; s += 2) {
            double zr = s * rootHalfZ;
            double zt = s * tipHalfZ;

            lb(t, rootX, baseY, zr, tipX, baseY, zt, beamT * 1.25D);
            lb(t, rootX, rootSlopedY, zr, tipX, tipSlopedY, zt, beamT * 1.1D);
            lb(t, tipX, baseY, zt, tipX, tipSlopedY, zt, beamT);

            for (int j = 0; j < n; j++) {
                double f0 = (double) j / n;
                double f1 = (double) (j + 1) / n;
                double xa = rootX + (tipX - rootX) * f0;
                double xb = rootX + (tipX - rootX) * f1;
                double za = zr + (zt - zr) * f0;
                double zb = zr + (zt - zr) * f1;
                double ya = rootSlopedY + (tipSlopedY - rootSlopedY) * f0;
                double yb = rootSlopedY + (tipSlopedY - rootSlopedY) * f1;

                if (j > 0) lb(t, xa, baseY, za, xa, ya, za, beamT * 0.9D);
                if ((j & 1) == 0) lb(t, xa, baseY, za, xb, yb, zb, beamT * 0.9D);
                else lb(t, xa, ya, za, xb, baseY, zb, beamT * 0.9D);
            }
        }

        for (int j = 0; j <= n; j++) {
            double f = (double) j / n;
            double x = rootX + (tipX - rootX) * f;
            double z = rootHalfZ + (tipHalfZ - rootHalfZ) * f;
            double ys = rootSlopedY + (tipSlopedY - rootSlopedY) * f;
            lb(t, x, baseY, -z, x, baseY, z, beamT);
            lb(t, x, ys, -z, x, ys, z, beamT * 0.9D);
            if (j < n) {
                double f1 = (double) (j + 1) / n;
                double x1 = rootX + (tipX - rootX) * f1;
                double z1 = rootHalfZ + (tipHalfZ - rootHalfZ) * f1;
                lb(t, x, baseY, -z, x1, baseY, z1, beamT * 0.85D);
            }
        }

        if (slopeDeltaY > 0.0D) {
            lb(t, tipX, baseY, 0.0D, tipX, baseY - Lep330.A_MOUNT_DROP, 0.0D, 0.08D);
        }
    }

    private void centreHead(Tessellator t, double armY, double beamT) {
        double reach = Lep330.A_STRING_LEN * 0.5D;
        for (int s = -1; s <= 1; s += 2) {
            double x = s * 0.22D;
            lb(t, x, armY, -reach, x, armY, reach, beamT * 1.2D);
            lb(t, x, armY + 0.45D, -0.2D, x, armY, -reach, beamT);
            lb(t, x, armY + 0.45D, 0.2D, x, armY, reach, beamT);
        }
        lb(t, -0.22D, armY, -reach, 0.22D, armY, -reach, beamT);
        lb(t, -0.22D, armY, reach, 0.22D, armY, reach, beamT);
        lb(t, 0.0D, armY, 0.0D, 0.0D, armY - Lep330.A_MOUNT_DROP, 0.0D, 0.08D);
    }
}