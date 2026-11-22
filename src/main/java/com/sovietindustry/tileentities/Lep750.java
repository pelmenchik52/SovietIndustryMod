package com.sovietindustry.tileentities;

public final class Lep750 {
    private Lep750() {
    }

    public static final double MAX_SPAN_750 = 250.0D;

    // Пучок із 4 проводів
    public static final int STRINGS = 1;
    public static final double STRING_SPACING = 0.0D;
    public static final int WIRES_PER_STRING = 4;
    public static final double BUNDLE_HALF = 0.20D; // півширина квадрата пучка (40x40 см)
    public static final double WIRE_SPACING = BUNDLE_HALF * 2.0D;

    // ---------------------------------------------------------------- проміжна опора (Фото 1)
    /** Висота 21 блок, ширина 15 блоків (гармонійно для Minecraft, вище за 330 кВ). */
    public static final int HEIGHT = 21;

    public static final double LEG_BASE_X = 4.4D;
    public static final double LEG_TOP_Y = 17.5D;
    public static final double LEG_TOP_X = 3.2D;

    public static final double ARM_Y = 17.5D;
    public static final double ARM_HALF = 7.5D; // траверса від -7.5 до +7.5 (ширина 15 блоків)

    public static final double HORN_TOP_Y = 20.3D;
    public static final double HORN_TOP_X = 3.8D;
    public static final double GROUND_X = 4.2D;
    public static final double GROUND_Y = 20.0D;

    public static final double[] PHASE_X = { -6.5D, 0.0D, 6.5D };

    public static final int DISCS = 20;
    public static final double STRING_TOP = ARM_Y - 0.08D; // 17.42D
    public static final double YOKE_Y = 15.35D;
    public static final double MOUNT_Y = 15.00D;

    public static double discY(int k) {
        return STRING_TOP - 0.25D - k * 0.08D;
    }

    public static double legCentreX(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, y / LEG_TOP_Y));
        return LEG_BASE_X + (LEG_TOP_X - LEG_BASE_X) * f;
    }

    public static double legHalf(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, y / LEG_TOP_Y));
        if (f < 0.15D) return 0.26D + (0.36D - 0.26D) * (f / 0.15D);
        return 0.36D - (0.36D - 0.28D) * ((f - 0.15D) / 0.85D);
    }

    public static double hornCentreX(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, (y - LEG_TOP_Y) / (HORN_TOP_Y - LEG_TOP_Y)));
        return LEG_TOP_X + (HORN_TOP_X - LEG_TOP_X) * f;
    }

    public static double hornHalf(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, (y - LEG_TOP_Y) / (HORN_TOP_Y - LEG_TOP_Y)));
        return 0.20D + (0.13D - 0.20D) * f;
    }

    // Відтяжки проміжної опори
    public static final double GUY_LEG_Y = 12.0D;
    public static final double GUY_ANCHOR_X = 4.8D;
    public static final double GUY_ANCHOR_Z = 5.0D;
    public static final double GUY_TIP_X = 8.8D;

    public static double[][] pMounts() {
        return new double[][] {
            { PHASE_X[0], MOUNT_Y, 0.0D },
            { PHASE_X[1], MOUNT_Y, 0.0D },
            { PHASE_X[2], MOUNT_Y, 0.0D }
        };
    }

    // ---------------------------------------------------------------- анкерно-кутова опора (Фото 2)
    /** Висота 19 блоків, три окремі вежі: X = -5.8, X = 0, X = +5.8 (загальна ширина ~13-14 блоків). */
    public static final int A_HEIGHT = 19;
    public static final double A_TOP = 19.0D;
    public static final double A_TOWER_X = 5.8D;

    public static final double A_MOUNT_Y = 16.5D;
    public static final double A_STRING_LEN = 2.1D;
    public static final double A_GROUND_Y = 19.0D;
    public static final double A_CORONA_RING_R = 0.40D;
    public static final double A_STANDOFF = 0.85D; // винос кріплення від осі вежі вздовж дроту

    public static double angleHalf(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, y / A_TOP));
        return 0.70D + (0.18D - 0.70D) * f;
    }

    public static double[][] aMounts() {
        return new double[][] {
            { -A_TOWER_X, A_MOUNT_Y, 0.0D },
            { 0.0D,       A_MOUNT_Y, 0.0D },
            {  A_TOWER_X, A_MOUNT_Y, 0.0D }
        };
    }
}
