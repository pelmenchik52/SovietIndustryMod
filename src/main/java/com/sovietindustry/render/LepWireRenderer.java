package com.sovietindustry.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.hbm.tileentity.network.TileEntityPylonBase;
import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.api.IBundledPylon;
import com.sovietindustry.api.IGroundWired;
import com.sovietindustry.api.IJumperPylon;
import com.sovietindustry.api.IMultiGroundWired;
import com.sovietindustry.api.IWiredPylon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Малює для опори: натяжні гірлянди (динамічні), шлейфи між ними та самі проводи з провисанням.
 * Усі координати відносно кута блока (xCoord, yCoord, zCoord), тому достатньо translate(x, y, z).
 *
 * Два випадки для кожного дроту:
 *  - сусід теж наша опора (IWiredPylon): повний дріт малює лише "менша" за координатами опора;
 *  - сусід з NTM: NTM сам малює свою половину до середини прольоту, тому ми малюємо лише
 *    свою половину тією самою кривою (sin), інакше був би подвійний дріт.
 *
 * Гірлянда повертається вздовж дотичної до дроту у СВОЇЙ точці кріплення, тому дивиться вниз,
 * якщо дріт іде нижче, вгору, якщо вище, і вбік на рівному прольоті.
 */
@SideOnly(Side.CLIENT)
public final class LepWireRenderer {
    private static final ResourceLocation INSULATOR =
        new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep_insulator.png");

    /** Стріла провисання власного дроту як частка довжини прольоту. */
    private static final double SAG = 0.03D;
    private static final double WIRE_HALF = 0.02D;
    private static final double GROUND_HALF = 0.014D;
    /** Роздвоєні натяжні гірлянди сходяться до точки кріплення: біля опори розстань це частка розстані на кінці. */
    private static final double FORK_ROOT = 0.75D;
    /** Радіус кружка-розпірки на кінці гірлянди (як на простій опорі). */
    private static final double RING_R = 0.22D;
    private static final ResourceLocation ATLAS =
        new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep110_single.png");
    private static final double[] A_STEEL = { 64.0D, 4.0D, 80.0D, 12.0D };
    private static final double[] A_STEEL_DARK = { 0.0D, 84.0D, 16.0D, 92.0D };
    private static final double[] A_CAP = { 96.0D, 48.0D, 112.0D, 56.0D };
    private static final double ATLAS_SIZE = 128.0D;

    private LepWireRenderer() {
    }

    private static final class Link {
        /** Кінець гірлянди (або точка кріплення, якщо гірлянд нема) на нашій опорі, за номером запису. */
        final double[][] mine;
        /** Те саме на другій опорі (для наших опор) або середина прольоту (для опор NTM). */
        final double[][] other;
        /** Індекс точки кріплення на нашій опорі для кожного запису. */
        final int[] mi;
        boolean draw;
        boolean ntm;
        /** Вісь пучка на нашій опорі й на сусідній (null, якщо опора без пучків). */
        double[] pm;
        double[] po;

        Link(int n) {
            mine = new double[n][];
            other = new double[n][];
            mi = new int[n];
        }
    }

    private static boolean isLower(TileEntity a, TileEntity b) {
        if (a.xCoord != b.xCoord) return a.xCoord < b.xCoord;
        if (a.yCoord != b.yCoord) return a.yCoord < b.yCoord;
        return a.zCoord < b.zCoord;
    }

