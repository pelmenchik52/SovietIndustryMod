package com.sovietindustry.tileentities;

public final class Lep750 {
    private Lep750() {
    }

    public static final double MAX_SPAN_750 = 250.0D;

    // Подвійні гірлянди + розщеплена фаза з 4 проводів квадратом
    public static final int STRINGS = 2;
    public static final double STRING_SPACING = 0.45D;
    public static final int WIRES_PER_STRING = 4;
    public static final double BUNDLE_HALF = 0.24D; // півширина квадрата пучка (48x48 см)
    public static final double WIRE_SPACING = BUNDLE_HALF * 2.0D;

    // ---------------------------------------------------------------- проміжна опора ПП750 (Фото 2)
    /** Висота ~25.4 блока (з громовідводами), ширина траверси 24 блоки. */
    public static final int HEIGHT = 25;

    public static final double LEG_BASE_X = 7.2D;
    public static final double LEG_TOP_Y = 21.0D;
    public static final double LEG_TOP_X = 5.6D;

    public static final double ARM_Y = 21.0D;
    public static final double ARM_HALF = 12.0D; // траверса від -12.0 до +12.0 (ширина 24 блоки)

    public static final double HORN_TOP_Y = 24.8D;
    public static final double HORN_TOP_X = 6.4D;
    public static final double GROUND_X = 6.9D;
    public static final double GROUND_Y = 24.4D;

    public static final double[] PHASE_X = { -10.8D, 0.0D, 10.8D };

    public static final int DISCS = 20;
    public static final double STRING_TOP = ARM_Y - 0.10D; // 20.90D
    public static final double YOKE_Y = 16.95D;
    public static final double MOUNT_Y = 16.55D;

    public static double discY(int k) {
        return STRING_TOP - 0.30D - k * 0.18D;
    }

    public static double legCentreX(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, y / LEG_TOP_Y));
        return LEG_BASE_X + (LEG_TOP_X - LEG_BASE_X) * f;
    }

    public static double legHalf(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, y / LEG_TOP_Y));
        if (f < 0.12D) return 0.34D + (0.48D - 0.34D) * (f / 0.12D);
        return 0.48D - (0.48D - 0.36D) * ((f - 0.12D) / 0.88D);
    }

    public static double hornCentreX(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, (y - LEG_TOP_Y) / (HORN_TOP_Y - LEG_TOP_Y)));
        return LEG_TOP_X + (HORN_TOP_X - LEG_TOP_X) * f;
    }

    public static double hornHalf(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, (y - LEG_TOP_Y) / (HORN_TOP_Y - LEG_TOP_Y)));
        return 0.28D + (0.15D - 0.28D) * f;
    }

    // Відтяжки проміжної опори
    public static final double GUY_ANCHOR_X = 8.2D;
    public static final double GUY_ANCHOR_Z = 7.5D;

    public static double[][] pMounts() {
        return new double[][] {
            { PHASE_X[0], MOUNT_Y, 0.0D },
            { PHASE_X[1], MOUNT_Y, 0.0D },
            { PHASE_X[2], MOUNT_Y, 0.0D }
        };
    }

    // ---------------------------------------------------------------- анкерно-кутова опора У750 (Фото 1)
    /** Висота ~20.6 блока, три окремі пірамідальні вежі: X = -13.5, 0, +13.5 (загальна ширина ~30 блоків). */
    public static final int A_HEIGHT = 20;
    public static final double A_TOP = 20.0D;
    public static final double A_TOWER_X = 13.5D;

    public static final double A_MOUNT_Y = 15.8D;
    public static final double A_STRING_LEN = 3.8D;
    public static final double A_GROUND_Y = 20.0D;
    public static final double A_CORONA_RING_R = 0.52D;
    public static final double A_STANDOFF = 1.15D; // винос кріплення від осі вежі вздовж дроту

    public static double angleHalf(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, y / A_TOP));
        return 1.35D + (0.24D - 1.35D) * f;
    }

    public static double[][] aMounts() {
        return new double[][] {
            { -A_TOWER_X, A_MOUNT_Y, 0.0D },
            {  0.0D,      A_MOUNT_Y, 0.0D },
            {  A_TOWER_X, A_MOUNT_Y, 0.0D }
        };
    }
}