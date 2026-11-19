package com.sovietindustry.models;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.render.LepGeo;
import com.sovietindustry.tileentities.TileEntityLep110Angle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;

/**
 * Решітчаста анкерно-кутова опора 110 кВ за ескізом У35-1, збудована з балок (не з ModelRenderer),
 * тому діагоналі справжні. Геометрія компілюється в display list один раз.
 * Одиниці: блоки, Y вгору, початок у центрі основи. Без дзеркала: рендерер не перевертає модель.
 * Гірлянд тут немає: їх малює LepWireRenderer повернутими в бік проводу.
 */
@SideOnly(Side.CLIENT)
public class ModelLep110Angle {

    private static final double LEG_T = 0.10D;
    private static final double BEAM_T = 0.055D;
    private static final double ARM_Z = 0.16D;

    private static final double[] YS = {
        0.0D, 1.6D, 3.2D, 4.8D, 6.0D,
        TileEntityLep110Angle.LOW_Y, 8.3D, TileEntityLep110Angle.UP_Y, 11.2D, TileEntityLep110Angle.TOP_Y
    };
    private static final int[][] SIGN = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };

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

    private static double w(double y) {
        return TileEntityLep110Angle.legHalfWidth(y);
    }

    private static double[] corner(int k, double y) {
        double hw = w(y);
        return new double[] { SIGN[k & 3][0] * hw, y, SIGN[k & 3][1] * hw };
    }

    private void build() {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();

        // 4 ноги
        for (int k = 0; k < 4; k++) {
            for (int i = 0; i < YS.length - 1; i++) {
                double[] a = corner(k, YS[i]);
                double[] b = corner(k, YS[i + 1]);
                LepGeo.beam(t, a[0], a[1], a[2], b[0], b[1], b[2], LEG_T);
            }
        }

        // горизонтальні пояси
        for (int i = 0; i < YS.length; i++) {
            for (int k = 0; k < 4; k++) {
                double[] a = corner(k, YS[i]);
                double[] b = corner(k + 1, YS[i]);
                LepGeo.beam(t, a[0], a[1], a[2], b[0], b[1], b[2], BEAM_T);
            }
        }

        // розкоси на гранях: хрест у нижній частині, одинарні вище
        for (int i = 0; i < YS.length - 1; i++) {
            boolean cross = YS[i] < TileEntityLep110Angle.LOW_Y - 0.01D;
            for (int k = 0; k < 4; k++) {
                double[] a0 = corner(k, YS[i]);
                double[] b0 = corner(k + 1, YS[i]);
                double[] a1 = corner(k, YS[i + 1]);
                double[] b1 = corner(k + 1, YS[i + 1]);
                LepGeo.beam(t, a0[0], a0[1], a0[2], b1[0], b1[1], b1[2], BEAM_T);
                if (cross) {
                    LepGeo.beam(t, b0[0], b0[1], b0[2], a1[0], a1[1], a1[2], BEAM_T);
                }
            }
        }

        // траверси: нижня має обидва плеча, верхня лише коротке (-X)
        arm(t, TileEntityLep110Angle.SHORT_ARM, TileEntityLep110Angle.LOW_Y);
        arm(t, -TileEntityLep110Angle.LONG_ARM, TileEntityLep110Angle.LOW_Y);
        arm(t, -TileEntityLep110Angle.SHORT_ARM, TileEntityLep110Angle.UP_Y);

        // шпиль тросостійки
        LepGeo.beam(t, 0.0D, TileEntityLep110Angle.TOP_Y, 0.0D, 0.0D, TileEntityLep110Angle.TOP_Y + 0.45D, 0.0D, 0.06D);

        // бетонні фундаменти під ногами
        for (int k = 0; k < 4; k++) {
            double[] c = corner(k, 0.0D);
            LepGeo.box(t, c[0], 0.14D, c[2], 0.27D, 0.14D, 0.27D);
        }

        t.draw();
    }

    /** Одне плече траверси: дві паралельні ферми (z = ±ARM_Z) із верхнім і нижнім поясом та решіткою. */
    private void arm(Tessellator t, double tipX, double armY) {
        double sgn = Math.signum(tipX);
        double rootX = sgn * Math.max(0.2D, w(armY) - 0.05D);
        double topY = armY + 0.10D;
        double tipLowY = armY - 0.10D;
        double rootLowY = armY - 0.80D;
        int n = 4;

        for (int s = -1; s <= 1; s += 2) {
            double z = s * ARM_Z;
            LepGeo.beam(t, rootX, topY, z, tipX, topY, z, BEAM_T);
            LepGeo.beam(t, rootX, rootLowY, z, tipX, tipLowY, z, BEAM_T);
            LepGeo.beam(t, tipX, topY, z, tipX, tipLowY, z, BEAM_T);
            for (int j = 0; j <= n; j++) {
                double f = (double) j / n;
                double x = rootX + (tipX - rootX) * f;
                double ly = rootLowY + (tipLowY - rootLowY) * f;
                if (j > 0 && j < n) {
                    LepGeo.beam(t, x, topY, z, x, ly, z, BEAM_T);
                }
                if (j < n) {
                    double x1 = rootX + (tipX - rootX) * (double) (j + 1) / n;
                    LepGeo.beam(t, x, ly, z, x1, topY, z, BEAM_T);
                }
            }
        }

        // поперечні зв'язки між фермами та підвіс під точку кріплення гірлянди
        LepGeo.beam(t, tipX, topY, -ARM_Z, tipX, topY, ARM_Z, BEAM_T);
        LepGeo.beam(t, tipX, tipLowY, -ARM_Z, tipX, tipLowY, ARM_Z, BEAM_T * 1.3D);
        LepGeo.beam(t, rootX, topY, -ARM_Z, rootX, topY, ARM_Z, BEAM_T);
        LepGeo.beam(t, rootX, rootLowY, -ARM_Z, rootX, rootLowY, ARM_Z, BEAM_T);
        LepGeo.beam(t, tipX, tipLowY, 0.0D, tipX, armY - TileEntityLep110Angle.MOUNT_DROP, 0.0D, 0.07D);
    }
}
