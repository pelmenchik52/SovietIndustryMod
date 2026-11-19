package com.sovietindustry;

import com.sovietindustry.tileentities.TileEntityIndustrialLamp;
import com.sovietindustry.tileentities.TileEntityIndustrialPipe;
import com.sovietindustry.tileentities.TileEntityKnifeSwitch;
import com.sovietindustry.tileentities.TileEntityLep110Angle;
import com.sovietindustry.tileentities.TileEntityLep110v1;
import com.sovietindustry.tileentities.TileEntityLepDummy;
import com.sovietindustry.tileentities.TileEntityPO2Fence;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {
    public void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityKnifeSwitch.class, "SovietKnifeSwitch");
        GameRegistry.registerTileEntity(TileEntityPO2Fence.class, "SovietPO2Fence");
        GameRegistry.registerTileEntity(TileEntityIndustrialLamp.class, "SovietIndustrialLamp");
        GameRegistry.registerTileEntity(TileEntityIndustrialPipe.class, "SovietIndustrialPipe");
        GameRegistry.registerTileEntity(TileEntityLep110v1.class, "SovietLep110v1");
        GameRegistry.registerTileEntity(TileEntityLep110Angle.class, "SovietLep110Angle");
        GameRegistry.registerTileEntity(TileEntityLepDummy.class, "SovietLepDummy");
    }

    public void registerRenderers() {}
}
