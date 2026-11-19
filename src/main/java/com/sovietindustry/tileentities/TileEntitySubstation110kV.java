package com.sovietindustry.tileentities;

import java.util.ArrayList;
import java.util.List;
import api.hbm.energymk2.Nodespace;

import com.hbm.tileentity.network.TileEntityPylonBase;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.sovietindustry.api.IWiredPylon;

import api.hbm.energymk2.Nodespace.PowerNode;
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
 * - портал ВН (ConnectionType.QUAD): сюди чіпляється велика ЛЕП NTM (4 дроти: 2 стовпи x 2 яруси);
 * - портал 110 кВ (TileEntitySubstationPort110kV, ConnectionType.TRIPLE) малий/середній ЛЕП NTM та наші ЛЕП;
 * - 2 кабельні виводи (помаранчеві 4x4 площадки на боках плити) для мідних кабелів NTM.
 *   Вони реалізовані як додаткові позиції вузла в Nodespace NTM, тому кабель просто
 *   ставиться впритул до плити зовні, як і до будь-якої машини NTM.
 */
public class TileEntitySubstation110kV extends TileEntityPylonBase implements IWiredPylon {

    public static final double MAX_SPAN = 128.0D;

    /** Поворот підстанції: 0, 90, 180, 270 градусів. */
    public float angle = 0.0F;

    /**
     * Точки кріплення порталу ВН (локальні координати). Порядок такий самий, як у великої опори NTM:
     * [+бік верх, +бік низ, -бік верх, -бік низ], щоб дроти між нашим порталом і опорою NTM не схрещувалися.
     */
    private static final double[][] NTM_QUAD_MOUNTS = {
        {  1.70D, 6.40D, -1.95D },
        {  1.70D, 5.40D, -1.95D },
        { -1.70D, 6.40D, -1.95D },
        { -1.70D, 5.40D, -1.95D }
    };

    public void setAngle(float angle) {
        this.angle = angle;
        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    /** Локальні (за поворотом моделі) координати -> зміщення в світі. */
    public double[] toWorldOffset(double lx, double ly, double lz) {
        double th = Math.toRadians(-angle);
        double c = Math.cos(th);
        double s = Math.sin(th);
        return new double[] { lx * c + lz * s, ly, -lx * s + lz * c };
    }

    /** Зворотне перетворення: світове зміщення (dx, dz) від центру -> локальні (lx, lz). */
    public double[] toLocalOffset(double dx, double dz) {
        double th = Math.toRadians(-angle);
        double c = Math.cos(th);
        double s = Math.sin(th);
        return new double[] { dx * c - dz * s, dx * s + dz * c };
    }

    /** Зміщення суб-порталу 110 кВ (3 фази) відносно центру підстанції. */
    public int[] getPort110Offset() {
        double[] off = toWorldOffset(0.0D, 0.0D, 2.0D);
        return new int[] { (int) Math.round(off[0]), 0, (int) Math.round(off[2]) };
    }

    /** Зміщення 2 кабельних портів (лівий та правий край плити 5x5). */
    public int[][] getCablePortOffsets() {
        double[] left = toWorldOffset(-2.0D, 0.0D, 0.0D);
        double[] right = toWorldOffset(2.0D, 0.0D, 0.0D);
        return new int[][] {
            { (int) Math.round(left[0]), 0, (int) Math.round(left[2]) },
            { (int) Math.round(right[0]), 0, (int) Math.round(right[2]) }
        };
    }

    /**
     * Яку опору NTM/нашу чіпляти, коли гравець клікнув по блоку підстанції зі зміщенням (dx, dz) від центру:
     * передня (110 кВ) сторона веде на TileEntitySubstationPort110kV, усе інше на портал ВН.
     */
    public TileEntityPylonBase getWireTarget(int dx, int dz) {
        double[] l = toLocalOffset(dx, dz);
        if (l[1] > 1.5D && worldObj != null) {
            int[] po = getPort110Offset();
            TileEntity pt = worldObj.getTileEntity(xCoord + po[0], yCoord + po[1], zCoord + po[2]);
            if (pt instanceof TileEntitySubstationPort110kV) {
                return (TileEntitySubstationPort110kV) pt;
            }
        }
        return this;
    }

    private boolean internalLinked = false;
    private int initTicks = 0;

    @Override
    public void updateEntity() {
        if (this.isInvalid()) return;
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote || this.isInvalid()) return;

        if (!internalLinked) {
            initTicks++;
            if (initTicks < 5) return;

            int[] pOff = getPort110Offset();
            int px = xCoord + pOff[0];
            int py = yCoord + pOff[1];
            int pz = zCoord + pOff[2];

            if (worldObj.blockExists(px, py, pz)) {
                TileEntity sub = worldObj.getTileEntity(px, py, pz);
                if (sub instanceof TileEntitySubstationPort110kV && !sub.isInvalid()) {
                    TileEntitySubstationPort110kV port110 = (TileEntitySubstationPort110kV) sub;
                    try {
                        if (!isLinkedTo(px, py, pz)) {
                            this.addConnection(px, py, pz);
                        }
                        if (!port110.isLinkedTo(xCoord, yCoord, zCoord)) {
                            port110.addConnection(xCoord, yCoord, zCoord);
                        }
                        internalLinked = true;
                        Nodespace.destroyNode(worldObj, xCoord, yCoord, zCoord);
                    } catch (Throwable t) {
                        initTicks = 0;
                    }
                }
            }
        }
    }

