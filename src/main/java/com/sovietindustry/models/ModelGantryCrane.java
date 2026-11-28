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
 * Козловий кран за референсом.
 *
 * Опори: ґратчасті піраміди. Збоку (вздовж колії) це чистий трикутник: основа широка, верх вузький, діагоналей на
 * цих гранях немає, лише рідкі горизонтальні в'язі. Спереду (впоперек колії) навпаки: опора вузька біля коліс і
 * розходиться вгору під міст, тут ґратка з діагоналями. Жорстка опора має драбину й кабіну, гнучка тонша.
 *
 * Міст: ферма трикутного перерізу (вершиною вгору), як на референсі. Знизу суцільна широка балка, на ній два
 * ряди розкосів Warren сходяться до одинарного верхнього поясу, через вузол трикутні рами. Поручнів і настилу
 * немає. За опорою міст виходить консоллю: півтори секції повної висоти, далі верхній пояс під 45° спускається
 * до балки, а навколо кінця балки висить клітка-майданчик із поручнями.
 *
 * Локальні осі: X це напрям колії (кран їде вздовж X), Z це поперечна вісь моста, Y угору. Початок координат
 * у центрі крана (між рейками) на рівні низу рейкового блока. Жорстка опора стоїть на +gauge, гнучка на -gauge,
 * де gauge це половина відстані між осями рейок.
 *
 * Колір: текстура металу майже біла, колір фарби накладається через glColor, тож кран можна фарбувати.
 * Темні деталі (візки, трос, гак), кабіна й шафа не фарбуються.
 *
 * Нерухома частина компілюється в display list для кожної пари (висота, відстань між рейками), чотири списки:
 * фарбований метал, темні деталі, кабіна, шафа. Візок, трос і гак малюються щокадру, бо рухаються.
 */
@SideOnly(Side.CLIENT)
public class ModelGantryCrane {

    private static final ResourceLocation PAINT = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_paint.png");
    private static final ResourceLocation DARK = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_dark.png");
    private static final ResourceLocation CABIN = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_cabin.png");
    private static final ResourceLocation BOX = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_box.png");

    private static final double RAIL_TOP = BlockCraneRail.HEIGHT;

    /** Півширина опор біля коліс (впоперек колії) і півдовжина верху вздовж колії. */
    private static final double RIGID_FOOT_Z = 0.5D;
    private static final double RIGID_TOP_X = 0.55D;
    private static final double FLEX_FOOT_Z = 0.4D;
    private static final double FLEX_TOP_X = 0.5D;

    /** Півширина нижньої балки моста, її висота, і півширина основи трикутної ферми (по краях верху балки). */
    private static final double GIRDER_HALF = 0.9D;
    private static final double GIRDER_H = 0.5D;
    private static final double BASE_X = 0.75D;
    /** Підлога кабіни нижче низу моста. Кабіна велика, тому підлога низько, щоб вона не впиралась у балку. */
    private static final double CAB_FLOOR_BELOW = 3.0D;
    /** Півдовжина, висота й півширина кабіни, і зсув її центру вздовж колії від осі опори. */
    private static final double CAB_HX = 1.15D;
    private static final double CAB_HY = 1.1D;
    private static final double CAB_HZ = 1.2D;
    private static final double CAB_X = 1.8D;

    /** key (height, span) -> перший id із чотирьох display list. */
    private final Map<Integer, Integer> lists = new HashMap<Integer, Integer>();

    private static int key(int height, int span) {
        return height * 64 + span;
    }

    /**
     * @param height висота верху моста над низом рейок
     * @param span відстань між осями рейок
     * @param trolleyZ положення візка поперек колії від центру крана
     * @param hookH довжина троса від низу моста до верху гака
     * @param r червона, зелена, синя складові кольору фарби (0..1)
     */
    public void render(int height, int span, double trolleyZ, double hookH, float r, float g, float b) {
        Minecraft mc = Minecraft.getMinecraft();
        Integer id = lists.get(key(height, span));
        if (id == null) {
            int base = GLAllocation.generateDisplayLists(4);
            GL11.glNewList(base, GL11.GL_COMPILE);
            buildPainted(height, span);
            GL11.glEndList();
            GL11.glNewList(base + 1, GL11.GL_COMPILE);
            buildDark(height, span);
            GL11.glEndList();
            GL11.glNewList(base + 2, GL11.GL_COMPILE);
            buildCabin(height, span);
            GL11.glEndList();
            GL11.glNewList(base + 3, GL11.GL_COMPILE);
            buildCabinet(height, span);
            GL11.glEndList();
            id = Integer.valueOf(base);
            lists.put(key(height, span), id);
        }

        // фарбований метал: нерухома ферма й візок
        mc.renderEngine.bindTexture(PAINT);
        GL11.glColor3f(r, g, b);
        GL11.glCallList(id.intValue());
        drawTrolley(height, span, trolleyZ);

        // решта без фарби
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(DARK);
        GL11.glCallList(id.intValue() + 1);
        drawHook(height, span, trolleyZ, hookH);

        mc.renderEngine.bindTexture(CABIN);
        GL11.glCallList(id.intValue() + 2);

        mc.renderEngine.bindTexture(BOX);
        GL11.glCallList(id.intValue() + 3);
    }

