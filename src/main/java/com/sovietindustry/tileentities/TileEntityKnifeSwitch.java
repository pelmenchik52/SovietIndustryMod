package com.sovietindustry.tileentities;

import net.minecraft.tileentity.TileEntity;

public class TileEntityKnifeSwitch extends TileEntity {
    @Override
    public boolean canUpdate() {
        return false; // Економить FPS, бо модель оновлюється лише при кліку
    }
}