    /**
     * Перевпорядковує точки кріплення двох наших опор для одного прольоту. Вісь "поперек" береться за канонічним
     * напрямком (від нижчої опори до вищої за координатами), тому обидві опори, що малюють свою частину дроту,
     * отримують однакову пару, а перестановка після повороту прольоту дає ті самі фізичні пари.
     */
    private static int[][] pairByPosition(TileEntity te, Vec3[] mine, int[] mi, TileEntity o, Vec3[] om, int[] oi) {
        boolean lower = isLower(te, o);
        TileEntity a = lower ? te : o;
        TileEntity b = lower ? o : te;
        double sx = b.xCoord - a.xCoord, sz = b.zCoord - a.zCoord;
        double sl = Math.sqrt(sx * sx + sz * sz);
        if (sl < 1.0E-6D) return new int[][] { mi, oi };
        double ux = -sz / sl, uz = sx / sl; // поперек прольоту (горизонтально)

        Vec3[] ma = lower ? mine : om;
        Vec3[] mb = lower ? om : mine;
        int[] ia = lower ? mi : oi;
        int[] ib = lower ? oi : mi;

        double[] la = new double[ia.length], ya = new double[ia.length];
        double[] lb = new double[ib.length], yb = new double[ib.length];
        for (int i = 0; i < ia.length; i++) {
            if (ia[i] >= ma.length) return new int[][] { mi, oi };
            la[i] = ma[ia[i]].xCoord * ux + ma[ia[i]].zCoord * uz;
            ya[i] = ma[ia[i]].yCoord;
        }
        for (int i = 0; i < ib.length; i++) {
            if (ib[i] >= mb.length) return new int[][] { mi, oi };
            lb[i] = mb[ib[i]].xCoord * ux + mb[ib[i]].zCoord * uz;
            yb[i] = mb[ib[i]].yCoord;
        }
        int[][] r = MountMatcher.reorder(ia, la, ya, ib, lb, yb);
        return lower ? new int[][] { r[0], r[1] } : new int[][] { r[1], r[0] };
    }

    private static double[] unit(double x, double y, double z) {
        double l = Math.sqrt(x * x + y * y + z * z);
        if (l < 1.0E-6D) return new double[] { 0.0D, -1.0D, 0.0D };
        return new double[] { x / l, y / l, z / l };
    }

    private static int[] all(int n) {
        int[] r = new int[n];
        for (int i = 0; i < n; i++) r[i] = i;
        return r;
    }

