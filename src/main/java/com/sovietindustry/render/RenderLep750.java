package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.models.ModelLep750;
import com.sovietindustry.tileentities.TileEntityLep750Angle;
import com.sovietindustry.tileentities.TileEntityLep750Base;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/** Рендер опор 750 кВ (проміжної та анкерно-кутової) разом із дротами, гірляндами й тросами. */
@SideOnly(Side.CLIENT)
public class RenderLep750 extends TileEntitySpecialRenderer {
    private final ModelLep750 portal = new ModelLep750(false);
    private final ModelLep750 anchor = new ModelLep750(true);

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntityLep750Base lep = (TileEntityLep750Base) te;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glRotatef(-lep.angle, 0.0F, 1.0F, 0.0F);
        GL11.glDisable(GL11.GL_CULL_FACE);
        (te instanceof TileEntityLep750Angle ? anchor : portal).renderAll();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();

        LepWireRenderer.render(lep, x, y, z);
    }
}
