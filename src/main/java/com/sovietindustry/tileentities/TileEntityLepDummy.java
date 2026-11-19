package com.sovietindustry.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/** Зберігає координати майстер-блока, до якого належить цей сегмент опори. */
public class TileEntityLepDummy extends TileEntity {

    public int mx, my, mz;
    public boolean valid = false;

    @Override
    public boolean canUpdate() {
        return false;
    }

    public void setMaster(int x, int y, int z) {
        mx = x;
        my = y;
        mz = z;
        valid = true;
        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("valid", valid);
        nbt.setInteger("mx", mx);
        nbt.setInteger("my", my);
        nbt.setInteger("mz", mz);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        valid = nbt.getBoolean("valid");
        mx = nbt.getInteger("mx");
        my = nbt.getInteger("my");
        mz = nbt.getInteger("mz");
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }
}
