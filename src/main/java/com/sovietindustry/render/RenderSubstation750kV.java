package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.models.ModelSubstation750kV;
import com.sovietindustry.tileentities.Substation750;
import com.sovietindustry.tileentities.TileEntitySubstation750kV;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/** Рендер підстанції 750 кВ: модель плюс дроти й гірлянди порталу входу. */
@SideOnly(Side.CLIENT)
public class RenderSubstation750kV extends TileEntitySpecialRenderer {

    private final ModelSubstation750kV model = new ModelSubstation750kV();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntitySubstation750kV sub = (TileEntitySubstation750kV) te;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glRotatef(-sub.angle, 0.0F, 1.0F, 0.0F);
        // модель центрована по плиті 9x9, а майстер-блок стоїть у середині краю, у клітинці (0, 0): центр плити зсунутий по Z на -4
        GL11.glTranslated(Substation750.CENTER_X, 0.0D, Substation750.CENTER_Z);
        GL11.glDisable(GL11.GL_CULL_FACE);
        model.renderAll();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();

        LepWireRenderer.render(sub, x, y, z);
    }
}
