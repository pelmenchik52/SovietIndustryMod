package com.sovietindustry.models;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.blocks.BlockCraneRail;
import com.sovietindustry.render.LepGeo;
import com.sovietindustry.tileentities.TileEntityCraneController;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/**
 * Козловий (портальний) кран з ґратчастою фермою моста, за фото: жорстка широка А-подібна опора з драбиною
 * й кабіною, гнучка вузька опора, міст з консолями за опорами, візок із лебідкою та гак на тросі.
 *
 * Локальні осі: X це напрям колії (кран їде вздовж X), Z це поперечна вісь моста, Y угору. Початок координат
 * у центрі клітинки контролера на рівні низу рейкового блока. Жорстка опора стоїть на +gauge, гнучка на -gauge.
 *
 * Нерухома частина компілюється в display list окремо для кожної пари (висота, ширина колії), три списки:
 * жовтий метал, темні деталі й кабіна. Візок, трос і гак малюються щокадру, бо рухаються.
 */
@SideOnly(Side.CLIENT)
public class ModelGantryCrane {

    private static final ResourceLocation YELLOW = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_yellow.png");
    private static final ResourceLocation DARK = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_dark.png");
    private static final ResourceLocation CABIN = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_cabin.png");

    private static final double RAIL_TOP = BlockCraneRail.HEIGHT;
    private static final double RIGID_FOOT = TileEntityCraneController.HALF_FOOT;
    private static final double RIGID_TOP = 0.55D;
    private static final double FLEX_FOOT = 1.0D;
    private static final double FLEX_TOP = 0.4D;
    /** Відстань від осі опори до кожної з двох площин ґратки. */
    private static final double RIGID_PLANE = 0.45D;
    private static final double FLEX_PLANE = 0.28D;
    /** Півширина моста вздовж колії та висота нижньої балки. */
    private static final double GIRDER_HALF = 0.75D;
    private static final double GIRDER_H = 0.7D;
    private static final double CHORD_X = 0.65D;

    /** key (height, gauge) -> перший id із трьох display list. */
    private final Map<Integer, Integer> lists = new HashMap<Integer, Integer>();

    private static int key(int height, int gauge) {
        return height * 64 + gauge;
    }

    /**
     * @param height висота верху моста над низом рейок
     * @param gauge відстань від осі до рейок
     * @param trolleyZ положення візка поперек колії
     * @param hookH довжина троса від низу моста до верху гака
     */
    public void render(int height, int gauge, double trolleyZ, double hookH) {
        Minecraft mc = Minecraft.getMinecraft();
        Integer id = lists.get(key(height, gauge));
        if (id == null) {
            int base = GLAllocation.generateDisplayLists(3);
            GL11.glNewList(base, GL11.GL_COMPILE);
            buildYellow(height, gauge);
            GL11.glEndList();
            GL11.glNewList(base + 1, GL11.GL_COMPILE);
            buildDark(gauge);
            GL11.glEndList();
            GL11.glNewList(base + 2, GL11.GL_COMPILE);
            buildCabin(height, gauge);
            GL11.glEndList();
            id = Integer.valueOf(base);
            lists.put(key(height, gauge), id);
        }

        mc.renderEngine.bindTexture(YELLOW);
        GL11.glCallList(id.intValue());
        drawTrolley(height, gauge, trolleyZ);

        mc.renderEngine.bindTexture(DARK);
        GL11.glCallList(id.intValue() + 1);
        drawHook(height, gauge, trolleyZ, hookH);

        mc.renderEngine.bindTexture(CABIN);
        GL11.glCallList(id.intValue() + 2);
    }

    // ------------------------------------------------------------ жовта сталь: опори й міст

    private static void buildYellow(int height, int gauge) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();

        double depth = TileEntityCraneController.bridgeDepth(gauge);
        double yBot = height - depth;
        double yTop = height;
        double half = gauge + TileEntityCraneController.cantilever(gauge);

        // жорстка опора на +gauge: широка ґратчаста А-подібна вежа з драбиною
        tower(t, gauge, RIGID_PLANE, RIGID_FOOT, RIGID_TOP, yBot, 0.24D, Math.max(4, (int) Math.round((yBot - RAIL_TOP) / 2.0D)));
        ladder(t, gauge, -1, RIGID_PLANE, yBot - 2.2D);
        // майданчик під кабіною
        LepGeo.box(t, 0.0D, yBot - 2.2D, gauge - (RIGID_PLANE + 1.3D), 0.95D, 0.06D, 0.95D);

