package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;
import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.models.ModelIndustrialPipe;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderIndustrialPipe extends TileEntitySpecialRenderer {
    private static final ResourceLocation texture = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/industrial_pipe.png");
    private final ModelIndustrialPipe model = new ModelIndustrialPipe();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        int meta = te.hasWorldObj() ? te.getBlockMetadata() : 0;

        this.bindTexture(texture);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y + 0.5D, z + 0.5D);

        // Поворот труби за напрямком встановлення гравцем
        if ((meta & 1) == 1) {
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        }

        model.renderModel();

        GL11.glPopMatrix();
    }
}
