package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.models.ModelSubstation500kV;
import com.sovietindustry.tileentities.Substation500;
import com.sovietindustry.tileentities.TileEntitySubstation500kV;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/** Рендер підстанції 500 кВ: модель плюс дроти й гірлянди порталу входу. */
@SideOnly(Side.CLIENT)
public class RenderSubstation500kV extends TileEntitySpecialRenderer {

    private final ModelSubstation500kV model = new ModelSubstation500kV();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntitySubstation500kV sub = (TileEntitySubstation500kV) te;
        // поки від сервера не прийшов кут, не малюємо: інакше на мить видно плиту в неправильному місці
        if (!sub.isSynced()) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glRotatef(-sub.angle, 0.0F, 1.0F, 0.0F);
        // модель центрована по плиті 13x13, а майстер-блок стоїть у середині краю, у клітинці (0, 0): центр плити зсунутий по Z на -6
        GL11.glTranslated(Substation500.CENTER_X, 0.0D, Substation500.CENTER_Z);
        GL11.glDisable(GL11.GL_CULL_FACE);
        model.renderAll();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();

        LepWireRenderer.render(sub, x, y, z);
    }
}
