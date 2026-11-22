package com.sovietindustry.tileentities;

import java.util.ArrayList;
import java.util.List;

import com.hbm.tileentity.network.TileEntityPylonBase;
import com.sovietindustry.api.IWiredPylon;

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
public abstract class TileEntityLepPylon extends TileEntityPylonBase implements IWiredPylon {

    /** Максимальна довжина прольоту 3-контактних опор 110 кВ у блоках. */
    public static final double MAX_SPAN = 75.0D;
    /** Максимум з'єднань на одну лінійну опору (вхід + вихід). */
    public static final int MAX_LINE_CONNECTIONS = 2;

    /** Кут повороту навколо Y в градусах (крок 30°). */
    public float angle = 0.0F;

    /** Локальні точки кріплення [pin][x,y,z] (блоки, від центру основи, до повороту). */
    protected abstract double[][] getLocalMounts();

    /** true: натяжні гірлянди йдуть горизонтально в бік проводу (анкерна опора). */
    @Override
    public boolean hasAnchorStrings() {
        return false;
    }

    /** Довжина натяжної гірлянди в блоках. */
    @Override
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

    @Override
    public List<int[]> getLinks() {
        List<int[]> out = new ArrayList<int[]>();
        for (Object o : connected) {
            out.add((int[]) o);
        }
        return out;
    }

    /** За замовчуванням дріт використовує всі точки кріплення. */
    @Override
    public int[] getLinkMounts(int x, int y, int z) {
        return null;
    }

    /** Висота опори над основою для рамки рендера. */
    protected double renderHeight() {
        return 15.0D;
    }

    /** true, коли вузол NTM цієї опори вже створений (створюється в першому updateEntity). */
    public boolean hasNode() {
        return node != null && !node.expired;
    }

    /** Перевірка за СИРИМ списком connected (getLinks() може ховати внутрішні зв'язки). */
    public boolean isLinkedTo(int x, int y, int z) {
        for (Object o : connected) {
            int[] p = (int[]) o;
            if (p[0] == x && p[1] == y && p[2] == z) return true;
        }
        return false;
    }

    /** Зрізає всі дроти й повідомляє клієнтів на обох кінцях. */
    public void disconnectAndSync() {
        List<int[]> links = new ArrayList<int[]>();
        for (Object o : connected) {
            links.add((int[]) o);
        }
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
        double maxX = xCoord + 6.0D, maxY = yCoord + renderHeight(), maxZ = zCoord + 6.0D;
        for (int[] p : getLinks()) {
            minX = Math.min(minX, p[0] - 5.0D);
            minY = Math.min(minY, p[1] - 1.0D);
            minZ = Math.min(minZ, p[2] - 5.0D);
            maxX = Math.max(maxX, p[0] + 6.0D);
            maxY = Math.max(maxY, p[1] + 20.0D);
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