    public static void render(TileEntityPylonBase te, double x, double y, double z) {
        if (!(te instanceof IWiredPylon)) return;
        IWiredPylon me = (IWiredPylon) te;
        World world = te.getWorldObj();
        if (world == null) return;
        List<int[]> links = me.getLinks();
        if (links.isEmpty()) return;

        Vec3[] mine = te.getMountPos();
        boolean anchor = me.hasAnchorStrings();
        double myStr = me.getStringLength();
        List<Link> list = new ArrayList<Link>();

        for (int[] p : links) {
            TileEntity o = world.getTileEntity(p[0], p[1], p[2]);
            if (!(o instanceof TileEntityPylonBase)) continue;
            Vec3[] om = ((TileEntityPylonBase) o).getMountPos();
            if (om == null || om.length == 0) continue;

            boolean ours = o instanceof IWiredPylon;
            IWiredPylon oth = ours ? (IWiredPylon) o : null;
            boolean otherAnchor = ours && oth.hasAnchorStrings();

            // які точки кріплення використовує саме цей дріт (6-контактна АКО ділить їх на два кола)
            int[] mi = me.getLinkMounts(p[0], p[1], p[2]);
            if (mi == null) mi = all(mine.length);
            int[] oi = ours ? oth.getLinkMounts(te.xCoord, te.yCoord, te.zCoord) : null;
            if (oi == null) oi = all(om.length);

            // дроти між нашими опорами зіставляються за ПОЛОЖЕННЯМ точок поперек прольоту й по висоті, а не за номером,
            // тому вони не перехрещуються, хоч як опори повернуті одна до одної. Для опор NTM номер лишається:
            // свою половину дроту NTM малює сам за власним порядком.
            if (ours && mi.length == oi.length && mi.length > 1) {
                int[][] ord = pairByPosition(te, mine, mi, o, om, oi);
                mi = ord[0];
                oi = ord[1];
            }

            int n = Math.min(mi.length, oi.length);
            double ox = o.xCoord - te.xCoord;
            double oy = o.yCoord - te.yCoord;
            double oz = o.zCoord - te.zCoord;

            Link l = new Link(n);
            if (te instanceof IBundledPylon) {
                l.pm = ((IBundledPylon) te).getBundleAxis(ox, oz);
            }
            if (o instanceof IBundledPylon) {
                l.po = ((IBundledPylon) o).getBundleAxis(-ox, -oz);
            }
            l.ntm = !ours;
            l.draw = !ours || isLower(te, o);
            for (int i = 0; i < n; i++) {
                if (mi[i] >= mine.length || oi[i] >= om.length) {
                    l.mine[i] = null;
                    continue;
                }
                Vec3 m = mine[mi[i]];
                Vec3 q = om[oi[i]];
                l.mi[i] = mi[i];
                double bx = ox + q.xCoord, by = oy + q.yCoord, bz = oz + q.zCoord;
                double dx = bx - m.xCoord, dy = by - m.yCoord, dz = bz - m.zCoord;
                double len = Math.sqrt(dx * dx + dy * dy + dz * dz);

                double[] d;
                if (ours) {
                    // дотична параболи на початку: нахил прольоту мінус провисання
                    d = unit(dx, dy - 4.0D * SAG * len, dz);
                } else {
                    // дотична половини NTM-дроту: sin-крива з провисанням hang на середині
                    double half = len * 0.5D;
                    double hang = Math.min(half / 15.0D, 2.5D);
                    d = unit(dx * 0.5D, dy * 0.5D - hang * Math.PI * 0.5D, dz * 0.5D);
                }
                l.mine[i] = anchor
                    ? new double[] { m.xCoord + d[0] * myStr, m.yCoord + d[1] * myStr, m.zCoord + d[2] * myStr }
                    : new double[] { m.xCoord, m.yCoord, m.zCoord };

                if (ours) {
                    double[] e = otherAnchor ? unit(-dx, -dy - 4.0D * SAG * len, -dz) : null;
                    double s = otherAnchor ? oth.getStringLength() : 0.0D;
                    l.other[i] = otherAnchor
                        ? new double[] { bx + e[0] * s, by + e[1] * s, bz + e[2] * s }
                        : new double[] { bx, by, bz };
                } else {
                    // середина між точками кріплення, там зустрічається половина NTM
                    l.other[i] = new double[] { m.xCoord + dx * 0.5D, m.yCoord + dy * 0.5D, m.zCoord + dz * 0.5D };
                }
            }
            list.add(l);
        }

        Vec3[] ground = groundMounts(te);
        if (list.isEmpty() && ground == null) return;
        IBundledPylon bun = te instanceof IBundledPylon ? (IBundledPylon) te : null;

        // верхні гірлянди й шлейф з'являються лише тоді, коли до цієї фази підключено дріт
        IJumperPylon jump = te instanceof IJumperPylon ? (IJumperPylon) te : null;
        int jm = jump != null ? jump.getJumperMount() : -1;
        boolean jumperUsed = false;
        if (jump != null && bun != null) {
            for (Link l : list) {
                for (int i = 0; i < l.mine.length; i++) {
                    if (l.mine[i] != null && l.mi[i] == jm && l.pm != null) jumperUsed = true;
                }
            }
        }

        Tessellator t = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glDisable(GL11.GL_CULL_FACE);

        // --- гірлянди
        if (anchor && !list.isEmpty()) {
            Minecraft.getMinecraft().renderEngine.bindTexture(INSULATOR);
            t.startDrawingQuads();
            for (Link l : list) {
                for (int i = 0; i < l.mine.length; i++) {
                    if (l.mine[i] == null) continue;
                    Vec3 m = mine[l.mi[i]];
                    double[] base = { m.xCoord, m.yCoord, m.zCoord };
                    if (bun != null && l.pm != null) {
                        for (int si = 0; si < bun.getStringsPerMount(); si++) {
                            double o = stringOffset(bun, si);
                            bundledString(t, shift(base, l.pm, o * FORK_ROOT), shift(l.mine[i], l.pm, o));
                        }
                    } else {
                        string(t, base, l.mine[i]);
                    }
                }
            }
            if (jumperUsed) {
                for (Vec3[] js : jump.getJumperStrings()) {
                    hangString(t, new double[] { js[0].xCoord, js[0].yCoord, js[0].zCoord },
                                  new double[] { js[1].xCoord, js[1].yCoord, js[1].zCoord });
                }
            }
            t.draw();

            // один сталевий овальний кружок на обидві гірлянди пари (як на фото) із хрестом і затискачами
            if (bun != null) {
                Minecraft.getMinecraft().renderEngine.bindTexture(ATLAS);
                t.startDrawingQuads();
                for (Link l : list) {
                    for (int i = 0; i < l.mine.length; i++) {
                        if (l.mine[i] == null || l.pm == null) continue;
                        Vec3 m = mine[l.mi[i]];
                        double[] u = unit(l.mine[i][0] - m.xCoord, l.mine[i][1] - m.yCoord, l.mine[i][2] - m.zCoord);
                        double[][] ends = new double[bun.getStringsPerMount()][];
                        for (int si = 0; si < ends.length; si++) {
                            ends[si] = shift(l.mine[i], l.pm, stringOffset(bun, si));
                        }
                        endFitting(t, ends, u);
                    }
                }
                if (jumperUsed) {
                    Vec3[][] jsAll = jump.getJumperStrings();
                    double[][] ends = new double[jsAll.length][];
                    for (int k = 0; k < jsAll.length; k++) {
                        ends[k] = new double[] { jsAll[k][1].xCoord, jsAll[k][1].yCoord, jsAll[k][1].zCoord };
                    }
                    double[] u = unit(jsAll[0][1].xCoord - jsAll[0][0].xCoord, jsAll[0][1].yCoord - jsAll[0][0].yCoord,
                                      jsAll[0][1].zCoord - jsAll[0][0].zCoord);
                    endFitting(t, ends, u);
                }
                t.draw();
            }
        }

        // --- проводи
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        t.startDrawingQuads();
        t.setColorOpaque_F(0.09F, 0.09F, 0.1F);
        for (Link l : list) {
            if (!l.draw) continue;
            for (int i = 0; i < l.mine.length; i++) {
                if (l.mine[i] == null) continue;
                if (bun != null && l.pm != null) {
                    // пучок: подвійна гірлянда x кілька дротів на гірлянді
                    double sign = (!l.ntm && l.po != null && dot(l.pm, l.po) < 0.0D) ? -1.0D : 1.0D;
                    for (int si = 0; si < bun.getStringsPerMount(); si++) {
                        for (int wi = 0; wi < bun.getWiresPerString(); wi++) {
                            double o = wireOffset(bun, si, wi);
                            double[] a = shift(l.mine[i], l.pm, o);
                            if (l.ntm) {
                                ntmHalf(t, a, l.other[i]);
                            } else {
                                double[] b = l.po != null ? shift(l.other[i], l.po, o * sign) : l.other[i];
                                wire(t, a, b, dist(a, b) * SAG, WIRE_HALF);
                            }
                        }
                    }
                } else if (l.ntm) {
                    ntmHalf(t, l.mine[i], l.other[i]);
                } else {
                    double len = dist(l.mine[i], l.other[i]);
                    wire(t, l.mine[i], l.other[i], len * SAG, WIRE_HALF);
                }
            }
        }
        // шлейфи між гірляндами однієї точки кріплення (наскрізна анкерна опора, розгалуження 6 -> 3 + 3)
        if (anchor) {
            Map<Integer, List<double[]>> perMount = new HashMap<Integer, List<double[]>>();
            Map<Integer, List<double[]>> perAxis = new HashMap<Integer, List<double[]>>();
            for (Link l : list) {
                for (int i = 0; i < l.mine.length; i++) {
                    if (l.mine[i] == null) continue;
                    List<double[]> ends = perMount.get(l.mi[i]);
                    List<double[]> axes = perAxis.get(l.mi[i]);
                    if (ends == null) {
                        ends = new ArrayList<double[]>();
                        axes = new ArrayList<double[]>();
                        perMount.put(l.mi[i], ends);
                        perAxis.put(l.mi[i], axes);
                    }
                    ends.add(l.mine[i]);
                    axes.add(l.pm);
                }
            }
            for (Map.Entry<Integer, List<double[]>> e : perMount.entrySet()) {
                if (jump != null && e.getKey().intValue() == jm) continue; // цей шлейф іде через верхні гірлянди
                List<double[]> ends = e.getValue();
                List<double[]> axes = perAxis.get(e.getKey());
                if (ends.size() < 2) continue;
                if (bun != null && axes.get(0) != null && axes.get(1) != null) {
                    double sign = dot(axes.get(0), axes.get(1)) < 0.0D ? -1.0D : 1.0D;
                    for (int si = 0; si < bun.getStringsPerMount(); si++) {
                        for (int wi = 0; wi < bun.getWiresPerString(); wi++) {
                            double o = wireOffset(bun, si, wi);
                            double[] a = shift(ends.get(0), axes.get(0), o);
                            double[] b = shift(ends.get(1), axes.get(1), o * sign);
                            wire(t, a, b, Math.min(0.45D, dist(a, b) * 0.3D), WIRE_HALF);
                        }
                    }
                } else {
                    double d = dist(ends.get(0), ends.get(1));
                    wire(t, ends.get(0), ends.get(1), Math.min(0.45D, d * 0.3D), WIRE_HALF);
                }
            }
        }

        // шлейф центральної фази: від кінця натяжної гірлянди вгору до верхньої гірлянди й з неї назад вниз
        if (jumperUsed) {
            Vec3[][] js = jump.getJumperStrings();
            double[] jax = jump.getJumperAxis();
            double[][] clamp = new double[js.length][];
            for (int k = 0; k < js.length; k++) {
                clamp[k] = new double[] { js[k][1].xCoord, js[k][1].yCoord, js[k][1].zCoord };
            }
            boolean[] used = new boolean[js.length];
            for (Link l : list) {
                for (int i = 0; i < l.mine.length; i++) {
                    if (l.mine[i] == null || l.mi[i] != jm || l.pm == null) continue;
                    int best = 0;
                    double bestD = Double.MAX_VALUE;
                    for (int k = 0; k < clamp.length; k++) {
                        double dd = dist(l.mine[i], clamp[k]);
                        if (dd < bestD) {
                            bestD = dd;
                            best = k;
                        }
                    }
                    used[best] = true;
                    for (int si = 0; si < bun.getStringsPerMount(); si++) {
                        double o = stringOffset(bun, si);
                        double[] a = shift(l.mine[i], l.pm, o);
                        double[] c = shift(clamp[best], jax, o);
                        wire(t, a, c, Math.min(0.5D, dist(a, c) * 0.08D), WIRE_HALF);
                    }
                }
            }
            // коли підключено обидві сторони, дріт переходить між верхніми гірляндами й іде назад
            if (clamp.length >= 2 && used[0] && used[1]) {
                for (int si = 0; si < bun.getStringsPerMount(); si++) {
                    double o = stringOffset(bun, si);
                    double[] a = shift(clamp[0], jax, o);
                    double[] c = shift(clamp[1], jax, o);
                    wire(t, a, c, Math.min(0.45D, dist(a, c) * 0.3D), WIRE_HALF);
                }
            }
        }

        // --- грозозахисний трос: лише між двома опорами з тросом, на кінці лінії обривається
        if (ground != null) {
            t.setColorOpaque_F(0.24F, 0.25F, 0.27F);
            for (int[] p : links) {
                TileEntity o = world.getTileEntity(p[0], p[1], p[2]);
                if (!(o instanceof IGroundWired) || !isLower(te, o)) continue;
                Vec3[] og = groundMounts(o);
                if (og == null) continue;
                double sx = o.xCoord - te.xCoord, sz = o.zCoord - te.zCoord;
                double sl = Math.sqrt(sx * sx + sz * sz);
                double ux = sl < 1.0E-6D ? 1.0D : -sz / sl;
                double uz = sl < 1.0E-6D ? 0.0D : sx / sl;
                double[] ka = new double[ground.length];
                double[] kb = new double[og.length];
                for (int i = 0; i < ground.length; i++) ka[i] = ground[i].xCoord * ux + ground[i].zCoord * uz;
                for (int i = 0; i < og.length; i++) {
                    kb[i] = (sx + og[i].xCoord) * ux + (sz + og[i].zCoord) * uz;
                }
                int[] ia = order(ka);
                int[] ib = order(kb);
                int cnt = Math.min(ia.length, ib.length);
                for (int k = 0; k < cnt; k++) {
                    Vec3 g1 = ground[ia[k]];
                    Vec3 g2 = og[ib[k]];
                    double[] a = { g1.xCoord, g1.yCoord, g1.zCoord };
                    double[] b = { o.xCoord - te.xCoord + g2.xCoord, o.yCoord - te.yCoord + g2.yCoord,
                                   o.zCoord - te.zCoord + g2.zCoord };
                    wire(t, a, b, dist(a, b) * SAG, GROUND_HALF);
                }
            }
        }
        t.draw();

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }


