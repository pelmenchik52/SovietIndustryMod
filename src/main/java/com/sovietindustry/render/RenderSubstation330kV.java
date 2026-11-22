package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.models.ModelSubstation330kV;
import com.sovietindustry.tileentities.Substation330;
import com.sovietindustry.tileentities.TileEntitySubstation330kV;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/** Рендер підстанції 330 кВ: модель плюс дроти й гірлянди порталу входу. */
@SideOnly(Side.CLIENT)
public class RenderSubstation330kV extends TileEntitySpecialRenderer {

    private final ModelSubstation330kV model = new ModelSubstation330kV();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntitySubstation330kV sub = (TileEntitySubstation330kV) te;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glRotatef(-sub.angle, 0.0F, 1.0F, 0.0F);
        // модель центрована по плиті 7x6, а майстер-блок стоїть у клітинці (0, 0): центр плити зсунутий по Z на -0.5
        GL11.glTranslated(Substation330.CENTER_X, 0.0D, Substation330.CENTER_Z);
        GL11.glDisable(GL11.GL_CULL_FACE);
        model.renderAll();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();

        LepWireRenderer.render(sub, x, y, z);
    }
}
