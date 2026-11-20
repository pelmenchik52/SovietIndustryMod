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

    /**
     * Паралелепіпед із тайловими UV: текстура повторюється кожні 1/tpb блока
     * (tpb = кількість повторів на блок), тому великі плити не розтягують текстуру.
     */
    public static void boxTiled(Tessellator t, double cx, double cy, double cz,
                                double hx, double hy, double hz, double tpb) {
        double x1 = cx - hx, x2 = cx + hx, y1 = cy - hy, y2 = cy + hy, z1 = cz - hz, z2 = cz + hz;
        double ux = 2 * hx * tpb, uy = 2 * hy * tpb, uz = 2 * hz * tpb;
        t.setNormal(0, -1, 0);
        t.addVertexWithUV(x1, y1, z1, 0, 0);  t.addVertexWithUV(x2, y1, z1, ux, 0);
        t.addVertexWithUV(x2, y1, z2, ux, uz); t.addVertexWithUV(x1, y1, z2, 0, uz);
        t.setNormal(0, 1, 0);
        t.addVertexWithUV(x1, y2, z2, 0, uz); t.addVertexWithUV(x2, y2, z2, ux, uz);
        t.addVertexWithUV(x2, y2, z1, ux, 0); t.addVertexWithUV(x1, y2, z1, 0, 0);
        t.setNormal(0, 0, -1);
        t.addVertexWithUV(x1, y1, z1, 0, uy); t.addVertexWithUV(x1, y2, z1, 0, 0);
        t.addVertexWithUV(x2, y2, z1, ux, 0); t.addVertexWithUV(x2, y1, z1, ux, uy);
        t.setNormal(0, 0, 1);
        t.addVertexWithUV(x2, y1, z2, 0, uy); t.addVertexWithUV(x2, y2, z2, 0, 0);
        t.addVertexWithUV(x1, y2, z2, ux, 0); t.addVertexWithUV(x1, y1, z2, ux, uy);
        t.setNormal(-1, 0, 0);
        t.addVertexWithUV(x1, y1, z2, 0, uy); t.addVertexWithUV(x1, y2, z2, 0, 0);
        t.addVertexWithUV(x1, y2, z1, uz, 0); t.addVertexWithUV(x1, y1, z1, uz, uy);
        t.setNormal(1, 0, 0);
        t.addVertexWithUV(x2, y1, z1, 0, uy); t.addVertexWithUV(x2, y2, z1, 0, 0);
        t.addVertexWithUV(x2, y2, z2, uz, 0); t.addVertexWithUV(x2, y1, z2, uz, uy);
    }

    /** Площадка 0.25x0.25 на бічній грані (+X або -X), повністю накрита текстурою 4x4. */
    public static void padX(Tessellator t, double x, double cy, double cz, double half, boolean positive) {
        double y1 = cy - half, y2 = cy + half, z1 = cz - half, z2 = cz + half;
        t.setNormal(positive ? 1 : -1, 0, 0);
        if (positive) {
            t.addVertexWithUV(x, y1, z1, 0, 1); t.addVertexWithUV(x, y2, z1, 0, 0);
            t.addVertexWithUV(x, y2, z2, 1, 0); t.addVertexWithUV(x, y1, z2, 1, 1);
        } else {
            t.addVertexWithUV(x, y1, z2, 0, 1); t.addVertexWithUV(x, y2, z2, 0, 0);
            t.addVertexWithUV(x, y2, z1, 1, 0); t.addVertexWithUV(x, y1, z1, 1, 1);
        }
    }

    /**
     * Балка з ділянкою атласу uv = {u0, v0, u1, v1} (пікселі, atlas = розмір атласу, напр. 128).
     * Вздовж балки йде u, впоперек v. Довга балка ріжеться на відрізки по 0.5 блока,
     * і кожен накривається цією ділянкою, тому деталі текстури не розтягуються.
     */
    public static void beamUV(Tessellator t, double x1, double y1, double z1,
                              double x2, double y2, double z2, double th,
                              double[] uv, double atlas) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6D) return;
        double ax = dx / len, ay = dy / len, az = dz / len;

        double rx = 0.0D, ry = 0.0D, rz = 0.0D;
        if (Math.abs(ay) > 0.9D) rx = 1.0D; else ry = 1.0D;

        double ux = ay * rz - az * ry, uy = az * rx - ax * rz, uz = ax * ry - ay * rx;
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul; uy /= ul; uz /= ul;
        double vx = ay * uz - az * uy, vy = az * ux - ax * uz, vz = ax * uy - ay * ux;

        double h = th * 0.5D;
        double[][] cs = { { 1, 1 }, { -1, 1 }, { -1, -1 }, { 1, -1 } };
        double[][] nr = { { vx, vy, vz }, { -ux, -uy, -uz }, { -vx, -vy, -vz }, { ux, uy, uz } };

        double[][] off = new double[4][3];
        for (int k = 0; k < 4; k++) {
            off[k][0] = (ux * cs[k][0] + vx * cs[k][1]) * h;
            off[k][1] = (uy * cs[k][0] + vy * cs[k][1]) * h;
            off[k][2] = (uz * cs[k][0] + vz * cs[k][1]) * h;
        }

        double u0 = (uv[0] + 0.05D) / atlas, v0 = (uv[1] + 0.05D) / atlas;
        double u1 = (uv[2] - 0.05D) / atlas, v1 = (uv[3] - 0.05D) / atlas;
        int n = (int) Math.max(1.0D, Math.ceil(len / 0.5D));

        for (int s = 0; s < n; s++) {
            double f0 = (double) s / n, f1 = (double) (s + 1) / n;
            double sx = x1 + dx * f0, sy = y1 + dy * f0, sz = z1 + dz * f0;
            double ex = x1 + dx * f1, ey = y1 + dy * f1, ez = z1 + dz * f1;
            for (int k = 0; k < 4; k++) {
                double[] a = off[k];
                double[] b = off[(k + 1) & 3];
                t.setNormal((float) nr[k][0], (float) nr[k][1], (float) nr[k][2]);
                t.addVertexWithUV(sx + a[0], sy + a[1], sz + a[2], u0, v0);
                t.addVertexWithUV(ex + a[0], ey + a[1], ez + a[2], u1, v0);
                t.addVertexWithUV(ex + b[0], ey + b[1], ez + b[2], u1, v1);
                t.addVertexWithUV(sx + b[0], sy + b[1], sz + b[2], u0, v1);
            }
        }

        t.setNormal((float) ax, (float) ay, (float) az);
        for (int k = 0; k < 4; k++) {
            t.addVertexWithUV(x2 + off[k][0], y2 + off[k][1], z2 + off[k][2],
                cs[k][0] > 0 ? u1 : u0, cs[k][1] > 0 ? v1 : v0);
        }
        t.setNormal((float) -ax, (float) -ay, (float) -az);
        for (int k = 3; k >= 0; k--) {
            t.addVertexWithUV(x1 + off[k][0], y1 + off[k][1], z1 + off[k][2],
                cs[k][0] > 0 ? u1 : u0, cs[k][1] > 0 ? v1 : v0);
        }
    }

    /** Паралелепіпед за центром і півсторонами, усі грані з однією ділянкою атласу uv = {u0, v0, u1, v1}. */
    public static void boxUV(Tessellator t, double cx, double cy, double cz,
                             double hx, double hy, double hz, double[] uv, double atlas) {
        double x1 = cx - hx, x2 = cx + hx, y1 = cy - hy, y2 = cy + hy, z1 = cz - hz, z2 = cz + hz;
        double u0 = (uv[0] + 0.05D) / atlas, v0 = (uv[1] + 0.05D) / atlas;
        double u1 = (uv[2] - 0.05D) / atlas, v1 = (uv[3] - 0.05D) / atlas;
        t.setNormal(0, -1, 0);
        faceUV(t, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, u0, v0, u1, v1);
        t.setNormal(0, 1, 0);
        faceUV(t, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, u0, v0, u1, v1);
        t.setNormal(0, 0, -1);
        faceUV(t, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, u0, v0, u1, v1);
        t.setNormal(0, 0, 1);
        faceUV(t, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2, u0, v0, u1, v1);
        t.setNormal(-1, 0, 0);
        faceUV(t, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1, u0, v0, u1, v1);
        t.setNormal(1, 0, 0);
        faceUV(t, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, u0, v0, u1, v1);
    }

    private static void faceUV(Tessellator t,
                               double ax, double ay, double az, double bx, double by, double bz,
                               double cx, double cy, double cz, double dx, double dy, double dz,
                               double u0, double v0, double u1, double v1) {
        t.addVertexWithUV(ax, ay, az, u0, v1);
        t.addVertexWithUV(bx, by, bz, u0, v0);
        t.addVertexWithUV(cx, cy, cz, u1, v0);
        t.addVertexWithUV(dx, dy, dz, u1, v1);
    }
}