    // ------------------------------------------------------------ пучки й троси

    private static double stringOffset(IBundledPylon b, int si) {
        return (si - (b.getStringsPerMount() - 1) * 0.5D) * b.getStringSpacing();
    }

    private static double wireOffset(IBundledPylon b, int si, int wi) {
        return stringOffset(b, si) + (wi - (b.getWiresPerString() - 1) * 0.5D) * b.getWireSpacing();
    }

    private static double[] shift(double[] p, double[] axis, double o) {
        return new double[] { p[0] + axis[0] * o, p[1] + axis[1] * o, p[2] + axis[2] * o };
    }

    private static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    /** Усі точки тросів опори (одна або кілька); null, якщо троса нема. */
    private static Vec3[] groundMounts(TileEntity te) {
        if (te instanceof IMultiGroundWired) {
            Vec3[] m = ((IMultiGroundWired) te).getGroundMounts();
            return m == null || m.length == 0 ? null : m;
        }
        if (te instanceof IGroundWired) {
            Vec3 g = ((IGroundWired) te).getGroundMount();
            return g == null ? null : new Vec3[] { g };
        }
        return null;
    }

    /** Індекси масиву за зростанням значень (для зіставлення тросів двох опор). */
    private static int[] order(double[] k) {
        int[] r = new int[k.length];
        for (int i = 0; i < r.length; i++) r[i] = i;
        for (int i = 1; i < r.length; i++) {
            int v = r[i];
            int j = i - 1;
            while (j >= 0 && k[r[j]] > k[v]) {
                r[j + 1] = r[j];
                j--;
            }
            r[j + 1] = v;
        }
        return r;
    }

