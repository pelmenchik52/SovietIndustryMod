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
 * Порядок вершин кожного чотирикутника підбирається автоматично за заданою нормаллю,
 * тож відсікання задніх граней (culling) працює правильно.
 */
public class ChimneyRenderer implements ISimpleBlockRenderingHandler {
    private static final int N = 16;
    private static final double CX = 0.5D;
    private static final double CZ = 0.5D;
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
        IIcon icon = block.getIcon(0, meta);
        Tessellator t = Tessellator.instance;
        t.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));

        boolean top = !(world.getBlock(x, y + 1, z) instanceof BlockChimney);
        boolean bottom = !(world.getBlock(x, y - 1, z) instanceof BlockChimney);
        drawTube(t, icon, x, y, z, top, bottom, false);
        return true;
    }

    // ------------------------------------------------------------ в інвентарі
    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks r) {
        IIcon icon = block.getIcon(0, metadata);
        Tessellator t = Tessellator.instance;
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        t.startDrawingQuads();
        drawTube(t, icon, 0.0D, 0.0D, 0.0D, true, true, true);
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
                          boolean top, boolean bottom, boolean inv) {
        double vTop = icon.getInterpolatedV(0.0D);
        double vBot = icon.getInterpolatedV(16.0D);

        for (int i = 0; i < N; i++) {
            double c0 = COS[i], s0 = SIN[i], c1 = COS[i + 1], s1 = SIN[i + 1];

            // зовнішня нормаль сегмента
            double mx = c0 + c1, mz = s0 + s1;
            double len = Math.sqrt(mx * mx + mz * mz);
            mx /= len;
            mz /= len;
            float shade = (float) (0.6D * mx * mx + 0.8D * mz * mz);

            // кожен сегмент бере 1/4 ширини текстури, текстура обертається 4 рази
            double u0 = icon.getInterpolatedU((i % 4) * 4.0D);
            double u1 = icon.getInterpolatedU((i % 4) * 4.0D + 4.0D);

            // зовнішня стінка
            t.setColorOpaque_F(shade, shade, shade);
            quad(t, inv,
                 vtx(x + CX + R_OUT * c0, y + 1.0D, z + CZ + R_OUT * s0, u0, vTop),
                 vtx(x + CX + R_OUT * c0, y,        z + CZ + R_OUT * s0, u0, vBot),
                 vtx(x + CX + R_OUT * c1, y,        z + CZ + R_OUT * s1, u1, vBot),
                 vtx(x + CX + R_OUT * c1, y + 1.0D, z + CZ + R_OUT * s1, u1, vTop),
                 mx, 0.0D, mz);

            // внутрішня стінка (закопчена, темніша)
            float inner = shade * 0.55F;
            t.setColorOpaque_F(inner, inner, inner);
            quad(t, inv,
                 vtx(x + CX + R_IN * c0, y + 1.0D, z + CZ + R_IN * s0, u0, vTop),
                 vtx(x + CX + R_IN * c0, y,        z + CZ + R_IN * s0, u0, vBot),
                 vtx(x + CX + R_IN * c1, y,        z + CZ + R_IN * s1, u1, vBot),
                 vtx(x + CX + R_IN * c1, y + 1.0D, z + CZ + R_IN * s1, u1, vTop),
                 -mx, 0.0D, -mz);

            // верхнє кільце
            if (top) {
                t.setColorOpaque_F(1.0F, 1.0F, 1.0F);
                quad(t, inv,
                     ring(icon, x, y + 1.0D, z, R_OUT, c0, s0),
                     ring(icon, x, y + 1.0D, z, R_OUT, c1, s1),
                     ring(icon, x, y + 1.0D, z, R_IN, c1, s1),
                     ring(icon, x, y + 1.0D, z, R_IN, c0, s0),
                     0.0D, 1.0D, 0.0D);
            }
            // нижнє кільце
            if (bottom) {
                t.setColorOpaque_F(0.5F, 0.5F, 0.5F);
                quad(t, inv,
                     ring(icon, x, y, z, R_OUT, c0, s0),
                     ring(icon, x, y, z, R_OUT, c1, s1),
                     ring(icon, x, y, z, R_IN, c1, s1),
                     ring(icon, x, y, z, R_IN, c0, s0),
                     0.0D, -1.0D, 0.0D);
            }
        }
    }

    private double[] ring(IIcon icon, double x, double y, double z, double radius, double c, double s) {
        double lx = CX + radius * c;
        double lz = CZ + radius * s;
        return vtx(x + lx, y, z + lz, icon.getInterpolatedU(lx * 16.0D), icon.getInterpolatedV(lz * 16.0D));
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
