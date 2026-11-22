package com.sovietindustry.blocks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.sovietindustry.tileentities.TileEntityLep110Angle;
import com.sovietindustry.tileentities.Lep110x2;
import com.sovietindustry.tileentities.Lep330;
import com.sovietindustry.tileentities.Lep750;
import com.sovietindustry.tileentities.Substation330;
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
        return bounds(master, dx, dy, dz, 0.0F);
    }

    /** Те саме, але з кутом повороту майстра (потрібен П-опорі 330 кВ: стійки не лежать на сітці при 30°). */
    public static float[] bounds(Block master, int dx, int dy, int dz, float rot) {
        if (master == ModBlocks.substation110kV) {
            return substationBounds(dx, dy, dz);
        }
        if (master == ModBlocks.substation330kV) {
            return Substation330.worldBounds(dx, dy, dz, rot);
        }
        if (master == ModBlocks.lep750) {
            if (dx == 0 && dz == 0) return new float[] { 0.2F, 0.0F, 0.2F, 0.8F, 0.15F, 0.8F };
            double r = p750Reach(dy, rot);
            for (int side = -1; side <= 1; side += 2) {
                double[] c = p750LegCentre(rot, side, dy);
                double x0 = Math.max(0.0D, c[0] - r - (dx - 0.5D)), x1 = Math.min(1.0D, c[0] + r - (dx - 0.5D));
                double z0 = Math.max(0.0D, c[1] - r - (dz - 0.5D)), z1 = Math.min(1.0D, c[1] + r - (dz - 0.5D));
                if (x1 - x0 > 0.02D && z1 - z0 > 0.02D) {
                    return new float[] { (float) x0, 0.0F, (float) z0, (float) x1, 1.0F, (float) z1 };
                }
            }
            return new float[] { 0.45F, 0.0F, 0.45F, 0.55F, 1.0F, 0.55F };
        }
        if (master == ModBlocks.lep750Angle) {
            double r = a750Reach(dy, rot);
            for (int idx = -1; idx <= 1; idx++) {
                double[] c = a750TowerCentre(rot, idx);
                double x0 = Math.max(0.0D, c[0] - r - (dx - 0.5D)), x1 = Math.min(1.0D, c[0] + r - (dx - 0.5D));
                double z0 = Math.max(0.0D, c[1] - r - (dz - 0.5D)), z1 = Math.min(1.0D, c[1] + r - (dz - 0.5D));
                if (x1 - x0 > 0.02D && z1 - z0 > 0.02D) {
                    return new float[] { (float) x0, 0.0F, (float) z0, (float) x1, 1.0F, (float) z1 };
                }
            }
            return new float[] { 0.45F, 0.0F, 0.45F, 0.55F, 1.0F, 0.55F };
        }
        if (master == ModBlocks.lep330) {
            if (dx == 0 && dz == 0) return new float[] { 0.2F, 0.0F, 0.2F, 0.8F, 0.15F, 0.8F };
            double r = p330Reach(dy, rot);
            for (int side = -1; side <= 1; side += 2) {
                double[] c = p330LegCentre(rot, side);
                double x0 = Math.max(0.0D, c[0] - r - (dx - 0.5D)), x1 = Math.min(1.0D, c[0] + r - (dx - 0.5D));
                double z0 = Math.max(0.0D, c[1] - r - (dz - 0.5D)), z1 = Math.min(1.0D, c[1] + r - (dz - 0.5D));
                if (x1 - x0 > 0.02D && z1 - z0 > 0.02D) {
                    return new float[] { (float) x0, 0.0F, (float) z0, (float) x1, 1.0F, (float) z1 };
                }
            }
            return new float[] { 0.45F, 0.0F, 0.45F, 0.55F, 1.0F, 0.55F };
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

    /** Центр стійки П-опори відносно центру майстра (x, z), поворот той самий, що й у TileEntityLepPylon. */
    public static double[] p330LegCentre(float angle, int side) {
        double th = Math.toRadians(-angle);
        double lx = side * Lep330.LEG_X;
        return new double[] { lx * Math.cos(th), -lx * Math.sin(th) };
    }

    /** Півширина охоплюючого квадрата стійки, повернутої на angle (квадрат із півсторони p330Half). */
    public static double p330Reach(int dy, float angle) {
        double th = Math.toRadians(angle);
        return p330Half(dy) * (Math.abs(Math.cos(th)) + Math.abs(Math.sin(th)));
    }

    /** Клітинки, яких торкається стійка на рівні dy: {dx, dz}. При 0/90/180/270 градусах це одна клітинка. */
    private static List<int[]> p330Cells(float angle, int side, int dy) {
        double[] c = p330LegCentre(angle, side);
        double r = p330Reach(dy, angle);
        int x0 = (int) Math.floor(c[0] - r + 0.5D), x1 = (int) Math.floor(c[0] + r - 1.0E-6D + 0.5D);
        int z0 = (int) Math.floor(c[1] - r + 0.5D), z1 = (int) Math.floor(c[1] + r - 1.0E-6D + 0.5D);
        List<int[]> out = new ArrayList<int[]>();
        for (int cx = x0; cx <= x1; cx++) {
            for (int cz = z0; cz <= z1; cz++) {
                out.add(new int[] { cx, cz });
            }
        }
        return out;
    }

    /** Усі 12 поворотів (крок 30°), як у решти опор мода. */
    public static boolean canPlaceP330Any(World w, int x, int y, int z) {
        return pickP330Angle(w, x, y, z, 0.0F) >= 0.0F;
    }

    /** Найближчий до preferred кут із кроком 30°, де опора вміщається; -1, якщо ніде. */
    public static float pickP330Angle(World w, int x, int y, int z, float preferred) {
        for (int k = 0; k <= 6; k++) {
            for (int sg = -1; sg <= 1; sg += 2) {
                float a = preferred + sg * 30.0F * k;
                while (a < 0.0F) a += 360.0F;
                while (a >= 360.0F) a -= 360.0F;
                if (canPlaceP330(w, x, y, z, a)) return a;
                if (k == 0) break;
            }
        }
        return -1.0F;
    }

    /** Центр і обидві стійки стоять на твердому, колони стійок вільні (центральна клітинка належить майстру). */
    public static boolean canPlaceP330(World w, int x, int y, int z, float angle) {
        if (!World.doesBlockHaveSolidTopSurface(w, x, y - 1, z)) return false;
        for (int side = -1; side <= 1; side += 2) {
            double[] c = p330LegCentre(angle, side);
            int gx = (int) Math.round(c[0]);
            int gz = (int) Math.round(c[1]);
            if (!World.doesBlockHaveSolidTopSurface(w, x + gx, y - 1, z + gz)) return false;
            for (int dy = 0; dy < Lep330.HEIGHT; dy++) {
                if (y + dy >= 256) return false;
                for (int[] cell : p330Cells(angle, side, dy)) {
                    if (!freeCell(w, x + cell[0], y + dy, z + cell[1])) return false;
                }
            }
        }
        return true;
    }

    public static void buildP330(World w, int x, int y, int z, float angle) {
        for (int side = -1; side <= 1; side += 2) {
            for (int dy = 0; dy < Lep330.HEIGHT; dy++) {
                for (int[] cell : p330Cells(angle, side, dy)) {
                    setDummy(w, x + cell[0], y + dy, z + cell[1], x, y, z);
                }
            }
        }
    }

    public static void removeP330(World w, int x, int y, int z, float angle) {
        for (int side = -1; side <= 1; side += 2) {
            for (int dy = 0; dy < Lep330.HEIGHT; dy++) {
                for (int[] cell : p330Cells(angle, side, dy)) {
                    removeOwnDummy(w, x + cell[0], y + dy, z + cell[1], x, y, z);
                }
            }
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

    // ------------------------------------------------------------ опори 750 кВ

    /** Півширина колізії стійки проміжної опори 750 кВ. */
    public static double p750Half(int dy) {
        return Math.min(0.5D, Lep750.legHalf(dy) + 0.08D);
    }

    /** Центр стійки на рівні dy відносно центру майстра (x, z) з урахуванням нахилу й повороту. */
    public static double[] p750LegCentre(float angle, int side, int dy) {
        double th = Math.toRadians(-angle);
        double lx = side * Lep750.legCentreX(dy);
        return new double[] { lx * Math.cos(th), -lx * Math.sin(th) };
    }

    public static double p750Reach(int dy, float angle) {
        double th = Math.toRadians(angle);
        return p750Half(dy) * (Math.abs(Math.cos(th)) + Math.abs(Math.sin(th)));
    }

    private static List<int[]> p750Cells(float angle, int side, int dy) {
        double[] c = p750LegCentre(angle, side, dy);
        double r = p750Reach(dy, angle);
        int x0 = (int) Math.floor(c[0] - r + 0.5D), x1 = (int) Math.floor(c[0] + r - 1.0E-6D + 0.5D);
        int z0 = (int) Math.floor(c[1] - r + 0.5D), z1 = (int) Math.floor(c[1] + r - 1.0E-6D + 0.5D);
        List<int[]> out = new ArrayList<int[]>();
        for (int cx = x0; cx <= x1; cx++) {
            for (int cz = z0; cz <= z1; cz++) {
                out.add(new int[] { cx, cz });
            }
        }
        return out;
    }

    public static boolean canPlaceP750Any(World w, int x, int y, int z) {
        return pickP750Angle(w, x, y, z, 0.0F) >= 0.0F;
    }

    public static float pickP750Angle(World w, int x, int y, int z, float preferred) {
        for (int k = 0; k <= 6; k++) {
            for (int sg = -1; sg <= 1; sg += 2) {
                float a = preferred + sg * 30.0F * k;
                while (a < 0.0F) a += 360.0F;
                while (a >= 360.0F) a -= 360.0F;
                if (canPlaceP750(w, x, y, z, a)) return a;
                if (k == 0) break;
            }
        }
        return -1.0F;
    }

    public static boolean canPlaceP750(World w, int x, int y, int z, float angle) {
        if (!World.doesBlockHaveSolidTopSurface(w, x, y - 1, z)) return false;
        for (int side = -1; side <= 1; side += 2) {
            double[] c = p750LegCentre(angle, side, 0);
            int gx = (int) Math.round(c[0]);
            int gz = (int) Math.round(c[1]);
            if (!World.doesBlockHaveSolidTopSurface(w, x + gx, y - 1, z + gz)) return false;
            for (int dy = 0; dy < Lep750.HEIGHT; dy++) {
                if (y + dy >= 256) return false;
                for (int[] cell : p750Cells(angle, side, dy)) {
                    if (!freeCell(w, x + cell[0], y + dy, z + cell[1])) return false;
                }
            }
        }
        return true;
    }

    public static void buildP750(World w, int x, int y, int z, float angle) {
        for (int side = -1; side <= 1; side += 2) {
            for (int dy = 0; dy < Lep750.HEIGHT; dy++) {
                for (int[] cell : p750Cells(angle, side, dy)) {
                    setDummy(w, x + cell[0], y + dy, z + cell[1], x, y, z);
                }
            }
        }
    }

    public static void removeP750(World w, int x, int y, int z, float angle) {
        for (int side = -1; side <= 1; side += 2) {
            for (int dy = 0; dy < Lep750.HEIGHT; dy++) {
                for (int[] cell : p750Cells(angle, side, dy)) {
                    removeOwnDummy(w, x + cell[0], y + dy, z + cell[1], x, y, z);
                }
            }
        }
    }

    // --- Анкерно-кутова 750 кВ (3 вежі як одна структура)

    // --- Анкерно-кутова 750 кВ (3 вежі як одна структура)

    public static double a750Half(int dy) {
        return Lep750.angleHalf(dy) + 0.06D;
    }

    public static double[] a750TowerCentre(float angle, int towerIdx) {
        double th = Math.toRadians(-angle);
        double tx = towerIdx * Lep750.A_TOWER_X;
        return new double[] { tx * Math.cos(th), -tx * Math.sin(th) };
    }

    public static double a750Reach(int dy, float angle) {
        double th = Math.toRadians(angle);
        return a750Half(dy) * (Math.abs(Math.cos(th)) + Math.abs(Math.sin(th)));
    }

    private static List<int[]> a750Cells(float angle, int towerIdx, int dy) {
        double[] c = a750TowerCentre(angle, towerIdx);
        double r = a750Reach(dy, angle);
        int x0 = (int) Math.floor(c[0] - r + 0.5D), x1 = (int) Math.floor(c[0] + r - 1.0E-6D + 0.5D);
        int z0 = (int) Math.floor(c[1] - r + 0.5D), z1 = (int) Math.floor(c[1] + r - 1.0E-6D + 0.5D);
        List<int[]> out = new ArrayList<int[]>();
        for (int cx = x0; cx <= x1; cx++) {
            for (int cz = z0; cz <= z1; cz++) {
                out.add(new int[] { cx, cz });
            }
        }
        return out;
    }

    public static boolean canPlaceA750Any(World w, int x, int y, int z) {
        return pickA750Angle(w, x, y, z, 0.0F) >= 0.0F;
    }

    public static float pickA750Angle(World w, int x, int y, int z, float preferred) {
        for (int k = 0; k <= 6; k++) {
            for (int sg = -1; sg <= 1; sg += 2) {
                float a = preferred + sg * 30.0F * k;
                while (a < 0.0F) a += 360.0F;
                while (a >= 360.0F) a -= 360.0F;
                if (canPlaceA750(w, x, y, z, a)) return a;
                if (k == 0) break;
            }
        }
        return -1.0F;
    }

    public static boolean canPlaceA750(World w, int x, int y, int z, float angle) {
        if (!World.doesBlockHaveSolidTopSurface(w, x, y - 1, z)) return false;
        for (int idx = -1; idx <= 1; idx++) {
            double[] c = a750TowerCentre(angle, idx);
            int gx = (int) Math.round(c[0]);
            int gz = (int) Math.round(c[1]);
            if (!World.doesBlockHaveSolidTopSurface(w, x + gx, y - 1, z + gz)) return false;
            for (int dy = 0; dy < Lep750.A_HEIGHT; dy++) {
                if (y + dy >= 256) return false;
                for (int[] cell : a750Cells(angle, idx, dy)) {
                    if (idx == 0 && dy == 0 && cell[0] == 0 && cell[1] == 0) continue;
                    if (!freeCell(w, x + cell[0], y + dy, z + cell[1])) return false;
                }
            }
        }
        return true;
    }

    public static void buildA750(World w, int x, int y, int z, float angle) {
        for (int idx = -1; idx <= 1; idx++) {
            for (int dy = 0; dy < Lep750.A_HEIGHT; dy++) {
                for (int[] cell : a750Cells(angle, idx, dy)) {
                    if (idx == 0 && dy == 0 && cell[0] == 0 && cell[1] == 0) continue;
                    setDummy(w, x + cell[0], y + dy, z + cell[1], x, y, z);
                }
            }
        }
    }

    public static void removeA750(World w, int x, int y, int z, float angle) {
        for (int idx = -1; idx <= 1; idx++) {
            for (int dy = 0; dy < Lep750.A_HEIGHT; dy++) {
                for (int[] cell : a750Cells(angle, idx, dy)) {
                    if (idx == 0 && dy == 0 && cell[0] == 0 && cell[1] == 0) continue;
                    removeOwnDummy(w, x + cell[0], y + dy, z + cell[1], x, y, z);
                }
            }
        }
    }
}
