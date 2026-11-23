package com.sovietindustry;

import com.sovietindustry.tileentities.TileEntitySubstation110kV;
import com.sovietindustry.tileentities.TileEntitySubstationPort110kV;
import com.sovietindustry.tileentities.TileEntitySubstation330kV;
import com.sovietindustry.tileentities.TileEntitySubstation750kV;
import com.sovietindustry.tileentities.TileEntityIndustrialLamp;
import com.sovietindustry.tileentities.TileEntityIndustrialPipe;
import com.sovietindustry.tileentities.TileEntityKnifeSwitch;
import com.sovietindustry.tileentities.TileEntityLep110Angle;
import com.sovietindustry.tileentities.TileEntityLep110v1;
import com.sovietindustry.tileentities.TileEntityLepDummy;
import com.sovietindustry.tileentities.TileEntityPO2Fence;
import cpw.mods.fml.common.registry.GameRegistry;
import com.sovietindustry.tileentities.TileEntityLep110x2;
import com.sovietindustry.tileentities.TileEntityLep110x2Angle;
import com.sovietindustry.tileentities.TileEntityLep330;
import com.sovietindustry.tileentities.TileEntityLep330Angle;
import com.sovietindustry.tileentities.TileEntityLep750;
import com.sovietindustry.tileentities.TileEntityLep750Angle;

public class CommonProxy {
    public void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityKnifeSwitch.class, "SovietKnifeSwitch");
        GameRegistry.registerTileEntity(TileEntityPO2Fence.class, "SovietPO2Fence");
        GameRegistry.registerTileEntity(TileEntityIndustrialLamp.class, "SovietIndustrialLamp");
        GameRegistry.registerTileEntity(TileEntityIndustrialPipe.class, "SovietIndustrialPipe");
        GameRegistry.registerTileEntity(TileEntityLep110v1.class, "SovietLep110v1");
        GameRegistry.registerTileEntity(TileEntityLep110Angle.class, "SovietLep110Angle");
        GameRegistry.registerTileEntity(TileEntityLepDummy.class, "SovietLepDummy");
        GameRegistry.registerTileEntity(TileEntitySubstation110kV.class, "TileEntitySubstation110kV");
        GameRegistry.registerTileEntity(TileEntitySubstationPort110kV.class, "TileEntitySubstationPort110kV");
        GameRegistry.registerTileEntity(TileEntitySubstation330kV.class, "TileEntitySubstation330kV");
        GameRegistry.registerTileEntity(TileEntitySubstation750kV.class, "TileEntitySubstation750kV");
        GameRegistry.registerTileEntity(TileEntityLep110x2.class, "SovietLep110x2");
        GameRegistry.registerTileEntity(TileEntityLep110x2Angle.class, "SovietLep110x2Angle");
        GameRegistry.registerTileEntity(TileEntityLep330.class, "SovietLep330");
        GameRegistry.registerTileEntity(TileEntityLep330Angle.class, "SovietLep330Angle");
        GameRegistry.registerTileEntity(TileEntityLep750.class, "SovietLep750");
        GameRegistry.registerTileEntity(TileEntityLep750Angle.class, "SovietLep750Angle");
    }

    public void registerRenderers() {}
}
