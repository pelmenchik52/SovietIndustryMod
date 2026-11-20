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
 * 6-контактна опора 110 кВ (УП110-2).
 *
 * angle = false: проміжна залізобетонна опора за фото. Квадратна стійка, що звужується вгору, три траверси
 * в верхній частині, від кінця кожної тяги йдуть вгору до стійки ("дашок"), над верхнім дашком сталева решітчаста
 * щогла з тримачем троса, на кінцях траверс довгі підвісні гірлянди. Текстура з атласу lep110_single.png (як у v1).
 *
 * angle = true: анкерно-кутова решітчаста опора за ескізом. Нижня піраміда з великими хрестами, далі вузька
 * колона постійної ширини з хрестами, три траверси-"крила" (верхній пояс скошений вгору до колони) і вузький шпиль.
 * Текстура lep_steel.png, та сама, що й у 3-контактної анкерної опори. Гірлянди малює LepWireRenderer.
 *
 * Одиниці: блоки, Y вгору, початок у центрі основи. Один display list.
 */
@SideOnly(Side.CLIENT)
public class ModelLep110x2 {

    private static final ResourceLocation ATLAS =
        new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep110_single.png");
    private static final ResourceLocation LATTICE_STEEL =
        new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep_steel.png");
    private static final double TEX = 128.0D;

    // Ділянки атласу {u0, v0, u1, v1} у пікселях
    private static final double[] STEEL = { 64.0D, 4.0D, 80.0D, 12.0D };
    private static final double[] STEEL_DARK = { 0.0D, 84.0D, 16.0D, 92.0D };
    private static final double[] GLASS = { 64.0D, 50.0D, 80.0D, 54.0D };
    private static final double[] CAP = { 96.0D, 48.0D, 112.0D, 56.0D };

