package com.sovietindustry.models;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.render.LepGeo;
import com.sovietindustry.tileentities.Substation750;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/**
 * Модель трансформаторної підстанції 750 кВ 13x13x20 (ширина x довжина x висота), компоновка як на реальному ВРП:
 *
 *   вхід (z' = +6)         лінійний портал: дві конічні бетонні стійки з ґратчастими щоглами-блискавкоприймачами,
 *                          ґратчаста траверса з виступом праворуч, два розкоси, що сходяться під траверсою;
 *   лінійна комірка        на кожну фазу роз'єднувач (два опорні ізолятори й ніж) та ОПН, що висить на шлейфі;
 *   трансформатор          великий бак із двома батареями радіаторів, консерватор-циліндр, три високі вводи ВН,
 *                          чотири вводи НН;
 *   вихід (z' = -6)        чотири кабельні порти: повні блоки 1x1x1 з оранжевою плитою розміром із кабель;
 *   кути виходу            два блискавкоприймальні ґратчасті щогли.
 *
 * Координати центровані по плиті: центр (0, 0, 0), x' -4.5..+4.5, z' -4.5..+4.5, y 0..15.
 * Рендерер зсуває модель на (CENTER_X, 0, CENTER_Z), тому майстер-блок лишається в клітинці (0, 0).
 */
@SideOnly(Side.CLIENT)
public class ModelSubstation750kV {

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

    private static final double SLAB_H = Substation750.SLAB_H;

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
        double pz = Substation750.PORTAL_Z;
        // плита 13x13 (0.5 блока на тайл => шви кожні 2 блоки)
        LepGeo.boxTiled(t, 0.0D, SLAB_H * 0.5D, 0.0D, 6.5D, SLAB_H * 0.5D, 6.5D, 0.5D);

        // фундаментні башмаки під чотири ноги кожної стійки порталу
        for (int s = -1; s <= 1; s += 2) {
            double cx = s * Substation750.PORTAL_X;
            double w = Substation750.COLUMN_HALF_BOTTOM;
            for (int i = -1; i <= 1; i += 2) {
                for (int j = -1; j <= 1; j += 2) {
                    LepGeo.boxTiled(t, cx + i * w, SLAB_H + 0.06D, pz + j * w, 0.22D, 0.06D, 0.22D, 0.5D);
                }
            }
        }

        // фундамент трансформатора (по габариту бака з радіаторами) і бортик маслоприймача
        double cz = Substation750.TANK_CZ;
        double fhx = Substation750.TANK_HX + 0.3D;
        double fhz = Substation750.TANK_HZ + 0.65D;
        LepGeo.boxTiled(t, 0.0D, SLAB_H + 0.05D, cz, fhx, 0.05D, fhz, 0.5D);
        LepGeo.boxTiled(t, 0.0D, SLAB_H + 0.06D, cz - fhz, fhx + 0.05D, 0.06D, 0.05D, 0.5D);
        LepGeo.boxTiled(t, 0.0D, SLAB_H + 0.06D, cz + fhz, fhx + 0.05D, 0.06D, 0.05D, 0.5D);
        LepGeo.boxTiled(t, -fhx, SLAB_H + 0.06D, cz, 0.05D, 0.06D, fhz, 0.5D);
        LepGeo.boxTiled(t, fhx, SLAB_H + 0.06D, cz, 0.05D, 0.06D, fhz, 0.5D);

        // фундаменти апаратів: роз'єднувач, трансформатор струму, ОПН на кожну фазу
        for (double px : Substation750.PHASE_X) {
            LepGeo.boxTiled(t, px, SLAB_H + 0.05D, Substation750.DISC_Z, 0.80D, 0.05D, 0.35D, 0.5D);
            LepGeo.boxTiled(t, px, SLAB_H + 0.05D, Substation750.CT_Z, 0.40D, 0.05D, 0.40D, 0.5D);
            LepGeo.boxTiled(t, px, SLAB_H + 0.08D, Substation750.ARR_Z, 0.22D, 0.08D, 0.22D, 0.5D);
        }

