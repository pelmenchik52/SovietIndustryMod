package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;
import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.models.ModelPO2Fence;
import com.sovietindustry.tileentities.TileEntityPO2Fence;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPO2Fence extends TileEntitySpecialRenderer {
    private static final ResourceLocation texture = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/po2_fence.png");
    private final ModelPO2Fence model = new ModelPO2Fence();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        int meta = te.hasWorldObj() ? te.getBlockMetadata() : 0;

        this.bindTexture(texture);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y + 0.5D, z + 0.5D);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

        // Поворот секції паркану (0/2 = по X, 1/3 = по Z)
        if ((meta & 1) == 1) {
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        }

        model.renderModel();

        GL11.glPopMatrix();
    }
}
