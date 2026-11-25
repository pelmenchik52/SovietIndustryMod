package com.sovietindustry.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import api.hbm.energymk2.IEnergyConnectorMK2;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/** Зберігає координати майстер-блока, до якого належить цей сегмент опори. */
public class TileEntityLepDummy extends TileEntity implements IEnergyConnectorMK2 {

    public int mx, my, mz;
    public boolean valid = false;

    @Override
    public boolean canUpdate() {
        return false;
    }
    @Override
    public boolean canConnect(ForgeDirection dir) {
        if (!valid || worldObj == null) return false;
        TileEntity master = worldObj.getTileEntity(mx, my, mz);
        if (master instanceof TileEntitySubstation500kV) {
            // чотири кабельні порти на торці, протилежному порталу: кабель лише ззовні порту
            TileEntitySubstation500kV sub500 = (TileEntitySubstation500kV) master;
            return sub500.isCablePort(xCoord - mx, yCoord - my, zCoord - mz) && dir == sub500.getCableOut();
        }
        if (master instanceof TileEntitySubstation750kV) {
            // чотири кабельні порти на торці, протилежному порталу: кабель лише ззовні порту
            TileEntitySubstation750kV sub750 = (TileEntitySubstation750kV) master;
            return sub750.isCablePort(xCoord - mx, yCoord - my, zCoord - mz) && dir == sub750.getCableOut();
        }
        if (master instanceof TileEntitySubstation330kV) {
            // три кабельні порти на торці, протилежному порталу: кабель лише ззовні порту
            TileEntitySubstation330kV sub330 = (TileEntitySubstation330kV) master;
            return sub330.isCablePort(xCoord - mx, yCoord - my, zCoord - mz) && dir == sub330.getCableOut();
        }
        if (master instanceof TileEntitySubstation110kV) {
            TileEntitySubstation110kV sub = (TileEntitySubstation110kV) master;
            int[][] ports = sub.getCablePortOffsets();
            int dx = xCoord - mx;
            int dz = zCoord - mz;
            for (int[] p : ports) {
                if (p[0] == dx && p[2] == dz) {
                    // Дозволяємо підключення кабелю лише із зовнішнього боку цього порту
                    if (dx > 0 && dir == ForgeDirection.EAST) return true;
                    if (dx < 0 && dir == ForgeDirection.WEST) return true;
                    if (dz > 0 && dir == ForgeDirection.SOUTH) return true;
                    if (dz < 0 && dir == ForgeDirection.NORTH) return true;
                }
            }
        }
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