        // гнучка опора на -gauge: вузька вежа з рідкою ґраткою
        tower(t, -gauge, FLEX_PLANE, FLEX_FOOT, FLEX_TOP, yBot, 0.17D, Math.max(3, (int) Math.round((yBot - RAIL_TOP) / 3.0D)));

        bridge(t, yBot, yTop, half);

        t.draw();
    }

    /**
     * Вежа опори в площині X-Y: дві площини ґратки (на zc +- plane), у кожній дві похилі колони,
     * що сходяться до моста, горизонтальні в'язі та діагоналі.
     */
    private static void tower(Tessellator t, double zc, double plane, double foot, double topHalf,
                              double yBot, double colTh, int levels) {
        double base = RAIL_TOP + 0.45D;
        for (int pl = -1; pl <= 1; pl += 2) {
            double pz = zc + pl * plane;
            for (int sx = -1; sx <= 1; sx += 2) {
                LepGeo.beam(t, sx * foot, base, pz, sx * topHalf, yBot, pz, colTh);
            }
            for (int i = 0; i <= levels; i++) {
                double y0 = base + (yBot - base) * i / levels;
                double w0 = lerp(foot, topHalf, (y0 - base) / (yBot - base));
                LepGeo.beam(t, -w0, y0, pz, w0, y0, pz, colTh * 0.45D);
                if (i < levels) {
                    double y1 = base + (yBot - base) * (i + 1) / levels;
                    double w1 = lerp(foot, topHalf, (y1 - base) / (yBot - base));
                    boolean flip = (i & 1) == 0;
                    LepGeo.beam(t, flip ? -w0 : w0, y0, pz, flip ? w1 : -w1, y1, pz, colTh * 0.35D);
                    if (foot > 1.5D) {
                        // у широкій опорі діагоналі хрестом
                        LepGeo.beam(t, flip ? w0 : -w0, y0, pz, flip ? -w1 : w1, y1, pz, colTh * 0.3D);
                    }
                }
            }
        }
        // поперечні в'язі між двома площинами
        for (int i = 0; i <= levels; i++) {
            double y0 = base + (yBot - base) * i / levels;
            double w0 = lerp(foot, topHalf, (y0 - base) / (yBot - base));
            for (int sx = -1; sx <= 1; sx += 2) {
                LepGeo.beam(t, sx * w0, y0, zc - plane, sx * w0, y0, zc + plane, colTh * 0.4D);
            }
        }
        // голова під мостом і балка-основа над візками
        LepGeo.box(t, 0.0D, yBot - 0.12D, zc, topHalf + 0.4D, 0.12D, plane + 0.3D);
        LepGeo.box(t, 0.0D, base - 0.05D, zc, foot + 0.2D, 0.1D, plane + 0.1D);
    }

    /** Вертикальна драбина з рідкими щаблями від рейки до майданчика кабіни, з боку центру моста. */
    private static void ladder(Tessellator t, double zc, int inward, double plane, double top) {
        double lz = zc + inward * (plane + 0.4D);
        t.setNormal(0.0F, 1.0F, 0.0F);
        LepGeo.beam(t, 0.0D, RAIL_TOP, lz - 0.22D, 0.0D, top, lz - 0.22D, 0.06D);
        LepGeo.beam(t, 0.0D, RAIL_TOP, lz + 0.22D, 0.0D, top, lz + 0.22D, 0.06D);
        for (double y = RAIL_TOP + 0.4D; y < top; y += 0.5D) {
            LepGeo.beam(t, 0.0D, y, lz - 0.22D, 0.0D, y, lz + 0.22D, 0.04D);
        }
    }

    /** Міст: суцільна нижня балка, дві ферми верхнього пояса з розкосами, настил, поручні, підвісні майданчики. */
    private static void bridge(Tessellator t, double yBot, double yTop, double half) {
        double yc = yBot + GIRDER_H;
        double yt = yTop - 0.1D;

        // нижня коробчата балка на всю довжину з консолями
        LepGeo.box(t, 0.0D, yBot + GIRDER_H * 0.5D, 0.0D, GIRDER_HALF, GIRDER_H * 0.5D, half);

        int segs = Math.max(8, (int) Math.round(2.0D * half / Math.max(0.8D, yt - yc)));
        if ((segs & 1) == 1) segs++;
        double step = 2.0D * half / segs;
        for (int p = -1; p <= 1; p += 2) {
            double px = p * CHORD_X;
            LepGeo.beam(t, px, yt, -half, px, yt, half, 0.18D);
            for (int i = 0; i < segs; i++) {
                boolean up = (i & 1) == 0;
                double za = -half + i * step;
                double zb = za + step;
                LepGeo.beam(t, px, up ? yc : yt, za, px, up ? yt : yc, zb, 0.08D);
            }
            for (int i = 0; i <= segs; i += 2) {
                double z = -half + i * step;
                LepGeo.beam(t, px, yc, z, px, yt, z, 0.07D);
            }
        }
        for (int i = 0; i <= segs; i += 2) {
            double z = -half + i * step;
            LepGeo.beam(t, -CHORD_X, yt, z, CHORD_X, yt, z, 0.08D);
        }

        // настил і поручні
        double deck = yt + 0.12D;
        LepGeo.box(t, 0.0D, deck, 0.0D, 1.0D, 0.025D, half);
        for (int p = -1; p <= 1; p += 2) {
            double rx = p * 1.1D;
            LepGeo.beam(t, rx, deck + 1.0D, -half, rx, deck + 1.0D, half, 0.05D);
            for (int i = 0; i <= segs; i += 2) {
                double z = -half + i * step;
                LepGeo.beam(t, rx, deck, z, rx, deck + 1.0D, z, 0.05D);
            }
        }

        // підвісні майданчики на кінцях консолей
        for (int e = -1; e <= 1; e += 2) {
            double ez = e * (half - 0.8D);
            LepGeo.box(t, 0.0D, yBot - 0.9D, ez, 0.95D, 0.05D, 0.75D);
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    LepGeo.beam(t, sx * 0.9D, yBot - 0.9D, ez + sz * 0.7D, sx * 0.9D, yBot - 0.05D, ez + sz * 0.7D, 0.05D);
                }
            }
        }
    }

    private static double lerp(double a, double b, double f) {
        return a + (b - a) * f;
    }

    // ------------------------------------------------------------ темні деталі: візки на рейках

    private static void buildDark(int gauge) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        // жорстка опора: два візки по краях основи
        for (int sx = -1; sx <= 1; sx += 2) {
            LepGeo.box(t, sx * (RIGID_FOOT - 0.55D), RAIL_TOP + 0.18D, gauge, 0.6D, 0.18D, 0.5D);
        }
        // гнучка опора: один візок
        LepGeo.box(t, 0.0D, RAIL_TOP + 0.18D, -gauge, FLEX_FOOT + 0.25D, 0.18D, 0.42D);
        t.draw();
    }

    // ------------------------------------------------------------ кабіна

    private static void buildCabin(int height, int gauge) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double yBot = height - TileEntityCraneController.bridgeDepth(gauge);
        double floor = yBot - 2.2D + 0.06D;
        LepGeo.box(t, 0.0D, floor + 0.7D, gauge - (RIGID_PLANE + 1.3D), 0.7D, 0.7D, 0.75D);
        t.draw();
    }

    // ------------------------------------------------------------ рухомі частини

    private static void drawTrolley(int height, int gauge, double tz) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double depth = TileEntityCraneController.bridgeDepth(gauge);
        double yBot = height - depth;
        double yTop = height;
        // лебідка на настилі
        LepGeo.box(t, 0.0D, yTop + 0.3D, tz, 0.8D, 0.3D, 0.6D);
        // бічні щоки, що охоплюють балку й ферму
        double cy = (yBot - 0.35D + yTop + 0.1D) * 0.5D;
        double hy = (yTop + 0.1D - (yBot - 0.35D)) * 0.5D;
        for (int p = -1; p <= 1; p += 2) {
            LepGeo.box(t, p * 0.88D, cy, tz, 0.07D, hy, 0.5D);
        }
        // нижня рама під балкою
        LepGeo.box(t, 0.0D, yBot - 0.28D, tz, 0.9D, 0.1D, 0.5D);
        t.draw();
    }

    private static void drawHook(int height, int gauge, double tz, double hookH) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double yBot = height - TileEntityCraneController.bridgeDepth(gauge);
        double hookTop = yBot - hookH;
        double cableTop = yBot - 0.38D;
        // трос
        LepGeo.beam(t, 0.0D, cableTop, tz, 0.0D, hookTop, tz, 0.06D);
        // обойма гака
        LepGeo.box(t, 0.0D, hookTop - 0.3D, tz, 0.24D, 0.3D, 0.24D);
        // стрижень і дзьоб
        LepGeo.beam(t, 0.0D, hookTop - 0.6D, tz, 0.0D, hookTop - 0.95D, tz, 0.1D);
        LepGeo.box(t, 0.0D, hookTop - 1.0D, tz, 0.16D, 0.05D, 0.16D);
        t.draw();
    }
}
