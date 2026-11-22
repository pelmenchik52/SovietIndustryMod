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
 * Модель трансформаторної підстанції 330 кВ 7x6x8 (ширина x довжина x висота), компоновка як на реальному ВРП:
 *
 *   вхід (z' = -3 .. -2)   лінійний портал: дві конічні бетонні стійки з ґратчастими щоглами-блискавкоприймачами,
 *                          ґратчаста траверса з виступом праворуч, два розкоси, що сходяться під траверсою;
 *   лінійна комірка        на кожну фазу роз'єднувач (два опорні ізолятори й ніж) та ОПН, що висить на шлейфі;
 *   трансформатор          бак із двома батареями радіаторів, консерватор-циліндр, три вводи ВН угору, три вводи НН;
 *   вихід (z' = +2 .. +3)  три кабельні порти: повні блоки 1x1x1 з оранжевою плитою на торці.
 *
 * Координати центровані по плиті: центр (0, 0, 0), x' -3.5..+3.5, z' -3..+3, y 0..8.
 * Рендерер зсуває модель на (CENTER_X, 0, CENTER_Z), тому майстер-блок лишається в клітинці (0, 0).
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
        double pz = Substation330.PORTAL_Z;

        // плита 7x6 (0.5 блока на тайл => шви кожні 2 блоки)
        LepGeo.boxTiled(t, 0.0D, SLAB_H * 0.5D, 0.0D, 3.5D, SLAB_H * 0.5D, 3.0D, 0.5D);

        // конічні бетонні стійки порталу (звужуються вгору) на фундаментних плитах
        for (int s = -1; s <= 1; s += 2) {
            double cx = s * Substation330.PORTAL_X;
            LepGeo.boxTiled(t, cx, SLAB_H + 0.05D, pz, 0.40D, 0.05D, 0.40D, 0.5D);
            int segs = 6;
            double h = (Substation330.COLUMN_TOP - SLAB_H - 0.10D) / segs;
            for (int i = 0; i < segs; i++) {
                double f = (i + 0.5D) / segs;
                double half = Substation330.COLUMN_HALF_BOTTOM
                    + (Substation330.COLUMN_HALF_TOP - Substation330.COLUMN_HALF_BOTTOM) * f;
                double y0 = SLAB_H + 0.10D + i * h;
                LepGeo.boxTiled(t, cx, y0 + h * 0.5D, pz, half, h * 0.5D, half, 1.0D);
            }
        }

        // фундамент трансформатора точно по клітинках бака (3x3) і бортик маслоприймача
        double cz = Substation330.TANK_CZ;
        LepGeo.boxTiled(t, 0.0D, SLAB_H + 0.05D, cz, 1.5D, 0.05D, 1.5D, 0.5D);
        LepGeo.boxTiled(t, 0.0D, SLAB_H + 0.06D, cz - 1.45D, 1.55D, 0.06D, 0.05D, 0.5D);
        LepGeo.boxTiled(t, 0.0D, SLAB_H + 0.06D, cz + 1.45D, 1.55D, 0.06D, 0.05D, 0.5D);
        LepGeo.boxTiled(t, -1.50D, SLAB_H + 0.06D, cz, 0.05D, 0.06D, 1.50D, 0.5D);
        LepGeo.boxTiled(t, 1.50D, SLAB_H + 0.06D, cz, 0.05D, 0.06D, 1.50D, 0.5D);

        // фундаменти лінійної комірки: під роз'єднувач і під ОПН кожної фази
        for (double px : Substation330.PHASE_X) {
            LepGeo.boxTiled(t, px, SLAB_H + 0.05D, Substation330.DISC_Z, 0.50D, 0.05D, 0.18D, 0.5D);
            LepGeo.boxTiled(t, px, SLAB_H + 0.10D, Substation330.ARR_Z, 0.16D, 0.10D, 0.16D, 0.5D);
        }

        // фундамент шафи керування
        LepGeo.boxTiled(t, Substation330.CAB_X, SLAB_H + 0.05D, Substation330.CAB_Z, 0.30D, 0.05D, 0.55D, 0.5D);

        // вихідні кабельні порти: повні блоки 1x1x1 (трохи більші за клітинку, щоб не мерехтіли грані з плитою)
        for (int lx : Substation330.PORT_LX) {
            LepGeo.boxTiled(t, Substation330.cellX(lx), 0.5D, Substation330.PORT_Z, 0.502D, 0.5D, 0.502D, 1.0D);
        }
    }

    // ------------------------------------------------------------ бак трансформатора

    private static void tank(Tessellator t) {
        double cz = Substation330.TANK_CZ;
        double cy = (Substation330.TANK_BOT + Substation330.TANK_TOP) * 0.5D;
        double hy = (Substation330.TANK_TOP - Substation330.TANK_BOT) * 0.5D;
        double hx = Substation330.TANK_HX;
        double hz = Substation330.TANK_HZ;

        // основний бак і фланець кришки
        LepGeo.box(t, 0.0D, cy, cz, hx, hy, hz);
        LepGeo.box(t, 0.0D, Substation330.LID_Y - 0.05D, cz, hx + 0.07D, 0.05D, hz + 0.07D);
        // ребра жорсткості на торцях бака
        for (int s = -1; s <= 1; s += 2) {
            for (int j = -1; j <= 1; j += 2) {
                LepGeo.box(t, s * (hx + 0.03D), cy, cz + j * 0.35D, 0.03D, hy - 0.08D, 0.05D);
            }
        }

        // радіаторні батареї з обох довгих боків (по 7 панелей), як на фото
        for (int s = -1; s <= 1; s += 2) {
            double rz = cz + s * (hz + 0.20D);
            for (int i = 0; i < 7; i++) {
                double rx = -1.20D + i * 0.40D;
                LepGeo.box(t, rx, 1.65D, rz, 0.07D, 1.10D, 0.17D);
            }
            // колектори радіаторів
            LepGeo.box(t, 0.0D, 2.72D, cz + s * (hz + 0.08D), 1.30D, 0.07D, 0.08D);
            LepGeo.box(t, 0.0D, 0.62D, cz + s * (hz + 0.08D), 1.30D, 0.07D, 0.08D);
        }

        // консерватор: горизонтальний циліндр на двох сідлових опорах
        cylinderX(t, Substation330.CONS_CX, Substation330.CONS_CY, Substation330.CONS_CZ,
            Substation330.CONS_R, Substation330.CONS_HALF, 14);
        double supportTop = Substation330.CONS_CY - Substation330.CONS_R + 0.06D;
        double supportMid = (Substation330.LID_Y + supportTop) * 0.5D;
        double supportHalf = (supportTop - Substation330.LID_Y) * 0.5D;
        for (int s = -1; s <= 1; s += 2) {
            LepGeo.box(t, Substation330.CONS_CX + s * 0.65D, supportMid, Substation330.CONS_CZ, 0.08D, supportHalf, 0.26D);
            cylinderX(t, Substation330.CONS_CX + s * 0.30D, Substation330.CONS_CY, Substation330.CONS_CZ,
                Substation330.CONS_R + 0.03D, 0.05D, 14);
        }
        // трубопровід до бака (газове реле)
        LepGeo.beam(t, Substation330.CONS_CX - 0.15D, Substation330.CONS_CY - Substation330.CONS_R,
            Substation330.CONS_CZ - 0.12D, Substation330.CONS_CX - 0.15D, Substation330.LID_Y,
            Substation330.CONS_CZ - 0.12D, 0.09D);
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
        double pz = Substation330.PORTAL_Z;
        double px = Substation330.PORTAL_X;

        // 1. щогли-блискавкоприймачі над бетонними стійками (ґратчасті, звужуються вгору)
        latticeMast(t, -px, pz, Substation330.COLUMN_TOP - 0.05D, Substation330.MAST_TOP, 0.22D, 0.07D, 4);
        latticeMast(t, px, pz, Substation330.COLUMN_TOP - 0.05D, Substation330.MAST_TOP, 0.22D, 0.07D, 4);

        // 2. ґратчаста траверса (дві паралельні ферми, як на фото) із виступом праворуч за стійку
        double x0 = Substation330.BEAM_X0, x1 = Substation330.BEAM_X1;
        double top = Substation330.BEAM_TOP, low = Substation330.BEAM_LOW;
        int segs = 14;
        double step = (x1 - x0) / segs;
        for (int s = -1; s <= 1; s += 2) {
            double z = pz + s * Substation330.BEAM_PLANE;
            LepGeo.beam(t, x0, top, z, x1, top, z, 0.10D);
            LepGeo.beam(t, x0, low, z, x1, low, z, 0.09D);
            for (int j = 0; j < segs; j++) {
                boolean up = (j & 1) == 0;
                LepGeo.beam(t, x0 + j * step, up ? low : top, z, x0 + (j + 1) * step, up ? top : low, z, 0.045D);
            }
        }
        for (int j = 0; j <= segs; j += 2) {
            double x = x0 + j * step;
            LepGeo.beam(t, x, top, pz - Substation330.BEAM_PLANE, x, top, pz + Substation330.BEAM_PLANE, 0.05D);
            LepGeo.beam(t, x, low, pz - Substation330.BEAM_PLANE, x, low, pz + Substation330.BEAM_PLANE, 0.05D);
        }

        // 3. два розкоси, що сходяться під траверсою в центрі й упираються в стійки
        for (int s = -1; s <= 1; s += 2) {
            double colX = s * (px - Substation330.COLUMN_HALF_BOTTOM + 0.05D);
            for (int j = -1; j <= 1; j += 2) {
                double z = pz + j * 0.10D;
                LepGeo.beam(t, 0.0D, low, z, colX, Substation330.BRACE_Y, z, 0.07D);
            }
            // поперечні в'язі розкосу
            for (int k = 1; k <= 3; k++) {
                double f = k / 4.0D;
                double bx = colX * f;
                double by = low + (Substation330.BRACE_Y - low) * f;
                LepGeo.beam(t, bx, by, pz - 0.10D, bx, by, pz + 0.10D, 0.04D);
            }
        }
        LepGeo.box(t, 0.0D, low - 0.04D, pz, 0.14D, 0.04D, 0.14D);

        // 4. підвіски трьох фаз під траверсою (сюди чіпляються анкерні гірлянди з дротами)
        for (double fx : Substation330.PHASE_X) {
            LepGeo.beam(t, fx, low, pz, fx, Substation330.MOUNT_Y, pz, 0.07D);
        }

        // 5. лінійна комірка: роз'єднувач на кожну фазу (база, дві опори, ніж) і шлейфи
        for (int i = 0; i < 3; i++) {
            double fx = Substation330.PHASE_X[i];
            double dz = Substation330.DISC_Z;
            double hh = Substation330.DISC_HALF;
            double by = Substation330.DISC_BASE_Y;
            // швелер бази на двох сталевих опорах
            LepGeo.beam(t, fx - hh - 0.08D, by, dz, fx + hh + 0.08D, by, dz, 0.11D);
            LepGeo.box(t, fx - hh, (SLAB_H + by) * 0.5D, dz, 0.06D, (by - SLAB_H) * 0.5D, 0.06D);
            LepGeo.box(t, fx + hh, (SLAB_H + by) * 0.5D, dz, 0.06D, (by - SLAB_H) * 0.5D, 0.06D);
            // ніж (горизонтальний розрив) між верхівками ізоляторів і клеми
            double ty = Substation330.DISC_BLADE_Y;
            LepGeo.beam(t, fx - hh, ty, dz, fx + hh - 0.05D, ty + 0.07D, dz, 0.05D);
            LepGeo.box(t, fx - hh, ty, dz, 0.07D, 0.05D, 0.07D);
            LepGeo.box(t, fx + hh, ty, dz, 0.07D, 0.05D, 0.07D);

            // шлейф 1: від підвіски порталу до клеми роз'єднувача
            cable(t, fx, Substation330.MOUNT_Y, pz, fx - hh, ty + 0.05D, dz, 0.30D);
            // шлейф 2: від другої клеми до верхівки вводу ВН трансформатора
            double tx = Substation330.HV_X[i] * Substation330.HV_TILT;
            double ty2 = Substation330.HV_TOP_Y + 0.12D;
            cable(t, fx + hh, ty + 0.05D, dz, tx, ty2, Substation330.HV_Z, 0.35D);
            // наконечник на верхівці вводу ВН
            LepGeo.box(t, tx, ty2, Substation330.HV_Z, 0.09D, 0.07D, 0.09D);

            // ОПН висить на шлейфі: короткий спуск від шлейфа до верхівки ОПН (шунт на землю)
            double f = (Substation330.ARR_Z - dz) / (Substation330.HV_Z - dz);
            double[] jp = Substation330.cablePoint(fx + hh, ty + 0.05D, dz, tx, ty2, Substation330.HV_Z, 0.35D, f);
            LepGeo.beam(t, fx, Substation330.ARR_TOP_Y + 0.08D, Substation330.ARR_Z, jp[0], jp[1], jp[2], 0.03D);
            // вирівнювальне кільце на верхівці ОПН і заземлювальна смуга
            LepGeo.box(t, fx, Substation330.ARR_TOP_Y + 0.04D, Substation330.ARR_Z, 0.17D, 0.03D, 0.17D);
            LepGeo.box(t, fx + 0.22D, 0.55D, Substation330.ARR_Z, 0.025D, 0.30D, 0.04D);
        }

        // 6. сторона НН: шини від трьох вводів до верхівок вихідних портів-блоків
        for (int i = 0; i < 3; i++) {
            double portX = Substation330.cellX(Substation330.PORT_LX[i]);
            cable(t, Substation330.LV_X[i], Substation330.LV_TOP_Y + 0.06D, Substation330.LV_Z,
                portX, 1.12D, Substation330.PORT_Z - 0.15D, 0.45D);
            LepGeo.box(t, Substation330.LV_X[i], Substation330.LV_TOP_Y + 0.07D, Substation330.LV_Z, 0.07D, 0.05D, 0.07D);
            // кабельна муфта на верхівці порту
            LepGeo.box(t, portX, 1.07D, Substation330.PORT_Z - 0.15D, 0.12D, 0.06D, 0.12D);
        }
    }

    /** Провисаючий декоративний кабель (парабола з 8 відрізків, формула та сама, що Substation330.cablePoint). */
    private static void cable(Tessellator t, double x1, double y1, double z1,
                              double x2, double y2, double z2, double sag) {
        int n = 8;
        double[] p = { x1, y1, z1 };
        for (int i = 1; i <= n; i++) {
            double[] c = Substation330.cablePoint(x1, y1, z1, x2, y2, z2, sag, (double) i / n);
            LepGeo.beam(t, p[0], p[1], p[2], c[0], c[1], c[2], 0.035D);
            p = c;
        }
    }

    /** Ґратчаста щогла на чотирьох ногах, що звужується від півширини w0 до w1, steps секцій із розкосами. */
    private static void latticeMast(Tessellator t, double cx, double cz, double y0, double y1,
                                    double w0, double w1, int steps) {
        double dh = (y1 - y0) / steps;
        for (int s = 0; s < steps; s++) {
            double ya = y0 + s * dh, yb = y0 + (s + 1) * dh;
            double wa = w0 + (w1 - w0) * s / steps;
            double wb = w0 + (w1 - w0) * (s + 1) / steps;
            double[][] c = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };
            for (int k = 0; k < 4; k++) {
                int n = (k + 1) & 3;
                LepGeo.beam(t, cx + c[k][0] * wa, ya, cz + c[k][1] * wa, cx + c[k][0] * wb, yb, cz + c[k][1] * wb, 0.05D);
                // розкіс на грані між ногами k і n
                LepGeo.beam(t, cx + c[k][0] * wa, ya, cz + c[k][1] * wa, cx + c[n][0] * wb, yb, cz + c[n][1] * wb, 0.035D);
                // горизонтальний пояс на рівні yb
                LepGeo.beam(t, cx + c[k][0] * wb, yb, cz + c[k][1] * wb, cx + c[n][0] * wb, yb, cz + c[n][1] * wb, 0.035D);
            }
        }
        // блискавкоприймальний стрижень на верхівці
        LepGeo.beam(t, cx, y1, cz, cx, y1 + 0.0D, cz, 0.04D);
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
        // три нижчі вводи НН
        for (int i = 0; i < 3; i++) {
            double bx = Substation330.LV_X[i];
            bushing(t, bx, Substation330.LID_Y, Substation330.LV_Z, bx, Substation330.LV_TOP_Y, Substation330.LV_Z,
                0.14D, 0.11D, 4);
        }
        // опорні ізолятори роз'єднувачів (по два на фазу) і колони ОПН
        for (double fx : Substation330.PHASE_X) {
            double hh = Substation330.DISC_HALF;
            bushing(t, fx - hh, Substation330.DISC_BASE_Y, Substation330.DISC_Z,
                fx - hh, Substation330.DISC_POST_TOP, Substation330.DISC_Z, 0.13D, 0.10D, 4);
            bushing(t, fx + hh, Substation330.DISC_BASE_Y, Substation330.DISC_Z,
                fx + hh, Substation330.DISC_POST_TOP, Substation330.DISC_Z, 0.13D, 0.10D, 4);
            bushing(t, fx, SLAB_H + 0.20D, Substation330.ARR_Z, fx, Substation330.ARR_TOP_Y, Substation330.ARR_Z,
                0.14D, 0.11D, 9);
        }
    }

    // ------------------------------------------------------------ шафа керування

    private static void cabinets(Tessellator t) {
        LepGeo.box(t, Substation330.CAB_X, SLAB_H + 0.60D, Substation330.CAB_Z, 0.20D, 0.60D, 0.45D);
    }

    // ------------------------------------------------------------ оранжеві плити на торцях портів

    private static void ports(Tessellator t) {
        // оранжева плита 0.75x0.75 на зовнішній грані порту (z' = +3), туди ставиться кабель NTM
        for (int lx : Substation330.PORT_LX) {
            padZ(t, Substation330.END_Z + 0.004D, Substation330.cellX(lx), 0.5D, 0.375D);
        }
    }

    /** Плита на торцевій грані +Z, повністю накрита текстурою. */
    private static void padZ(Tessellator t, double z, double cx, double cy, double half) {
        double x1 = cx - half, x2 = cx + half, y1 = cy - half, y2 = cy + half;
        t.setNormal(0.0F, 0.0F, 1.0F);
        t.addVertexWithUV(x2, y1, z, 0.0D, 1.0D);
        t.addVertexWithUV(x2, y2, z, 0.0D, 0.0D);
        t.addVertexWithUV(x1, y2, z, 1.0D, 0.0D);
        t.addVertexWithUV(x1, y1, z, 1.0D, 1.0D);
    }
}
