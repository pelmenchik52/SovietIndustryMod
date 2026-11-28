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
 * Козловий кран серії ККС (за фото-референсом).
 *
 * - Опори: А-подібні збоку (без горизонтальних в'язей), спереду (впоперек колії) — з ґраткою між ногами.
 *   Кожна нога спирається на окремий ходовий візок на рейці.
 * - Драбина: закріплена безпосередньо вздовж передньої ноги жорсткої опори з повноцінною захисною кліткою
 *   (дуги + вертикальні напрямні), виходить на оглядовий майданчик біля верхнього ригеля опори, а звідти
 *   коротким маршем зі сходами та поручнями — на виносну галерею моста.
 * - Міст: чиста тригранна ферма, підкоси передньої галереї впираються безпосередньо у верхню нерухому
 *   полицю двотаврової балки візка (не заважаючи руху коліс і щок візка).
 */
@SideOnly(Side.CLIENT)
public class ModelGantryCrane {

    private static final ResourceLocation PAINT = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_paint.png");
    private static final ResourceLocation DARK = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_dark.png");
    private static final ResourceLocation BOX = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_box.png");

    private static final double RAIL_TOP = BlockCraneRail.HEIGHT;

    /** Півширина опор біля коліс (впоперек колії) і півдовжина верху вздовж колії. */
    private static final double RIGID_FOOT_Z = 0.45D;
    private static final double RIGID_TOP_X = 0.55D;
    private static final double FLEX_FOOT_Z = 0.4D;
    private static final double FLEX_TOP_X = 0.5D;

    /** Півширина нижньої частини ферми моста та висота нижнього поясу. */
    private static final double GIRDER_HALF = 1.25D;
    private static final double GIRDER_H = 0.45D;
    private static final double BASE_X = GIRDER_HALF;

    /** Виносна галерея (прохід перед краном, з боку +X). */
    private static final double WALKWAY_HALF_W = 0.42D;
    private static final double WALKWAY_X = GIRDER_HALF + WALKWAY_HALF_W + 0.08D;

    /** Виліт торцевого балкона за межі ферми. */
    private static final double BALCONY_DEPTH = 1.65D;

    private static double floorY(double yBot) {
        return yBot + GIRDER_H;
    }

    /** Рейка візка під балкою (двотавр). */
    private static final double TRACK_FLANGE_HALF = 0.32D;
    private static final double TRACK_DROP = 0.45D;

    private final Map<Integer, Integer> lists = new HashMap<Integer, Integer>();

    private static int key(int height, int span) {
        return height * 64 + span;
    }

    public void render(int height, int span, double trolleyZ, double hookH, float r, float g, float b) {
        Minecraft mc = Minecraft.getMinecraft();
        Integer id = lists.get(key(height, span));
        if (id == null) {
            int base = GLAllocation.generateDisplayLists(3);
            GL11.glNewList(base, GL11.GL_COMPILE);
            buildPainted(height, span);
            GL11.glEndList();
            GL11.glNewList(base + 1, GL11.GL_COMPILE);
            buildDark(height, span);
            GL11.glEndList();
            GL11.glNewList(base + 2, GL11.GL_COMPILE);
            buildCabinet(height, span);
            GL11.glEndList();
            id = Integer.valueOf(base);
            lists.put(key(height, span), id);
        }

        mc.renderEngine.bindTexture(PAINT);
        GL11.glColor3f(r, g, b);
        GL11.glCallList(id.intValue());
        drawTrolley(height, span, trolleyZ);

        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(DARK);
        GL11.glCallList(id.intValue() + 1);
        drawHook(height, span, trolleyZ, hookH);

        mc.renderEngine.bindTexture(BOX);
        GL11.glCallList(id.intValue() + 2);
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

    // ------------------------------------------------------------ фарбований метал

    private static void buildPainted(int height, int span) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double gauge = span * 0.5D;

        double depth = TileEntityCraneController.bridgeDepth(span);
        double yBot = height - depth;
        double yTop = height;
        int cant = TileEntityCraneController.cantilever(span);
        double base = RAIL_TOP + 0.34D;
        double rfx = rigidFootX(height);
        double rtz = rigidTopZ(height);

        // 1. Жорстка опора (+gauge): окремі ноги, без перекладин збоку, але з ґраткою спереду
        tower(t, gauge, rfx, RIGID_FOOT_Z, RIGID_TOP_X, rtz, base, yBot,
            Math.max(4, (int) Math.round((yBot - base) / 2.2D)), 0.22D);

        // 2. Драбина вздовж передньої (+X) ноги жорсткої опори + проміжний майданчик опори + вихід на галерею
        double floor = floorY(yBot);
        ladderAndLanding(t, gauge, rfx, RIGID_FOOT_Z, RIGID_TOP_X, rtz, base, yBot, floor);

        // 3. Гнучка опора (-gauge)
        tower(t, -gauge, flexFootX(height), FLEX_FOOT_Z, FLEX_TOP_X, flexTopZ(height), base, yBot,
            Math.max(4, (int) Math.round((yBot - base) / 2.5D)), 0.19D);

        // 4. Міст тригранного перерізу + рейка візка + виносна галерея + винесені назовні балкони
        bridge(t, yBot, yTop, depth, gauge, cant);

        t.draw();
    }

    /**
     * Опора крана ККС:
     * - 4 окремі ноги спираються кожна на свій візок внизу (жодної суцільної платформи знизу).
     * - Вид збоку (площина X-Y): чисті похилі стійки без горизонтальних перекладин.
     * - Вид спереду (площина Z-Y): ґратка між двома ногами (горизонтальні в'язі та діагоналі).
     */
    private static void tower(Tessellator t, double zc, double footX, double footZ, double topX, double topZ,
                              double base, double yTop, int levels, double colTh) {
        int[][] sg = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };

        for (int i = 0; i < 4; i++) {
            double bx = sg[i][0] * footX;
            double bz = zc + sg[i][1] * footZ;
            double tx = sg[i][0] * topX;
            double tz = zc + sg[i][1] * topZ;

            LepGeo.beam(t, bx, base, bz, tx, yTop, tz, colTh);
            LepGeo.box(t, bx, base + 0.05D, bz, colTh * 1.45D, 0.08D, colTh * 1.45D);
        }

        for (int sx = -1; sx <= 1; sx += 2) {
            for (int lv = 1; lv < levels; lv++) {
                double f = (double) lv / levels;
                double y = base + (yTop - base) * f;
                double x = sx * lerp(footX, topX, f);
                double hz = lerp(footZ, topZ, f);

                LepGeo.beam(t, x, y, zc - hz, x, y, zc + hz, colTh * 0.45D);
            }

            for (int lv = 1; lv < levels; lv++) {
                double f1 = (double) lv / levels;
                double f2 = (double) (lv + 1) / levels;
                double y1 = base + (yTop - base) * f1;
                double y2 = base + (yTop - base) * f2;
                double x1 = sx * lerp(footX, topX, f1);
                double x2 = sx * lerp(footX, topX, f2);
                double hz1 = lerp(footZ, topZ, f1);
                double hz2 = lerp(footZ, topZ, f2);

                int dir = ((lv + (sx > 0 ? 0 : 1)) & 1) == 0 ? 1 : -1;
                LepGeo.beam(t, x1, y1, zc - dir * hz1, x2, y2, zc + dir * hz2, colTh * 0.36D);
            }
        }

        double yHead = yTop - 0.12D;
        for (int i = 0; i < 4; i++) {
            int[] a = sg[i];
            int[] b = sg[(i + 1) & 3];
            LepGeo.beam(t, a[0] * topX, yHead, zc + a[1] * topZ, b[0] * topX, yHead, zc + b[1] * topZ, colTh * 0.75D);
        }
        LepGeo.box(t, 0.0D, yTop - 0.06D, zc, topX + 0.25D, 0.08D, topZ + 0.25D);
    }

    /**
     * Реалістичний підйом на кран ККС:
     * 1. Похила драбина закріплена безпосередньо вздовж передньої (+X) лівої (+Z) ноги жорсткої опори
     *    від низу до оглядового майданчика опори (на рівні yLanding = yBot - 0.85D).
     * 2. Повноцінна захисна клітка навколо драбини: дуги + 3 вертикальні поздовжні смуги зв'язку.
     * 3. Проміжний оглядовий майданчик на опорі (навколо обох передніх ніг) з огородженням, на який
     *    людина виходить з драбини через відкритий люк/проріз.
     * 4. Короткі похилі сходи з поручнями від цього майданчика вгору на виносну галерею моста (floor).
     */
    private static void ladderAndLanding(Tessellator t, double zc, double footX, double footZ,
                                         double topX, double topZ, double base, double yBot, double floor) {
        double yLanding = yBot - 0.85D;
        double fLand = Math.max(0.1D, Math.min(0.95D, (yLanding - base) / (yBot - base)));

        // Координати передньої (+X) правої (+Z) ноги жорсткої опори
        double legX0 = footX;
        double legZ0 = zc + footZ;
        double legX1 = lerp(footX, topX, fLand);
        double legZ1 = zc + lerp(footZ, topZ, fLand);

        // Драбина йде паралельно нозі з відступом +0.26D по X на кронштейнах
        double offset = 0.26D;
        double ladX0 = legX0 + offset;
        double ladX1 = legX1 + offset;
        double halfW = 0.26D;

        // Тятиви драбини
        LepGeo.beam(t, ladX0, base, legZ0 - halfW, ladX1, yLanding, legZ1 - halfW, 0.055D);
        LepGeo.beam(t, ladX0, base, legZ0 + halfW, ladX1, yLanding, legZ1 + halfW, 0.055D);

        // Сходинки та кронштейни кріплення до ноги опори
        double climbH = yLanding - base;
        for (double y = base + 0.35D; y <= yLanding - 0.05D; y += 0.38D) {
            double f = (y - base) / climbH;
            double lx = lerp(ladX0, ladX1, f);
            double lz = lerp(legZ0, legZ1, f);
            LepGeo.beam(t, lx, y, lz - halfW, lx, y, lz + halfW, 0.04D);

            // Кронштейни до ноги опори кожні ~1.5 м
            if (((int) Math.round((y - base) / 0.38D)) % 4 == 0) {
                double colX = lerp(legX0, legX1, f);
                LepGeo.beam(t, colX, y, lz - halfW, lx, y, lz - halfW, 0.045D);
                LepGeo.beam(t, colX, y, lz + halfW, lx, y, lz + halfW, 0.045D);
            }
        }

        // Повноцінна захисна клітка навколо драбини (починається з висоти 2.1 блока і тягнеться до yLanding + 1.05)
        double cageStart = Math.min(base + 2.1D, base + climbH * 0.3D);
        double cageEnd = yLanding + 1.05D;
        double cageDepth = 0.68D; // глибина дуги за спиною людини (+X)
        double cageHalfZ = 0.42D;

        for (double y = cageStart; y <= cageEnd; y += 0.75D) {
            double f = Math.min(1.0D, (y - base) / climbH);
            double lx = lerp(ladX0, ladX1, f);
            double lz = lerp(legZ0, legZ1, f);
            // Дуга захисної клітки (3 сегменти навколо людини)
            LepGeo.beam(t, lx, y, lz - cageHalfZ, lx + cageDepth, y, lz - cageHalfZ, 0.032D);
            LepGeo.beam(t, lx + cageDepth, y, lz - cageHalfZ, lx + cageDepth, y, lz + cageHalfZ, 0.032D);
            LepGeo.beam(t, lx + cageDepth, y, lz + cageHalfZ, lx, y, lz + cageHalfZ, 0.032D);
        }

        // Поздовжні вертикально-похилі смуги зв'язку клітки (3 смуги: ззаду та по боках дуг)
        double fStart = (cageStart - base) / climbH;
        double fEnd = 1.0D;
        double sx0 = lerp(ladX0, ladX1, fStart);
        double sz0 = lerp(legZ0, legZ1, fStart);
        double sx1 = ladX1;
        double sz1 = legZ1;
        // Продовження до висоти огородження площадки (yLanding + 1.05D)
        double dxSlope = (ladX1 - ladX0) / climbH;
        double dzSlope = (legZ1 - legZ0) / climbH;
        double sxTop = sx1 + dxSlope * 1.05D;
        double szTop = sz1 + dzSlope * 1.05D;

        LepGeo.beam(t, sx0 + cageDepth, cageStart, sz0, sxTop + cageDepth, cageEnd, szTop, 0.032D);
        LepGeo.beam(t, sx0 + cageDepth, cageStart, sz0 - cageHalfZ, sxTop + cageDepth, cageEnd, szTop - cageHalfZ, 0.032D);
        LepGeo.beam(t, sx0 + cageDepth, cageStart, sz0 + cageHalfZ, sxTop + cageDepth, cageEnd, szTop + cageHalfZ, 0.032D);

        // ------------------------------------------------------------------
        // Проміжний оглядовий майданчик на опорі на висоті yLanding
        // ------------------------------------------------------------------
        double walkOuterX = WALKWAY_X + WALKWAY_HALF_W;
        double landMinX = legX1 - 0.15D;
        double landMaxX = Math.max(ladX1 + cageDepth + 0.15D, walkOuterX + 0.15D);
        double landMinZ = zc - topZ - 0.55D;
        double landMaxZ = legZ1 + cageHalfZ + 0.15D;

        double landCx = (landMinX + landMaxX) * 0.5D;
        double landHx = (landMaxX - landMinX) * 0.5D;
        double landCz = (landMinZ + landMaxZ) * 0.5D;
        double landHz = (landMaxZ - landMinZ) * 0.5D;

        // Настил майданчика опори (залишаємо виріз під вихід з драбини біля ladX1, legZ1)
        double holeMinX = ladX1 - 0.05D;
        double holeMinZ = legZ1 - cageHalfZ;
        // Основна плита ліворуч від отвору драбини (по Z)
        double mainHz = (holeMinZ - landMinZ) * 0.5D;
        if (mainHz > 0.05D) {
            LepGeo.box(t, landCx, yLanding - 0.03D, (landMinZ + holeMinZ) * 0.5D, landHx, 0.035D, mainHz);
        }
        // Смуга настилу внутрішніше від драбини (по X, між ногами опори)
        double stripHx = (holeMinX - landMinX) * 0.5D;
        double stripHz = (landMaxZ - holeMinZ) * 0.5D;
        if (stripHx > 0.05D && stripHz > 0.05D) {
            LepGeo.box(t, (landMinX + holeMinX) * 0.5D, yLanding - 0.03D, (holeMinZ + landMaxZ) * 0.5D, stripHx, 0.035D, stripHz);
        }

        // Несучі балки майданчика під настилом (кріпляться до передніх ніг опори)
        LepGeo.beam(t, landMinX, yLanding - 0.07D, landMinZ, landMaxX, yLanding - 0.07D, landMinZ, 0.06D);
        LepGeo.beam(t, landMinX, yLanding - 0.07D, holeMinZ, landMaxX, yLanding - 0.07D, holeMinZ, 0.06D);
        LepGeo.beam(t, landMinX, yLanding - 0.07D, landMaxZ, landMaxX, yLanding - 0.07D, landMaxZ, 0.06D);
        LepGeo.beam(t, landMaxX, yLanding - 0.07D, landMinZ, landMaxX, yLanding - 0.07D, landMaxZ, 0.06D);

        // Огородження проміжного майданчика опори
        double lTop = yLanding + 1.05D;
        double lMid = yLanding + 0.52D;
        // Зовнішній край (+X)
        LepGeo.beam(t, landMaxX, lTop, landMinZ, landMaxX, lTop, landMaxZ, 0.045D);
        LepGeo.beam(t, landMaxX, lMid, landMinZ, landMaxX, lMid, landMaxZ, 0.038D);
        LepGeo.beam(t, landMaxX, yLanding, landMinZ, landMaxX, lTop, landMinZ, 0.045D);
        LepGeo.beam(t, landMaxX, yLanding, landMaxZ, landMaxX, lTop, landMaxZ, 0.045D);
        LepGeo.beam(t, landMaxX, yLanding, holeMinZ, landMaxX, lTop, holeMinZ, 0.045D);
        // Торцевий край (+Z, за спиною при виході з драбини)
        LepGeo.beam(t, landMinX, lTop, landMaxZ, landMaxX, lTop, landMaxZ, 0.045D);
        LepGeo.beam(t, landMinX, lMid, landMaxZ, landMaxX, lMid, landMaxZ, 0.038D);
        LepGeo.beam(t, landMinX, yLanding, landMaxZ, landMinX, lTop, landMaxZ, 0.045D);

        // ------------------------------------------------------------------
        // Перехідні сходи з майданчика опори (yLanding) вгору на виносну галерею (floor)
        // ------------------------------------------------------------------
        double stairZ = zc;
        double stairStartZ = landMinZ + 0.45D; // початок підйому на майданчику опори
        double stairEndZ = zc - 0.40D;         // вихід на рівень галереї (floor)
        double stairX0 = WALKWAY_X - WALKWAY_HALF_W + 0.08D;
        double stairX1 = WALKWAY_X + WALKWAY_HALF_W - 0.08D;

        // Косоури сходів від майданчика опори (yLanding, stairStartZ) до настилу галереї (floor, stairEndZ)
        LepGeo.beam(t, stairX0, yLanding, stairStartZ, stairX0, floor, stairEndZ, 0.055D);
        LepGeo.beam(t, stairX1, yLanding, stairStartZ, stairX1, floor, stairEndZ, 0.055D);

        // 4 сходинки перехідного маршу
        int steps = 4;
        for (int s = 1; s <= steps; s++) {
            double sf = (double) s / (steps + 1);
            double sy = lerp(yLanding, floor, sf);
            double sz = lerp(stairStartZ, stairEndZ, sf);
            LepGeo.box(t, WALKWAY_X, sy, sz, WALKWAY_HALF_W - 0.08D, 0.025D, 0.11D);
        }

        // Поручні перехідних сходів
        for (double sx : new double[] { stairX0, stairX1 }) {
            LepGeo.beam(t, sx, yLanding, stairStartZ, sx, yLanding + 1.05D, stairStartZ, 0.045D);
            LepGeo.beam(t, sx, floor, stairEndZ, sx, floor + 1.05D, stairEndZ, 0.045D);
            LepGeo.beam(t, sx, yLanding + 1.05D, stairStartZ, sx, floor + 1.05D, stairEndZ, 0.045D);
            LepGeo.beam(t, sx, yLanding + 0.52D, stairStartZ, sx, floor + 0.52D, stairEndZ, 0.035D);
        }

        // Закриття протилежного краю (-Z) майданчика опори до сходів
        LepGeo.beam(t, landMinX, lTop, landMinZ, stairX0, lTop, landMinZ, 0.045D);
        LepGeo.beam(t, landMinX, lMid, landMinZ, stairX0, lMid, landMinZ, 0.038D);
        LepGeo.beam(t, landMinX, yLanding, landMinZ, landMinX, lTop, landMinZ, 0.045D);
        LepGeo.beam(t, stairX0, yLanding, landMinZ, stairX0, lTop, landMinZ, 0.045D);
    }

    /**
     * Міст крана ККС.
     */
    private static void bridge(Tessellator t, double yBot, double yTop, double depth, double hs, int cant) {
        double yg = yBot + GIRDER_H - 0.08D;
        double yt = yTop - 0.1D;
        double zEnd = hs + cant;
        double taper = Math.max(0.5D, (yt - yg) - 0.3D);
        double zFlat = zEnd - taper;
        double yEnd = yt - taper;
        double floor = floorY(yBot);

        int n = Math.max(4, (int) Math.round(2.0D * zFlat / depth));
        if ((n & 1) == 1) n++;
        double step = 2.0D * zFlat / n;

        girderClean(t, yBot, yg, zEnd, zFlat, step, n);

        double railHalf = zEnd - 0.25D;
        track(t, yBot, railHalf);

        LepGeo.beam(t, 0.0D, yt, -zFlat, 0.0D, yt, zFlat, 0.20D);
        for (int e = -1; e <= 1; e += 2) {
            LepGeo.beam(t, 0.0D, yt, e * zFlat, 0.0D, yEnd, e * zEnd, 0.18D);
        }

        for (int p = -1; p <= 1; p += 2) {
            double bx = p * BASE_X;
            for (int i = 0; i < n; i++) {
                boolean up = (i & 1) == 0;
                double za = -zFlat + i * step;
                double zb = za + step;
                if (up) {
                    LepGeo.beam(t, bx, yg, za, 0.0D, yt, zb, 0.095D);
                } else {
                    LepGeo.beam(t, 0.0D, yt, za, bx, yg, zb, 0.095D);
                }
            }
            for (int e = -1; e <= 1; e += 2) {
                LepGeo.beam(t, bx, yg, e * zFlat, 0.0D, yEnd, e * zEnd, 0.095D);
            }
        }

        for (int i = 0; i <= n; i += 2) {
            frame(t, -zFlat + i * step, yg, yt);
        }
        for (int e = -1; e <= 1; e += 2) {
            frame(t, e * zEnd, yg, yEnd);
        }

        double balcIn = zEnd;
        double balcOut = zEnd + BALCONY_DEPTH;
        for (int e = -1; e <= 1; e += 2) {
            cage(t, e, yBot, balcIn, balcOut);
        }

        frontWalkway(t, yBot, floor, balcIn, hs);
    }

    private static void girderClean(Tessellator t, double yBot, double yg, double zEnd, double zFlat, double step, int n) {
        for (int p = -1; p <= 1; p += 2) {
            double bx = p * BASE_X;
            LepGeo.beam(t, bx, yg, -zEnd, bx, yg, zEnd, 0.18D);
        }
        for (int i = 0; i <= n; i++) {
            double z = -zFlat + i * step;
            LepGeo.beam(t, -BASE_X, yg, z, BASE_X, yg, z, 0.11D);
            LepGeo.beam(t, 0.0D, yg, z, 0.0D, yBot, z, 0.08D);
        }
        for (int e = -1; e <= 1; e += 2) {
            LepGeo.beam(t, -BASE_X, yg, e * zEnd, BASE_X, yg, e * zEnd, 0.12D);
            LepGeo.beam(t, 0.0D, yg, e * zEnd, 0.0D, yBot, e * zEnd, 0.08D);
        }
    }

    /**
     * Виносна галерея перед краном (+X) від правого балкона до лівого.
     * Похилі підкоси впираються ТОЧНО в зовнішній край верхньої нерухомої полиці двотаврової балки
     * (braceAnchorX = TRACK_FLANGE_HALF = 0.32D, braceAnchorY = yBot - 0.03D), тому не висять у повітрі
     * і не заважають візку, який їздить по нижній полиці.
     */
    private static void frontWalkway(Tessellator t, double yBot, double floor, double balcIn, double ladderZ) {
        double railTop = floor + 1.05D;
        double railMid = floor + 0.52D;
        double outerX = WALKWAY_X + WALKWAY_HALF_W;
        double innerX = BASE_X;
        double braceAnchorX = TRACK_FLANGE_HALF; // точно в бічну грань верхньої полиці балки візка
        double braceAnchorY = yBot - 0.03D;

        LepGeo.box(t, WALKWAY_X, floor - 0.03D, 0.0D, WALKWAY_HALF_W, 0.035D, balcIn);

        int count = Math.max(4, (int) Math.round((2.0D * balcIn) / 2.2D));
        double dz = (2.0D * balcIn) / count;

        for (int i = 0; i <= count; i++) {
            double z = -balcIn + i * dz;
            LepGeo.beam(t, innerX, floor - 0.06D, z, outerX, floor - 0.06D, z, 0.06D);
            LepGeo.beam(t, braceAnchorX, braceAnchorY, z, outerX - 0.06D, floor - 0.06D, z, 0.05D);
            LepGeo.beam(t, outerX, floor, z, outerX, railTop, z, 0.045D);
        }

        // Суцільні поручні вздовж усієї передньої галереї (вхід із перехідних сходів іде знизу через настил біля ladderZ)
        LepGeo.beam(t, outerX, railTop, -balcIn, outerX, railTop, balcIn, 0.05D);
        LepGeo.beam(t, outerX, railMid, -balcIn, outerX, railMid, balcIn, 0.04D);
    }

    /**
     * Двотаврова монорейка під мостом.
     */
    private static void track(Tessellator t, double yBot, double half) {
        double yTopFlange = yBot - 0.03D;
        double yBottomFlange = yBot - TRACK_DROP + 0.04D;
        double webTop = yBot - 0.07D;
        double webBottom = yBot - TRACK_DROP + 0.08D;
        LepGeo.box(t, 0.0D, yTopFlange, 0.0D, TRACK_FLANGE_HALF, 0.04D, half);
        LepGeo.box(t, 0.0D, (webTop + webBottom) * 0.5D, 0.0D, 0.05D, (webTop - webBottom) * 0.5D, half);
        LepGeo.box(t, 0.0D, yBottomFlange, 0.0D, TRACK_FLANGE_HALF, 0.04D, half);
        for (int e = -1; e <= 1; e += 2) {
            LepGeo.box(t, 0.0D, yBot - TRACK_DROP * 0.5D, e * (half + 0.08D), 0.36D, 0.22D, 0.08D);
        }
    }

    private static void frame(Tessellator t, double z, double yg, double apexY) {
        LepGeo.beam(t, -BASE_X, yg, z, 0.0D, apexY, z, 0.085D);
        LepGeo.beam(t, BASE_X, yg, z, 0.0D, apexY, z, 0.085D);
    }

    /**
     * Торцевий балкон за межами ферми [zEnd .. zEnd + BALCONY_DEPTH].
     */
    private static void cage(Tessellator t, int e, double yBot, double zIn, double zOut) {
        double minX = -(GIRDER_HALF + 0.25D);
        double maxX = WALKWAY_X + WALKWAY_HALF_W;
        double cx = (minX + maxX) * 0.5D;
        double hx = (maxX - minX) * 0.5D;

        double floor = floorY(yBot);
        double top = floor + 1.05D;
        double mid = floor + 0.52D;
        double cz = e * (zIn + zOut) * 0.5D;
        double hz = (zOut - zIn) * 0.5D;
        double zzIn = e * zIn;
        double zzOut = e * zOut;
        double yg = yBot + GIRDER_H - 0.08D;

        LepGeo.box(t, cx, floor - 0.03D, cz, hx, 0.035D, hz);

        double[] zs = { zzIn, zzOut };
        for (double zz : zs) {
            LepGeo.beam(t, minX, floor - 0.06D, zz, maxX, floor - 0.06D, zz, 0.065D);
        }
        double braceAnchorZ = e * (zIn - 0.5D);
        LepGeo.beam(t, -BASE_X, yg, braceAnchorZ, minX + 0.1D, floor - 0.06D, zzOut - e * 0.1D, 0.055D);
        LepGeo.beam(t, BASE_X, yg, braceAnchorZ, maxX - 0.1D, floor - 0.06D, zzOut - e * 0.1D, 0.055D);

        double[] xs = { minX, maxX };
        for (double xx : xs) {
            for (double zz : zs) {
                LepGeo.beam(t, xx, floor - 0.06D, zz, xx, top, zz, 0.05D);
            }
        }

        for (double rx : xs) {
            LepGeo.beam(t, rx, top, zzIn, rx, top, zzOut, 0.05D);
            LepGeo.beam(t, rx, mid, zzIn, rx, mid, zzOut, 0.04D);
        }

        LepGeo.beam(t, minX, top, zzOut, maxX, top, zzOut, 0.05D);
        LepGeo.beam(t, minX, mid, zzOut, maxX, mid, zzOut, 0.04D);
        LepGeo.beam(t, 0.0D, floor, zzOut, 0.0D, top, zzOut, 0.045D);

        double walkLeftEdge = WALKWAY_X - WALKWAY_HALF_W;
        LepGeo.beam(t, minX, top, zzIn, walkLeftEdge, top, zzIn, 0.05D);
        LepGeo.beam(t, minX, mid, zzIn, walkLeftEdge, mid, zzIn, 0.04D);
        LepGeo.beam(t, walkLeftEdge, floor, zzIn, walkLeftEdge, top, zzIn, 0.045D);
    }

    private static double lerp(double a, double b, double f) {
        return a + (b - a) * f;
    }

    // ------------------------------------------------------------ темні деталі: окремі ходові візки на рейках

    private static void buildDark(int height, int span) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double gauge = span * 0.5D;
        double rfx = rigidFootX(height);
        double ffx = flexFootX(height);

        for (int sx = -1; sx <= 1; sx += 2) {
            drawRailBogie(t, sx * rfx, gauge, 0.55D);
            drawRailBogie(t, sx * ffx, -gauge, 0.50D);
        }
        t.draw();
    }

    private static void drawRailBogie(Tessellator t, double x, double z, double halfLen) {
        LepGeo.box(t, x, RAIL_TOP + 0.22D, z, halfLen, 0.12D, 0.36D);
        for (int w = -1; w <= 1; w += 2) {
            LepGeo.box(t, x + w * (halfLen * 0.58D), RAIL_TOP + 0.11D, z, 0.14D, 0.11D, 0.22D);
        }
    }

    // ------------------------------------------------------------ шафа керування

    private static void buildCabinet(int height, int span) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double gauge = span * 0.5D;
        double yBot = height - TileEntityCraneController.bridgeDepth(span);
        double floor = floorY(yBot);
        double zEnd = gauge + TileEntityCraneController.cantilever(span);

        double cabHalfX = 0.70D;
        double cabHalfY = 0.65D;
        double cabHalfZ = 0.42D;
        double cabX = -0.55D;
        double cabZ = -(zEnd + BALCONY_DEPTH * 0.5D);

        LepGeo.box(t, cabX, floor + cabHalfY, cabZ, cabHalfX, cabHalfY, cabHalfZ);
        t.draw();
    }

    // ------------------------------------------------------------ рухомі частини

    private static void drawTrolley(int height, int span, double tz) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double yBot = height - TileEntityCraneController.bridgeDepth(span);
        double yFl = yBot - TRACK_DROP;
        double cheekX = TRACK_FLANGE_HALF + 0.12D;
        for (int p = -1; p <= 1; p += 2) {
            LepGeo.box(t, p * cheekX, yFl - 0.035D, tz, 0.05D, 0.215D, 0.6D);
        }
        LepGeo.box(t, 0.0D, yFl - 0.28D, tz, cheekX + 0.05D, 0.05D, 0.6D);
        LepGeo.box(t, 0.0D, yFl - 0.55D, tz, 0.6D, 0.22D, 0.55D);
        t.draw();
    }

    private static void drawHook(int height, int span, double tz, double hookH) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double yBot = height - TileEntityCraneController.bridgeDepth(span);
        double hookTop = yBot - hookH;
        double yFl = yBot - TRACK_DROP;
        double cableTop = yFl - 0.77D;
        for (int p = -1; p <= 1; p += 2) {
            for (int q = -1; q <= 1; q += 2) {
                LepGeo.box(t, p * 0.28D, yFl + 0.17D, tz + q * 0.38D, 0.1D, 0.1D, 0.1D);
            }
        }
        LepGeo.beam(t, 0.0D, cableTop, tz, 0.0D, hookTop, tz, 0.06D);
        LepGeo.box(t, 0.0D, hookTop - 0.3D, tz, 0.24D, 0.3D, 0.24D);
        LepGeo.beam(t, 0.0D, hookTop - 0.6D, tz, 0.0D, hookTop - 0.95D, tz, 0.1D);
        LepGeo.box(t, 0.0D, hookTop - 1.0D, tz, 0.16D, 0.05D, 0.16D);
        t.draw();
    }
}