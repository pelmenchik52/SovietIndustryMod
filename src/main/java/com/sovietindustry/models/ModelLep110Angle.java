package com.sovietindustry.models;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.render.LepGeo;
import com.sovietindustry.tileentities.TileEntityLep110Angle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;

/**
 * Решітчаста анкерно-кутова опора 110 кВ за ескізом У35-1 (3 контакти), збудована з балок.
 *
 * Тіло: 4 ноги, що звужуються вгору за новим кресленням (без нижньої секції 5000 мм): стрункіша вежа з великими хрестами в нижній секції,
 * дрібнішими вище й вузькою головою. Траверси як на ескізі: нижній пояс горизонтальний,
 * верхній скошений вгору до стійки. Нижня траверса має обидва плеча (2800 / 3500 мм), верхня одне (2800 мм).
 * Угорі шпиль із тримачем троса. Під кінцем кожної траверси вушок, до якого чіпляється натяжна гірлянда.
 *
 * Гірлянд тут немає: їх малює LepWireRenderer повернутими в бік проводу.
 * Одиниці: блоки, Y вгору, початок у центрі основи. Без дзеркала. Текстура lep_steel.png (її прив'язує рендерер).
 */
@SideOnly(Side.CLIENT)
public class ModelLep110Angle {

    private static final double LEG_T = 0.075D;
    private static final double BEAM_T = 0.04D;
    private static final double ARM_Z = 0.12D;
    private static final int[][] SIGN = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };

    /**
     * Рівні поясів за кресленням (0.433 бл/м), без нижньої секції 5000 мм: секція 9000 мм (2 панелі),
     * секція 10500 мм до нижньої траверси (5 панелей), 4300 мм між траверсами (3 панелі), голова 6200 мм (5 панелей).
     */
    private static final double[] YS = {
        0.0D, 1.95D, TileEntityLep110Angle.SECTION_Y,
        4.81D, 5.72D, 6.63D, 7.54D, TileEntityLep110Angle.LOW_Y,
        9.07D, 9.69D, TileEntityLep110Angle.UP_Y,
        10.85D, 11.38D, 11.92D, 12.46D, TileEntityLep110Angle.TOP_Y
    };

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

        // хрести в кожній панелі на всіх гранях (нижня панель: один великий хрест)
        for (int i = 0; i < YS.length - 1; i++) {
            for (int k = 0; k < 4; k++) {
                double[] a0 = corner(k, YS[i]);
                double[] b0 = corner(k + 1, YS[i]);
                double[] a1 = corner(k, YS[i + 1]);
                double[] b1 = corner(k + 1, YS[i + 1]);
                LepGeo.beam(t, a0[0], a0[1], a0[2], b1[0], b1[1], b1[2], BEAM_T);
                LepGeo.beam(t, b0[0], b0[1], b0[2], a1[0], a1[1], a1[2], BEAM_T);
            }
        }

        // траверси: нижня має обидва плеча, верхня лише коротке (-X, праворуч на ескізі)
        arm(t, TileEntityLep110Angle.SHORT_ARM, TileEntityLep110Angle.LOW_Y, 0.65D);
        arm(t, -TileEntityLep110Angle.LONG_ARM, TileEntityLep110Angle.LOW_Y, 0.65D);
        arm(t, -TileEntityLep110Angle.SHORT_ARM, TileEntityLep110Angle.UP_Y, 0.45D);

        // шпиль і тримач троса
        double top = TileEntityLep110Angle.TOP_Y;
        LepGeo.beam(t, 0.0D, top, 0.0D, 0.0D, top + 0.50D, 0.0D, 0.05D);
        LepGeo.beam(t, -0.22D, top + 0.38D, 0.0D, 0.22D, top + 0.38D, 0.0D, BEAM_T);

        // бетонні фундаменти під ногами (вкладаються в 3x3 блоки)
        for (int k = 0; k < 4; k++) {
            double[] c = corner(k, 0.0D);
            LepGeo.box(t, c[0], 0.14D, c[2], 0.22D, 0.14D, 0.22D);
        }

        t.draw();
    }

    /**
     * Плече траверси за ескізом: нижній пояс горизонтальний на рівні кріплення, верхній скошений від кінця
     * вгору до стійки, між ними стійки й розкоси зигзагом. Дві паралельні ферми (z = +-ARM_Z).
     */
    private void arm(Tessellator t, double tipX, double armY, double rootRise) {
        double sgn = Math.signum(tipX);
        double len = Math.abs(tipX);
        double rootX = sgn * Math.max(0.12D, w(armY) - 0.03D);
        double rootTop = armY + rootRise;
        double tipTop = armY + 0.10D;
        int n = Math.max(3, (int) Math.round(len / 0.55D));

        for (int s = -1; s <= 1; s += 2) {
            double z = s * ARM_Z;
            LepGeo.beam(t, rootX, armY, z, tipX, armY, z, BEAM_T);          // нижній пояс
            LepGeo.beam(t, rootX, rootTop, z, tipX, tipTop, z, BEAM_T);     // скошений верхній пояс
            LepGeo.beam(t, tipX, armY, z, tipX, tipTop, z, BEAM_T);         // торцева стійка
            LepGeo.beam(t, rootX, armY, z, rootX, rootTop, z, BEAM_T);      // коренева стійка
            for (int j = 0; j <= n; j++) {
                double f = (double) j / n;
                double x = rootX + (tipX - rootX) * f;
                double yt = rootTop + (tipTop - rootTop) * f;
                if (j > 0 && j < n) {
                    LepGeo.beam(t, x, armY, z, x, yt, z, BEAM_T);
                }
                if (j < n) {
                    double f1 = (double) (j + 1) / n;
                    double x1 = rootX + (tipX - rootX) * f1;
                    double yt1 = rootTop + (tipTop - rootTop) * f1;
                    if ((j & 1) == 0) {
                        LepGeo.beam(t, x, armY, z, x1, yt1, z, BEAM_T);
                    } else {
                        LepGeo.beam(t, x, yt, z, x1, armY, z, BEAM_T);
                    }
                }
            }
        }

        // поперечні зв'язки між фермами
        LepGeo.beam(t, tipX, armY, -ARM_Z, tipX, armY, ARM_Z, BEAM_T);
        LepGeo.beam(t, tipX, tipTop, -ARM_Z, tipX, tipTop, ARM_Z, BEAM_T);
        LepGeo.beam(t, rootX, armY, -ARM_Z, rootX, armY, ARM_Z, BEAM_T);
        LepGeo.beam(t, rootX, rootTop, -ARM_Z, rootX, rootTop, ARM_Z, BEAM_T);

        // вушок під точку кріплення натяжної гірлянди (до MOUNT_DROP, куди чіпляється провід)
        LepGeo.beam(t, tipX, armY + 0.02D, 0.0D, tipX, armY - TileEntityLep110Angle.MOUNT_DROP, 0.0D, 0.06D);
    }
}
