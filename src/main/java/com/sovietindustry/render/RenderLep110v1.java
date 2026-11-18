package com.sovietindustry.render;

import org.lwjgl.opengl.GL11;

import com.sovietindustry.SovietIndustryMod;
import com.sovietindustry.models.ModelLep110v1;
import com.sovietindustry.tileentities.TileEntityLep110v1;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLep110v1 extends TileEntitySpecialRenderer {
    private static final ResourceLocation texture = new ResourceLocation(SovietIndustryMod.MODID, "textures/models/lep110_single.png");
    private final ModelLep110v1 model = new ModelLep110v1();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        TileEntityLep110v1 lep = (TileEntityLep110v1) te;
        float angle = lep != null ? lep.angle : 0.0F;

        this.bindTexture(texture);

        GL11.glPushMatrix();
        // Переносимося до основи стовпа на рівні землі
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        // Повертаємо відповідно до кута розміщення гравцем (із дискретним кроком 30°)
        GL11.glRotatef(-angle, 0.0F, 1.0F, 0.0F);
        // Перевертаємо систему координат моделі, щоб Y йшов вгору на всі 13 блоків
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

        model.renderAll();

        GL11.glPopMatrix();
    }
}
