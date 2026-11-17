package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;
import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.models.ModelKnifeSwitch;
import com.sovietindustry.tileentities.TileEntityKnifeSwitch;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderKnifeSwitch extends TileEntitySpecialRenderer {
    private static final ResourceLocation texture = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/knife_switch.png");
    private final ModelKnifeSwitch model = new ModelKnifeSwitch();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntityKnifeSwitch switchTE = (TileEntityKnifeSwitch) te;
        int meta = te.hasWorldObj() ? te.getBlockMetadata() : 0;
        boolean isOn = (meta & 1) == 1;
        int dir = (meta >> 1) & 3; // Орієнтація на стіні (північ, південь, схід, захід)

        this.bindTexture(texture);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y + 0.5D, z + 0.5D);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

        // Поворот залежно від стіни
        if (dir == 0) GL11.glRotatef(0.0F, 0.0F, 1.0F, 0.0F);
        else if (dir == 1) GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        else if (dir == 2) GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        else if (dir == 3) GL11.glRotatef(270.0F, 0.0F, 1.0F, 0.0F);

        model.renderModel(isOn);

        GL11.glPopMatrix();
    }
}
