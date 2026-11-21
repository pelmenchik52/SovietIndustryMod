package com.sovietindustry.tileentities;

public final class Lep330 {
    private Lep330() {
    }

    public static final int HEIGHT = 17;

    // ---------------------------------------------------------------- пучок
    public static final int STRINGS = 1;
    public static final double STRING_SPACING = 0.0D;
    public static final int WIRES_PER_STRING = 2;
    public static final double WIRE_SPACING = 0.26D;

    // ---------------------------------------------------------------- П-подібна опора
    public static final int LEG_OFFSET = 3;
    public static final double LEG_X = 3.0D;
    /** Висота бетонної частини стійки. */
    public static final double P_CONCRETE_TOP = 15.2D;
    /** Висота траверси. */
    public static final double P_ARM_Y = 14.0D;
    /** Висота підвісної гірлянди (до нижнього затискача). */
    public static final double P_STRING_DROP = 1.85D;
    public static final double[] P_PHASE_X = { -5.95D, 0.0D, 5.95D };
    /** Верхівка тросостійок. */
    public static final double P_TOP = 16.9D;
    /** Винос гачка під ізолятор грозотроса вбік від осі стійки. */
    public static final double P_HOOK_OUT = 0.42D;
    /** Точка кріплення грозотроса на кінці бічного гачка (під ізолятором). */
    public static final double P_GROUND_Y = 16.55D;
    public static final double P_GROUND_X = LEG_X + P_HOOK_OUT;

    public static double pLegHalf(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, y / P_CONCRETE_TOP));
        return 0.30D + (0.20D - 0.30D) * f;
    }

    public static double[][] pMounts() {
        double[][] r = new double[3][];
        for (int i = 0; i < 3; i++) {
            r[i] = new double[] { P_PHASE_X[i], P_ARM_Y - P_STRING_DROP, 0.0D };
        }
        return r;
    }

    // ---------------------------------------------------------------- анкерно-кутова опора (Фото 3)
    public static final double A_ARM_Y = 11.4D;
    public static final double A_ARM_LEN = 4.2D;
    /** Верхній ярус траверси (перевернуті консолі: довга вліво, коротка вправо). */
    public static final double A_UPPER_Y = 15.8D;
    public static final double A_UPPER_LONG_LEN = 3.8D;
    public static final double A_UPPER_SHORT_LEN = 2.0D;
    public static final double A_MOUNT_DROP = 0.15D;
    public static final double A_STRING_LEN = 1.6D;
    public static final double A_TOP = 16.9D;
    public static final double A_GROUND_Y = 16.9D;

    private static final double[][] A_KEYS = {
        { 0.0D, 2.05D }, { 5.6D, 1.20D }, { A_ARM_Y, 0.65D }, { A_UPPER_Y, 0.45D }, { A_TOP, 0.32D }
    };

    public static double angleLegHalf(double y) {
        if (y <= A_KEYS[0][0]) return A_KEYS[0][1];
        for (int i = 0; i < A_KEYS.length - 1; i++) {
            if (y <= A_KEYS[i + 1][0]) {
                double f = (y - A_KEYS[i][0]) / (A_KEYS[i + 1][0] - A_KEYS[i][0]);
                return A_KEYS[i][1] + (A_KEYS[i + 1][1] - A_KEYS[i][1]) * f;
            }
        }
        return A_KEYS[A_KEYS.length - 1][1];
    }

    /**
     * Ліва, центральна (знизу) та права фази. Центральний провід піднімається шлейфом
     * на верхню довгу консоль і повертається назад до центру.
     */
    public static double[][] aMounts() {
        double y = A_ARM_Y - A_MOUNT_DROP;
        return new double[][] {
            { -A_ARM_LEN, y, 0.0D },
            { 0.0D,       y, 0.0D },
            { A_ARM_LEN,  y, 0.0D }
        };
    }
}