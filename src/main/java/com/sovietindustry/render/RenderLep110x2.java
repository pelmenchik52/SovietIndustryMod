package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.models.ModelLep110x2;
import com.sovietindustry.tileentities.TileEntityLep110x2Angle;
import com.sovietindustry.tileentities.TileEntityLep110x2Base;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/** Рендер обох 6-контактних опор (проміжної й анкерно-кутової) разом із дротами, гірляндами й тросом. */
@SideOnly(Side.CLIENT)
public class RenderLep110x2 extends TileEntitySpecialRenderer {
    private final ModelLep110x2 suspension = new ModelLep110x2(false);
    private final ModelLep110x2 tension = new ModelLep110x2(true);

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntityLep110x2Base lep = (TileEntityLep110x2Base) te;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glRotatef(-lep.angle, 0.0F, 1.0F, 0.0F);
        GL11.glDisable(GL11.GL_CULL_FACE);
        (te instanceof TileEntityLep110x2Angle ? tension : suspension).renderAll();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();

        LepWireRenderer.render(lep, x, y, z);
    }
}
