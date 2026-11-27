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
 * Козловий кран із ґратчастим мостом за кресленням і фото-референсом (синій кран на колії).
 *
 * Опори це плоскі А-подібні ґратчасті рами. Вид уздовж моста (торець): два товсті похилі стояки, широкі
 * внизу (там колісні візки на рейці) і зведені вгору під міст, знизу міцна опорна балка, а на жорсткій
 * опорі ще похила драбина із захисною кліткою. Вид уздовж колії (фронт): жорстка опора це вузька колона,
 * що трохи розширюється вгору й має густу ґратку з кабіною під мостом, а гнучка опора це трикутник вістрям
 * донизу (шарнір на рейці), що розходиться під міст великими діагоналями.
 *
 * Міст це повноцінна просторова ферма: два вертикальні пояси (верхній і нижній) із трикутною ґраткою
 * та стійками, поперечні в'язі, з боків пішохідні настили з поручнями, на кінцях консолей підвісні
 * майданчики-клітки. Візок їздить по верхніх поясах над настилом, а лебідка висить усередині ферми, і трос
 * з гаком іде вниз крізь відкритий низ ферми, тож візок вільно проїжджає над опорами.
 *
 * Локальні осі: X це напрям колії (кран їде вздовж X), Z це поперечна вісь моста, Y угору. Початок координат
 * у центрі клітинки контролера на рівні низу рейкового блока. Жорстка опора стоїть на +gauge, гнучка на -gauge.
 *
 * Колір: текстура металу майже біла, а колір фарби накладається через glColor, тому кран можна фарбувати
 * в будь-який колір. Темні деталі (візки, колеса, трос, гак) і кабіна не фарбуються.
 *
 * Нерухома частина компілюється в display list для кожної пари (висота, відстань між рейками), три списки:
 * фарбований метал, темні деталі й кабіна. Візок, трос і гак малюються щокадру, бо рухаються.
 */
@SideOnly(Side.CLIENT)
public class ModelGantryCrane {

    private static final ResourceLocation PAINT = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_paint.png");
    private static final ResourceLocation DARK = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_dark.png");
    private static final ResourceLocation CABIN = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/crane_cabin.png");

    private static final double RAIL_TOP = BlockCraneRail.HEIGHT;

    /** Низ ґратчастої частини опори: над візками й опорною балкою. */
    private static final double LEG_BASE = RAIL_TOP + 0.65D;

    // жорстка опора (з кабіною й драбиною): основа вздовж колії, основа впоперек, верх вздовж, верх впоперек
    private static final double RIGID_FOOT_X = 2.3D;
    private static final double RIGID_FOOT_Z = 0.7D;
    private static final double RIGID_TOP_X = 0.7D;
    private static final double RIGID_TOP_Z = 1.3D;

    // гнучка опора: вістря донизу (шарнір), широка вгорі під мостом
    private static final double FLEX_FOOT_X = 2.0D;
    private static final double FLEX_FOOT_Z = 0.2D;
    private static final double FLEX_TOP_X = 0.7D;
    private static final double FLEX_TOP_Z = 1.9D;

    /** Відстань від осі моста до кожної вертикальної ферми (поясу). */
    private static final double CHORD_X = 0.7D;
    /** Півширина пішохідного настилу від осі поясу назовні та положення поручнів. */
    private static final double WALK_IN = 0.8D;
    private static final double WALK_OUT = 1.35D;

    /** key (height, span) -> перший id із трьох display list. */
    private final Map<Integer, Integer> lists = new HashMap<Integer, Integer>();

    private static int key(int height, int span) {
        return height * 64 + span;
    }

