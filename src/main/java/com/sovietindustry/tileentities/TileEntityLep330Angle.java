package com.sovietindustry.tileentities;

import com.sovietindustry.api.IJumperPylon;
import com.sovietindustry.api.IStandoffPylon;

import net.minecraft.util.Vec3;

/**
 * Анкерно-кутова опора 330 кВ. Один грозотрос іде через меншу верхню консоль,
 * другий — через середину верхівки опори.
 */
public class TileEntityLep330Angle extends TileEntityLep330Base implements IJumperPylon, IStandoffPylon {
    private static final double[][] GROUND = {
        { Lep330.A_UPPER_SHORT_LEN, Lep330.A_UPPER_Y - 0.15D, 0.0D },
        { 0.0D,                     Lep330.A_GROUND_Y + 0.15D, 0.0D }
    };

    public TileEntityLep330Angle() {
    }

    @Override
    protected double[][] getLocalMounts() {
        return Lep330.aMounts();
    }

    @Override
    protected double[][] getLocalGround() {
        return GROUND;
    }

    @Override
    public boolean hasAnchorStrings() {
        return true;
    }

    @Override
    public double getStringLength() {
        return Lep330.A_STRING_LEN;
    }

    // ------------------------------------------------------------ роздвоєні гірлянди

    @Override
    public int getStringsPerMount() {
        return Lep330.A_STRINGS;
    }

    @Override
    public double getStringSpacing() {
        return Lep330.A_STRING_SPACING;
    }

    @Override
    public int getWiresPerString() {
        return Lep330.A_WIRES_PER_STRING;
    }

    @Override
    public double getWireSpacing() {
        return Lep330.A_WIRE_SPACING;
    }

    // ------------------------------------------------------------ верхні гірлянди й шлейф

    @Override
    public int getJumperMount() {
        return Lep330.A_JUMPER_MOUNT;
    }

    @Override
    public Vec3[][] getJumperStrings() {
        double[][][] loc = Lep330.aJumperStrings();
        Vec3[][] r = new Vec3[loc.length][2];
        for (int i = 0; i < loc.length; i++) {
            for (int j = 0; j < 2; j++) {
                double[] w = toWorldOffset(loc[i][j][0], loc[i][j][1], loc[i][j][2]);
                r[i][j] = Vec3.createVectorHelper(0.5D + w[0], w[1], 0.5D + w[2]);
            }
        }
        return r;
    }

    @Override
    public double[] getJumperAxis() {
        double[] a = toWorldOffset(1.0D, 0.0D, 0.0D);
        return new double[] { a[0], 0.0D, a[2] };
    }

    // ------------------------------------------------------------ відступ гірлянди від каркаса

    /**
     * Корінь гірлянди виноситься від точки кріплення вздовж напрямку дроту рівно настільки, щоб вийти за зону
     * каркаса (кінець траверси або тіло опори з рамкою центральної голівки). Напрямок переводиться у локальні осі
     * опори, тому працює при будь-якому куті повороту.
     */
    @Override
    public double getStringStandoff(int mount, double dirX, double dirZ) {
        double l = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (l < 1.0E-6D) return 0.0D;
        dirX /= l;
        dirZ /= l;
        double th = Math.toRadians(-angle);
        double c = Math.cos(th);
        double s = Math.sin(th);
        double lx = Math.abs(c * dirX - s * dirZ);
        double lz = Math.abs(s * dirX + c * dirZ);
        boolean center = mount == Lep330.A_JUMPER_MOUNT;
        double hx = center ? Lep330.A_CENTER_CLEAR_X : Lep330.A_SIDE_CLEAR_X;
        double hz = center ? Lep330.A_CENTER_CLEAR_Z : Lep330.A_SIDE_CLEAR_Z;
        double t = Double.MAX_VALUE;
        if (lx > 1.0E-4D) t = Math.min(t, hx / lx);
        if (lz > 1.0E-4D) t = Math.min(t, hz / lz);
        return Math.min(t, Lep330.A_STANDOFF_MAX);
    }
}
