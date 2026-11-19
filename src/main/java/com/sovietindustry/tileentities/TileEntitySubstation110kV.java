package com.sovietindustry.tileentities;

import java.util.ArrayList;
import java.util.List;

import com.hbm.tileentity.network.TileEntityPylonBase;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Головний контролер трансформаторної підстанції 5x5:
 * - Має 4-точковий портал (ConnectionType.QUAD) для великих опор NTM.
 * - Має кабельні виводи збоку (canConnect = true для горизонтальних сторін).
 * - Внутрішньо з'єднує свій вузол NTM із вихідним 3-фазним порталом 110 кВ.
 */
public class TileEntitySubstation110kV extends TileEntityPylonBase {

    public static final double MAX_SPAN = 128.0D;

    /** Поворот підстанції: 0, 90, 180, 270 градусів. */
    public float angle = 0.0F;

    /** Локальні координати 4 точок кріплення на порталі ВН (для великої ЛЕП NTM). */
    private static final double[][] NTM_QUAD_MOUNTS = {
        { -1.65D, 6.35D, -1.95D },
        { -0.55D, 6.35D, -1.95D },
        {  0.55D, 6.35D, -1.95D },
        {  1.65D, 6.35D, -1.95D }
    };

    public void setAngle(float angle) {
        this.angle = angle;
        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public double[] toWorldOffset(double lx, double ly, double lz) {
        double th = Math.toRadians(-angle);
        double c = Math.cos(th);
        double s = Math.sin(th);
        return new double[] { lx * c + lz * s, ly, -lx * s + lz * c };
    }

    /** Зміщення суб-порталу 110 кВ (3 фази) відносно центру підстанції. */
    public int[] getPort110Offset() {
        double[] off = toWorldOffset(0.0D, 0.0D, 2.0D);
        return new int[] { (int) Math.round(off[0]), 0, (int) Math.round(off[2]) };
    }

    /** Координати 2 кабельних портів (лівий та правий край підстанції 5x5). */
    public int[][] getCablePortOffsets() {
        double[] left = toWorldOffset(-2.0D, 0.0D, 0.0D);
        double[] right = toWorldOffset(2.0D, 0.0D, 0.0D);
        return new int[][] {
            { (int) Math.round(left[0]), 0, (int) Math.round(left[2]) },
            { (int) Math.round(right[0]), 0, (int) Math.round(right[2]) }
        };
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;

        // Підтримуємо внутрішній шиноз'єднувач між порталом NTM (QUAD) та порталом 110 кВ (TRIPLE)
        int[] pOff = getPort110Offset();
        int px = xCoord + pOff[0];
        int py = yCoord + pOff[1];
        int pz = zCoord + pOff[2];

        TileEntity sub = worldObj.getTileEntity(px, py, pz);
        if (sub instanceof TileEntitySubstationPort110kV) {
            TileEntitySubstationPort110kV port110 = (TileEntitySubstationPort110kV) sub;
            if (!isLinkedTo(px, py, pz)) {
                this.addConnection(px, py, pz);
            }
            if (!port110.isLinkedTo(xCoord, yCoord, zCoord)) {
                port110.addConnection(xCoord, yCoord, zCoord);
            }
        }
    }

    public boolean isLinkedTo(int x, int y, int z) {
        for (int[] p : getLinks()) {
            if (p[0] == x && p[1] == y && p[2] == z) return true;
        }
        return false;
    }

    @Override
    public Vec3[] getMountPos() {
        Vec3[] r = new Vec3[NTM_QUAD_MOUNTS.length];
        for (int i = 0; i < NTM_QUAD_MOUNTS.length; i++) {
            double[] w = toWorldOffset(NTM_QUAD_MOUNTS[i][0], NTM_QUAD_MOUNTS[i][1], NTM_QUAD_MOUNTS[i][2]);
            r[i] = Vec3.createVectorHelper(0.5D + w[0], w[1], 0.5D + w[2]);
        }
        return r;
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.QUAD;
    }

    @Override
    public double getMaxWireLength() {
        return MAX_SPAN;
    }

    @Override
    public boolean canConnect(ForgeDirection dir) {
        return dir != ForgeDirection.UP && dir != ForgeDirection.DOWN;
    }

    public List<int[]> getLinks() {
        List<int[]> out = new ArrayList<int[]>();
        for (Object o : connected) {
            out.add((int[]) o);
        }
        return out;
    }

    public void disconnectAndSync() {
        List<int[]> links = getLinks();
        disconnectAll();
        connected.clear();
        markDirty();
        if (worldObj != null) {
            for (int[] p : links) {
                worldObj.markBlockForUpdate(p[0], p[1], p[2]);
            }
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
        connected.clear();
        readFromNBT(pkt.func_148857_g());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        double minX = xCoord - 6.0D, minY = yCoord - 1.0D, minZ = zCoord - 6.0D;
        double maxX = xCoord + 7.0D, maxY = yCoord + 10.0D, maxZ = zCoord + 7.0D;
        for (int[] p : getLinks()) {
            minX = Math.min(minX, p[0] - 5.0D);
            minY = Math.min(minY, p[1] - 1.0D);
            minZ = Math.min(minZ, p[2] - 5.0D);
            maxX = Math.max(maxX, p[0] + 6.0D);
            maxY = Math.max(maxY, p[1] + 15.0D);
            maxZ = Math.max(maxZ, p[2] + 6.0D);
        }
        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}