    /**
     * @param height висота верху моста над низом рейок
     * @param span відстань між осями рейок
     * @param trolleyZ положення візка поперек колії
     * @param hookH довжина троса від низу моста до верху гака
     * @param r червона, зелена, синя складові кольору фарби (0..1)
     */
    public void render(int height, int span, double trolleyZ, double hookH, float r, float g, float b) {
        Minecraft mc = Minecraft.getMinecraft();
        Integer id = lists.get(key(height, span));
        if (id == null) {
            int base = GLAllocation.generateDisplayLists(3);
            GL11.glNewList(base, GL11.GL_COMPILE);
            buildPainted(height, span);
            GL11.glEndList();
            GL11.glNewList(base + 1, GL11.GL_COMPILE);
            buildDark(span);
            GL11.glEndList();
            GL11.glNewList(base + 2, GL11.GL_COMPILE);
            buildCabin(height, span);
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
    }

    // ------------------------------------------------------------ фарбований метал: опори, драбина, міст

    private static void buildPainted(int height, int span) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double gauge = span * 0.5D; // половина відстані між рейками: опори стоять на +-gauge від осі

        double depth = TileEntityCraneController.bridgeDepth(span);
        double yBot = height - depth;
        double yTop = height;
        double half = gauge + TileEntityCraneController.cantilever(span);

        // жорстка опора на +gauge: вузька колона, густа ґратка, драбина й кабіна
        leg(t, gauge, RIGID_FOOT_X, RIGID_FOOT_Z, RIGID_TOP_X, RIGID_TOP_Z, LEG_BASE, yBot, 1.2D, 3.6D, 0.3D);
        ladder(t, gauge, RIGID_FOOT_X, RIGID_TOP_X, LEG_BASE, yBot - 1.0D, yBot);
        // майданчик на верху опори, звідки драбина виходить до настилу моста
        LepGeo.box(t, 0.0D, yBot - 1.0D, gauge, RIGID_TOP_X + 0.5D, 0.05D, RIGID_TOP_Z + 0.2D);
        // кронштейн кабіни від опори до майданчика кабіни
        double cabZ = gauge - (RIGID_TOP_Z + 1.1D);
        LepGeo.box(t, 0.0D, yBot - 2.0D, cabZ + 0.2D, 0.9D, 0.06D, 1.0D);
        LepGeo.beam(t, 0.0D, yBot - 2.0D, gauge - RIGID_TOP_Z, 0.0D, yBot - 0.4D, gauge - RIGID_TOP_Z + 0.2D, 0.12D);

        // гнучка опора на -gauge: трикутник вістрям донизу, великі діагоналі
        leg(t, -gauge, FLEX_FOOT_X, FLEX_FOOT_Z, FLEX_TOP_X, FLEX_TOP_Z, LEG_BASE, yBot, 2.6D, 4.2D, 0.26D);

        bridge(t, gauge, yBot, yTop, half);

        t.draw();
    }

    /**
     * Плоска А-подібна ґратчаста опора (усічена піраміда). Чотири товсті похилі стояки йдуть від кутів основи
     * (foot) до кутів верху (top). Грані, перпендикулярні до X, це ґратка зигзагом уздовж моста (її видно
     * з фронту). Грані, перпендикулярні до Z, це А-рама з хрестовими в'язями (її видно з торця). Знизу
     * опорна балка уздовж колії над візками, зверху голова під мостом. zc це положення осі опори поперек
     * колії, а по X опора симетрична відносно нуля.
     *
     * @param panelZ висота панелі ґратки на гранях X (видно з фронту)
     * @param panelX висота панелі А-рами на гранях Z (видно з торця)
     */
    private static void leg(Tessellator t, double zc, double footX, double footZ, double topX, double topZ,
                            double base, double yTop, double panelZ, double panelX, double colTh) {
        int[][] sg = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };
        double h = yTop - base;

        // товсті стояки
        for (int i = 0; i < 4; i++) {
            LepGeo.beam(t, sg[i][0] * footX, base, zc + sg[i][1] * footZ, sg[i][0] * topX, yTop, zc + sg[i][1] * topZ, colTh);
        }

