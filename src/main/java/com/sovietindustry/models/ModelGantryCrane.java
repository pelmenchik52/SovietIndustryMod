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
 * Козловий кран з ґратчастим мостом за кресленням і фото: обидві опори це усічені піраміди, широкі вздовж колії
 * внизу й вузькі вгорі (вид збоку: А), а поперек колії вузькі внизу й розходяться вгору під міст (вид спереду: V).
 * Жорстка опора широка, з похилою драбиною вздовж колони та кабіною, гнучка опора тонша. Міст має консолі
 * за опорами з підвісними майданчиками, на настилі їздить візок із лебідкою, під ним на тросі гак.
 *
 * Локальні осі: X це напрям колії (кран їде вздовж X), Z це поперечна вісь моста, Y угору. Початок координат
 * у центрі клітинки контролера на рівні низу рейкового блока. Жорстка опора стоїть на +gauge, гнучка на -gauge.
 *
 * Колір: текстура металу майже біла, а колір фарби накладається через glColor, тому кран можна фарбувати
 * в будь-який колір. Темні деталі (візки, трос, гак) і кабіна не фарбуються.
 *
 * Нерухома частина компілюється в display list для кожної пари (висота, ширина колії), три списки:
 * фарбований метал, темні деталі й кабіна. Візок, трос і гак малюються щокадру, бо рухаються.
 */
@SideOnly(Side.CLIENT)
public class ModelGantryCrane {

    private static final ResourceLocation PAINT = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_paint.png");
    private static final ResourceLocation DARK = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_dark.png");
    private static final ResourceLocation CABIN = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_cabin.png");

    private static final double RAIL_TOP = BlockCraneRail.HEIGHT;

    // жорстка опора (з кабіною й драбиною): основа вздовж колії, основа впоперек, верх вздовж, верх впоперек
    private static final double RIGID_FOOT_X = 2.3D;
    private static final double RIGID_FOOT_Z = 0.5D;
    private static final double RIGID_TOP_X = 0.6D;
    private static final double RIGID_TOP_Z = 1.4D;

    // гнучка опора: тонша
    private static final double FLEX_FOOT_X = 1.3D;
    private static final double FLEX_FOOT_Z = 0.3D;
    private static final double FLEX_TOP_X = 0.5D;
    private static final double FLEX_TOP_Z = 1.0D;

    /** Півширина нижньої коробчастої балки моста й відстань від осі до кожного верхнього пояса. */
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
     * @param r червона, зелена, синя складові кольору фарби (0..1)
     */
    public void render(int height, int gauge, double trolleyZ, double hookH, float r, float g, float b) {
        Minecraft mc = Minecraft.getMinecraft();
        Integer id = lists.get(key(height, gauge));
        if (id == null) {
            int base = GLAllocation.generateDisplayLists(3);
            GL11.glNewList(base, GL11.GL_COMPILE);
            buildPainted(height, gauge);
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

        // фарбований метал: нерухома ферма й візок
        mc.renderEngine.bindTexture(PAINT);
        GL11.glColor3f(r, g, b);
        GL11.glCallList(id.intValue());
        drawTrolley(height, gauge, trolleyZ);

        // решта без фарби
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(DARK);
        GL11.glCallList(id.intValue() + 1);
        drawHook(height, gauge, trolleyZ, hookH);

        mc.renderEngine.bindTexture(CABIN);
        GL11.glCallList(id.intValue() + 2);
    }

    // ------------------------------------------------------------ фарбований метал: опори, драбина, міст

    private static void buildPainted(int height, int gauge) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();

        double depth = TileEntityCraneController.bridgeDepth(gauge);
        double yBot = height - depth;
        double yTop = height;
        double half = gauge + TileEntityCraneController.cantilever(gauge);
        double base = RAIL_TOP + 0.4D;

        // жорстка опора на +gauge: густа ґратка, діагоналі хрестом
        tower(t, gauge, RIGID_FOOT_X, RIGID_FOOT_Z, RIGID_TOP_X, RIGID_TOP_Z, base, yBot,
            Math.max(5, (int) Math.round((yBot - base) / 1.8D)), 0.24D, true);
        ladder(t, gauge, RIGID_FOOT_X, RIGID_TOP_X, base, yBot - 1.0D);
        // майданчик на верху опори, звідки драбина виходить до кабіни
        LepGeo.box(t, 0.0D, yBot - 1.0D, gauge, RIGID_TOP_X + 0.5D, 0.05D, RIGID_TOP_Z + 0.2D);
        // кронштейн кабіни від опори до майданчика кабіни
        double cabZ = gauge - (RIGID_TOP_Z + 1.1D);
        LepGeo.box(t, 0.0D, yBot - 2.0D, cabZ + 0.2D, 0.9D, 0.06D, 1.0D);
        LepGeo.beam(t, 0.0D, yBot - 2.0D, gauge - RIGID_TOP_Z, 0.0D, yBot - 0.4D, gauge - RIGID_TOP_Z + 0.2D, 0.12D);

        // гнучка опора на -gauge: тонша, рідша ґратка
        tower(t, -gauge, FLEX_FOOT_X, FLEX_FOOT_Z, FLEX_TOP_X, FLEX_TOP_Z, base, yBot,
            Math.max(4, (int) Math.round((yBot - base) / 2.6D)), 0.18D, false);

        bridge(t, yBot, yTop, half);

        t.draw();
    }