    /** Половина дроту до середини прольоту тією самою sin-кривою, що й у RenderPylonBase з NTM. */
    private static void ntmHalf(Tessellator t, double[] a, double[] mid) {
        double hx = mid[0] - a[0], hy = mid[1] - a[1], hz = mid[2] - a[2];
        double hang = Math.min(Math.sqrt(hx * hx + hy * hy + hz * hz) / 15.0D, 2.5D);
        int count = 10;
        double px = a[0], py = a[1], pz = a[2];
        for (int j = 1; j <= count; j++) {
            double f = (double) j / count;
            double cx = a[0] + hx * f;
            double cy = a[1] + hy * f - Math.sin(f * Math.PI * 0.5D) * hang;
            double cz = a[2] + hz * f;
            ribbon(t, px, py, pz, cx, cy, cz, WIRE_HALF);
            px = cx; py = cy; pz = cz;
        }
    }

    private static double dist(double[] a, double[] b) {
        double dx = b[0] - a[0], dy = b[1] - a[1], dz = b[2] - a[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** Натяжна гірлянда: стрижень, 6 тарілок і арматура на кінцях, вздовж a -> b. */
    private static void string(Tessellator t, double[] a, double[] b) {
        double len = dist(a, b);
        if (len < 1.0E-4D) return;
        double ux = (b[0] - a[0]) / len, uy = (b[1] - a[1]) / len, uz = (b[2] - a[2]) / len;

        LepGeo.beam(t, a[0], a[1], a[2], b[0], b[1], b[2], 0.07D);
        for (int k = 0; k < 6; k++) {
            double c = 0.22D + k * 0.17D;
            double s0 = c - 0.035D, s1 = c + 0.035D;
            LepGeo.beam(t, a[0] + ux * s0, a[1] + uy * s0, a[2] + uz * s0,
                           a[0] + ux * s1, a[1] + uy * s1, a[2] + uz * s1, 0.30D);
        }
        LepGeo.beam(t, a[0], a[1], a[2], a[0] + ux * 0.12D, a[1] + uy * 0.12D, a[2] + uz * 0.12D, 0.14D);
        LepGeo.beam(t, b[0] - ux * 0.10D, b[1] - uy * 0.10D, b[2] - uz * 0.10D, b[0], b[1], b[2], 0.14D);
    }

    /**
     * Натяжна гірлянда 330 кВ: стрижень, 8 тарілок рівномірно від початку до кінця (на 2 тарілки довша, ніж у 110 кВ)
     * і арматура на кінцях. Кружок на кінці малюється окремо (ring).
     */
    private static void bundledString(Tessellator t, double[] a, double[] b) {
        double len = dist(a, b);
        if (len < 1.0E-4D) return;
        double ux = (b[0] - a[0]) / len, uy = (b[1] - a[1]) / len, uz = (b[2] - a[2]) / len;

        LepGeo.beam(t, a[0], a[1], a[2], b[0], b[1], b[2], 0.07D);
        int n = 8;
        for (int k = 0; k < n; k++) {
            double c = 0.22D + k * ((len - 0.52D) / (n - 1));
            double s0 = c - 0.035D, s1 = c + 0.035D;
            LepGeo.beam(t, a[0] + ux * s0, a[1] + uy * s0, a[2] + uz * s0,
                           a[0] + ux * s1, a[1] + uy * s1, a[2] + uz * s1, 0.30D);
        }
        LepGeo.beam(t, a[0], a[1], a[2], a[0] + ux * 0.12D, a[1] + uy * 0.12D, a[2] + uz * 0.12D, 0.14D);
        LepGeo.beam(t, b[0] - ux * 0.10D, b[1] - uy * 0.10D, b[2] - uz * 0.10D, b[0], b[1], b[2], 0.14D);
    }

    /**
     * Кінцева арматура пари гірлянд, як на фото: ОДИН сталевий овальний кружок навколо кінців усіх гірлянд пари
     * (витягнутий уздовж лінії, що з'єднує кінці; площина перпендикулярна осі гірлянд u), зі спицями-хрестом,
     * і темний затискач за кінцем кожної гірлянди. Для однієї гірлянди кружок круглий.
     */
    private static void endFitting(Tessellator t, double[][] ends, double[] u) {
        int n = ends.length;
        double mx = 0.0D, my = 0.0D, mz = 0.0D;
        for (double[] e : ends) {
            mx += e[0] / n; my += e[1] / n; mz += e[2] / n;
        }

        // довга вісь овала: від першого кінця до останнього, або будь-яка перпендикулярна u для однієї гірлянди
        double ax, ay, az, rA;
        if (n > 1) {
            ax = ends[n - 1][0] - ends[0][0];
            ay = ends[n - 1][1] - ends[0][1];
            az = ends[n - 1][2] - ends[0][2];
            double l = Math.sqrt(ax * ax + ay * ay + az * az);
            if (l < 1.0E-6D) return;
            rA = l * 0.5D + 0.20D;
            ax /= l; ay /= l; az /= l;
        } else {
            double rx = 0.0D, ry = 1.0D, rz = 0.0D;
            if (Math.abs(u[1]) > 0.9D) {
                rx = 1.0D;
                ry = 0.0D;
            }
            ax = u[1] * rz - u[2] * ry;
            ay = u[2] * rx - u[0] * rz;
            az = u[0] * ry - u[1] * rx;
            double l = Math.sqrt(ax * ax + ay * ay + az * az);
            if (l < 1.0E-6D) return;
            ax /= l; ay /= l; az /= l;
            rA = RING_R;
        }
        // коротка вісь: u x a
        double bx = u[1] * az - u[2] * ay, by = u[2] * ax - u[0] * az, bz = u[0] * ay - u[1] * ax;
        double lb = Math.sqrt(bx * bx + by * by + bz * bz);
        if (lb < 1.0E-6D) return;
        bx /= lb; by /= lb; bz /= lb;
        double rB = RING_R;

        // центр кружка трохи назад від кінців, у бік гірлянд
        double cx = mx - u[0] * 0.08D, cy = my - u[1] * 0.08D, cz = mz - u[2] * 0.08D;

        int segs = 12;
        double px = cx + ax * rA, py = cy + ay * rA, pz = cz + az * rA;
        for (int i = 1; i <= segs; i++) {
            double a = 2.0D * Math.PI * i / segs;
            double ca = Math.cos(a) * rA, sa = Math.sin(a) * rB;
            double qx = cx + ax * ca + bx * sa;
            double qy = cy + ay * ca + by * sa;
            double qz = cz + az * ca + bz * sa;
            LepGeo.beamUV(t, px, py, pz, qx, qy, qz, 0.03D, A_STEEL, ATLAS_SIZE);
            px = qx; py = qy; pz = qz;
        }
        // спиці хрестом
        LepGeo.beamUV(t, cx - ax * rA, cy - ay * rA, cz - az * rA, cx + ax * rA, cy + ay * rA, cz + az * rA,
            0.025D, A_STEEL_DARK, ATLAS_SIZE);
        LepGeo.beamUV(t, cx - bx * rB, cy - by * rB, cz - bz * rB, cx + bx * rB, cy + by * rB, cz + bz * rB,
            0.025D, A_STEEL_DARK, ATLAS_SIZE);
        // затискач за кінцем кожної гірлянди
        for (double[] e : ends) {
            LepGeo.beamUV(t, e[0] - u[0] * 0.02D, e[1] - u[1] * 0.02D, e[2] - u[2] * 0.02D,
                e[0] + u[0] * 0.16D, e[1] + u[1] * 0.16D, e[2] + u[2] * 0.16D, 0.12D, A_CAP, ATLAS_SIZE);
        }
    }

    /** Верхня гірлянда, що висить униз від a до b: стрижень, 10 тарілок рівномірно по довжині й арматура на кінцях. */
    private static void hangString(Tessellator t, double[] a, double[] b) {
        double len = dist(a, b);
        if (len < 1.0E-4D) return;
        double ux = (b[0] - a[0]) / len, uy = (b[1] - a[1]) / len, uz = (b[2] - a[2]) / len;

        LepGeo.beam(t, a[0], a[1], a[2], b[0], b[1], b[2], 0.07D);
        int n = 10;
        for (int k = 0; k < n; k++) {
            double c = 0.16D + k * ((len - 0.32D) / (n - 1));
            double s0 = c - 0.035D, s1 = c + 0.035D;
            LepGeo.beam(t, a[0] + ux * s0, a[1] + uy * s0, a[2] + uz * s0,
                           a[0] + ux * s1, a[1] + uy * s1, a[2] + uz * s1, 0.30D);
        }
        LepGeo.beam(t, a[0], a[1], a[2], a[0] + ux * 0.12D, a[1] + uy * 0.12D, a[2] + uz * 0.12D, 0.14D);
        LepGeo.beam(t, b[0] - ux * 0.10D, b[1] - uy * 0.10D, b[2] - uz * 0.10D, b[0], b[1], b[2], 0.14D);
    }

    /** Провисаючий провід (парабола) із двох схрещених стрічок. */
    private static void wire(Tessellator t, double[] a, double[] b, double sag, double half) {
        double len = dist(a, b);
        int n = (int) Math.max(6, Math.min(48, len / 2.0D));
        double px = a[0], py = a[1], pz = a[2];
        for (int i = 1; i <= n; i++) {
            double f = (double) i / n;
            double cx = a[0] + (b[0] - a[0]) * f;
            double cy = a[1] + (b[1] - a[1]) * f - sag * 4.0D * f * (1.0D - f);
            double cz = a[2] + (b[2] - a[2]) * f;
            ribbon(t, px, py, pz, cx, cy, cz, half);
            px = cx; py = cy; pz = cz;
        }
    }

    private static void ribbon(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2, double hw) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6D) return;
        dx /= len; dy /= len; dz /= len;

        // n1 горизонтальний, перпендикулярний до проводу
        double n1x = -dz, n1y = 0.0D, n1z = dx;
        double l1 = Math.sqrt(n1x * n1x + n1z * n1z);
        if (l1 < 1.0E-4D) { n1x = 1.0D; n1z = 0.0D; l1 = 1.0D; }
        n1x = n1x / l1 * hw; n1z = n1z / l1 * hw;

        // n2 = d x n1
        double n2x = dy * n1z - dz * n1y, n2y = dz * n1x - dx * n1z, n2z = dx * n1y - dy * n1x;
        double l2 = Math.sqrt(n2x * n2x + n2y * n2y + n2z * n2z);
        if (l2 > 1.0E-6D) { n2x = n2x / l2 * hw; n2y = n2y / l2 * hw; n2z = n2z / l2 * hw; }

        t.addVertex(x1 - n1x, y1 - n1y, z1 - n1z);
        t.addVertex(x1 + n1x, y1 + n1y, z1 + n1z);
        t.addVertex(x2 + n1x, y2 + n1y, z2 + n1z);
        t.addVertex(x2 - n1x, y2 - n1y, z2 - n1z);

        t.addVertex(x1 - n2x, y1 - n2y, z1 - n2z);
        t.addVertex(x1 + n2x, y1 + n2y, z1 + n2z);
        t.addVertex(x2 + n2x, y2 + n2y, z2 + n2z);
        t.addVertex(x2 - n2x, y2 - n2y, z2 - n2z);
    }
}
