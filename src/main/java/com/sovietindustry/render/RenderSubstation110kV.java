package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.models.ModelSubstation110kV;
import com.sovietindustry.tileentities.TileEntitySubstation110kV;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

@SideOnly(Side.CLIENT)
public class RenderSubstation110kV extends TileEntitySpecialRenderer {

    private final ModelSubstation110kV model = new ModelSubstation110kV();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntitySubstation110kV sub = (TileEntitySubstation110kV) te;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glRotatef(-sub.angle, 0.0F, 1.0F, 0.0F);
        GL11.glDisable(GL11.GL_CULL_FACE);
        model.renderAll();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();

        // дроти та гірлянди порталу ВН (раніше для головного блока не малювались зовсім)
        LepWireRenderer.render(sub, x, y, z);
    }
}
