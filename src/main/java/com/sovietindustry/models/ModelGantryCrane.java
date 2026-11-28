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
 * - Опори: А-подібні збоку (без горизонтальних в'язей), а спереду (впоперек колії) — з ґраткою між ногами.
 *   Кожна нога спирається на окремий ходовий візок на рейці.
 * - Міст: чиста тригранна ферма; двотаврова балка візка доходить майже до кінця консолі.
 * - Торцеві балкони: винесені за межі трикутної ферми назовні, щоб ферма не заходила всередину балкона.
 * - Виносна галерея: передня доріжка від правого балкона до лівого, кронштейни якої оминають зону руху візка.
 * - Шафа керування: встановлена на лівому торцевому балконі (на рівні проходу), а не на верхньому поясі ферми.
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

        // 2. Похила драбина вздовж передньої (+X) грані жорсткої опори з виходом на перехідний місток
        double floor = floorY(yBot);
        ladder(t, gauge, rfx, RIGID_TOP_X, base, yBot, floor);

        // 3. Гнучка опора (-gauge): така сама чиста А-форма збоку та ґратка спереду
        tower(t, -gauge, flexFootX(height), FLEX_FOOT_Z, FLEX_TOP_X, flexTopZ(height), base, yBot,
            Math.max(4, (int) Math.round((yBot - base) / 2.5D)), 0.19D);

        // 4. Міст тригранного перерізу + подовжена рейка візка + виносна галерея + винесені назовні балкони
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
     * Похила драбина вздовж зовнішньої (+X) грані жорсткої опори.
     * Верх драбини виходить точно на зовнішній край перехідного містка (topLadderX),
     * тому гравець піднімається на площадку, а не впирається в неї знизу.
     */
    private static void ladder(Tessellator t, double zc, double footX, double topX, double base, double legTop, double floor) {
        double out = 0.26D;
        double x0 = footX + out;
        double x1 = lerp(footX, topX, Math.min(1.0D, (floor - base) / (legTop - base))) + out;
        double halfW = 0.30D;
        double walkOuterX = WALKWAY_X + WALKWAY_HALF_W;

        double topLadderX = Math.max(x1, walkOuterX + 0.55D);

        // Тятиви драбини до рівня підлоги
        LepGeo.beam(t, x0, base, zc - halfW, topLadderX, floor, zc - halfW, 0.06D);
        LepGeo.beam(t, x0, base, zc + halfW, topLadderX, floor, zc + halfW, 0.06D);

        // Сходинки
        double len = floor - base;
        for (double y = base + 0.35D; y < floor - 0.05D; y += 0.42D) {
            double f = (y - base) / len;
            double x = lerp(x0, topLadderX, f);
            LepGeo.beam(t, x, y, zc - halfW, x, y, zc + halfW, 0.04D);
        }

        // Захисна клітка за спиною (+X від тятив) до рівня поясу над площадкою
        for (double y = base + len * 0.62D; y <= floor + 0.95D; y += 0.85D) {
            double f = Math.min(1.0D, (y - base) / len);
            double x = lerp(x0, topLadderX, f);
            LepGeo.beam(t, x, y, zc - 0.42D, x + 0.38D, y, zc - 0.42D, 0.035D);
            LepGeo.beam(t, x + 0.38D, y, zc - 0.42D, x + 0.38D, y, zc + 0.42D, 0.035D);
            LepGeo.beam(t, x + 0.38D, y, zc + 0.42D, x, y, zc + 0.42D, 0.035D);
        }

        // Перехідний місток від верху драбини (topLadderX) всередину до галереї (walkOuterX)
        double midX = (walkOuterX + topLadderX) * 0.5D;
        double spanX = (topLadderX - walkOuterX) * 0.5D;
        LepGeo.box(t, midX, floor - 0.03D, zc, spanX, 0.035D, 0.45D);

        // Бічні поручні перехідного містка
        double railTop = floor + 1.05D;
        double railMid = floor + 0.52D;
        for (int s = -1; s <= 1; s += 2) {
            double zz = zc + s * 0.42D;
            LepGeo.beam(t, topLadderX, floor, zc + s * halfW, topLadderX, railTop, zz, 0.045D);
            LepGeo.beam(t, topLadderX, railTop, zz, walkOuterX, railTop, zz, 0.045D);
            LepGeo.beam(t, topLadderX, railMid, zz, walkOuterX, railMid, zz, 0.035D);
        }
    }

    /**
     * Міст крана ККС:
     * - Тригранна ферма закінчується на +-zEnd (не заходячи в балкон).
     * - Двотаврова рейка візка іде майже до самого кінця ферми.
     * - Торцеві балкони винесені назовні за межі ферми.
     * - Передня виносна галерея йде між балконами, а її підкоси оминають габарит візка.
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
     * Кронштейни кріпляться строго до переднього нижнього поясу ферми (+BASE_X) і зовнішнього краю
     * верхньої нерухомої полиці балки (збоку від габариту візка), не заважаючи руху візка.
     */
    private static void frontWalkway(Tessellator t, double yBot, double floor, double balcIn, double ladderZ) {
        double railTop = floor + 1.05D;
        double railMid = floor + 0.52D;
        double outerX = WALKWAY_X + WALKWAY_HALF_W;
        double innerX = BASE_X;
        double braceAnchorX = TRACK_FLANGE_HALF + 0.30D;
        double braceAnchorY = yBot - 0.03D;

        LepGeo.box(t, WALKWAY_X, floor - 0.03D, 0.0D, WALKWAY_HALF_W, 0.035D, balcIn);

        int count = Math.max(4, (int) Math.round((2.0D * balcIn) / 2.2D));
        double dz = (2.0D * balcIn) / count;
        double gateMin = ladderZ - 0.42D;
        double gateMax = ladderZ + 0.42D;

        for (int i = 0; i <= count; i++) {
            double z = -balcIn + i * dz;
            LepGeo.beam(t, innerX, floor - 0.06D, z, outerX, floor - 0.06D, z, 0.06D);
            LepGeo.beam(t, braceAnchorX, braceAnchorY, z, outerX - 0.06D, floor - 0.06D, z, 0.05D);

            if (z <= gateMin || z >= gateMax) {
                LepGeo.beam(t, outerX, floor, z, outerX, railTop, z, 0.045D);
            }
        }

        LepGeo.beam(t, outerX, floor, gateMin, outerX, railTop, gateMin, 0.045D);
        LepGeo.beam(t, outerX, floor, gateMax, outerX, railTop, gateMax, 0.045D);

        LepGeo.beam(t, outerX, railTop, -balcIn, outerX, railTop, gateMin, 0.05D);
        LepGeo.beam(t, outerX, railMid, -balcIn, outerX, railMid, gateMin, 0.04D);

        LepGeo.beam(t, outerX, railTop, gateMax, outerX, railTop, balcIn, 0.05D);
        LepGeo.beam(t, outerX, railMid, gateMax, outerX, railMid, balcIn, 0.04D);
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

    /**
     * Електрична шафа керування краном: стоїть на лівому торцевому балконі (-z, біля гнучкої опори)
     * на рівні підлоги проходу, біля глухої стінки, щоб не заважати проходу з галереї.
     */
    private static void buildCabinet(int height, int span) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double gauge = span * 0.5D;
        double yBot = height - TileEntityCraneController.bridgeDepth(span);
        double floor = floorY(yBot);
        double zEnd = gauge + TileEntityCraneController.cantilever(span);

        // Координати на лівому балконі (-zEnd .. -(zEnd + BALCONY_DEPTH)), зміщено в бік -X від входу з галереї
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