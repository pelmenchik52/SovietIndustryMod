package com.sovietindustry.blocks;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.sovietindustry.tileentities.TileEntityLep110Angle;
import com.sovietindustry.tileentities.Lep110x2;
import com.sovietindustry.tileentities.Lep330;
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

    /** Півширина колізії 6-контактної проміжної опори (колона 1x1, звужується вгору). */
    public static double x2Half(int dy) {
        return Math.min(0.5D, Lep110x2.legHalf(dy, false) + 0.07D);
    }

    /** Півширина колізії 6-контактної анкерно-кутової опори на рівні dy. */
    public static double x2AngleHalf(int dy) {
        return Lep110x2.legHalf(dy, true) + 0.12D;
    }

    public static boolean x2AngleHasBlock(int dx, int dy, int dz) {
        if (dy < 0 || dy >= Lep110x2.HEIGHT) return false;
        if (dx == 0 && dz == 0) return true;
        if (Math.abs(dx) > 1 || Math.abs(dz) > 1) return false;
        return x2AngleHalf(dy) >= RING_MIN_HALF;
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
        if (master == ModBlocks.lep330) {
            if (dx == 0 && dz == 0) return new float[] { 0.2F, 0.0F, 0.2F, 0.8F, 0.15F, 0.8F };
            double e = p330Half(dy);
            return new float[] { (float) (0.5D - e), 0.0F, (float) (0.5D - e), (float) (0.5D + e), 1.0F, (float) (0.5D + e) };
        }
        if (master == ModBlocks.lep330Angle) {
            double h3 = a330Half(dy);
            double[] bx = axisRangeN(dx, h3);
            double[] bz = axisRangeN(dz, h3);
            return new float[] { (float) bx[0], 0.0F, (float) bz[0], (float) bx[1], 1.0F, (float) bz[1] };
        }
        if (master == ModBlocks.lep110x2) {
            double[] a = axisRange(0, x2Half(dy));
            return new float[] { (float) a[0], 0.0F, (float) a[0], (float) a[1], 1.0F, (float) a[1] };
        }
        if (master == ModBlocks.lep110x2Angle) {
            double h2 = x2AngleHalf(dy);
            double[] ax2 = axisRange(dx, h2);
            double[] az2 = axisRange(dz, h2);
            return new float[] { (float) ax2[0], 0.0F, (float) az2[0], (float) ax2[1], 1.0F, (float) az2[1] };
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

    // ------------------------------------------------------------ 6-контактні опори (висота 16)

    private static boolean freeCell(World w, int x, int y, int z) {
        Block b = w.getBlock(x, y, z);
        return w.isAirBlock(x, y, z) || b.isReplaceable(w, x, y, z);
    }

    public static boolean canPlaceX2(World w, int x, int y, int z) {
        if (!World.doesBlockHaveSolidTopSurface(w, x, y - 1, z)) return false;
        for (int dy = 1; dy < Lep110x2.HEIGHT; dy++) {
            if (y + dy >= 256 || !freeCell(w, x, y + dy, z)) return false;
        }
        return true;
    }

    public static void buildX2(World w, int x, int y, int z) {
        for (int dy = 1; dy < Lep110x2.HEIGHT; dy++) {
            setDummy(w, x, y + dy, z, x, y, z);
        }
    }

    public static void removeX2(World w, int x, int y, int z) {
        for (int dy = 1; dy < Lep110x2.HEIGHT; dy++) {
            if (w.getBlock(x, y + dy, z) == ModBlocks.lep110Dummy) w.setBlockToAir(x, y + dy, z);
        }
    }

    public static boolean canPlaceX2Angle(World w, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!World.doesBlockHaveSolidTopSurface(w, x + dx, y - 1, z + dz)) return false;
            }
        }
        for (int dy = 0; dy < Lep110x2.HEIGHT; dy++) {
            if (y + dy >= 256) return false;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    if (x2AngleHasBlock(dx, dy, dz) && !freeCell(w, x + dx, y + dy, z + dz)) return false;
                }
            }
        }
        return true;
    }

    public static void buildX2Angle(World w, int x, int y, int z) {
        for (int dy = 0; dy < Lep110x2.HEIGHT; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    if (x2AngleHasBlock(dx, dy, dz)) setDummy(w, x + dx, y + dy, z + dz, x, y, z);
                }
            }
        }
    }

    public static void removeX2Angle(World w, int x, int y, int z) {
        for (int dy = 0; dy < Lep110x2.HEIGHT; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    if (x2AngleHasBlock(dx, dy, dz) && w.getBlock(x + dx, y + dy, z + dz) == ModBlocks.lep110Dummy) {
                        w.setBlockToAir(x + dx, y + dy, z + dz);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------ опори 330 кВ

    /** Півширина колізії стійки П-опори. */
    public static double p330Half(int dy) {
        return Math.min(0.5D, Lep330.pLegHalf(dy) + 0.07D);
    }

    /** Півширина колізії анкерно-кутової 330 кВ на рівні dy (береться низ блока, де опора найширша). */
    public static double a330Half(int dy) {
        return Lep330.angleLegHalf(dy) + 0.12D;
    }

    public static boolean a330HasBlock(int dx, int dy, int dz) {
        if (dy < 0 || dy >= Lep330.HEIGHT) return false;
        if (dx == 0 && dz == 0) return true;
        if (Math.abs(dx) > 2 || Math.abs(dz) > 2) return false;
        return a330Half(dy) > Math.max(Math.abs(dx), Math.abs(dz)) - 0.5D + 0.1D;
    }

    /** Діапазон коробки в клітинці зі зміщенням d для симетричної фігури півширини h (будь-яке |d|). */
    private static double[] axisRangeN(int d, double h) {
        if (d == 0) {
            double e = Math.min(h, 0.5D);
            return new double[] { 0.5D - e, 0.5D + e };
        }
        if (d > 0) {
            double hi = Math.min(1.0D, h - d + 0.5D);
            return new double[] { 0.0D, Math.max(0.05D, hi) };
        }
        double lo = Math.max(0.0D, 0.5D - h - d);
        return new double[] { Math.min(0.95D, lo), 1.0D };
    }

    /** Зміщення (dx, dz) стійки П-опори; side = -1 або +1. Поворот той самий, що й у TileEntityLepPylon. */
    public static int[] p330Leg(float angle, int side) {
        double th = Math.toRadians(-angle);
        double lx = side * Lep330.LEG_OFFSET;
        return new int[] { (int) Math.round(lx * Math.cos(th)), (int) Math.round(-lx * Math.sin(th)) };
    }

    /** Чи можна поставити П-опору хоч в одній з двох орієнтацій (до відомого повороту). */
    public static boolean canPlaceP330Any(World w, int x, int y, int z) {
        return canPlaceP330(w, x, y, z, 0.0F) || canPlaceP330(w, x, y, z, 90.0F);
    }

    /** Центр і обидві стійки стоять на твердому, колони стійок вільні (центральна клітинка належить майстру). */
    public static boolean canPlaceP330(World w, int x, int y, int z, float angle) {
        if (!World.doesBlockHaveSolidTopSurface(w, x, y - 1, z)) return false;
        for (int side = -1; side <= 1; side += 2) {
            int[] l = p330Leg(angle, side);
            if (!World.doesBlockHaveSolidTopSurface(w, x + l[0], y - 1, z + l[1])) return false;
            for (int dy = 0; dy < Lep330.HEIGHT; dy++) {
                if (y + dy >= 256 || !freeCell(w, x + l[0], y + dy, z + l[1])) return false;
            }
        }
        return true;
    }

    public static void buildP330(World w, int x, int y, int z, float angle) {
        for (int side = -1; side <= 1; side += 2) {
            int[] l = p330Leg(angle, side);
            for (int dy = 0; dy < Lep330.HEIGHT; dy++) {
                setDummy(w, x + l[0], y + dy, z + l[1], x, y, z);
            }
        }
    }

    public static void removeP330(World w, int x, int y, int z, float angle) {
        for (int side = -1; side <= 1; side += 2) {
            int[] l = p330Leg(angle, side);
            for (int dy = 0; dy < Lep330.HEIGHT; dy++) removeOwnDummy(w, x + l[0], y + dy, z + l[1], x, y, z);
        }
    }

    /** Знімає dummy лише якщо він належить саме цьому майстру (не чіпає чужі опори). */
    private static void removeOwnDummy(World w, int x, int y, int z, int mx, int my, int mz) {
        if (w.getBlock(x, y, z) != ModBlocks.lep110Dummy) return;
        TileEntity te = w.getTileEntity(x, y, z);
        if (te instanceof TileEntityLepDummy) {
            TileEntityLepDummy d = (TileEntityLepDummy) te;
            if (d.valid && d.mx == mx && d.my == my && d.mz == mz) w.setBlockToAir(x, y, z);
        }
    }

    public static boolean canPlaceA330(World w, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!World.doesBlockHaveSolidTopSurface(w, x + dx, y - 1, z + dz)) return false;
            }
        }
        for (int dy = 0; dy < Lep330.HEIGHT; dy++) {
            if (y + dy >= 256) return false;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    if (a330HasBlock(dx, dy, dz) && !freeCell(w, x + dx, y + dy, z + dz)) return false;
                }
            }
        }
        return true;
    }

    public static void buildA330(World w, int x, int y, int z) {
        for (int dy = 0; dy < Lep330.HEIGHT; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    if (a330HasBlock(dx, dy, dz)) setDummy(w, x + dx, y + dy, z + dz, x, y, z);
                }
            }
        }
    }

    public static void removeA330(World w, int x, int y, int z) {
        for (int dy = 0; dy < Lep330.HEIGHT; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    if (a330HasBlock(dx, dy, dz)) removeOwnDummy(w, x + dx, y + dy, z + dz, x, y, z);
                }
            }
        }
    }
}