    private boolean hasRawLink(int x, int y, int z) {
        for (Object o : connected) {
            int[] p = (int[]) o;
            if (p[0] == x && p[1] == y && p[2] == z) return true;
        }
        return false;
    }

    public boolean isLinkedTo(int x, int y, int z) {
        return hasRawLink(x, y, z);
    }

    // ------------------------------------------------------------ вузол мережі NTM

    /**
     * Вузол займає центр + 2 кабельні порти (позиції на краях плити),
     * а до звичайних зв'язків пілона додаються входи для кабелів із зовнішнього боку порту.
     */
    @Override
    public PowerNode createNode() {
        int[][] ports = getCablePortOffsets();
        PowerNode node = new PowerNode(
            new BlockPos(xCoord, yCoord, zCoord),
            new BlockPos(xCoord + ports[0][0], yCoord, zCoord + ports[0][2]),
            new BlockPos(xCoord + ports[1][0], yCoord, zCoord + ports[1][2]))
            .setConnections(new DirPos(xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN));

        for (Object o : connected) {
            int[] pos = (int[]) o;
            node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
        }

        for (int[] port : ports) {
            ForgeDirection out = outward(port[0], port[2]);
            if (out == ForgeDirection.UNKNOWN) continue;
            node.addConnection(new DirPos(
                xCoord + port[0] + out.offsetX, yCoord, zCoord + port[2] + out.offsetZ, out));
        }
        return node;
    }

    private static ForgeDirection outward(int ox, int oz) {
        if (Math.abs(ox) >= Math.abs(oz)) {
            return ox > 0 ? ForgeDirection.EAST : (ox < 0 ? ForgeDirection.WEST : ForgeDirection.UNKNOWN);
        }
        return oz > 0 ? ForgeDirection.SOUTH : ForgeDirection.NORTH;
    }

    // ------------------------------------------------------------ геометрія / NTM API

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
    public boolean hasAnchorStrings() {
        return true;
    }

    @Override
    public double getStringLength() {
        return 1.05D;
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

    /** Зовнішні дроти (без внутрішнього зв'язку з порталом 110 кВ). */
    @Override
    public List<int[]> getLinks() {
        List<int[]> out = new ArrayList<int[]>();
        for (Object o : connected) {
            int[] p = (int[]) o;
            if (worldObj != null) {
                TileEntity te = worldObj.getTileEntity(p[0], p[1], p[2]);
                if (te instanceof TileEntitySubstationPort110kV) continue;
            }
            out.add(p);
        }
        return out;
    }

    public void disconnectAndSync() {
        List<int[]> links = new ArrayList<int[]>();
        for (Object o : connected) {
            links.add((int[]) o);
        }
        disconnectAll();
        connected.clear();
        internalLinked = false;
        initTicks = 0;
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
