package com.sovietindustry.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.blocks.BlockChimney;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

/**
 * Порожниста 16-гранна труба: зовнішня стінка, внутрішня (темніша, закопчена) та торцеві кільця.
 * Підтримує вертикальне розміщення (Y, axis=0) та горизонтальне (X, axis=1; Z, axis=2).
 */
public class ChimneyRenderer implements ISimpleBlockRenderingHandler {
    private static final int N = 16;
    private static final double C = 0.5D;
    private static final double R_OUT = 7.4D / 16.0D;
    private static final double R_IN = 6.0D / 16.0D;
    private static final double[] COS = new double[N + 1];
    private static final double[] SIN = new double[N + 1];

    static {
        for (int i = 0; i <= N; i++) {
            double a = 2.0D * Math.PI * i / N + Math.PI / N;
            COS[i] = Math.cos(a);
            SIN[i] = Math.sin(a);
        }
    }

    // ------------------------------------------------------------ у світі
    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks r) {
        int meta = world.getBlockMetadata(x, y, z);
        int axis = (meta >> 2) & 3;
        IIcon icon = block.getIcon(0, meta);
        Tessellator t = Tessellator.instance;
        t.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));

        boolean cap0, cap1;
        if (axis == 0) { // Y
            cap0 = !isMatchingChimney(world, x, y - 1, z, 0);
            cap1 = !isMatchingChimney(world, x, y + 1, z, 0);
        } else if (axis == 1) { // X
            cap0 = !isMatchingChimney(world, x - 1, y, z, 1);
            cap1 = !isMatchingChimney(world, x + 1, y, z, 1);
        } else { // Z
            cap0 = !isMatchingChimney(world, x, y, z - 1, 2);
            cap1 = !isMatchingChimney(world, x, y, z + 1, 2);
        }

        drawTube(t, icon, x, y, z, axis, cap0, cap1, false);
        return true;
    }

    private boolean isMatchingChimney(IBlockAccess world, int x, int y, int z, int targetAxis) {
        if (world.getBlock(x, y, z) instanceof BlockChimney) {
            int m = world.getBlockMetadata(x, y, z);
            return ((m >> 2) & 3) == targetAxis;
        }
        return false;
    }

    // ------------------------------------------------------------ в інвентарі
    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks r) {
        IIcon icon = block.getIcon(0, metadata);
        Tessellator t = Tessellator.instance;
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        t.startDrawingQuads();
        drawTube(t, icon, 0.0D, 0.0D, 0.0D, 0, true, true, true);
        t.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return BlockChimney.renderId;
    }

    // ------------------------------------------------------------ геометрія
    private void drawTube(Tessellator t, IIcon icon, double x, double y, double z,
                          int axis, boolean cap0, boolean cap1, boolean inv) {
        double vStart = icon.getInterpolatedV(0.0D);
        double vEnd = icon.getInterpolatedV(16.0D);

        for (int i = 0; i < N; i++) {
            double c0 = COS[i], s0 = SIN[i], c1 = COS[i + 1], s1 = SIN[i + 1];

            // нормаль
            double m1 = c0 + c1, m2 = s0 + s1;
            double len = Math.sqrt(m1 * m1 + m2 * m2);
            m1 /= len;
            m2 /= len;

            double u0 = icon.getInterpolatedU((i % 4) * 4.0D);
            double u1 = icon.getInterpolatedU((i % 4) * 4.0D + 4.0D);

            float shade = (float) (0.6D * m1 * m1 + 0.8D * m2 * m2);
            float inner = shade * 0.55F;

            if (axis == 0) { // Вздовж Y
                // зовнішня стінка
                t.setColorOpaque_F(shade, shade, shade);
                quad(t, inv,
                     vtx(x + C + R_OUT * c0, y + 1.0D, z + C + R_OUT * s0, u0, vStart),
                     vtx(x + C + R_OUT * c0, y,        z + C + R_OUT * s0, u0, vEnd),
                     vtx(x + C + R_OUT * c1, y,        z + C + R_OUT * s1, u1, vEnd),
                     vtx(x + C + R_OUT * c1, y + 1.0D, z + C + R_OUT * s1, u1, vStart),
                     m1, 0.0D, m2);

                // внутрішня стінка
                t.setColorOpaque_F(inner, inner, inner);
                quad(t, inv,
                     vtx(x + C + R_IN * c0, y + 1.0D, z + C + R_IN * s0, u0, vStart),
                     vtx(x + C + R_IN * c0, y,        z + C + R_IN * s0, u0, vEnd),
                     vtx(x + C + R_IN * c1, y,        z + C + R_IN * s1, u1, vEnd),
                     vtx(x + C + R_IN * c1, y + 1.0D, z + C + R_IN * s1, u1, vStart),
                     -m1, 0.0D, -m2);

                // кришки
                if (cap1) { // y + 1
                    t.setColorOpaque_F(1.0F, 1.0F, 1.0F);
                    quad(t, inv,
                         ringY(icon, x, y + 1.0D, z, R_OUT, c0, s0),
                         ringY(icon, x, y + 1.0D, z, R_OUT, c1, s1),
                         ringY(icon, x, y + 1.0D, z, R_IN, c1, s1),
                         ringY(icon, x, y + 1.0D, z, R_IN, c0, s0),
                         0.0D, 1.0D, 0.0D);
                }
                if (cap0) { // y
                    t.setColorOpaque_F(0.5F, 0.5F, 0.5F);
                    quad(t, inv,
                         ringY(icon, x, y, z, R_OUT, c0, s0),
                         ringY(icon, x, y, z, R_OUT, c1, s1),
                         ringY(icon, x, y, z, R_IN, c1, s1),
                         ringY(icon, x, y, z, R_IN, c0, s0),
                         0.0D, -1.0D, 0.0D);
                }
            } else if (axis == 1) { // Вздовж X
                // зовнішня стінка
                t.setColorOpaque_F(shade, shade, shade);
                quad(t, inv,
                     vtx(x + 1.0D, y + C + R_OUT * c0, z + C + R_OUT * s0, u0, vStart),
                     vtx(x,        y + C + R_OUT * c0, z + C + R_OUT * s0, u0, vEnd),
                     vtx(x,        y + C + R_OUT * c1, z + C + R_OUT * s1, u1, vEnd),
                     vtx(x + 1.0D, y + C + R_OUT * c1, z + C + R_OUT * s1, u1, vStart),
                     0.0D, m1, m2);

                // внутрішня стінка
                t.setColorOpaque_F(inner, inner, inner);
                quad(t, inv,
                     vtx(x + 1.0D, y + C + R_IN * c0, z + C + R_IN * s0, u0, vStart),
                     vtx(x,        y + C + R_IN * c0, z + C + R_IN * s0, u0, vEnd),
                     vtx(x,        y + C + R_IN * c1, z + C + R_IN * s1, u1, vEnd),
                     vtx(x + 1.0D, y + C + R_IN * c1, z + C + R_IN * s1, u1, vStart),
                     0.0D, -m1, -m2);

                // кришки
                if (cap1) { // x + 1
                    t.setColorOpaque_F(0.8F, 0.8F, 0.8F);
                    quad(t, inv,
                         ringX(icon, x + 1.0D, y, z, R_OUT, c0, s0),
                         ringX(icon, x + 1.0D, y, z, R_OUT, c1, s1),
                         ringX(icon, x + 1.0D, y, z, R_IN, c1, s1),
                         ringX(icon, x + 1.0D, y, z, R_IN, c0, s0),
                         1.0D, 0.0D, 0.0D);
                }
                if (cap0) { // x
                    t.setColorOpaque_F(0.8F, 0.8F, 0.8F);
                    quad(t, inv,
                         ringX(icon, x, y, z, R_OUT, c0, s0),
                         ringX(icon, x, y, z, R_OUT, c1, s1),
                         ringX(icon, x, y, z, R_IN, c1, s1),
                         ringX(icon, x, y, z, R_IN, c0, s0),
                         -1.0D, 0.0D, 0.0D);
                }
            } else { // Вздовж Z
                // зовнішня стінка
                t.setColorOpaque_F(shade, shade, shade);
                quad(t, inv,
                     vtx(x + C + R_OUT * c0, y + C + R_OUT * s0, z + 1.0D, u0, vStart),
                     vtx(x + C + R_OUT * c0, y + C + R_OUT * s0, z,        u0, vEnd),
                     vtx(x + C + R_OUT * c1, y + C + R_OUT * s1, z,        u1, vEnd),
                     vtx(x + C + R_OUT * c1, y + C + R_OUT * s1, z + 1.0D, u1, vStart),
                     m1, m2, 0.0D);

                // внутрішня стінка
                t.setColorOpaque_F(inner, inner, inner);
                quad(t, inv,
                     vtx(x + C + R_IN * c0, y + C + R_IN * s0, z + 1.0D, u0, vStart),
                     vtx(x + C + R_IN * c0, y + C + R_IN * s0, z,        u0, vEnd),
                     vtx(x + C + R_IN * c1, y + C + R_IN * s1, z,        u1, vEnd),
                     vtx(x + C + R_IN * c1, y + C + R_IN * s1, z + 1.0D, u1, vStart),
                     -m1, -m2, 0.0D);

                // кришки
                if (cap1) { // z + 1
                    t.setColorOpaque_F(0.8F, 0.8F, 0.8F);
                    quad(t, inv,
                         ringZ(icon, x, y, z + 1.0D, R_OUT, c0, s0),
                         ringZ(icon, x, y, z + 1.0D, R_OUT, c1, s1),
                         ringZ(icon, x, y, z + 1.0D, R_IN, c1, s1),
                         ringZ(icon, x, y, z + 1.0D, R_IN, c0, s0),
                         0.0D, 0.0D, 1.0D);
                }
                if (cap0) { // z
                    t.setColorOpaque_F(0.8F, 0.8F, 0.8F);
                    quad(t, inv,
                         ringZ(icon, x, y, z, R_OUT, c0, s0),
                         ringZ(icon, x, y, z, R_OUT, c1, s1),
                         ringZ(icon, x, y, z, R_IN, c1, s1),
                         ringZ(icon, x, y, z, R_IN, c0, s0),
                         0.0D, 0.0D, -1.0D);
                }
            }
        }
    }

    private double[] ringY(IIcon icon, double x, double y, double z, double radius, double c, double s) {
        double lx = C + radius * c;
        double lz = C + radius * s;
        return vtx(x + lx, y, z + lz, icon.getInterpolatedU(lx * 16.0D), icon.getInterpolatedV(lz * 16.0D));
    }

    private double[] ringX(IIcon icon, double x, double y, double z, double radius, double c, double s) {
        double ly = C + radius * c;
        double lz = C + radius * s;
        return vtx(x, y + ly, z + lz, icon.getInterpolatedU(ly * 16.0D), icon.getInterpolatedV(lz * 16.0D));
    }

    private double[] ringZ(IIcon icon, double x, double y, double z, double radius, double c, double s) {
        double lx = C + radius * c;
        double ly = C + radius * s;
        return vtx(x + lx, y + ly, z, icon.getInterpolatedU(lx * 16.0D), icon.getInterpolatedV(ly * 16.0D));
    }

    private static double[] vtx(double x, double y, double z, double u, double v) {
        return new double[] { x, y, z, u, v };
    }

    /** Малює чотирикутник так, щоб його лицьовий бік дивився в напрямку (nx, ny, nz). */
    private void quad(Tessellator t, boolean inv, double[] a, double[] b, double[] c, double[] d,
                      double nx, double ny, double nz) {
        double e1x = b[0] - a[0], e1y = b[1] - a[1], e1z = b[2] - a[2];
        double e2x = d[0] - a[0], e2y = d[1] - a[1], e2z = d[2] - a[2];
        double cx = e1y * e2z - e1z * e2y;
        double cy = e1z * e2x - e1x * e2z;
        double cz = e1x * e2y - e1y * e2x;
        if (cx * nx + cy * ny + cz * nz < 0.0D) {
            double[] tmp = b;
            b = d;
            d = tmp;
        }
        if (inv) {
            t.setNormal((float) nx, (float) ny, (float) nz);
        }
        t.addVertexWithUV(a[0], a[1], a[2], a[3], a[4]);
        t.addVertexWithUV(b[0], b[1], b[2], b[3], b[4]);
        t.addVertexWithUV(c[0], c[1], c[2], c[3], c[4]);
        t.addVertexWithUV(d[0], d[1], d[2], d[3], d[4]);
    }
}
