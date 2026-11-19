package com.sovietindustry.render;

import net.minecraft.client.renderer.Tessellator;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Прості генератори геометрії для Tessellator (викликати між startDrawingQuads() і draw()).
 * beam: балка квадратного перерізу між двома точками (годиться для діагоналей решітки).
 */
@SideOnly(Side.CLIENT)
public final class LepGeo {
    private LepGeo() {
    }

    public static void beam(Tessellator t, double x1, double y1, double z1,
                            double x2, double y2, double z2, double th) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6D) return;
        double ax = dx / len, ay = dy / len, az = dz / len;

        double rx = 0.0D, ry = 0.0D, rz = 0.0D;
        if (Math.abs(ay) > 0.9D) rx = 1.0D; else ry = 1.0D;

        // u = a x r, v = a x u (обидва одиничні й перпендикулярні до осі)
        double ux = ay * rz - az * ry, uy = az * rx - ax * rz, uz = ax * ry - ay * rx;
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul; uy /= ul; uz /= ul;
        double vx = ay * uz - az * uy, vy = az * ux - ax * uz, vz = ax * uy - ay * ux;

        double h = th * 0.5D;
        double[][] cs = { { 1, 1 }, { -1, 1 }, { -1, -1 }, { 1, -1 } };
        double[][] nr = { { vx, vy, vz }, { -ux, -uy, -uz }, { -vx, -vy, -vz }, { ux, uy, uz } };
        double tv = Math.max(0.25D, len * 2.0D);

        double[][] off = new double[4][3];
        for (int k = 0; k < 4; k++) {
            off[k][0] = (ux * cs[k][0] + vx * cs[k][1]) * h;
            off[k][1] = (uy * cs[k][0] + vy * cs[k][1]) * h;
            off[k][2] = (uz * cs[k][0] + vz * cs[k][1]) * h;
        }

        for (int k = 0; k < 4; k++) {
            double[] a = off[k];
            double[] b = off[(k + 1) & 3];
            t.setNormal((float) nr[k][0], (float) nr[k][1], (float) nr[k][2]);
            t.addVertexWithUV(x1 + a[0], y1 + a[1], z1 + a[2], 0.0D, 0.0D);
            t.addVertexWithUV(x2 + a[0], y2 + a[1], z2 + a[2], 0.0D, tv);
            t.addVertexWithUV(x2 + b[0], y2 + b[1], z2 + b[2], 1.0D, tv);
            t.addVertexWithUV(x1 + b[0], y1 + b[1], z1 + b[2], 1.0D, 0.0D);
        }

        // торці
        t.setNormal((float) ax, (float) ay, (float) az);
        for (int k = 0; k < 4; k++) {
            t.addVertexWithUV(x2 + off[k][0], y2 + off[k][1], z2 + off[k][2], cs[k][0] > 0 ? 1 : 0, cs[k][1] > 0 ? 1 : 0);
        }
        t.setNormal((float) -ax, (float) -ay, (float) -az);
        for (int k = 3; k >= 0; k--) {
            t.addVertexWithUV(x1 + off[k][0], y1 + off[k][1], z1 + off[k][2], cs[k][0] > 0 ? 1 : 0, cs[k][1] > 0 ? 1 : 0);
        }
    }

    /** Паралелепіпед за центром і півсторонами. */
    public static void box(Tessellator t, double cx, double cy, double cz, double hx, double hy, double hz) {
        double x1 = cx - hx, x2 = cx + hx, y1 = cy - hy, y2 = cy + hy, z1 = cz - hz, z2 = cz + hz;
        t.setNormal(0, -1, 0);
        t.addVertexWithUV(x1, y1, z1, 0, 0); t.addVertexWithUV(x2, y1, z1, 1, 0);
        t.addVertexWithUV(x2, y1, z2, 1, 1); t.addVertexWithUV(x1, y1, z2, 0, 1);
        t.setNormal(0, 1, 0);
        t.addVertexWithUV(x1, y2, z2, 0, 1); t.addVertexWithUV(x2, y2, z2, 1, 1);
        t.addVertexWithUV(x2, y2, z1, 1, 0); t.addVertexWithUV(x1, y2, z1, 0, 0);
        t.setNormal(0, 0, -1);
        t.addVertexWithUV(x1, y1, z1, 0, 1); t.addVertexWithUV(x1, y2, z1, 0, 0);
        t.addVertexWithUV(x2, y2, z1, 1, 0); t.addVertexWithUV(x2, y1, z1, 1, 1);
        t.setNormal(0, 0, 1);
        t.addVertexWithUV(x2, y1, z2, 0, 1); t.addVertexWithUV(x2, y2, z2, 0, 0);
        t.addVertexWithUV(x1, y2, z2, 1, 0); t.addVertexWithUV(x1, y1, z2, 1, 1);
        t.setNormal(-1, 0, 0);
        t.addVertexWithUV(x1, y1, z2, 0, 1); t.addVertexWithUV(x1, y2, z2, 0, 0);
        t.addVertexWithUV(x1, y2, z1, 1, 0); t.addVertexWithUV(x1, y1, z1, 1, 1);
        t.setNormal(1, 0, 0);
        t.addVertexWithUV(x2, y1, z1, 0, 1); t.addVertexWithUV(x2, y2, z1, 0, 0);
        t.addVertexWithUV(x2, y2, z2, 1, 0); t.addVertexWithUV(x2, y1, z2, 1, 1);
    }
}
