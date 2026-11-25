package com.sovietindustry.models;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.render.LepGeo;
import com.sovietindustry.tileentities.Portal750;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/**
 * Окремий лінійний портал 750 кВ, той самий, що стоїть на вході підстанції: дві ґратчасті стійки з блискавкоприймальними
 * щоглами, ґратчаста траверса з трьома підвісками фаз і грозозахисний трос між верхівками щогл.
 * Гірлянди й дроти малює LepWireRenderer, бо їх напрям залежить від того, куди ведуть дроти.
 *
 * Початок координат у центрі майстер-блока (під серединою траверси), Y вгору, траверса вздовж осі X.
 * Дві текстури (бетон і сталь), тому два display list.
 */
@SideOnly(Side.CLIENT)
public class ModelPortal750 {

    private static final int CONCRETE = 0;
    private static final int STEEL = 1;
    private static final int PARTS = 2;

    private static final String[] TEXTURE_NAMES = { "substation_concrete", "substation_steel" };
    private static ResourceLocation[] textures;

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
        if (part == CONCRETE) concrete(t); else steel(t);
        t.draw();
    }

    // ------------------------------------------------------------ бетон

    private static void concrete(Tessellator t) {
        double h = Portal750.FOOT_H;
        double w = Portal750.COLUMN_HALF_BOTTOM;
        for (int s = -1; s <= 1; s += 2) {
            double cx = s * Portal750.PORTAL_X;
            // спільна плита під стійкою й башмаки під кожну ногу
            LepGeo.boxTiled(t, cx, h * 0.5D - 0.02D, 0.0D, w + 0.30D, h * 0.5D - 0.02D, w + 0.30D, 0.5D);
            for (int i = -1; i <= 1; i += 2) {
                for (int j = -1; j <= 1; j += 2) {
                    LepGeo.boxTiled(t, cx + i * w, h + 0.06D, j * w, 0.22D, 0.06D, 0.22D, 0.5D);
                }
            }
        }
        // заземлювальна коробка під серединою траверси
        LepGeo.boxTiled(t, 0.0D, Portal750.PAD_H * 0.5D, 0.0D, Portal750.PAD_HALF, Portal750.PAD_H * 0.5D, Portal750.PAD_HALF, 1.0D);
    }

    // ------------------------------------------------------------ сталь

    private static void steel(Tessellator t) {
        // 1. дві ґратчасті стійки: ноги розходяться до основи, угорі тонка щогла-блискавкоприймач
        for (int s = -1; s <= 1; s += 2) {
            latticeColumn(t, s * Portal750.PORTAL_X, 0.0D, Portal750.FOOT_H + 0.1D, Portal750.MAST_TOP, 36, 0.05D);
            // коміри на рівні траверси й підкіс під нею
            LepGeo.box(t, s * Portal750.PORTAL_X, Portal750.BEAM_TOP, 0.0D, 0.46D, 0.05D, 0.46D);
            LepGeo.box(t, s * Portal750.PORTAL_X, Portal750.BEAM_LOW, 0.0D, 0.46D, 0.05D, 0.46D);
            LepGeo.box(t, s * Portal750.PORTAL_X, Portal750.MAST_TOP + 0.08D, 0.0D, 0.22D, 0.06D, 0.22D);
            LepGeo.box(t, s * Portal750.PORTAL_X, Portal750.BEAM_LOW - 0.80D, 0.0D, 0.44D, 0.04D, 0.44D);
        }

        // 2. ґратчаста траверса (дві паралельні ферми)
        double x0 = Portal750.BEAM_X0, x1 = Portal750.BEAM_X1;
        double top = Portal750.BEAM_TOP, low = Portal750.BEAM_LOW;
        int segs = 32;
        double step = (x1 - x0) / segs;
        for (int s = -1; s <= 1; s += 2) {
            double z = s * Portal750.BEAM_PLANE;
            LepGeo.beam(t, x0, top, z, x1, top, z, 0.12D);
            LepGeo.beam(t, x0, low, z, x1, low, z, 0.11D);
            for (int j = 0; j < segs; j++) {
                boolean up = (j & 1) == 0;
                LepGeo.beam(t, x0 + j * step, up ? low : top, z, x0 + (j + 1) * step, up ? top : low, z, 0.05D);
            }
        }
        for (int j = 0; j <= segs; j += 2) {
            double x = x0 + j * step;
            LepGeo.beam(t, x, top, -Portal750.BEAM_PLANE, x, top, Portal750.BEAM_PLANE, 0.05D);
            LepGeo.beam(t, x, low, -Portal750.BEAM_PLANE, x, low, Portal750.BEAM_PLANE, 0.05D);
        }

        // 3. підвіски трьох фаз під траверсою (сюди чіпляються анкерні гірлянди з дротами)
        for (double fx : Portal750.MOUNT_X) {
            LepGeo.beam(t, fx, low, 0.0D, fx, Portal750.MOUNT_Y, 0.0D, 0.14D);
            // вушко кріплення гірлянди: пластина по всій ширині, щоб гірлянда не висіла в повітрі
            LepGeo.box(t, fx, Portal750.MOUNT_Y + 0.05D, 0.0D, 0.30D, 0.08D, 0.35D);
        }

        // 4. грозозахисний трос між верхівками щогл (декоративний, провисає)
        double ty = Portal750.MAST_TOP + 0.14D;
        double px = Portal750.PORTAL_X;
        cable(t, -px, ty, 0.0D, px, ty, 0.0D, 0.45D);
        // і короткі випуски троса назовні за стійки
        cable(t, -px, ty, 0.0D, -px - 1.6D, ty - 0.9D, 0.0D, 0.15D);
        cable(t, px, ty, 0.0D, px + 1.6D, ty - 0.9D, 0.0D, 0.15D);

        // 5. заземлення: смуга від кожної стійки до коробки під траверсою
        for (int s = -1; s <= 1; s += 2) {
            LepGeo.box(t, s * (px * 0.5D + 0.35D), 0.04D, 0.0D, px * 0.5D - 0.35D, 0.02D, 0.04D);
        }
        LepGeo.box(t, 0.0D, Portal750.PAD_H + 0.03D, 0.0D, Portal750.PAD_HALF + 0.03D, 0.03D, Portal750.PAD_HALF + 0.03D);
    }

    /** Провисаючий декоративний кабель (парабола з 8 відрізків, формула та сама, що Portal750.cablePoint). */
    private static void cable(Tessellator t, double x1, double y1, double z1,
                              double x2, double y2, double z2, double sag) {
        int n = 8;
        double[] p = { x1, y1, z1 };
        for (int i = 1; i <= n; i++) {
            double[] c = Portal750.cablePoint(x1, y1, z1, x2, y2, z2, sag, (double) i / n);
            LepGeo.beam(t, p[0], p[1], p[2], c[0], c[1], c[2], 0.035D);
            p = c;
        }
    }

    /** Ґратчаста стійка: півширина береться з Portal750.columnHalf(y), тонкі ноги, розкоси й пояси. */
    private static void latticeColumn(Tessellator t, double cx, double cz, double y0, double y1, int steps, double th) {
        double dh = (y1 - y0) / steps;
        double[][] c = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };
        for (int s = 0; s < steps; s++) {
            double ya = y0 + s * dh, yb = y0 + (s + 1) * dh;
            double wa = Portal750.columnHalf(ya), wb = Portal750.columnHalf(yb);
            for (int k = 0; k < 4; k++) {
                int n = (k + 1) & 3;
                LepGeo.beam(t, cx + c[k][0] * wa, ya, cz + c[k][1] * wa, cx + c[k][0] * wb, yb, cz + c[k][1] * wb, th);
                LepGeo.beam(t, cx + c[k][0] * wa, ya, cz + c[k][1] * wa, cx + c[n][0] * wb, yb, cz + c[n][1] * wb, th * 0.6D);
                LepGeo.beam(t, cx + c[k][0] * wb, yb, cz + c[k][1] * wb, cx + c[n][0] * wb, yb, cz + c[n][1] * wb, th * 0.6D);
            }
        }
    }
}