        // грані X (бічні, ґратка зигзагом): видно з фронту
        int nz = Math.max(3, (int) Math.round(h / panelZ));
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int lv = 0; lv <= nz; lv++) {
                double f = (double) lv / nz;
                double y = base + h * f;
                double hx = lerp(footX, topX, f);
                double hz = lerp(footZ, topZ, f);
                LepGeo.beam(t, sx * hx, y, zc - hz, sx * hx, y, zc + hz, 0.12D);
                if (lv < nz) {
                    double f2 = (double) (lv + 1) / nz;
                    double y2 = base + h * f2;
                    double hx2 = lerp(footX, topX, f2);
                    double hz2 = lerp(footZ, topZ, f2);
                    boolean flip = (lv & 1) == 0;
                    double za = flip ? -hz : hz;
                    double zb = flip ? hz2 : -hz2;
                    LepGeo.beam(t, sx * hx, y, zc + za, sx * hx2, y2, zc + zb, 0.1D);
                }
            }
        }

        // грані Z (торцеві, А-рама з хрестовими в'язями): видно з торця
        int nx = Math.max(2, (int) Math.round(h / panelX));
        for (int sz = -1; sz <= 1; sz += 2) {
            for (int lv = 0; lv <= nx; lv++) {
                double f = (double) lv / nx;
                double y = base + h * f;
                double hx = lerp(footX, topX, f);
                double hz = lerp(footZ, topZ, f);
                LepGeo.beam(t, -hx, y, zc + sz * hz, hx, y, zc + sz * hz, 0.12D);
                if (lv < nx) {
                    double f2 = (double) (lv + 1) / nx;
                    double y2 = base + h * f2;
                    double hx2 = lerp(footX, topX, f2);
                    double hz2 = lerp(footZ, topZ, f2);
                    LepGeo.beam(t, -hx, y, zc + sz * hz, hx2, y2, zc + sz * hz2, 0.1D);
                    LepGeo.beam(t, hx, y, zc + sz * hz, -hx2, y2, zc + sz * hz2, 0.1D);
                }
            }
        }

        // опорна балка над візками (вздовж колії) й голова під мостом
        for (int sz = -1; sz <= 1; sz += 2) {
            LepGeo.box(t, 0.0D, base - 0.1D, zc + sz * footZ, footX + 0.15D, 0.1D, 0.13D);
        }
        LepGeo.box(t, 0.0D, yTop - 0.12D, zc, topX + 0.35D, 0.12D, topZ + 0.3D);
    }

    /**
     * Похила драбина вздовж зовнішньої (+X) грані жорсткої опори від основи до майданчика, далі
     * вертикальний марш до настилу моста. Лінія драбини повторює нахил стояка.
     */
    private static void ladder(Tessellator t, double zc, double footX, double topX, double base, double top, double yBot) {
        double out = 0.3D;
        double h = yBot - base;
        double x0 = footX + out;
        double x1 = lerp(footX, topX, (top - base) / h) + out;
        LepGeo.beam(t, x0, base, zc - 0.3D, x1, top, zc - 0.3D, 0.07D);
        LepGeo.beam(t, x0, base, zc + 0.3D, x1, top, zc + 0.3D, 0.07D);
        double len = top - base;
        for (double y = base + 0.4D; y < top; y += 0.5D) {
            double x = lerp(x0, x1, (y - base) / len);
            LepGeo.beam(t, x, y, zc - 0.3D, x, y, zc + 0.3D, 0.05D);
        }
        // кільця захисної клітки на верхній третині
        for (double y = base + len * 0.65D; y < top; y += 1.0D) {
            double x = lerp(x0, x1, (y - base) / len);
            LepGeo.beam(t, x, y, zc - 0.45D, x + 0.3D, y, zc - 0.45D, 0.04D);
            LepGeo.beam(t, x + 0.3D, y, zc - 0.45D, x + 0.3D, y, zc + 0.45D, 0.04D);
            LepGeo.beam(t, x + 0.3D, y, zc + 0.45D, x, y, zc + 0.45D, 0.04D);
        }
        // перила майданчика
        double px = RIGID_TOP_X + 1.0D;
        LepGeo.beam(t, px, top + 0.9D, zc - RIGID_TOP_Z - 0.2D, px, top + 0.9D, zc + RIGID_TOP_Z + 0.2D, 0.05D);
    }

    /**
     * Міст: просторова ферма. Дві вертикальні ферми (на +-CHORD_X) із верхнім і нижнім поясом, трикутною
     * ґраткою й стійками, поперечні в'язі зверху на кожному другому вузлі та знизу лише над опорами й на кінцях
     * (низ відкритий для троса), бічні настили з поручнями, підвісні майданчики-клітки на кінцях консолей.
     */
    private static void bridge(Tessellator t, double gauge, double yBot, double yTop, double half) {
        double yt = yTop - 0.1D;
        double yb = yBot + 0.1D;
        double depth = yt - yb;

        int segs = Math.max(8, (int) Math.round(2.0D * half / Math.max(1.0D, depth)));
        if ((segs & 1) == 1) segs++;
        double step = 2.0D * half / segs;

        for (int p = -1; p <= 1; p += 2) {
            double px = p * CHORD_X;
            LepGeo.beam(t, px, yt, -half, px, yt, half, 0.24D);
            LepGeo.beam(t, px, yb, -half, px, yb, half, 0.22D);
            for (int i = 0; i < segs; i++) {
                boolean up = (i & 1) == 0;
                double za = -half + i * step;
                double zb = za + step;
                LepGeo.beam(t, px, up ? yb : yt, za, px, up ? yt : yb, zb, 0.1D);
            }
            for (int i = 0; i <= segs; i++) {
                double z = -half + i * step;
                LepGeo.beam(t, px, yb, z, px, yt, z, 0.09D);
            }
        }
        // верхні поперечні в'язі
        for (int i = 0; i <= segs; i += 2) {
            double z = -half + i * step;
            LepGeo.beam(t, -CHORD_X, yt, z, CHORD_X, yt, z, 0.1D);
        }
        // нижні в'язі лише на кінцях і над опорами
        double[] zs = { -half, -gauge, gauge, half };
        for (int k = 0; k < zs.length; k++) {
            LepGeo.beam(t, -CHORD_X, yb, zs[k], CHORD_X, yb, zs[k], 0.1D);
        }

        // бічні настили й поручні
        double deck = yTop + 0.04D;
        double wMid = (WALK_IN + WALK_OUT) * 0.5D;
        double wHalf = (WALK_OUT - WALK_IN) * 0.5D;
        for (int p = -1; p <= 1; p += 2) {
            LepGeo.box(t, p * wMid, deck, 0.0D, wHalf, 0.025D, half);
            // кронштейни під настилом до верхнього поясу
            for (int i = 0; i <= segs; i += 2) {
                double z = -half + i * step;
                LepGeo.beam(t, p * CHORD_X, yt, z, p * WALK_OUT, deck - 0.03D, z, 0.06D);
            }
            double rx = p * WALK_OUT;
            LepGeo.beam(t, rx, deck + 1.0D, -half, rx, deck + 1.0D, half, 0.05D);
            LepGeo.beam(t, rx, deck + 0.5D, -half, rx, deck + 0.5D, half, 0.04D);
            for (int i = 0; i <= segs; i += 2) {
                double z = -half + i * step;
                LepGeo.beam(t, rx, deck, z, rx, deck + 1.0D, z, 0.05D);
            }
        }
        // торцеві поручні
        for (int e = -1; e <= 1; e += 2) {
            LepGeo.beam(t, -WALK_OUT, deck + 1.0D, e * half, WALK_OUT, deck + 1.0D, e * half, 0.05D);
        }

        // підвісні майданчики-клітки на кінцях консолей
        for (int e = -1; e <= 1; e += 2) {
            double ez = e * (half - 0.9D);
            double fy = yBot - 1.0D;
            double fx = 1.1D;
            double fz = 0.9D;
            LepGeo.box(t, 0.0D, fy, ez, fx, 0.04D, fz);
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    // підвіска до нижнього поясу й стійка поручня
                    LepGeo.beam(t, sx * CHORD_X, fy, ez + sz * (fz - 0.05D), sx * CHORD_X, yb, ez + sz * (fz - 0.05D), 0.07D);
                    LepGeo.beam(t, sx * (fx - 0.05D), fy, ez + sz * (fz - 0.05D), sx * (fx - 0.05D), fy + 1.0D, ez + sz * (fz - 0.05D), 0.05D);
                }
                LepGeo.beam(t, sx * (fx - 0.05D), fy + 1.0D, ez - fz, sx * (fx - 0.05D), fy + 1.0D, ez + fz, 0.05D);
            }
            LepGeo.beam(t, -fx, fy + 1.0D, ez + e * (fz - 0.05D), fx, fy + 1.0D, ez + e * (fz - 0.05D), 0.05D);
            LepGeo.beam(t, -fx, fy + 1.0D, ez - e * (fz - 0.05D), fx, fy + 1.0D, ez - e * (fz - 0.05D), 0.05D);
        }
    }

    private static double lerp(double a, double b, double f) {
        return a + (b - a) * f;
    }

    // ------------------------------------------------------------ темні деталі: візки з колесами на рейках

    private static void buildDark(int span) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double gauge = span * 0.5D;
        for (int sx = -1; sx <= 1; sx += 2) {
            bogie(t, sx * (RIGID_FOOT_X - 0.55D), gauge);
            bogie(t, sx * (FLEX_FOOT_X - 0.55D), -gauge);
        }
        t.draw();
    }

    /** Візок на рейці: рама з двома колесами (кожне колесо це два хрестоподібні паралелепіпеди, схожі на восьмикутник). */
    private static void bogie(Tessellator t, double cx, double zc) {
        LepGeo.box(t, cx, RAIL_TOP + 0.5D, zc, 0.8D, 0.1D, 0.3D);
        for (int w = -1; w <= 1; w += 2) {
            double x = cx + w * 0.45D;
            double y = RAIL_TOP + 0.2D;
            LepGeo.box(t, x, y, zc, 0.2D, 0.14D, 0.12D);
            LepGeo.box(t, x, y, zc, 0.14D, 0.2D, 0.12D);
            // кронштейн букси
            LepGeo.box(t, x, RAIL_TOP + 0.38D, zc, 0.08D, 0.1D, 0.2D);
        }
    }

    // ------------------------------------------------------------ кабіна

    private static void buildCabin(int height, int span) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double gauge = span * 0.5D;
        double yBot = height - TileEntityCraneController.bridgeDepth(span);
        double floor = yBot - 2.0D + 0.06D;
        double cabZ = gauge - (RIGID_TOP_Z + 1.1D);
        LepGeo.box(t, 0.0D, floor + 0.7D, cabZ, 0.75D, 0.7D, 0.8D);
        t.draw();
    }

    // ------------------------------------------------------------ рухомі частини

    /**
     * Візок: рама на верхніх поясах із чотирма ходовими колесами, привід зверху, лебідка всередині ферми
     * (підвішена на двох підвісках), тож трос і гак виходять через відкритий низ моста.
     */
    private static void drawTrolley(int height, int span, double tz) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double yTop = height;
        // ходові колеса на верхніх поясах
        for (int p = -1; p <= 1; p += 2) {
            for (int q = -1; q <= 1; q += 2) {
                LepGeo.box(t, p * CHORD_X, yTop + 0.12D, tz + q * 0.4D, 0.16D, 0.1D, 0.1D);
            }
        }
        // рама й привід
        LepGeo.box(t, 0.0D, yTop + 0.3D, tz, 0.95D, 0.08D, 0.55D);
        LepGeo.box(t, 0.0D, yTop + 0.58D, tz, 0.45D, 0.2D, 0.35D);
        // підвіски й корпус лебідки всередині ферми
        for (int p = -1; p <= 1; p += 2) {
            LepGeo.beam(t, p * 0.45D, yTop + 0.3D, tz - 0.4D, p * 0.45D, yTop - 0.4D, tz - 0.4D, 0.1D);
            LepGeo.beam(t, p * 0.45D, yTop + 0.3D, tz + 0.4D, p * 0.45D, yTop - 0.4D, tz + 0.4D, 0.1D);
        }
        LepGeo.box(t, 0.0D, yTop - 0.62D, tz, 0.5D, 0.35D, 0.45D);
        t.draw();
    }

    private static void drawHook(int height, int span, double tz, double hookH) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        double yBot = height - TileEntityCraneController.bridgeDepth(span);
        double hookTop = yBot - hookH;
        double cableTop = height - 0.97D;
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