    private static final double ARM_Z = 0.15D;
    private static final int[][] SIGN = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };

    /** Наскільки вище траверси сходяться тяги на стійці. */
    private static final double APEX_RISE = 0.85D;
    /** Бетон закінчується там, де сходяться тяги верхньої траверси; вище йде сталева щогла. */
    private static final double POLE_TOP = Lep110x2.ARM_Y[0] + APEX_RISE;
    private static final double MAST_TOP = 16.2D;

    private final boolean angle;
    private final double legT;
    private final double beamT;
    private final double[] ys;
    private int list = -1;

    public ModelLep110x2(boolean angle) {
        this.angle = angle;
        this.legT = angle ? 0.10D : 0.085D;
        this.beamT = angle ? 0.055D : 0.045D;
        double[] a = Lep110x2.ARM_Y;
        // рівні панелей: основа, піраміда до 8.5, колона з рівнями траверс, шпиль
        this.ys = new double[] {
            0.0D, 1.0D, 1.9D, 3.5D, 5.1D, 6.8D, 8.5D,
            a[2], (a[2] + a[1]) * 0.5D, a[1], (a[1] + a[0]) * 0.5D, a[0],
            14.4D, 15.2D, 16.0D
        };
    }

    public void renderAll() {
        if (list < 0) {
            list = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(list, GL11.GL_COMPILE);
            build();
            GL11.glEndList();
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(angle ? LATTICE_STEEL : ATLAS);
        GL11.glCallList(list);
    }

    private void build() {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        if (angle) {
            buildLattice(t);
        } else {
            buildConcretePole(t);
            buildMast(t);
            buildPoleSteel(t);
            buildInsulators(t);
        }
        t.draw();
    }

    private double w(double y) {
        return Lep110x2.legHalf(y, angle);
    }

    private double[] corner(int k, double y) {
        double hw = w(y);
        return new double[] { SIGN[k & 3][0] * hw, y, SIGN[k & 3][1] * hw };
    }

    /** Основна сталь бетонної опори (атлас). */
    private static void sb(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2, double th) {
        LepGeo.beamUV(t, x1, y1, z1, x2, y2, z2, th, STEEL, TEX);
    }

    /** Другорядна сталь бетонної опори (атлас). */
    private static void sd(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2, double th) {
        LepGeo.beamUV(t, x1, y1, z1, x2, y2, z2, th, STEEL_DARK, TEX);
    }

    /** Балка решітчастої опори (звичайна текстура lep_steel.png). */
    private static void lb(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2, double th) {
        LepGeo.beam(t, x1, y1, z1, x2, y2, z2, th);
    }

    // ------------------------------------------------------------ анкерно-кутова: решітчаста опора за ескізом

    private void buildLattice(Tessellator t) {
        // 4 ноги
        for (int k = 0; k < 4; k++) {
            for (int i = 0; i < ys.length - 1; i++) {
                double[] a = corner(k, ys[i]);
                double[] b = corner(k, ys[i + 1]);
                lb(t, a[0], a[1], a[2], b[0], b[1], b[2], legT);
            }
        }

        // горизонтальні пояси на кожному рівні
        for (int i = 0; i < ys.length; i++) {
            for (int k = 0; k < 4; k++) {
                double[] a = corner(k, ys[i]);
                double[] b = corner(k + 1, ys[i]);
                lb(t, a[0], a[1], a[2], b[0], b[1], b[2], beamT);
            }
        }

        // хрести в кожній панелі на всіх гранях (піраміда, колона й шпиль)
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

        // три траверси-крила з обох боків
        for (int a = 0; a < 3; a++) {
            latticeArm(t, Lep110x2.ARM_LEN[a], Lep110x2.ARM_Y[a]);
            latticeArm(t, -Lep110x2.ARM_LEN[a], Lep110x2.ARM_Y[a]);
        }

        // шпиль і тримач троса
        lb(t, 0.0D, 16.0D, 0.0D, 0.0D, Lep110x2.GROUND_Y + 0.1D, 0.0D, 0.05D);
        lb(t, -0.25D, Lep110x2.GROUND_Y - 0.12D, 0.0D, 0.25D, Lep110x2.GROUND_Y - 0.12D, 0.0D, beamT);

        // фундаменти під ногами (вкладаються в 3x3 блоки)
        for (int k = 0; k < 4; k++) {
            double[] c = corner(k, 0.0D);
            LepGeo.box(t, c[0], 0.14D, c[2], 0.20D, 0.14D, 0.20D);
        }
    }

    /**
     * Крило траверси за ескізом: нижній пояс горизонтальний на рівні кріплення, верхній пояс скошений
     * від кінця вгору до колони, між ними стійки й розкоси зигзагом. Дві паралельні ферми (z = +-ARM_Z).
     */
    private void latticeArm(Tessellator t, double tipX, double armY) {
        double sgn = Math.signum(tipX);
        double rootX = sgn * Math.max(0.15D, w(armY));
        double rootTop = armY + 0.65D;
        double tipTop = armY + 0.05D;
        int n = 4;

        for (int s = -1; s <= 1; s += 2) {
            double z = s * ARM_Z;
            lb(t, rootX, armY, z, tipX, armY, z, beamT);
            lb(t, rootX, rootTop, z, tipX, tipTop, z, beamT);
            lb(t, tipX, armY, z, tipX, tipTop, z, beamT);
            for (int j = 0; j <= n; j++) {
                double f = (double) j / n;
                double x = rootX + (tipX - rootX) * f;
                double yt = rootTop + (tipTop - rootTop) * f;
                if (j > 0 && j < n) {
                    lb(t, x, armY, z, x, yt, z, beamT);
                }
                if (j < n) {
                    double f1 = (double) (j + 1) / n;
                    double x1 = rootX + (tipX - rootX) * f1;
                    double yt1 = rootTop + (tipTop - rootTop) * f1;
                    if ((j & 1) == 0) {
                        lb(t, x, armY, z, x1, yt1, z, beamT);
                    } else {
                        lb(t, x, yt, z, x1, armY, z, beamT);
                    }
                }
            }
        }

        // поперечні зв'язки між фермами
        lb(t, tipX, armY, -ARM_Z, tipX, armY, ARM_Z, beamT);
        lb(t, tipX, tipTop, -ARM_Z, tipX, tipTop, ARM_Z, beamT);
        lb(t, rootX, armY, -ARM_Z, rootX, armY, ARM_Z, beamT);
        lb(t, rootX, rootTop, -ARM_Z, rootX, rootTop, ARM_Z, beamT);

        // короткий підвіс під точку кріплення динамічної гірлянди
        lb(t, tipX, armY, 0.0D, tipX, armY - Lep110x2.TENSION_DROP, 0.0D, 0.07D);
    }

    // ------------------------------------------------------------ проміжна опора: квадратна бетонна стійка

    /**
     * Квадратна стійка, що звужується вгору, з фундаментом і фланцем. Кожен блок висоти накритий окремою плиткою
     * 16x16 з бетонної частини атласу (u 0..64, v 0..48): щільність текстури як у v1, плитки чергуються.
     */
    private void buildConcretePole(Tessellator t) {
        for (double y0 = 0.0D; y0 < POLE_TOP - 1.0E-6D; y0 += 1.0D) {
            int i = (int) y0;
            double y1 = Math.min(POLE_TOP, y0 + 1.0D);
            double r0 = w(y0);
            double r1 = w(y1);
            double v = 16.0D * (i % 3);
            double vh = v + 16.0D * (y1 - y0);
            for (int k = 0; k < 4; k++) {
                int[] s0 = SIGN[k];
                int[] s1 = SIGN[(k + 1) & 3];
                double u = 16.0D * ((k + i) & 3);
                t.setNormal((s0[0] + s1[0]) * 0.5F, 0.15F, (s0[1] + s1[1]) * 0.5F);
                quad(t,
                    s0[0] * r0, y0, s0[1] * r0,
                    s0[0] * r1, y1, s0[1] * r1,
                    s1[0] * r1, y1, s1[1] * r1,
                    s1[0] * r0, y0, s1[1] * r0,
                    u, v, u + 16.0D, vh);
            }
        }

        // верхня шапка
        double rt = w(POLE_TOP);
        t.setNormal(0.0F, 1.0F, 0.0F);
        quad(t, -rt, POLE_TOP, -rt, -rt, POLE_TOP, rt, rt, POLE_TOP, rt, rt, POLE_TOP, -rt, 0.0D, 0.0D, 16.0D, 16.0D);

        // фундамент 14x8x14 px (атлас 0,48) і фланець 12x2x12 px (атлас 0,64), як у v1
        squareBlock(t, 7.0D / 16.0D, 0.0D, 8.0D / 16.0D,
            14.0D, 62.0D, 28.0D, 70.0D, 14.0D, 48.0D, 28.0D, 62.0D);
        squareBlock(t, 6.0D / 16.0D, 8.0D / 16.0D, 10.0D / 16.0D,
            12.0D, 76.0D, 24.0D, 78.0D, 12.0D, 64.0D, 24.0D, 76.0D);
    }

    private static void squareBlock(Tessellator t, double h, double y0, double y1,
                                    double su0, double sv0, double su1, double sv1,
                                    double tu0, double tv0, double tu1, double tv1) {
        for (int k = 0; k < 4; k++) {
            int[] a = SIGN[k];
            int[] b = SIGN[(k + 1) & 3];
            t.setNormal((a[0] + b[0]) * 0.5F, 0.0F, (a[1] + b[1]) * 0.5F);
            quad(t,
                a[0] * h, y0, a[1] * h,
                a[0] * h, y1, a[1] * h,
                b[0] * h, y1, b[1] * h,
                b[0] * h, y0, b[1] * h,
                su0, sv0, su1, sv1);
        }
        t.setNormal(0.0F, 1.0F, 0.0F);
        quad(t, -h, y1, -h, -h, y1, h, h, y1, h, h, y1, -h, tu0, tv0, tu1, tv1);
    }

    /** Чотирикутник: вершини зліва-знизу, зліва-зверху, справа-зверху, справа-знизу; UV у пікселях. */
    private static void quad(Tessellator t,
                             double x0, double y0, double z0, double x1, double y1, double z1,
                             double x2, double y2, double z2, double x3, double y3, double z3,
                             double u0, double v0, double u1, double v1) {
        t.addVertexWithUV(x0, y0, z0, (u0 + 0.05D) / TEX, (v1 - 0.05D) / TEX);
        t.addVertexWithUV(x1, y1, z1, (u0 + 0.05D) / TEX, (v0 + 0.05D) / TEX);
        t.addVertexWithUV(x2, y2, z2, (u1 - 0.05D) / TEX, (v0 + 0.05D) / TEX);
        t.addVertexWithUV(x3, y3, z3, (u1 - 0.05D) / TEX, (v1 - 0.05D) / TEX);
    }

    /** Сталева решітчаста щогла над бетоном: чотири стійки, пояси й хрести, далі шпиль і тримач троса. */
    private void buildMast(Tessellator t) {
        double[] lv = { POLE_TOP, POLE_TOP + 0.60D, POLE_TOP + 1.20D, MAST_TOP };
        double[] hw = { 0.16D, 0.13D, 0.10D, 0.07D };
        for (int i = 0; i < lv.length - 1; i++) {
            for (int k = 0; k < 4; k++) {
                int[] s0 = SIGN[k];
                int[] s1 = SIGN[(k + 1) & 3];
                sb(t, s0[0] * hw[i], lv[i], s0[1] * hw[i], s0[0] * hw[i + 1], lv[i + 1], s0[1] * hw[i + 1], 0.05D);
                sd(t, s0[0] * hw[i], lv[i], s0[1] * hw[i], s1[0] * hw[i + 1], lv[i + 1], s1[1] * hw[i + 1], 0.03D);
                sd(t, s1[0] * hw[i], lv[i], s1[1] * hw[i], s0[0] * hw[i + 1], lv[i + 1], s0[1] * hw[i + 1], 0.03D);
            }
        }
        for (int i = 0; i < lv.length; i++) {
            for (int k = 0; k < 4; k++) {
                int[] s0 = SIGN[k];
                int[] s1 = SIGN[(k + 1) & 3];
                sd(t, s0[0] * hw[i], lv[i], s0[1] * hw[i], s1[0] * hw[i], lv[i], s1[1] * hw[i], 0.035D);
            }
        }
        // шпиль і тримач троса
        sb(t, 0.0D, MAST_TOP, 0.0D, 0.0D, Lep110x2.GROUND_Y + 0.1D, 0.0D, 0.05D);
        sb(t, -0.25D, Lep110x2.GROUND_Y - 0.12D, 0.0D, 0.25D, Lep110x2.GROUND_Y - 0.12D, 0.0D, 0.05D);
    }

    // ------------------------------------------------------------ сталь бетонної опори

    private void buildPoleSteel(Tessellator t) {
        for (int a = 0; a < 3; a++) {
            double apexY = Lep110x2.ARM_Y[a] + APEX_RISE;
            poleArm(t, Lep110x2.ARM_LEN[a], Lep110x2.ARM_Y[a], apexY);
            poleArm(t, -Lep110x2.ARM_LEN[a], Lep110x2.ARM_Y[a], apexY);
            double r = w(Lep110x2.ARM_Y[a]) + 0.05D;
            LepGeo.boxUV(t, 0.0D, Lep110x2.ARM_Y[a], 0.0D, r, 0.08D, r, STEEL, TEX);   // хомут траверси
        }
    }

    /**
     * Траверса як на фото: два паралельні швелери, розпірки й тяги від кінця траверси вгору до стійки.
     * Тяги з двох боків сходяться на стійці й дають "дашок"; біля стійки короткий підкіс.
     */
    private void poleArm(Tessellator t, double tipX, double y, double apexY) {
        double sgn = Math.signum(tipX);
        double root = sgn * w(y);
        double apex = sgn * Math.max(0.05D, w(apexY));
        for (int s = -1; s <= 1; s += 2) {
            double z = s * 0.10D;
            sb(t, root, y, z, tipX, y, z, 0.07D);                          // швелер
            sd(t, tipX, y + 0.04D, z, apex, apexY, s * 0.04D, 0.045D);      // тяга вгору до стійки
            sd(t, root, y - 0.55D, z, tipX * 0.45D, y, z, 0.05D);           // підкіс біля стійки
        }
        for (double f : new double[] { 0.35D, 0.70D, 1.0D }) {
            sd(t, tipX * f, y, -0.10D, tipX * f, y, 0.10D, 0.05D);
        }
        sd(t, tipX * 0.45D, y, -0.10D, tipX * 0.45D, y, 0.10D, 0.05D);
        LepGeo.boxUV(t, apex, apexY, 0.0D, 0.07D, 0.05D, 0.07D, STEEL, TEX); // вузол тяг на стійці
        sb(t, tipX, y, 0.0D, tipX, y - 0.10D, 0.0D, 0.07D);                  // тримач гірлянди
    }

    // ------------------------------------------------------------ підвісні гірлянди (лише проміжна опора)

    private void buildInsulators(Tessellator t) {
        for (int a = 0; a < 3; a++) {
            double top = Lep110x2.ARM_Y[a] - 0.10D;
            double bottom = Lep110x2.ARM_Y[a] - Lep110x2.SUSPENSION_DROP;
            hangString(t, Lep110x2.ARM_LEN[a], top, bottom);
            hangString(t, -Lep110x2.ARM_LEN[a], top, bottom);
        }
    }

    /** Гірлянда з 6 бірюзових тарілок: стрижень, диски й темна арматура на кінцях. */
    private static void hangString(Tessellator t, double x, double top, double bottom) {
        double len = top - bottom;
        LepGeo.beamUV(t, x, top, 0.0D, x, bottom, 0.0D, 0.07D, CAP, TEX);
        for (int k = 0; k < 6; k++) {
            double c = 0.14D + k * ((len - 0.28D) / 5.0D);
            LepGeo.boxUV(t, x, top - c, 0.0D, 0.15D, 0.03D, 0.15D, GLASS, TEX);
        }
        LepGeo.beamUV(t, x, top, 0.0D, x, top - 0.12D, 0.0D, 0.14D, CAP, TEX);
        LepGeo.beamUV(t, x, bottom + 0.10D, 0.0D, x, bottom, 0.0D, 0.14D, CAP, TEX);
    }
}
