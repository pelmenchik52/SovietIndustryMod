package com.sovietindustry.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.tileentity.network.TileEntityPylonBase;
import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.tileentities.TileEntityLepPylon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Малює для опори: натяжні гірлянди (лише анкерна), шлейфи між ними та самі проводи з провисанням.
 * Усі координати відносно кута блока (xCoord, yCoord, zCoord), тому достатньо translate(x, y, z).
 * Кожен дріт малює тільки "нижня" з двох опор, щоб не було дубля.
 */
@SideOnly(Side.CLIENT)
public final class LepWireRenderer {
    private static final ResourceLocation INSULATOR =
        new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep_insulator.png");

    /** Стріла провисання як частка довжини прольоту. */
    private static final double SAG = 0.03D;
    private static final double WIRE_HALF = 0.02D;

    private LepWireRenderer() {
    }

    private static final class Link {
        final double[][] mine;
        final double[][] other;
        boolean draw;

        Link(int n) {
            mine = new double[n][];
            other = new double[n][];
        }
    }

    private static boolean isLower(TileEntity a, TileEntity b) {
        if (a.xCoord != b.xCoord) return a.xCoord < b.xCoord;
        if (a.yCoord != b.yCoord) return a.yCoord < b.yCoord;
        return a.zCoord < b.zCoord;
    }

    public static void render(TileEntityLepPylon te, double x, double y, double z) {
        World world = te.getWorldObj();
        if (world == null) return;
        List<int[]> links = te.getLinks();
        if (links.isEmpty()) return;

        Vec3[] mine = te.getMountPos();
        List<Link> list = new ArrayList<Link>();

        for (int[] p : links) {
            TileEntity o = world.getTileEntity(p[0], p[1], p[2]);
            if (!(o instanceof TileEntityPylonBase)) continue;
            Vec3[] om = ((TileEntityPylonBase) o).getMountPos();
            if (om == null) continue;

            int n = Math.min(mine.length, om.length);
            double ox = o.xCoord - te.xCoord;
            double oy = o.yCoord - te.yCoord;
            double oz = o.zCoord - te.zCoord;
            boolean ours = o instanceof TileEntityLepPylon;

            Link l = new Link(n);
            l.draw = !ours || isLower(te, o);
            for (int i = 0; i < n; i++) {
                double bx = ox + om[i].xCoord, by = oy + om[i].yCoord, bz = oz + om[i].zCoord;
                double tx = bx - mine[i].xCoord, tz = bz - mine[i].zCoord;
                l.mine[i] = te.getWireEnd(i, tx, tz);
                if (ours) {
                    double[] e = ((TileEntityLepPylon) o).getWireEnd(i, -tx, -tz);
                    l.other[i] = new double[] { ox + e[0], oy + e[1], oz + e[2] };
                } else {
                    l.other[i] = new double[] { bx, by, bz };
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
        if (te.hasAnchorStrings()) {
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
                double len = dist(l.mine[i], l.other[i]);
                wire(t, l.mine[i], l.other[i], len * SAG);
            }
        }
        // шлейфи між двома натяжними гірляндами однієї фази
        if (te.hasAnchorStrings() && list.size() >= 2) {
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
