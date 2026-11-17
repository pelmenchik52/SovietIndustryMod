package com.sovietindustry.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.blocks.BlockLep;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

/**
 * Складає блок ЛЕП з кількох паралелепіпедів (координати у пікселях 0..16),
 * кожен малюється звичайною текстурою блока з обрізанням за розміром коробки.
 */
public class LepRenderer implements ISimpleBlockRenderingHandler {
    private static final double P = 1.0 / 16.0;

    // ------------------------------------------------------------ у світі
    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks r) {
        BlockLep b = (BlockLep) block;
        int meta = world.getBlockMetadata(x, y, z);

        // стійка 6x16x6
        r.setRenderBounds(5 * P, 0, 5 * P, 11 * P, 1, 11 * P);
        r.renderStandardBlock(block, x, y, z);

        if (b.getType() == BlockLep.CROSSARM) {
            boolean ax = (meta & 1) == 0;
            part(r, block, x, y, z, ax, 0, 10, 6, 16, 12, 10, b.iconArm);   // траверса
            part(r, block, x, y, z, ax, 3, 8, 7, 13, 10, 9, b.iconArm);     // підкіс під траверсою
            for (int px = 1; px <= 13; px += 6) {                            // 3 гірлянди ізоляторів
                part(r, block, x, y, z, ax, px, 3, 7, px + 2, 10, 9, b.iconInsulator);
            }
        }
        r.setRenderBounds(0, 0, 0, 1, 1, 1);
        return true;
    }

    private void part(RenderBlocks r, Block block, int x, int y, int z, boolean alongX,
                      double x1, double y1, double z1, double x2, double y2, double z2, IIcon icon) {
        if (alongX) {
            r.setRenderBounds(x1 * P, y1 * P, z1 * P, x2 * P, y2 * P, z2 * P);
        } else {
            r.setRenderBounds(z1 * P, y1 * P, x1 * P, z2 * P, y2 * P, x2 * P);
        }
        r.setOverrideBlockTexture(icon);
        r.renderStandardBlock(block, x, y, z);
        r.clearOverrideBlockTexture();
    }

    // ------------------------------------------------------------ в інвентарі
    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks r) {
        BlockLep b = (BlockLep) block;
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        invBox(r, block, 5, 0, 5, 11, 16, 11, b.iconSide, b.iconTop);
        if (b.getType() == BlockLep.CROSSARM) {
            invBox(r, block, 0, 10, 6, 16, 12, 10, b.iconArm, b.iconArm);
            invBox(r, block, 3, 8, 7, 13, 10, 9, b.iconArm, b.iconArm);
            for (int px = 1; px <= 13; px += 6) {
                invBox(r, block, px, 3, 7, px + 2, 10, 9, b.iconInsulator, b.iconInsulator);
            }
        }
        r.setRenderBounds(0, 0, 0, 1, 1, 1);
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }

    private void invBox(RenderBlocks r, Block block, double x1, double y1, double z1,
                        double x2, double y2, double z2, IIcon side, IIcon topBottom) {
        Tessellator t = Tessellator.instance;
        r.setRenderBounds(x1 * P, y1 * P, z1 * P, x2 * P, y2 * P, z2 * P);

        t.startDrawingQuads(); t.setNormal(0.0F, -1.0F, 0.0F); r.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, topBottom); t.draw();
        t.startDrawingQuads(); t.setNormal(0.0F, 1.0F, 0.0F);  r.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, topBottom); t.draw();
        t.startDrawingQuads(); t.setNormal(0.0F, 0.0F, -1.0F); r.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, side); t.draw();
        t.startDrawingQuads(); t.setNormal(0.0F, 0.0F, 1.0F);  r.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, side); t.draw();
        t.startDrawingQuads(); t.setNormal(-1.0F, 0.0F, 0.0F); r.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, side); t.draw();
        t.startDrawingQuads(); t.setNormal(1.0F, 0.0F, 0.0F);  r.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, side); t.draw();
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return BlockLep.renderId;
    }
}