    /**
     * Ґратчаста вежа у формі усіченої піраміди: чотири колони від кутів основи (foot) до кутів верху (top),
     * на кожному рівні замкнене кільце в'язей, між рівнями діагоналі на всіх чотирьох гранях.
     * zc це положення осі вежі впоперек колії, а x вежа симетрична відносно нуля.
     */
    private static void tower(Tessellator t, double zc, double footX, double footZ, double topX, double topZ,
                              double base, double yTop, int levels, double colTh, boolean crossBraces) {
        int[][] sg = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };

        for (int i = 0; i < 4; i++) {
            LepGeo.beam(t, sg[i][0] * footX, base, zc + sg[i][1] * footZ, sg[i][0] * topX, yTop, zc + sg[i][1] * topZ, colTh);
        }
        for (int lv = 0; lv <= levels; lv++) {
            double f = (double) lv / levels;
            double y = base + (yTop - base) * f;
            double hx = lerp(footX, topX, f);
            double hz = lerp(footZ, topZ, f);
            for (int i = 0; i < 4; i++) {
                int[] a = sg[i];
                int[] b = sg[(i + 1) & 3];
                LepGeo.beam(t, a[0] * hx, y, zc + a[1] * hz, b[0] * hx, y, zc + b[1] * hz, colTh * 0.45D);
            }
            if (lv < levels) {
                double f2 = (double) (lv + 1) / levels;
                double y2 = base + (yTop - base) * f2;
                double hx2 = lerp(footX, topX, f2);
                double hz2 = lerp(footZ, topZ, f2);
                for (int i = 0; i < 4; i++) {
                    int[] a = sg[i];
                    int[] b = sg[(i + 1) & 3];
                    boolean flip = ((lv + i) & 1) == 0;
                    int[] pa = flip ? a : b;
                    int[] pb = flip ? b : a;
                    LepGeo.beam(t, pa[0] * hx, y, zc + pa[1] * hz, pb[0] * hx2, y2, zc + pb[1] * hz2, colTh * 0.35D);
                    if (crossBraces) {
                        LepGeo.beam(t, pb[0] * hx, y, zc + pb[1] * hz, pa[0] * hx2, y2, zc + pa[1] * hz2, colTh * 0.3D);
                    }
                }
            }
        }
        // голова під мостом і балка-основа над візками
        LepGeo.box(t, 0.0D, yTop - 0.12D, zc, topX + 0.35D, 0.12D, topZ + 0.3D);
        LepGeo.box(t, 0.0D, base - 0.05D, zc, footX + 0.2D, 0.1D, footZ + 0.2D);
    }

    /** Похила драбина вздовж зовнішньої (+X) грані жорсткої опори: від основи до майданчика вгорі. */
    private static void ladder(Tessellator t, double zc, double footX, double topX, double base, double top) {
        double out = 0.28D;
        double x0 = footX + out;
        double x1 = topX + out;
        LepGeo.beam(t, x0, base, zc - 0.3D, x1, top, zc - 0.3D, 0.07D);
        LepGeo.beam(t, x0, base, zc + 0.3D, x1, top, zc + 0.3D, 0.07D);
        double len = top - base;
        for (double y = base + 0.4D; y < top; y += 0.5D) {
            double f = (y - base) / len;
            double x = lerp(x0, x1, f);
            LepGeo.beam(t, x, y, zc - 0.3D, x, y, zc + 0.3D, 0.05D);
        }
        // кільця захисної клітки на верхній третині
        for (double y = base + len * 0.65D; y < top; y += 1.0D) {
            double f = (y - base) / len;
            double x = lerp(x0, x1, f);
            LepGeo.beam(t, x, y, zc - 0.45D, x + 0.3D, y, zc - 0.45D, 0.04D);
            LepGeo.beam(t, x + 0.3D, y, zc - 0.45D, x + 0.3D, y, zc + 0.45D, 0.04D);
            LepGeo.beam(t, x + 0.3D, y, zc + 0.45D, x, y, zc + 0.45D, 0.04D);
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
            LepGeo.beam(t, px, yt, -half, px, yt, half, 0.2D);
            for (int i = 0; i < segs; i++) {
                boolean up = (i & 1) == 0;
                double za = -half + i * step;
                double zb = za + step;
                LepGeo.beam(t, px, up ? yc : yt, za, px, up ? yt : yc, zb, 0.09D);
            }
        }
        // поперечні в'язі верхнього поясу й стійки на кінцях
        for (int i = 0; i <= segs; i += 2) {
            double z = -half + i * step;
            LepGeo.beam(t, -CHORD_X, yt, z, CHORD_X, yt, z, 0.08D);
        }
        for (int e = -1; e <= 1; e += 2) {
            for (int p = -1; p <= 1; p += 2) {
                LepGeo.beam(t, p * CHORD_X, yc, e * half, p * CHORD_X, yt, e * half, 0.1D);
            }
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
            LepGeo.box(t, sx * (RIGID_FOOT_X - 0.55D), RAIL_TOP + 0.18D, gauge, 0.6D, 0.18D, 0.5D);
        }
        // гнучка опора: два візки ближче одне до одного
        for (int sx = -1; sx <= 1; sx += 2) {
            LepGeo.box(t, sx * (FLEX_FOOT_X - 0.4D), RAIL_TOP + 0.18D, -gauge, 0.55D, 0.18D, 0.45D);
        }
        t.draw();
    }

    // ------------------------------------------------------------ кабіна

    private static void buildCabin(int height, int gauge) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double yBot = height - TileEntityCraneController.bridgeDepth(gauge);
        double floor = yBot - 2.0D + 0.06D;
        double cabZ = gauge - (RIGID_TOP_Z + 1.1D);
        LepGeo.box(t, 0.0D, floor + 0.7D, cabZ, 0.75D, 0.7D, 0.8D);
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
