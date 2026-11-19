package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.sovietindustry.tileentities.TileEntityLep110Angle;
import com.sovietindustry.tileentities.TileEntityLepDummy;

/**
 * Розстановка, перевірка місця й знесення мультиблоків опор, плюс форма колізії dummy-блоків.
 * Координати dummy задаються зміщенням (dx, dy, dz) від майстер-блока.
 */
public final class LepMultiblock {
    public static final int TOWER_HEIGHT = 13;
    /** Кільце 3x3 навколо стовпа ставиться лише там, де опора ще ширша за 0.62 блока від осі. */
    private static final double RING_MIN_HALF = 0.62D;

    private LepMultiblock() {
    }

    // ------------------------------------------------------------ форма

    /** Півширина колізії анкерної опори на рівні dy (блоки від осі). */
    public static double angleHalf(int dy) {
        return TileEntityLep110Angle.legHalfWidth(dy) + 0.12D;
    }

    /** Півширина колізії проміжної опори на рівні dy (пікселі 9/8/7/6 як було). */
    public static double v1Half(int dy) {
        if (dy <= 3) return 4.5D / 16.0D;
        if (dy <= 6) return 4.0D / 16.0D;
        if (dy <= 9) return 3.5D / 16.0D;
        return 3.0D / 16.0D;
    }

    public static boolean angleHasBlock(int dx, int dy, int dz) {
        if (dy < 0 || dy >= TOWER_HEIGHT) return false;
        if (dx == 0 && dz == 0) return true;
        if (Math.abs(dx) > 1 || Math.abs(dz) > 1) return false;
        return angleHalf(dy) >= RING_MIN_HALF;
    }

    private static double[] axisRange(int d, double h) {
        if (d == 0) {
            double e = Math.min(h, 0.5D);
            return new double[] { 0.5D - e, 0.5D + e };
        }
        double e = Math.min(h, 1.5D);
        if (d > 0) return new double[] { 0.0D, e - 0.5D };
        return new double[] { 1.5D - e, 1.0D };
    }

    /** Бокс {minX,minY,minZ,maxX,maxY,maxZ} для сегмента із зміщенням (dx,dy,dz). */
    public static float[] bounds(Block master, int dx, int dy, int dz) {
        if (master == ModBlocks.substation110kV) {
            return substationBounds(dx, dy, dz);
        }
        boolean angle = master == ModBlocks.lep110Angle;
        double h = angle ? angleHalf(dy) : v1Half(dy);
        double[] ax = angle ? axisRange(dx, h) : axisRange(0, h);
        double[] az = angle ? axisRange(dz, h) : axisRange(0, h);
        return new float[] { (float) ax[0], 0.0F, (float) az[0], (float) ax[1], 1.0F, (float) az[1] };
    }

    /**
     * Колізія сегментів підстанції (симетрична, тому не залежить від повороту):
     * плита 4 пікселі заввишки, трансформатор по центру, тонкі стійки порталів по кутах.
     */
    private static float[] substationBounds(int dx, int dy, int dz) {
        boolean centre = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
        boolean pillar = Math.abs(dx) == 2 && Math.abs(dz) == 2;
        if (dy == 0 && !centre && !pillar) {
            return new float[] { 0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F };
        }
        if (pillar) {
            return new float[] { 0.30F, 0.0F, 0.30F, 0.70F, 1.0F, 0.70F };
        }
        // центр: габарит бака 2.1 x 1.56 блока, вище 2.5 блока лише вводи
        double hx = dy >= 3 ? 0.80D : 1.05D;
        double hz = dy >= 3 ? 0.60D : 1.00D;
        double[] ax = axisRange(dx, hx);
        double[] az = axisRange(dz, hz);
        return new float[] { (float) ax[0], 0.0F, (float) az[0], (float) ax[1], dy >= 3 ? 0.9F : 1.0F, (float) az[1] };
    }

    // ------------------------------------------------------------ перевірка місця

    public static boolean canPlaceAngle(World w, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!World.doesBlockHaveSolidTopSurface(w, x + dx, y - 1, z + dz)) return false;
            }
        }
        for (int dy = 0; dy < TOWER_HEIGHT; dy++) {
            if (y + dy >= 256) return false;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    if (!angleHasBlock(dx, dy, dz)) continue;
                    Block b = w.getBlock(x + dx, y + dy, z + dz);
                    if (!w.isAirBlock(x + dx, y + dy, z + dz) && !b.isReplaceable(w, x + dx, y + dy, z + dz)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------ побудова / знесення

    private static void setDummy(World w, int x, int y, int z, int mx, int my, int mz) {
        w.setBlock(x, y, z, ModBlocks.lep110Dummy, 0, 2);
        TileEntity te = w.getTileEntity(x, y, z);
        if (te instanceof TileEntityLepDummy) {
            ((TileEntityLepDummy) te).setMaster(mx, my, mz);
        }
    }

    public static void buildAngle(World w, int x, int y, int z) {
        for (int dy = 0; dy < TOWER_HEIGHT; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    if (angleHasBlock(dx, dy, dz)) setDummy(w, x + dx, y + dy, z + dz, x, y, z);
                }
            }
        }
    }

    public static void removeAngle(World w, int x, int y, int z) {
        for (int dy = 0; dy < TOWER_HEIGHT; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    if (angleHasBlock(dx, dy, dz) && w.getBlock(x + dx, y + dy, z + dz) == ModBlocks.lep110Dummy) {
                        w.setBlockToAir(x + dx, y + dy, z + dz);
                    }
                }
            }
        }
    }

    public static void buildV1(World w, int x, int y, int z) {
        for (int dy = 1; dy < TOWER_HEIGHT; dy++) {
            setDummy(w, x, y + dy, z, x, y, z);
        }
    }

    public static void removeV1(World w, int x, int y, int z) {
        for (int dy = 1; dy < TOWER_HEIGHT; dy++) {
            if (w.getBlock(x, y + dy, z) == ModBlocks.lep110Dummy) {
                w.setBlockToAir(x, y + dy, z);
            }
        }
    }
}
