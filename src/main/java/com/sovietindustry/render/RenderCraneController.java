package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.models.ModelGantryCrane;
import com.sovietindustry.tileentities.TileEntityCraneController;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** Малює портальний кран у поточному положенні, з інтерполяцією між тактами. */
@SideOnly(Side.CLIENT)
public class RenderCraneController extends TileEntitySpecialRenderer {

    /** Поворот навколо Y для напрямів 0 = +X, 1 = +Z, 2 = -X, 3 = -Z. */
    private static final float[] ANGLE = { 0.0F, -90.0F, 180.0F, 90.0F };
    private static final int[] DX = { 1, 0, -1, 0 };
    private static final int[] DZ = { 0, 1, 0, -1 };

    private final ModelGantryCrane model = new ModelGantryCrane();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntityCraneController c = (TileEntityCraneController) te;
        if (!c.assembled) return;

        double s = c.prevS + (c.s - c.prevS) * partialTicks;
        double t = c.prevT + (c.t - c.prevT) * partialTicks;
        double h = c.prevH + (c.h - c.prevH) * partialTicks;
        int dir = c.dir & 3;

        // Світло беремо там, де зараз стоїть кран. Сам блок контролера непрозорий, і в його клітинці світла
        // немає, тому за стандартним освітленням TESR кран виходив майже чорним.
        World w = c.getWorldObj();
        if (w != null) {
            int bx = c.xCoord + DX[dir] * (int) Math.round(s);
            int bz = c.zCoord + DZ[dir] * (int) Math.round(s);
            int light = w.getLightBrightnessForSkyBlocks(bx, c.yCoord + 2, bz, 0);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) (light % 65536), (float) (light / 65536));
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glRotatef(ANGLE[dir], 0.0F, 1.0F, 0.0F);
        GL11.glTranslated(s, 0.0D, c.centerOffset()); // кран стоїть по центру між рейками, а не там, де контролер
        GL11.glDisable(GL11.GL_CULL_FACE);
        float r = ((c.color >> 16) & 255) / 255.0F;
        float g = ((c.color >> 8) & 255) / 255.0F;
        float b = (c.color & 255) / 255.0F;
        model.render(c.height, c.span(), t, h, r, g, b);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }
}