    // ------------------------------------------------------------ розміри опор від висоти

    private static double rigidFootX(int height) {
        return TileEntityCraneController.rigidFootX(height);
    }

    private static double flexFootX(int height) {
        return Math.max(2.4D, rigidFootX(height) * 0.75D);
    }

    private static double rigidTopZ(int height) {
        return TileEntityCraneController.rigidTopZ(height);
    }

    private static double flexTopZ(int height) {
        return Math.max(0.9D, rigidTopZ(height) * 0.8D);
    }

    // ------------------------------------------------------------ фарбований метал: опори, драбина, міст

    private static void buildPainted(int height, int span) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double gauge = span * 0.5D; // опори стоять на +-gauge від осі крана

        double depth = TileEntityCraneController.bridgeDepth(span);
        double yBot = height - depth;
        double yTop = height;
        int cant = TileEntityCraneController.cantilever(span);
        double base = RAIL_TOP + 0.4D;
        double rfx = rigidFootX(height);
        double rtz = rigidTopZ(height);

        // жорстка опора на +gauge
        tower(t, gauge, rfx, RIGID_FOOT_Z, RIGID_TOP_X, rtz, base, yBot,
            Math.max(3, (int) Math.round((yBot - base) / 2.4D)), 0.24D);
        // драбина до підлоги кабіни, майданчик на рівні підлоги й кронштейн від опори до кабіни
        double yc = yBot - CAB_FLOOR_BELOW;
        double fc = (yc - base) / (yBot - base);
        double hxHere = lerp(rfx, RIGID_TOP_X, fc);
        double hzHere = lerp(RIGID_FOOT_Z, rtz, fc);
        double cabZ = gauge - (hzHere + CAB_HZ + 0.1D);
        ladder(t, gauge, rfx, RIGID_TOP_X, base, yBot, yc);
        LepGeo.box(t, 0.0D, yc, gauge, hxHere + 0.6D, 0.05D, hzHere + 0.2D);
        LepGeo.box(t, CAB_X, yc, cabZ, CAB_HX + 0.2D, 0.06D, CAB_HZ + 0.1D);
        LepGeo.beam(t, 0.0D, yc, gauge - hzHere, CAB_X, yc, gauge - hzHere, 0.14D);
        LepGeo.beam(t, CAB_X, yc, gauge - hzHere, CAB_X, yc, cabZ, 0.14D);

        // гнучка опора на -gauge
        tower(t, -gauge, flexFootX(height), FLEX_FOOT_Z, FLEX_TOP_X, flexTopZ(height), base, yBot,
            Math.max(3, (int) Math.round((yBot - base) / 3.0D)), 0.2D);

        bridge(t, yBot, yTop, depth, gauge, cant);

