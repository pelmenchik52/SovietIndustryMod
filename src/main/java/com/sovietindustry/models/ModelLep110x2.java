package com.sovietindustry.models;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.render.LepGeo;
import com.sovietindustry.tileentities.Lep110x2;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/**
 * Решітчаста 6-контактна опора 110 кВ за ескізом УП110-2: ноги, що звужуються вгору, три траверси
 * (верхня й нижня +-1.75, середня +-2.5 блока) і спільний шпиль із кріпленням троса на вершині.
 *
 * angle = false: проміжна опора, колона 1x1, підвісні гірлянди запечені в моделі;
 * angle = true: анкерно-кутова, основа 3x3, гірлянди малює LepWireRenderer вздовж проводу.
 * Одиниці: блоки, Y вгору, початок у центрі основи. Два display list: сталь і ізолятори.
 */
@SideOnly(Side.CLIENT)
public class ModelLep110x2 {

    private static final ResourceLocation STEEL =
        new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep_steel.png");
    private static final ResourceLocation INSULATOR =
        new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep_insulator.png");

    private static final double ARM_Z = 0.15D;
    private static final int[][] SIGN = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };

    private final boolean angle;
    private final double legT;
    private final double beamT;
    private final double[] ys;
    private int steelList = -1;
    private int insulatorList = -1;

    public ModelLep110x2(boolean angle) {
        this.angle = angle;
        this.legT = angle ? 0.10D : 0.085D;
        this.beamT = angle ? 0.055D : 0.045D;
        this.ys = new double[] {
            0.0D, 1.7D, 3.4D, 5.1D, Lep110x2.ARM_Y[2], 8.1D, Lep110x2.ARM_Y[1],
            10.7D, Lep110x2.ARM_Y[0], 13.4D, 14.8D, 16.0D
        };
    }

    public void renderAll() {
        if (steelList < 0) {
            steelList = GLAllocation.generateDisplayLists(2);
            insulatorList = steelList + 1;
            GL11.glNewList(steelList, GL11.GL_COMPILE);
            buildSteel();
            GL11.glEndList();
            GL11.glNewList(insulatorList, GL11.GL_COMPILE);
            buildInsulators();
            GL11.glEndList();
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(STEEL);
        GL11.glCallList(steelList);
        if (!angle) {
            Minecraft.getMinecraft().renderEngine.bindTexture(INSULATOR);
            GL11.glCallList(insulatorList);
        }
    }

    private double w(double y) {
        return Lep110x2.legHalf(y, angle);
    }

    private double[] corner(int k, double y) {
        double hw = w(y);
        return new double[] { SIGN[k & 3][0] * hw, y, SIGN[k & 3][1] * hw };
    }

    // ------------------------------------------------------------ сталь

    private void buildSteel() {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();

        // 4 ноги
        for (int k = 0; k < 4; k++) {
            for (int i = 0; i < ys.length - 1; i++) {
                double[] a = corner(k, ys[i]);
                double[] b = corner(k, ys[i + 1]);
                LepGeo.beam(t, a[0], a[1], a[2], b[0], b[1], b[2], legT);
            }
        }

        // горизонтальні пояси на кожному рівні
        for (int i = 0; i < ys.length; i++) {
            for (int k = 0; k < 4; k++) {
                double[] a = corner(k, ys[i]);
                double[] b = corner(k + 1, ys[i]);
                LepGeo.beam(t, a[0], a[1], a[2], b[0], b[1], b[2], beamT);
            }
        }

        // розкоси: хрест під нижньою траверсою, одинарні вище
        for (int i = 0; i < ys.length - 1; i++) {
            boolean cross = ys[i] < Lep110x2.ARM_Y[2] - 0.01D;
            for (int k = 0; k < 4; k++) {
                double[] a0 = corner(k, ys[i]);
                double[] b0 = corner(k + 1, ys[i]);
                double[] a1 = corner(k, ys[i + 1]);
                double[] b1 = corner(k + 1, ys[i + 1]);
                LepGeo.beam(t, a0[0], a0[1], a0[2], b1[0], b1[1], b1[2], beamT);
                if (cross) {
                    LepGeo.beam(t, b0[0], b0[1], b0[2], a1[0], a1[1], a1[2], beamT);
                }
            }
        }

        // три траверси з обох боків
        for (int a = 0; a < 3; a++) {
            arm(t, Lep110x2.ARM_LEN[a], Lep110x2.ARM_Y[a]);
            arm(t, -Lep110x2.ARM_LEN[a], Lep110x2.ARM_Y[a]);
        }

        // шпиль і тримач троса
        LepGeo.beam(t, 0.0D, 16.0D, 0.0D, 0.0D, 16.55D, 0.0D, 0.06D);
        LepGeo.beam(t, 0.0D, Lep110x2.GROUND_Y - 0.12D, 0.0D, 0.0D, Lep110x2.GROUND_Y, 0.0D, 0.10D);
        LepGeo.beam(t, -0.25D, 15.55D, 0.0D, 0.25D, 15.55D, 0.0D, beamT);

        // фундаменти під ногами
        double fh = angle ? 0.27D : 0.20D;
        for (int k = 0; k < 4; k++) {
            double[] c = corner(k, 0.0D);
            LepGeo.box(t, c[0], 0.14D, c[2], fh, 0.14D, fh);
        }

        t.draw();
    }

    /** Одне плече траверси: дві паралельні ферми (z = +-ARM_Z), верхній і нижній пояс, решітка. */
    private void arm(Tessellator t, double tipX, double armY) {
        double sgn = Math.signum(tipX);
        double rootX = sgn * Math.max(0.15D, w(armY) - 0.04D);
        double topY = armY + 0.10D;
        double tipLowY = armY - 0.10D;
        double rootLowY = armY - 0.75D;
        int n = 4;

        for (int s = -1; s <= 1; s += 2) {
            double z = s * ARM_Z;
            LepGeo.beam(t, rootX, topY, z, tipX, topY, z, beamT);
            LepGeo.beam(t, rootX, rootLowY, z, tipX, tipLowY, z, beamT);
            LepGeo.beam(t, tipX, topY, z, tipX, tipLowY, z, beamT);
            for (int j = 0; j <= n; j++) {
                double f = (double) j / n;
                double x = rootX + (tipX - rootX) * f;
                double ly = rootLowY + (tipLowY - rootLowY) * f;
                if (j > 0 && j < n) {
                    LepGeo.beam(t, x, topY, z, x, ly, z, beamT);
                }
                if (j < n) {
                    double x1 = rootX + (tipX - rootX) * (double) (j + 1) / n;
                    LepGeo.beam(t, x, ly, z, x1, topY, z, beamT);
                }
            }
        }

        // поперечні зв'язки між фермами
        LepGeo.beam(t, tipX, topY, -ARM_Z, tipX, topY, ARM_Z, beamT);
        LepGeo.beam(t, tipX, tipLowY, -ARM_Z, tipX, tipLowY, ARM_Z, beamT * 1.3D);
        LepGeo.beam(t, rootX, topY, -ARM_Z, rootX, topY, ARM_Z, beamT);
        LepGeo.beam(t, rootX, rootLowY, -ARM_Z, rootX, rootLowY, ARM_Z, beamT);

        // анкерна опора: короткий підвіс під точку кріплення динамічної гірлянди
        if (angle) {
            LepGeo.beam(t, tipX, tipLowY, 0.0D, tipX, armY - Lep110x2.TENSION_DROP, 0.0D, 0.07D);
        }
    }

    // ------------------------------------------------------------ підвісні гірлянди (лише проміжна опора)

    private void buildInsulators() {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (int a = 0; a < 3; a++) {
            double top = Lep110x2.ARM_Y[a] - 0.10D;
            double bottom = Lep110x2.ARM_Y[a] - Lep110x2.SUSPENSION_DROP;
            hangString(t, Lep110x2.ARM_LEN[a], top, bottom);
            hangString(t, -Lep110x2.ARM_LEN[a], top, bottom);
        }
        t.draw();
    }

    /** Гірлянда з 6 тарілок: стрижень, диски й арматура на кінцях. */
    private static void hangString(Tessellator t, double x, double top, double bottom) {
        double len = top - bottom;
        LepGeo.beam(t, x, top, 0.0D, x, bottom, 0.0D, 0.07D);
        for (int k = 0; k < 6; k++) {
            double c = 0.14D + k * ((len - 0.28D) / 5.0D);
            LepGeo.beam(t, x, top - c + 0.03D, 0.0D, x, top - c - 0.03D, 0.0D, 0.30D);
        }
        LepGeo.beam(t, x, top, 0.0D, x, top - 0.12D, 0.0D, 0.14D);
        LepGeo.beam(t, x, bottom + 0.10D, 0.0D, x, bottom, 0.0D, 0.14D);
    }
}
