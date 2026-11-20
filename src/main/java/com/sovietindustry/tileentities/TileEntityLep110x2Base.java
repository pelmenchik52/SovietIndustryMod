package com.sovietindustry.tileentities;

import java.util.ArrayList;
import java.util.List;

import com.hbm.tileentity.network.TileEntityPylonBase;
import com.sovietindustry.api.IGroundWired;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;

/**
 * База 6-контактних опор. Усі 6 дротів це два 3-фазні кола (група 0 і група 1).
 *
 * Для NTM опора лишається одним вузлом ConnectionType.TRIPLE (енергія йде мережею як зазвичай),
 * а розбиття на кола суто візуальне й конструктивне:
 *  - дріт до іншої 6-контактної опори займає всі 6 точок;
 *  - дріт до 3-контактної опори (лише анкерно-кутової) займає одну групу з 3 точок,
 *    так 6-контактна АКО розходиться на дві 3-контактні.
 * Кожна точка кріплення витримує не більше двох дротів (вхід + вихід).
 */
public abstract class TileEntityLep110x2Base extends TileEntityLepPylon implements IGroundWired {

    public static final int MAX_PER_MOUNT = 2;

    /** Зв'язки з 3-контактними сусідами: {x, y, z, група}. */
    private final List<int[]> groups = new ArrayList<int[]>();

    /** Глибина точки кріплення під траверсою. */
    protected abstract double mountDrop();

    @Override
    protected double[][] getLocalMounts() {
        double[][] r = new double[6][];
        for (int i = 0; i < 6; i++) {
            r[i] = Lep110x2.mount(i, mountDrop());
        }
        return r;
    }

    @Override
    public Vec3 getGroundMount() {
        double[] w = toWorldOffset(0.0D, Lep110x2.GROUND_Y, 0.0D);
        return Vec3.createVectorHelper(0.5D + w[0], w[1], 0.5D + w[2]);
    }

    @Override
    protected double renderHeight() {
        return 19.0D;
    }

    // ------------------------------------------------------------ групи

    private int groupOf(int x, int y, int z) {
        for (int[] g : groups) {
            if (g[0] == x && g[1] == y && g[2] == z) return g[3];
        }
        return -1;
    }

    private void forgetGroup(int x, int y, int z) {
        for (int i = groups.size() - 1; i >= 0; i--) {
            int[] g = groups.get(i);
            if (g[0] == x && g[1] == y && g[2] == z) groups.remove(i);
        }
    }

    @Override
    public int[] getLinkMounts(int x, int y, int z) {
        int g = groupOf(x, y, z);
        if (g >= 0) return Lep110x2.GROUPS[g];
        if (worldObj != null && !(worldObj.getTileEntity(x, y, z) instanceof TileEntityLep110x2Base)) {
            return Lep110x2.GROUPS[0]; // 3-контактний сусід без запису (дроти з котушки NTM): перше коло
        }
        return null;
    }

    /** Скільки дротів уже чіпляється до кожної з 6 точок (без дроту до (skipX, skipY, skipZ)). */
    private int[] mountUsage(int skipX, int skipY, int skipZ, boolean skip) {
        int[] counts = new int[6];
        for (int[] p : getLinks()) {
            if (skip && p[0] == skipX && p[1] == skipY && p[2] == skipZ) continue;
            int[] m = getLinkMounts(p[0], p[1], p[2]);
            if (m == null) {
                for (int i = 0; i < 6; i++) counts[i]++;
            } else {
                for (int i : m) counts[i]++;
            }
        }
        return counts;
    }

    private static int maxOf(int[] counts, int[] idx) {
        int m = 0;
        for (int i : idx) m = Math.max(m, counts[i]);
        return m;
    }

    /** Яке коло найменш завантажене, при рівності перше. */
    private int bestGroup(int[] counts) {
        return maxOf(counts, Lep110x2.GROUPS[1]) < maxOf(counts, Lep110x2.GROUPS[0]) ? 1 : 0;
    }

    /** Чи можна додати дріт до цього сусіда (перевірка до з'єднання). */
    public boolean canAcceptLink(TileEntityPylonBase other) {
        int[] counts = mountUsage(0, 0, 0, false);
        int[] cand;
        if (other instanceof TileEntityLep110x2Base) {
            cand = new int[] { 0, 1, 2, 3, 4, 5 };
        } else {
            cand = Lep110x2.GROUPS[bestGroup(counts)];
        }
        return maxOf(counts, cand) < MAX_PER_MOUNT;
    }

    /** Усі зв'язки (котушка нашого мода і NTM) проходять тут, тому коло призначається автоматично. */
    @Override
    public void addConnection(int x, int y, int z) {
        super.addConnection(x, y, z);
        if (worldObj == null || worldObj.isRemote) return;
        TileEntity other = worldObj.getTileEntity(x, y, z);
        forgetGroup(x, y, z);
        if (other != null && !(other instanceof TileEntityLep110x2Base)) {
            int g = bestGroup(mountUsage(x, y, z, true));
            groups.add(new int[] { x, y, z, g });
        }
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void disconnectAndSync() {
        super.disconnectAndSync();
        groups.clear();
    }

    // ------------------------------------------------------------ NBT

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        int[] flat = new int[groups.size() * 4];
        for (int i = 0; i < groups.size(); i++) {
            System.arraycopy(groups.get(i), 0, flat, i * 4, 4);
        }
        nbt.setIntArray("x2groups", flat);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        groups.clear();
        int[] flat = nbt.getIntArray("x2groups");
        for (int i = 0; i + 3 < flat.length; i += 4) {
            groups.add(new int[] { flat[i], flat[i + 1], flat[i + 2], flat[i + 3] });
        }
    }
}
