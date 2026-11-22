package com.sovietindustry.tileentities;

public final class Lep750 {
    private Lep750() {
    }

    public static final double MAX_SPAN_750 = 250.0D;

    // Поздовжні подвійні гірлянди + розщеплена фаза з 4 проводів квадратом
    public static final int STRINGS = 2;
    public static final double STRING_SPACING = 0.70D; // рознос гірлянд уздовж осі Z (вздовж дроту)
    public static final int WIRES_PER_STRING = 4;
    public static final double BUNDLE_HALF = 0.22D;
    public static final double WIRE_SPACING = BUNDLE_HALF * 2.0D;

    // ---------------------------------------------------------------- проміжна опора (Фото 2: два трикутники)
    /** Висота ~24 блоки (з масивними рогами), ширина траверси 18 блоків. */
    public static final int HEIGHT = 24;

    public static final double LEG_BASE_X = 4.6D;
    public static final double LEG_TOP_Y = 19.5D;
    public static final double LEG_TOP_X = 3.8D;

    public static final double ARM_Y = 19.5D;
    public static final double ARM_HALF = 9.0D; // ширина траверси 18 блоків (-9.0 .. +9.0)

    public static final double HORN_TOP_Y = 23.4D;
    public static final double HORN_TOP_X = 4.8D;
    public static final double GROUND_X = 5.2D;
    public static final double GROUND_Y = 23.0D;

    public static final double[] PHASE_X = { -8.2D, 0.0D, 8.2D };

    public static final int DISCS = 20;
    public static final double STRING_TOP = ARM_Y - 0.10D;
    public static final double YOKE_Y = 15.55D;
    public static final double MOUNT_Y = 15.15D;

    public static double discY(int k) {
        return STRING_TOP - 0.28D - k * 0.175D;
    }

    public static double legCentreX(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, y / LEG_TOP_Y));
        return LEG_BASE_X + (LEG_TOP_X - LEG_BASE_X) * f;
    }

    public static double legHalf(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, y / LEG_TOP_Y));
        return 0.52D + (0.34D - 0.52D) * f;
    }

    public static double hornCentreX(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, (y - LEG_TOP_Y) / (HORN_TOP_Y - LEG_TOP_Y)));
        return LEG_TOP_X + (HORN_TOP_X - LEG_TOP_X) * f;
    }

    public static double hornHalf(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, (y - LEG_TOP_Y) / (HORN_TOP_Y - LEG_TOP_Y)));
        return 0.34D + (0.18D - 0.34D) * f;
    }

    // Відтяжки проміжної опори
    public static final double GUY_ANCHOR_X = 6.2D;
    public static final double GUY_ANCHOR_Z = 6.5D;

    public static double[][] pMounts() {
        return new double[][] {
            { PHASE_X[0], MOUNT_Y, 0.0D },
            { PHASE_X[1], MOUNT_Y, 0.0D },
            { PHASE_X[2], MOUNT_Y, 0.0D }
        };
    }

    // ---------------------------------------------------------------- анкерно-кутова опора (Фото 1: широка база + стрімка верхня секція)
    /** Висота ~20.5 блока, три вежі на X = -8.5, 0, +8.5 (ширина по базі ~20.5 блоків). */
    public static final int A_HEIGHT = 20;
    public static final double A_NECK_Y = 14.5D; // рівень перемички, де закінчується широка нижня башта
    public static final double A_TOP = 20.0D;
    public static final double A_TOWER_X = 8.5D;

    public static final double A_MOUNT_Y = 14.5D;
    public static final double A_STRING_LEN = 3.4D;
    public static final double A_GROUND_Y = 20.0D;
    public static final double A_CORONA_RING_R = 0.48D;
    public static final double A_STANDOFF = 0.95D;

    /** Двосекційний профіль вежі: широка основа (1.65) до перемички (0.52) і вузька стрімка верхня секція (0.52 -> 0.16). */
    public static double angleHalf(double y) {
        if (y <= A_NECK_Y) {
            double f = Math.max(0.0D, y / A_NECK_Y);
            return 1.65D + (0.52D - 1.65D) * f;
        }
        double f = Math.min(1.0D, (y - A_NECK_Y) / (A_TOP - A_NECK_Y));
        return 0.52D + (0.16D - 0.52D) * f;
    }

    public static double[][] aMounts() {
        return new double[][] {
            { -A_TOWER_X, A_MOUNT_Y, 0.0D },
            {  0.0D,      A_MOUNT_Y, 0.0D },
            {  A_TOWER_X, A_MOUNT_Y, 0.0D }
        };
    }
}