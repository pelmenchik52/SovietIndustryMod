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
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Спільна база для опор ЛЕП 110 кВ. Наслідується від TileEntityPylonBase з Nuclear Tech Mod,
 * тому вузол мережі, список з'єднань (connected), збереження в NBT і передача енергії
 * працюють через NTM. Тут лише геометрія: кут повороту, точки кріплення, синхронізація.
 *
 * Три фази = ConnectionType.TRIPLE (сумісно з опорами NTM того ж типу).
 * Грозозахисний трос декоративний і енергії не несе.
 */
public abstract class TileEntityLepPylon extends TileEntityPylonBase {

    /** Максимальна довжина прольоту в блоках. */
    public static final double MAX_SPAN = 96.0D;
    /** Максимум з'єднань на одну лінійну опору (вхід + вихід). */
    public static final int MAX_LINE_CONNECTIONS = 2;

    /** Кут повороту навколо Y в градусах (крок 30°). */
    public float angle = 0.0F;

    /** Локальні точки кріплення [pin][x,y,z] (блоки, від центру основи, до повороту). */
    protected abstract double[][] getLocalMounts();

    /** true: натяжні гірлянди йдуть горизонтально в бік проводу (анкерна опора). */
    public boolean hasAnchorStrings() {
        return false;
    }

    /** Довжина натяжної гірлянди в блоках. */
    public double getStringLength() {
        return 1.2D;
    }

    public void setAngle(float angle) {
        this.angle = angle;
        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    // ------------------------------------------------------------ геометрія

    /** Той самий поворот, що й GL11.glRotatef(-angle, 0, 1, 0) у рендерері. */
    public double[] toWorldOffset(double lx, double ly, double lz) {
        double th = Math.toRadians(-angle);
        double c = Math.cos(th);
        double s = Math.sin(th);
        return new double[] { lx * c + lz * s, ly, -lx * s + lz * c };
    }

    /** Точки кріплення відносно кута блока (xCoord, yCoord, zCoord), у світових осях. */
    @Override
    public Vec3[] getMountPos() {
        double[][] m = getLocalMounts();
        Vec3[] r = new Vec3[m.length];
        for (int i = 0; i < m.length; i++) {
            double[] w = toWorldOffset(m[i][0], m[i][1], m[i][2]);
            r[i] = Vec3.createVectorHelper(0.5D + w[0], w[1], 0.5D + w[2]);
        }
        return r;
    }

    /**
     * Де фактично чіпляється провід: для підвісної опори це точка кріплення,
     * для анкерної кінець натяжної гірлянди, що дивиться в бік (towardX, towardZ).
     * Результат відносно кута блока.
     */
    public double[] getWireEnd(int pin, double towardX, double towardZ) {
        Vec3 m = getMountPos()[pin];
        double ex = m.xCoord;
        double ey = m.yCoord;
        double ez = m.zCoord;
        if (hasAnchorStrings()) {
            double len = Math.sqrt(towardX * towardX + towardZ * towardZ);
            if (len > 1.0E-4D) {
                ex += towardX / len * getStringLength();
                ez += towardZ / len * getStringLength();
            }
        }
        return new double[] { ex, ey, ez };
    }

    // ------------------------------------------------------------ NTM

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.TRIPLE;
    }

    @Override
    public double getMaxWireLength() {
        return MAX_SPAN;
    }

    /** До сторін опора нічого не під'єднує, лише дроти. */
    @Override
    public boolean canConnect(ForgeDirection dir) {
        return false;
    }

    public List<int[]> getLinks() {
        List<int[]> out = new ArrayList<int[]>();
        for (Object o : connected) {
            out.add((int[]) o);
        }
        return out;
    }

    public boolean isLinkedTo(int x, int y, int z) {
        for (int[] p : getLinks()) {
            if (p[0] == x && p[1] == y && p[2] == z) return true;
        }
        return false;
    }

    /** Зрізає всі дроти й повідомляє клієнтів на обох кінцях. */
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

    // ------------------------------------------------------------ NBT і синхронізація

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

    /** Бокс охоплює опору й усі підключені опори, щоб дроти не зникали. */
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        double minX = xCoord - 5.0D, minY = yCoord - 1.0D, minZ = zCoord - 5.0D;
        double maxX = xCoord + 6.0D, maxY = yCoord + 15.0D, maxZ = zCoord + 6.0D;
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
