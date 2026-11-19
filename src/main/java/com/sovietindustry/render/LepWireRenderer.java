package com.sovietindustry.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.tileentity.network.TileEntityPylonBase;
import com.sovietindustry.SovietIndustryMod;
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

    private LepWireRenderer() {
    }

    private static final class Link {
        /** Кінець гірлянди (або точка кріплення, якщо гірлянд нема) на нашій опорі. */
        final double[][] mine;
        /** Те саме на другій опорі (лише для наших опор). */
        final double[][] other;
        /** Куди дивиться наша гірлянда (одиничний вектор). */
        final double[][] dir;
        boolean draw;
        boolean ntm;

        Link(int n) {
            mine = new double[n][];
            other = new double[n][];
            dir = new double[n][];
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

            int n = Math.min(mine.length, om.length);
            double ox = o.xCoord - te.xCoord;
            double oy = o.yCoord - te.yCoord;
            double oz = o.zCoord - te.zCoord;

            Link l = new Link(n);
            l.ntm = !ours;
            l.draw = !ours || isLower(te, o);
            for (int i = 0; i < n; i++) {
                double bx = ox + om[i].xCoord, by = oy + om[i].yCoord, bz = oz + om[i].zCoord;
                double dx = bx - mine[i].xCoord, dy = by - mine[i].yCoord, dz = bz - mine[i].zCoord;
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
                l.dir[i] = d;
                l.mine[i] = anchor
                    ? new double[] { mine[i].xCoord + d[0] * myStr, mine[i].yCoord + d[1] * myStr, mine[i].zCoord + d[2] * myStr }
                    : new double[] { mine[i].xCoord, mine[i].yCoord, mine[i].zCoord };

                if (ours) {
                    double[] e = otherAnchor
                        ? unit(-dx, -dy - 4.0D * SAG * len, -dz)
                        : null;
                    double s = otherAnchor ? oth.getStringLength() : 0.0D;
                    l.other[i] = otherAnchor
                        ? new double[] { bx + e[0] * s, by + e[1] * s, bz + e[2] * s }
                        : new double[] { bx, by, bz };
                } else {
                    // середина між точками кріплення, там зустрічається половина NTM
                    l.other[i] = new double[] {
                        mine[i].xCoord + dx * 0.5D, mine[i].yCoord + dy * 0.5D, mine[i].zCoord + dz * 0.5D };
                }
            }
            list.add(l);
        }
        if (list.isEmpty()) return;

        Tessellator t = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glDisable(GL11.GL_CULL_FACE);

        // --- гірлянди
        if (anchor) {
            Minecraft.getMinecraft().renderEngine.bindTexture(INSULATOR);
            t.startDrawingQuads();
            for (Link l : list) {
                for (int i = 0; i < l.mine.length; i++) {
                    double[] base = { mine[i].xCoord, mine[i].yCoord, mine[i].zCoord };
                    string(t, base, l.mine[i]);
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
                if (l.ntm) {
                    ntmHalf(t, l.mine[i], l.other[i]);
                } else {
                    double len = dist(l.mine[i], l.other[i]);
                    wire(t, l.mine[i], l.other[i], len * SAG);
                }
            }
        }
        // шлейфи між двома натяжними гірляндами однієї фази
        if (anchor && list.size() >= 2) {
            int n = Math.min(list.get(0).mine.length, list.get(1).mine.length);
            for (int i = 0; i < n; i++) {
                wire(t, list.get(0).mine[i], list.get(1).mine[i], 0.45D);
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
            ribbon(t, px, py, pz, cx, cy, cz);
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
    private static void wire(Tessellator t, double[] a, double[] b, double sag) {
        double len = dist(a, b);
        int n = (int) Math.max(6, Math.min(48, len / 2.0D));
        double px = a[0], py = a[1], pz = a[2];
        for (int i = 1; i <= n; i++) {
            double f = (double) i / n;
            double cx = a[0] + (b[0] - a[0]) * f;
            double cy = a[1] + (b[1] - a[1]) * f - sag * 4.0D * f * (1.0D - f);
            double cz = a[2] + (b[2] - a[2]) * f;
            ribbon(t, px, py, pz, cx, cy, cz);
            px = cx; py = cy; pz = cz;
        }
    }

    private static void ribbon(Tessellator t, double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6D) return;
        dx /= len; dy /= len; dz /= len;

        // n1 горизонтальний, перпендикулярний до проводу
        double n1x = -dz, n1y = 0.0D, n1z = dx;
        double l1 = Math.sqrt(n1x * n1x + n1z * n1z);
        if (l1 < 1.0E-4D) { n1x = 1.0D; n1z = 0.0D; l1 = 1.0D; }
        n1x = n1x / l1 * WIRE_HALF; n1z = n1z / l1 * WIRE_HALF;

        // n2 = d x n1
        double n2x = dy * n1z - dz * n1y, n2y = dz * n1x - dx * n1z, n2z = dx * n1y - dy * n1x;
        double l2 = Math.sqrt(n2x * n2x + n2y * n2y + n2z * n2z);
        if (l2 > 1.0E-6D) { n2x = n2x / l2 * WIRE_HALF; n2y = n2y / l2 * WIRE_HALF; n2z = n2z / l2 * WIRE_HALF; }

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
