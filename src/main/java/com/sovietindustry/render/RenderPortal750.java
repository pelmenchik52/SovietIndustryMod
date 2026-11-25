package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.models.ModelPortal750;
import com.sovietindustry.tileentities.TileEntityPortal750;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/** Рендер окремого порталу 750 кВ: модель плюс дроти й гірлянди до всіх підключених опор. */
@SideOnly(Side.CLIENT)
public class RenderPortal750 extends TileEntitySpecialRenderer {

    private final ModelPortal750 model = new ModelPortal750();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntityPortal750 portal = (TileEntityPortal750) te;
        // поки від сервера не прийшов кут, не малюємо: інакше на мить видно портал у неправильному положенні
        if (!portal.isSynced()) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glRotatef(-portal.angle, 0.0F, 1.0F, 0.0F);
        GL11.glDisable(GL11.GL_CULL_FACE);
        model.renderAll();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();

        LepWireRenderer.render(portal, x, y, z);
    }
}
