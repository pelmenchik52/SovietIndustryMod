package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;
import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.models.ModelIndustrialLamp;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderIndustrialLamp extends TileEntitySpecialRenderer {
    private static final ResourceLocation texture = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/industrial_lamp.png");
    private final ModelIndustrialLamp model = new ModelIndustrialLamp();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        this.bindTexture(texture);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y + 0.5D, z + 0.5D);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

        model.renderModel();

        GL11.glPopMatrix();
    }
}
