package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.models.ModelLep750;
import com.sovietindustry.tileentities.TileEntityLep750Angle;
import com.sovietindustry.tileentities.TileEntityLep750Base;
import com.sovietindustry.tileentities.TileEntityLep750Single;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/** Рендер опор 750 кВ (проміжної та анкерно-кутової) разом із дротами, гірляндами й тросами. */
@SideOnly(Side.CLIENT)
public class RenderLep750 extends TileEntitySpecialRenderer {
    private final ModelLep750 portal = new ModelLep750(false);
    private final ModelLep750 anchor = new ModelLep750(true);
    private final ModelLep750 single = new ModelLep750(true, true, 0);
    private final ModelLep750 singleLeft = new ModelLep750(true, true, -1);
    private final ModelLep750 singleRight = new ModelLep750(true, true, 1);

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntityLep750Base lep = (TileEntityLep750Base) te;
        if (!lep.isSynced()) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glRotatef(-lep.angle, 0.0F, 1.0F, 0.0F);
        GL11.glDisable(GL11.GL_CULL_FACE);
        ModelLep750 model = portal;
        if (te instanceof TileEntityLep750Single) {
            int v = ((TileEntityLep750Single) te).getVariant();
            model = v < 0 ? singleLeft : v > 0 ? singleRight : single;
        } else if (te instanceof TileEntityLep750Angle) {
            model = anchor;
        }
        model.renderAll();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();

        LepWireRenderer.render(lep, x, y, z);
    }
}
