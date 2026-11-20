package com.sovietindustry.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.hbm.tileentity.network.TileEntityPylonBase;
import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.api.IGroundWired;
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

            int n = Math.min(mi.length, oi.length);
            double ox = o.xCoord - te.xCoord;
            double oy = o.yCoord - te.yCoord;
            double oz = o.zCoord - te.zCoord;

            Link l = new Link(n);
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

        Vec3 ground = te instanceof IGroundWired ? ((IGroundWired) te).getGroundMount() : null;
        if (list.isEmpty() && ground == null) return;

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
                    string(t, new double[] { m.xCoord, m.yCoord, m.zCoord }, l.mine[i]);
                }
            }
            t.draw();
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
                if (l.ntm) {
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
            for (Link l : list) {
                for (int i = 0; i < l.mine.length; i++) {
                    if (l.mine[i] == null) continue;
                    List<double[]> ends = perMount.get(l.mi[i]);
                    if (ends == null) {
                        ends = new ArrayList<double[]>();
                        perMount.put(l.mi[i], ends);
                    }
                    ends.add(l.mine[i]);
                }
            }
            for (List<double[]> ends : perMount.values()) {
                if (ends.size() >= 2) {
                    double d = dist(ends.get(0), ends.get(1));
                    wire(t, ends.get(0), ends.get(1), Math.min(0.45D, d * 0.3D), WIRE_HALF);
                }
            }
        }

        // --- грозозахисний трос: лише між двома опорами з тросом, на кінці лінії обривається
        if (ground != null) {
            t.setColorOpaque_F(0.24F, 0.25F, 0.27F);
            for (int[] p : links) {
                TileEntity o = world.getTileEntity(p[0], p[1], p[2]);
                if (!(o instanceof IGroundWired) || !isLower(te, o)) continue;
                Vec3 og = ((IGroundWired) o).getGroundMount();
                double[] a = { ground.xCoord, ground.yCoord, ground.zCoord };
                double[] b = { o.xCoord - te.xCoord + og.xCoord, o.yCoord - te.yCoord + og.yCoord,
                               o.zCoord - te.zCoord + og.zCoord };
                wire(t, a, b, dist(a, b) * SAG, GROUND_HALF);
            }
        }
        t.draw();

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
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