        LepGeo.boxTiled(t, Substation750.CAB_X, SLAB_H + 0.05D, Substation750.CAB_Z, 0.30D, 0.05D, 0.55D, 0.5D);
        for (int s = -1; s <= 1; s += 2) {
            LepGeo.boxTiled(t, s * Substation750.LIGHT_X, SLAB_H + 0.05D, Substation750.LIGHT_Z, 0.40D, 0.05D, 0.40D, 0.5D);
        }
        for (int lx : Substation750.PORT_LX) {
            LepGeo.boxTiled(t, Substation750.cellX(lx), SLAB_H + 0.02D, Substation750.PORT_Z, 0.46D, 0.02D, 0.46D, 1.0D);
        }
    }

    // ------------------------------------------------------------ бак трансформатора

    private static void tank(Tessellator t) {
        double cz = Substation750.TANK_CZ;
        double cy = (Substation750.TANK_BOT + Substation750.TANK_TOP) * 0.5D;
        double hy = (Substation750.TANK_TOP - Substation750.TANK_BOT) * 0.5D;
        double hx = Substation750.TANK_HX;
        double hz = Substation750.TANK_HZ;

        // основний бак і фланець кришки
        LepGeo.box(t, 0.0D, cy, cz, hx, hy, hz);
        LepGeo.box(t, 0.0D, Substation750.LID_Y - 0.05D, cz, hx + 0.07D, 0.05D, hz + 0.07D);
        // ребра жорсткості на торцях бака
        for (int s = -1; s <= 1; s += 2) {
            for (int j = -1; j <= 1; j += 2) {
                LepGeo.box(t, s * (hx + 0.03D), cy, cz + j * 0.60D, 0.03D, hy - 0.08D, 0.06D);
            }
        }

        // радіаторні батареї з обох довгих боків (по 7 панелей), як на фото
        for (int s = -1; s <= 1; s += 2) {
            double rz = cz + s * (hz + 0.20D);
            for (int i = 0; i < 15; i++) {
                double rx = -2.80D + i * 0.40D;
                LepGeo.box(t, rx, 1.95D, rz, 0.07D, 1.40D, 0.17D);
            }
            // колектори радіаторів
            LepGeo.box(t, 0.0D, 3.42D, cz + s * (hz + 0.08D), 2.95D, 0.07D, 0.08D);
            LepGeo.box(t, 0.0D, 0.62D, cz + s * (hz + 0.08D), 2.95D, 0.07D, 0.08D);
        }

        // консерватор: горизонтальний циліндр на двох сідлових опорах
        cylinderX(t, Substation750.CONS_CX, Substation750.CONS_CY, Substation750.CONS_CZ,
            Substation750.CONS_R, Substation750.CONS_HALF, 16);
        double supportTop = Substation750.CONS_CY - Substation750.CONS_R + 0.06D;
        double supportMid = (Substation750.LID_Y + supportTop) * 0.5D;
        double supportHalf = (supportTop - Substation750.LID_Y) * 0.5D;
        for (int s = -1; s <= 1; s += 2) {
            LepGeo.box(t, Substation750.CONS_CX + s * 1.40D, supportMid, Substation750.CONS_CZ, 0.10D, supportHalf, 0.34D);
            cylinderX(t, Substation750.CONS_CX + s * 0.70D, Substation750.CONS_CY, Substation750.CONS_CZ,
                Substation750.CONS_R + 0.03D, 0.06D, 16);
        }
        // трубопровід до бака (газове реле)
        LepGeo.beam(t, Substation750.CONS_CX - 0.15D, Substation750.CONS_CY - Substation750.CONS_R,
            Substation750.CONS_CZ - 0.12D, Substation750.CONS_CX - 0.15D, Substation750.LID_Y,
            Substation750.CONS_CZ - 0.12D, 0.09D);
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
        double pz = Substation750.PORTAL_Z;
        double px = Substation750.PORTAL_X;

        // 1. дві ґратчасті стійки (як на фото): ноги розходяться до основи, вся стійка плавно звужується до тонкої
        //    верхівки-щогли, траверса примикає до неї ближче до верху; стійки стоять усередині траверси, а не по краях
        for (int s = -1; s <= 1; s += 2) {
            latticeColumn(t, s * px, pz, SLAB_H + 0.1D, Substation750.MAST_TOP, 36, 0.05D);
            // коміри на рівні траверси (місце кріплення поясів)
            LepGeo.box(t, s * px, Substation750.BEAM_TOP, pz, 0.46D, 0.05D, 0.46D);
            LepGeo.box(t, s * px, Substation750.BEAM_LOW, pz, 0.46D, 0.05D, 0.46D);
            LepGeo.box(t, s * px, Substation750.MAST_TOP + 0.08D, pz, 0.22D, 0.06D, 0.22D);
            LepGeo.box(t, s * px, Substation750.BEAM_LOW - 0.80D, pz, 0.44D, 0.04D, 0.44D);
        }

        // 2. ґратчаста траверса (дві паралельні ферми) із виступом праворуч за стійку
        double x0 = Substation750.BEAM_X0, x1 = Substation750.BEAM_X1;
        double top = Substation750.BEAM_TOP, low = Substation750.BEAM_LOW;
        int segs = 32;
        double step = (x1 - x0) / segs;
        for (int s = -1; s <= 1; s += 2) {
            double z = pz + s * Substation750.BEAM_PLANE;
            LepGeo.beam(t, x0, top, z, x1, top, z, 0.12D);
            LepGeo.beam(t, x0, low, z, x1, low, z, 0.11D);
            for (int j = 0; j < segs; j++) {
                boolean up = (j & 1) == 0;
                LepGeo.beam(t, x0 + j * step, up ? low : top, z, x0 + (j + 1) * step, up ? top : low, z, 0.05D);
            }
        }
        for (int j = 0; j <= segs; j += 2) {
            double x = x0 + j * step;
            LepGeo.beam(t, x, top, pz - Substation750.BEAM_PLANE, x, top, pz + Substation750.BEAM_PLANE, 0.05D);
            LepGeo.beam(t, x, low, pz - Substation750.BEAM_PLANE, x, low, pz + Substation750.BEAM_PLANE, 0.05D);
        }

        // 4. підвіски трьох фаз під траверсою (сюди чіпляються анкерні гірлянди з дротами)
        for (double fx : Substation750.MOUNT_X) {
            LepGeo.beam(t, fx, low, pz, fx, Substation750.MOUNT_Y, pz, 0.14D);
            // вушко кріплення гірлянди під траверсою (пластина по всій ширині, щоб гірлянда не висіла в повітрі)
            LepGeo.box(t, fx, Substation750.MOUNT_Y + 0.05D, pz, 0.30D, 0.08D, 0.35D);
        }

        // 5. лінійна комірка на кожну фазу
        for (int i = 0; i < 3; i++) {
            double fx = Substation750.PHASE_X[i];
            double dz = Substation750.DISC_Z;
            double hh = Substation750.DISC_HALF;
            double by = Substation750.DISC_BASE_Y;
            double ty = Substation750.DISC_BLADE_Y;

            // роз'єднувач: дві ґратчасті підставки, швелер, ніж
            latticeMast(t, fx - hh, dz, SLAB_H, by, 0.26D, 0.20D, 3, 0.06D);
            latticeMast(t, fx + hh, dz, SLAB_H, by, 0.26D, 0.20D, 3, 0.06D);
            LepGeo.beam(t, fx - hh - 0.10D, by, dz, fx + hh + 0.10D, by, dz, 0.14D);
            LepGeo.beam(t, fx - hh, ty, dz, fx + hh - 0.05D, ty + 0.08D, dz, 0.06D);
            LepGeo.box(t, fx - hh, ty, dz, 0.08D, 0.05D, 0.08D);
            LepGeo.box(t, fx + hh, ty, dz, 0.08D, 0.05D, 0.08D);

            // трансформатор струму: ґратчаста підставка і сталева голівка
            latticeMast(t, fx, Substation750.CT_Z, SLAB_H, Substation750.CT_BASE_Y, 0.34D, 0.24D, 4, 0.07D);
            LepGeo.box(t, fx, Substation750.CT_BASE_Y + 0.25D, Substation750.CT_Z, 0.34D, 0.25D, 0.34D);
            LepGeo.box(t, fx, Substation750.CT_TOP_Y + 0.06D, Substation750.CT_Z, 0.10D, 0.06D, 0.10D);

            // шлейф 1: від підвіски порталу до клеми роз'єднувача
            cable(t, Substation750.MOUNT_X[i], Substation750.MOUNT_Y, pz, fx - hh, ty + 0.05D, dz, 0.40D);
            // шлейф 2: від другої клеми до верхівки ТС
            double cty = Substation750.CT_TOP_Y + 0.12D;
            cable(t, fx + hh, ty + 0.05D, dz, fx, cty, Substation750.CT_Z, 0.35D);
            // шлейф 3: від ТС до верхівки вводу ВН трансформатора
            double tx = Substation750.HV_X[i] * Substation750.HV_TILT;
            double ty2 = Substation750.HV_TOP_Y + 0.12D;
            cable(t, fx, cty, Substation750.CT_Z, tx, ty2, Substation750.HV_Z, 0.90D);
            LepGeo.box(t, tx, ty2, Substation750.HV_Z, 0.09D, 0.07D, 0.09D);

            // ОПН висить на шлейфі 3: короткий спуск до верхівки ОПН (шунт на землю)
            double f = (Substation750.ARR_Z - Substation750.CT_Z) / (Substation750.HV_Z - Substation750.CT_Z);
            double[] jp = Substation750.cablePoint(fx, cty, Substation750.CT_Z, tx, ty2, Substation750.HV_Z, 0.90D, f);
            LepGeo.beam(t, fx, Substation750.ARR_TOP_Y + 0.08D, Substation750.ARR_Z, jp[0], jp[1], jp[2], 0.03D);
            LepGeo.box(t, fx, Substation750.ARR_TOP_Y + 0.04D, Substation750.ARR_Z, 0.20D, 0.03D, 0.20D);
            LepGeo.box(t, fx + 0.25D, 0.60D, Substation750.ARR_Z, 0.025D, 0.33D, 0.04D);
        }

        // 5a. блискавкоприймальні щогли на виході
        for (int s = -1; s <= 1; s += 2) {
            latticeMast(t, s * Substation750.LIGHT_X, Substation750.LIGHT_Z, SLAB_H + 0.10D, Substation750.LIGHT_TOP, 0.26D, 0.07D, 9);
        }

        // 5b. фланець під кабель на кожній шафі порту
        for (int lx : Substation750.PORT_LX) {
            double cx = Substation750.cellX(lx);
            double fh = Substation750.PORT_PAD_HALF + 0.05D;
            LepGeo.box(t, cx, Substation750.PORT_PAD_Y, Substation750.END_Z - 0.012D, fh, fh, 0.012D);
            LepGeo.box(t, cx - 0.47D, 0.5D, Substation750.END_Z - 0.006D, 0.03D, 0.47D, 0.006D);
            LepGeo.box(t, cx + 0.47D, 0.5D, Substation750.END_Z - 0.006D, 0.03D, 0.47D, 0.006D);
        }

        // 6. сторона НН: шини від чотирьох вводів до верхівок вихідних портів-блоків
        for (int i = 0; i < Substation750.LV_X.length; i++) {
            double portX = Substation750.cellX(Substation750.PORT_LX[i]);
            cable(t, Substation750.LV_X[i], Substation750.LV_TOP_Y + 0.06D, Substation750.LV_Z,
                portX, 1.12D, Substation750.PORT_Z + 0.15D, 0.60D);
            LepGeo.box(t, Substation750.LV_X[i], Substation750.LV_TOP_Y + 0.07D, Substation750.LV_Z, 0.07D, 0.05D, 0.07D);
            LepGeo.box(t, portX, 1.07D, Substation750.PORT_Z + 0.15D, 0.12D, 0.06D, 0.12D);
        }
    }

    /** Провисаючий декоративний кабель (парабола з 8 відрізків, формула та сама, що Substation750.cablePoint). */
    private static void cable(Tessellator t, double x1, double y1, double z1,
                              double x2, double y2, double z2, double sag) {
        int n = 8;
        double[] p = { x1, y1, z1 };
        for (int i = 1; i <= n; i++) {
            double[] c = Substation750.cablePoint(x1, y1, z1, x2, y2, z2, sag, (double) i / n);
            LepGeo.beam(t, p[0], p[1], p[2], c[0], c[1], c[2], 0.035D);
            p = c;
        }
    }

    /** Ґратчаста стійка порталу: півширина береться з Substation750.columnHalf(y), тонкі ноги, розкоси й пояси. */
    private static void latticeColumn(Tessellator t, double cx, double cz, double y0, double y1, int steps, double th) {
        double dh = (y1 - y0) / steps;
        double[][] c = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };
        for (int s = 0; s < steps; s++) {
            double ya = y0 + s * dh, yb = y0 + (s + 1) * dh;
            double wa = Substation750.columnHalf(ya), wb = Substation750.columnHalf(yb);
            for (int k = 0; k < 4; k++) {
                int n = (k + 1) & 3;
                LepGeo.beam(t, cx + c[k][0] * wa, ya, cz + c[k][1] * wa, cx + c[k][0] * wb, yb, cz + c[k][1] * wb, th);
                LepGeo.beam(t, cx + c[k][0] * wa, ya, cz + c[k][1] * wa, cx + c[n][0] * wb, yb, cz + c[n][1] * wb, th * 0.6D);
                LepGeo.beam(t, cx + c[k][0] * wb, yb, cz + c[k][1] * wb, cx + c[n][0] * wb, yb, cz + c[n][1] * wb, th * 0.6D);
            }
        }
    }

    /** Ґратчаста щогла на чотирьох ногах, що звужується від півширини w0 до w1, steps секцій із розкосами. */
    private static void latticeMast(Tessellator t, double cx, double cz, double y0, double y1,
                                    double w0, double w1, int steps) {
        latticeMast(t, cx, cz, y0, y1, w0, w1, steps, 0.05D);
    }

    private static void latticeMast(Tessellator t, double cx, double cz, double y0, double y1,
                                    double w0, double w1, int steps, double th) {
        double dh = (y1 - y0) / steps;
        for (int s = 0; s < steps; s++) {
            double ya = y0 + s * dh, yb = y0 + (s + 1) * dh;
            double wa = w0 + (w1 - w0) * s / steps;
            double wb = w0 + (w1 - w0) * (s + 1) / steps;
            double[][] c = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };
            for (int k = 0; k < 4; k++) {
                int n = (k + 1) & 3;
                LepGeo.beam(t, cx + c[k][0] * wa, ya, cz + c[k][1] * wa, cx + c[k][0] * wb, yb, cz + c[k][1] * wb, th);
                // розкіс на грані між ногами k і n
                LepGeo.beam(t, cx + c[k][0] * wa, ya, cz + c[k][1] * wa, cx + c[n][0] * wb, yb, cz + c[n][1] * wb, th * 0.7D);
                // горизонтальний пояс на рівні yb
                LepGeo.beam(t, cx + c[k][0] * wb, yb, cz + c[k][1] * wb, cx + c[n][0] * wb, yb, cz + c[n][1] * wb, th * 0.7D);
            }
        }
        // блискавкоприймальний стрижень на верхівці
        LepGeo.beam(t, cx, y1, cz, cx, y1 + 0.0D, cz, th);
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
        for (int i = 0; i < 3; i++) {
            double bx = Substation750.HV_X[i];
            bushing(t, bx, Substation750.LID_Y, Substation750.HV_Z,
                bx * Substation750.HV_TILT, Substation750.HV_TOP_Y, Substation750.HV_Z, 0.26D, 0.22D, 14);
        }
        for (int i = 0; i < Substation750.LV_X.length; i++) {
            double bx = Substation750.LV_X[i];
            bushing(t, bx, Substation750.LID_Y, Substation750.LV_Z, bx, Substation750.LV_TOP_Y, Substation750.LV_Z,
                0.16D, 0.12D, 6);
        }
        for (double fx : Substation750.PHASE_X) {
            double hh = Substation750.DISC_HALF;
            // опорні ізолятори роз'єднувача
            bushing(t, fx - hh, Substation750.DISC_BASE_Y, Substation750.DISC_Z,
                fx - hh, Substation750.DISC_POST_TOP, Substation750.DISC_Z, 0.16D, 0.12D, 6);
            bushing(t, fx + hh, Substation750.DISC_BASE_Y, Substation750.DISC_Z,
                fx + hh, Substation750.DISC_POST_TOP, Substation750.DISC_Z, 0.16D, 0.12D, 6);
            // колона трансформатора струму (високий ребристий ізолятор)
            bushing(t, fx, Substation750.CT_BASE_Y + 0.5D, Substation750.CT_Z,
                fx, Substation750.CT_TOP_Y, Substation750.CT_Z, 0.20D, 0.16D, 9);
            // ОПН
            bushing(t, fx, SLAB_H + 0.20D, Substation750.ARR_Z, fx, Substation750.ARR_TOP_Y, Substation750.ARR_Z,
                0.16D, 0.12D, 11);
        }
    }

    // ------------------------------------------------------------ шафа керування

    private static void cabinets(Tessellator t) {
        LepGeo.box(t, Substation750.CAB_X, SLAB_H + 0.65D, Substation750.CAB_Z, 0.22D, 0.65D, 0.46D);

        // кабельні шафи вихідних портів: повний блок 1x1x1 (колізія й підключення кабелю NTM), але виглядає як
        // металева кабельна шафа з дахом-козирком, а не як голий куб
        for (int lx : Substation750.PORT_LX) {
            double cx = Substation750.cellX(lx);
            double cz = Substation750.PORT_Z;
            // корпус на всю клітинку (на 0.002 більший, щоб грані не мерехтіли з плитою)
            LepGeo.box(t, cx, 0.5D, cz, 0.502D, 0.5D, 0.502D);
            // дах-козирок
            LepGeo.box(t, cx, 1.02D, cz, 0.53D, 0.03D, 0.53D);
        }
    }

    // ------------------------------------------------------------ оранжеві плити на торцях портів

    private static void ports(Tessellator t) {
        // оранжева плита 4x4 пікселі по центру зовнішньої грані порту (z' = -4.5)
        for (int lx : Substation750.PORT_LX) {
            padZ(t, Substation750.END_Z - 0.026D, Substation750.cellX(lx), Substation750.PORT_PAD_Y,
                Substation750.PORT_PAD_HALF);
        }
    }

    /** Плита на торцевій грані -Z (бік виходу), повністю накрита текстурою. */
    private static void padZ(Tessellator t, double z, double cx, double cy, double half) {
        double x1 = cx - half, x2 = cx + half, y1 = cy - half, y2 = cy + half;
        t.setNormal(0.0F, 0.0F, -1.0F);
        t.addVertexWithUV(x1, y1, z, 1.0D, 1.0D);
        t.addVertexWithUV(x1, y2, z, 1.0D, 0.0D);
        t.addVertexWithUV(x2, y2, z, 0.0D, 0.0D);
        t.addVertexWithUV(x2, y1, z, 0.0D, 1.0D);
    }
}
