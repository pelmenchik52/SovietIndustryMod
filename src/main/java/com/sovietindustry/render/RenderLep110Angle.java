package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.models.ModelLep110Angle;
import com.sovietindustry.tileentities.TileEntityLep110Angle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderLep110Angle extends TileEntitySpecialRenderer {
    private static final ResourceLocation texture =
        new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep_steel.png");
    private final ModelLep110Angle model = new ModelLep110Angle();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntityLep110Angle lep = (TileEntityLep110Angle) te;

        this.bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glRotatef(-lep.angle, 0.0F, 1.0F, 0.0F);
        GL11.glDisable(GL11.GL_CULL_FACE);
        model.renderAll();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();

        LepWireRenderer.render(lep, x, y, z);
    }
}