        t.draw();
    }

    /**
     * Ґратчаста вежа у формі усіченої піраміди: чотири колони від кутів основи (foot) до кутів верху (top).
     * Грані в площині X-Y (вид збоку, між колонами А-рами) діагоналей не мають, лише горизонтальні в'язі через
     * рівень. Грані впоперек колії (вид спереду) мають в'язі на кожному рівні й розкоси зигзагом.
     * zc це положення осі вежі впоперек колії, а по x вежа симетрична відносно нуля.
     */
    private static void tower(Tessellator t, double zc, double footX, double footZ, double topX, double topZ,
                              double base, double yTop, int levels, double colTh) {
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
                boolean front = (i & 1) == 1; // грані x = const, їх видно спереду
                if (!front && (lv & 1) == 1) continue; // збоку в'язі тільки через рівень
                int[] a = sg[i];
                int[] b = sg[(i + 1) & 3];
                LepGeo.beam(t, a[0] * hx, y, zc + a[1] * hz, b[0] * hx, y, zc + b[1] * hz, colTh * 0.45D);
            }
            if (lv < levels) {
                double f2 = (double) (lv + 1) / levels;
                double y2 = base + (yTop - base) * f2;
                double hx2 = lerp(footX, topX, f2);
                double hz2 = lerp(footZ, topZ, f2);
                for (int i = 1; i < 4; i += 2) {
                    int[] a = sg[i];
                    int[] b = sg[(i + 1) & 3];
                    boolean flip = ((lv + (i >> 1)) & 1) == 0;
                    int[] pa = flip ? a : b;
                    int[] pb = flip ? b : a;
                    LepGeo.beam(t, pa[0] * hx, y, zc + pa[1] * hz, pb[0] * hx2, y2, zc + pb[1] * hz2, colTh * 0.35D);
                }
            }
        }
        // голова під мостом і балка-основа над візками
        LepGeo.box(t, 0.0D, yTop - 0.12D, zc, topX + 0.35D, 0.12D, topZ + 0.3D);
        LepGeo.box(t, 0.0D, base - 0.05D, zc, footX + 0.2D, 0.1D, footZ + 0.2D);
    }

    /**
     * Похила драбина вздовж зовнішньої (+X) грані жорсткої опори від основи до висоти top (підлога кабіни).
     * legTop це верх опори, за ним визначається нахил колон.
     */
    private static void ladder(Tessellator t, double zc, double footX, double topX, double base, double legTop, double top) {
        double out = 0.28D;
        double x0 = footX + out;
        double x1 = lerp(footX, topX, (top - base) / (legTop - base)) + out;
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

    /**
     * Міст трикутного перерізу. Нижня балка (прямокутна, суцільна) іде від кінця до кінця. На ній два ряди розкосів
     * Warren з основою по краях верху балки (x = +-BASE_X) сходяться до одинарного верхнього поясу (x = 0), у кожному
     * другому вузлі трикутна рама. Повна висота тримається до zFlat, далі верхній пояс спускається під 45° до кінця
     * консолі zEnd, де від ферми лишається невеликий трикутник. Настилу й поручнів немає.
     */
    private static void bridge(Tessellator t, double yBot, double yTop, double depth, double hs, int cant) {
        double yg = yBot + GIRDER_H; // верх балки
        double yt = yTop - 0.1D;     // верхній пояс
        double zEnd = hs + cant;
        double taper = Math.max(0.5D, (yt - yg) - 0.3D);
        double zFlat = zEnd - taper;
        double yEnd = yt - taper;

        // нижня балка на всю довжину, включно з консолями
        LepGeo.box(t, 0.0D, yBot + GIRDER_H * 0.5D, 0.0D, GIRDER_HALF, GIRDER_H * 0.5D, zEnd);

        // верхній пояс: прямо до zFlat, далі спускається під 45° до кінця
        LepGeo.beam(t, 0.0D, yt, -zFlat, 0.0D, yt, zFlat, 0.22D);
        for (int e = -1; e <= 1; e += 2) {
            LepGeo.beam(t, 0.0D, yt, e * zFlat, 0.0D, yEnd, e * zEnd, 0.2D);
        }

        int n = Math.max(4, (int) Math.round(2.0D * zFlat / depth));
        if ((n & 1) == 1) n++;
        double step = 2.0D * zFlat / n;

        for (int p = -1; p <= 1; p += 2) {
            double bx = p * BASE_X;
            // розкоси зигзагом між краєм балки й верхнім поясом
            for (int i = 0; i < n; i++) {
                boolean up = (i & 1) == 0;
                double za = -zFlat + i * step;
                double zb = za + step;
                if (up) {
                    LepGeo.beam(t, bx, yg, za, 0.0D, yt, zb, 0.1D);
                } else {
                    LepGeo.beam(t, 0.0D, yt, za, bx, yg, zb, 0.1D);
                }
            }
            // у звуженій частині по одному розкосу до кінця
            for (int e = -1; e <= 1; e += 2) {
                LepGeo.beam(t, bx, yg, e * zFlat, 0.0D, yEnd, e * zEnd, 0.1D);
            }
        }
        // трикутні рами в кожному другому вузлі, плюс на початку звуження й на кінці
        for (int i = 0; i <= n; i += 2) {
            frame(t, -zFlat + i * step, yg, yt);
        }
        for (int e = -1; e <= 1; e += 2) {
            frame(t, e * zEnd, yg, yEnd);
        }

        // клітки-майданчики навколо кінців балки
        for (int e = -1; e <= 1; e += 2) {
            cage(t, e, yBot, zEnd);
        }
    }

    /** Трикутна рама поперек моста: основа між краями верху балки й дві похилі до верхнього поясу. */
    private static void frame(Tessellator t, double z, double yg, double apexY) {
        LepGeo.beam(t, -BASE_X, yg, z, BASE_X, yg, z, 0.08D);
        LepGeo.beam(t, -BASE_X, yg, z, 0.0D, apexY, z, 0.09D);
        LepGeo.beam(t, BASE_X, yg, z, 0.0D, apexY, z, 0.09D);
    }

    /**
     * Клітка-майданчик навколо кінця консолі, як на референсі: підлога трохи нижче балки, чотири стійки
     * по кутах прив'язані до балки, по периметру дві планки поручнів.
     */
    private static void cage(Tessellator t, int e, double yBot, double zEnd) {
        double zIn = zEnd - 1.6D;
        double zOut = zEnd + 0.5D;
        double hx = 1.35D;
        double floor = yBot - 0.3D;
        double top = floor + 1.1D;
        double mid = floor + 0.55D;
        double cz = e * (zIn + zOut) * 0.5D;
        double hz = (zOut - zIn) * 0.5D;

        LepGeo.box(t, 0.0D, floor, cz, hx, 0.05D, hz);
        for (int p = -1; p <= 1; p += 2) {
            for (int z = 0; z < 2; z++) {
                double zz = e * (z == 0 ? zIn : zOut);
                LepGeo.beam(t, p * hx, floor, zz, p * hx, top, zz, 0.08D);
                // кутова стійка тримається за балку
                LepGeo.beam(t, p * hx, yBot + 0.25D, zz, p * GIRDER_HALF, yBot + 0.25D, zz, 0.08D);
            }
        }
        for (int p = -1; p <= 1; p += 2) {
            double rx = p * hx;
            LepGeo.beam(t, rx, top, e * zIn, rx, top, e * zOut, 0.05D);
            LepGeo.beam(t, rx, mid, e * zIn, rx, mid, e * zOut, 0.04D);
            for (double z = zIn + 0.8D; z < zOut - 0.3D; z += 0.8D) {
                LepGeo.beam(t, rx, floor, e * z, rx, top, e * z, 0.04D);
            }
        }
        for (int z = 0; z < 2; z++) {
            double zz = e * (z == 0 ? zIn : zOut);
            LepGeo.beam(t, -hx, top, zz, hx, top, zz, 0.05D);
            LepGeo.beam(t, -hx, mid, zz, hx, mid, zz, 0.04D);
        }
    }

    private static double lerp(double a, double b, double f) {
        return a + (b - a) * f;
    }

    // ------------------------------------------------------------ темні деталі: візки на рейках

    private static void buildDark(int height, int span) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double gauge = span * 0.5D;
        double rfx = rigidFootX(height);
        double ffx = flexFootX(height);
        // жорстка опора: два візки по краях основи
        for (int sx = -1; sx <= 1; sx += 2) {
            LepGeo.box(t, sx * (rfx - 0.55D), RAIL_TOP + 0.18D, gauge, 0.6D, 0.18D, 0.5D);
        }
        // гнучка опора: два візки ближче одне до одного
        for (int sx = -1; sx <= 1; sx += 2) {
            LepGeo.box(t, sx * (ffx - 0.5D), RAIL_TOP + 0.18D, -gauge, 0.55D, 0.18D, 0.45D);
        }
        t.draw();
    }

    // ------------------------------------------------------------ кабіна й шафа

    private static void buildCabin(int height, int span) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double gauge = span * 0.5D;
        double yBot = height - TileEntityCraneController.bridgeDepth(span);
        double base = RAIL_TOP + 0.4D;
        double yc = yBot - CAB_FLOOR_BELOW;
        double fc = (yc - base) / (yBot - base);
        double hzHere = lerp(RIGID_FOOT_Z, rigidTopZ(height), fc);
        double cabZ = gauge - (hzHere + CAB_HZ + 0.1D);
        LepGeo.box(t, CAB_X, yc + 0.06D + CAB_HY, cabZ, CAB_HX, CAB_HY, CAB_HZ);
        t.draw();
    }

    /** Сіра шафа керування всередині ферми, на балці над гнучкою опорою. */
    private static void buildCabinet(int height, int span) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double gauge = span * 0.5D;
        double yBot = height - TileEntityCraneController.bridgeDepth(span);
        LepGeo.box(t, 0.0D, yBot + GIRDER_H + 0.45D, -gauge + 1.2D, 0.33D, 0.45D, 0.9D);
        t.draw();
    }

    // ------------------------------------------------------------ рухомі частини

    /** Візок на нижній балці: рама під балкою, щоки по боках балки, лебідка на балці всередині ферми. */
    private static void drawTrolley(int height, int span, double tz) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double yBot = height - TileEntityCraneController.bridgeDepth(span);
        LepGeo.box(t, 0.0D, yBot - 0.15D, tz, GIRDER_HALF + 0.1D, 0.12D, 0.65D);
        for (int p = -1; p <= 1; p += 2) {
            LepGeo.box(t, p * (GIRDER_HALF + 0.05D), yBot + GIRDER_H * 0.5D - 0.05D, tz, 0.06D, GIRDER_H * 0.5D + 0.1D, 0.6D);
        }
        LepGeo.box(t, 0.0D, yBot + GIRDER_H + 0.3D, tz, 0.35D, 0.3D, 0.5D);
        t.draw();
    }

    private static void drawHook(int height, int span, double tz, double hookH) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double yBot = height - TileEntityCraneController.bridgeDepth(span);
        double hookTop = yBot - hookH;
        double cableTop = yBot - 0.27D;
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
