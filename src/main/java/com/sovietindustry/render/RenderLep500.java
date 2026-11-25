package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.models.ModelLep500;
import com.sovietindustry.tileentities.TileEntityLep500Angle;
import com.sovietindustry.tileentities.TileEntityLep500Base;
import com.sovietindustry.tileentities.TileEntityLep500Single;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/** Рендер опор 500 кВ (проміжної та анкерно-кутової) разом із дротами, гірляндами й тросами. */
@SideOnly(Side.CLIENT)
public class RenderLep500 extends TileEntitySpecialRenderer {
    private final ModelLep500 portal = new ModelLep500();
    private final ModelLep500 angleModel = new ModelLep500(true);
    private final ModelLep500 singleMid = new ModelLep500(0);
    private final ModelLep500 singleSide = new ModelLep500(1);

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntityLep500Base lep = (TileEntityLep500Base) te;
        if (!lep.isSynced()) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glRotatef(-lep.angle, 0.0F, 1.0F, 0.0F);
        GL11.glDisable(GL11.GL_CULL_FACE);
        if (lep instanceof TileEntityLep500Single) {
            (((TileEntityLep500Single) lep).getVariant() == TileEntityLep500Single.MID ? singleMid : singleSide).renderAll();
        } else if (lep instanceof TileEntityLep500Angle) {
            angleModel.renderAll(((TileEntityLep500Angle) lep).getLoopDir());
        } else {
            portal.renderAll();
        }
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();

        LepWireRenderer.render(lep, x, y, z);
    }
}
