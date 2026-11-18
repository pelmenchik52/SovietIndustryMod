package com.sovietindustry.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TileEntity майстер-блока (основи) опори ЛЕП 110 кВ.
 * Зберігає кут повороту конструкції та координати точок кріплення 3 фазних проводів і 1 грозозахисного троса.
 */
public class TileEntityLep110v1 extends TileEntity {

    /** Кут повороту навколо осі Y в градусах (0..360, крок 30°). */
    public float angle = 0.0F;

    public TileEntityLep110v1() {
    }

    @Override
    public boolean canUpdate() {
        return false; // Не навантажує CPU тіками
    }

    public void setAngle(float angle) {
        this.angle = angle;
        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setFloat("angle", angle);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        angle = nbt.getFloat("angle");
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

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        // Охоплює повну висоту 13 блоків та виліт траверс на 4-5 блоків у кожен бік
        return AxisAlignedBB.getBoundingBox(
            xCoord - 5.0D, yCoord, zCoord - 5.0D,
            xCoord + 6.0D, yCoord + 14.5D, zCoord + 6.0D
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D; // Видимість на дистанції до 256 блоків
    }

    /**
     * Повертає світові координати точки кріплення проводу [x, y, z].
     * pin 0: нижня ліва фаза (виліт 2.3 м)
     * pin 1: нижня права фаза (виліт 3.9 м)
     * pin 2: верхня права фаза (виліт 2.3 м)
     * pin 3: грозозахисний трос на верхівці шпиля
     */
    public double[] getWireAttachment(int pin) {
        double localX = 0.0D;
        double localY = 0.0D;
        double localZ = 0.0D;

        switch (pin) {
            case 0: // Нижня ліва фаза
                localX = -2.3125D; // -37 px
                localY = 8.875D;   // ~142 px від основи
                break;
            case 1: // Нижня права фаза
                localX = 3.875D;   // +62 px
                localY = 8.875D;
                break;
            case 2: // Верхня права фаза
                localX = 2.3125D;  // +37 px
                localY = 11.125D;  // ~178 px
                break;
            case 3: // Грозозахисний трос на шпилі
                localX = 0.0D;
                localY = 13.25D;   // 212 px
                break;
            default:
                return new double[] { xCoord + 0.5D, yCoord + 1.0D, zCoord + 0.5D };
        }

        // Обертання локального вектора на кут angle навколо осі Y
        double rad = Math.toRadians(-angle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        double rotX = localX * cos - localZ * sin;
        double rotZ = localX * sin + localZ * cos;

        return new double[] {
            xCoord + 0.5D + rotX,
            yCoord + localY,
            zCoord + 0.5D + rotZ
        };
    }
}
