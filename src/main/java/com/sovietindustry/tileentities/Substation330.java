package com.sovietindustry.tileentities;

/**
 * Геометрія трансформаторної підстанції 330 кВ: мультиблок 6 (ширина) x 7 (довжина) x 8 (висота).
 *
 * Локальна система координат (до повороту): майстер-блок у клітинці (0, 0), крок клітинки 1 блок.
 *   lx = -3..+2 (6 клітинок), lz = -3..+3 (7 клітинок), висота 8 блоків.
 *   Портал входу 330 кВ на краю lz = -3, три кабельні виводи на протилежному краї lz = +3.
 *
 * Модель малюється в "центрованій" системі (x', z): центр плити в (0, 0), x' = lx + 0.5 - (клітинка).
 * Тобто локальний x = x' + CENTER_X, де CENTER_X = -0.5 (ширина 6 парна, центр плити лежить на межі клітинок).
 *
 * Поворот лише кратний 90 градусам. Формули ті самі, що й у TileEntityLepPylon.toWorldOffset.
 */
public final class Substation330 {
    private Substation330() {
    }

    public static final int LX_MIN = -3;
    public static final int LX_MAX = 2;
    public static final int LZ_MIN = -3;
    public static final int LZ_MAX = 3;
    public static final int WIDTH = 6;
    public static final int LENGTH = 7;
    public static final int HEIGHT = 8;

    /** Локальний x центру плити (центр моделі). */
    public static final double CENTER_X = -0.5D;
    public static final double SLAB_H = 0.25D;

    // ---------------------------------------------------------------- портал входу 330 кВ (центрований x')
    public static final double PORTAL_Z = -3.0D;
    public static final double PORTAL_X = 2.2D;
    public static final double PORTAL_COLUMN_H = 7.55D;
    /** Верхній пояс верхнього ярусу траверси, нижній пояс на TIER_DEPTH нижче. */
    public static final double TIER_TOP = 7.55D;
    public static final double TIER_LOW = 6.55D;
    public static final double TIER_DEPTH = 0.38D;
    public static final double[] PHASE_X = { -1.7D, 0.0D, 1.7D };
    public static final double MOUNT_Y = TIER_TOP - TIER_DEPTH - 0.12D;
    public static final double STRING_LEN = 1.15D;

    // ---------------------------------------------------------------- трансформатор (центрований x')
    public static final double TANK_HX = 1.70D;
    public static final double TANK_HZ = 0.85D;
    public static final double TANK_BOT = 0.35D;
    public static final double TANK_TOP = 3.15D;
    public static final double LID_Y = 3.25D;

    public static final double[] HV_X = { -1.2D, 0.0D, 1.2D };
    public static final double HV_Z = -0.45D;
    public static final double HV_TOP_Y = 6.30D;
    /** Зовнішні вводи нахилені назовні (множник x верхівки). */
    public static final double HV_TILT = 1.3D;

    public static final double[] LV_X = { -1.2D, 0.0D, 1.2D };
    public static final double LV_Z = 0.80D;
    public static final double LV_TOP_Y = 4.50D;

    public static final double CONS_CX = -0.30D;
    public static final double CONS_CY = 4.30D;
    public static final double CONS_CZ = 0.20D;
    public static final double CONS_R = 0.42D;
    public static final double CONS_HALF = 1.30D;

    // ---------------------------------------------------------------- кабельні виводи (протилежний від порталу край)
    /** Клітинки lx кабельних портів на краї lz = LZ_MAX (через одну, щоб кабелі NTM не злипались). */
    public static final int[] PORT_LX = { -3, -1, 1 };
    public static final double POST_Z = 3.0D;
    /** Торець плити з кабельними виводами (центрований z). */
    public static final double LENGTH_END = 3.5D;

    /** Центрований x' клітинки lx. */
    public static double cellX(int lx) {
        return lx - CENTER_X;
    }

    // ---------------------------------------------------------------- колізійні компоненти: {x0, x1, z0, z1, y0, y1} у центрованих x'
    private static final double[][] COMPS = buildComps();

    private static double[][] buildComps() {
        double[][] r = new double[10 + PORT_LX.length][];
        int n = 0;
        // бак із радіаторами: на всю клітинку, щоб не лишалось щілин (клітинки lx -2..1, lz -1..1)
        r[n++] = new double[] { -2.0D, 2.0D, -1.5D, 1.5D, SLAB_H, 3.30D };
        // консерватор разом із опорами
        r[n++] = new double[] { CONS_CX - CONS_HALF, CONS_CX + CONS_HALF, CONS_CZ - CONS_R, CONS_CZ + CONS_R,
            3.30D, CONS_CY + CONS_R };
        // три вводи ВН (нахилені назовні), габарит від основи до верхівки
        r[n++] = new double[] { -1.75D, -1.00D, HV_Z - 0.20D, HV_Z + 0.20D, 3.30D, HV_TOP_Y + 0.12D };
        r[n++] = new double[] { -0.20D, 0.20D, HV_Z - 0.20D, HV_Z + 0.20D, 3.30D, HV_TOP_Y + 0.12D };
        r[n++] = new double[] { 1.00D, 1.75D, HV_Z - 0.20D, HV_Z + 0.20D, 3.30D, HV_TOP_Y + 0.12D };
        // три вводи 110/35 кВ (нижня сторона)
        for (double lvx : LV_X) {
            r[n++] = new double[] { lvx - 0.15D, lvx + 0.15D, LV_Z - 0.15D, LV_Z + 0.15D, 3.30D, LV_TOP_Y };
        }
        // стійки порталу
        r[n++] = new double[] { -PORTAL_X - 0.22D, -PORTAL_X + 0.22D, PORTAL_Z - 0.22D, PORTAL_Z + 0.22D, SLAB_H, 7.80D };
        r[n++] = new double[] { PORTAL_X - 0.22D, PORTAL_X + 0.22D, PORTAL_Z - 0.22D, PORTAL_Z + 0.22D, SLAB_H, 7.80D };
        // вихідні кабельні стійки (по одній на порт)
        for (int lx : PORT_LX) {
            double px = cellX(lx);
            r[n++] = new double[] { px - 0.12D, px + 0.12D, POST_Z - 0.12D, POST_Z + 0.12D, SLAB_H, 2.90D };
        }
        double[][] out = new double[n][];
        System.arraycopy(r, 0, out, 0, n);
        return out;
    }

    /**
     * Коробка сегмента в системі клітинки (0..1 по u, v; висота відносно низу блока) до повороту:
     * {minU, minY, minV, maxU, maxY, maxV}, або null, якщо у клітинці на цій висоті нічого немає.
     * Це єдине джерело правди і для розстановки dummy-блоків, і для колізії.
     */
    public static float[] localBox(int lx, int dy, int lz) {
        if (lx < LX_MIN || lx > LX_MAX || lz < LZ_MIN || lz > LZ_MAX || dy < 0 || dy >= HEIGHT) return null;
        double u0 = 9.0D, v0 = 9.0D, y0 = 9.0D, u1 = -9.0D, v1 = -9.0D, y1 = -9.0D;
        boolean any = false;
        for (double[] c : COMPS) {
            double ax0 = Math.max(c[0] + CENTER_X, lx - 0.5D);
            double ax1 = Math.min(c[1] + CENTER_X, lx + 0.5D);
            double az0 = Math.max(c[2], lz - 0.5D);
            double az1 = Math.min(c[3], lz + 0.5D);
            double ay0 = Math.max(c[4], dy);
            double ay1 = Math.min(c[5], dy + 1.0D);
            if (ax1 - ax0 < 0.02D || az1 - az0 < 0.02D || ay1 - ay0 < 0.02D) continue;
            any = true;
            u0 = Math.min(u0, ax0 - (lx - 0.5D));
            u1 = Math.max(u1, ax1 - (lx - 0.5D));
            v0 = Math.min(v0, az0 - (lz - 0.5D));
            v1 = Math.max(v1, az1 - (lz - 0.5D));
            y0 = Math.min(y0, ay0 - dy);
            y1 = Math.max(y1, ay1 - dy);
        }
        if (!any) {
            if (dy == 0) return new float[] { 0.0F, 0.0F, 0.0F, 1.0F, (float) SLAB_H, 1.0F };
            return null;
        }
        if (dy == 0) y0 = 0.0D; // обладнання стоїть на плиті, коробка від самої землі
        return new float[] { (float) u0, (float) y0, (float) v0, (float) u1, (float) y1, (float) v1 };
    }

    public static boolean hasBlock(int lx, int dy, int lz) {
        return localBox(lx, dy, lz) != null;
    }

    // ---------------------------------------------------------------- поворот (чверть-оберти)

    /** Кут у градусах -> кількість чвертей обороту 0..3 (так само, як glRotatef(-angle) у рендері). */
    public static int quarter(float angle) {
        int k = Math.round(angle / 90.0F) % 4;
        if (k < 0) k += 4;
        return k;
    }

    /** Локальне зміщення клітинки (lx, lz) -> світове {dx, dz}. */
    public static int[] toWorld(int k, int lx, int lz) {
        switch (k & 3) {
            case 1: return new int[] { -lz, lx };
            case 2: return new int[] { -lx, -lz };
            case 3: return new int[] { lz, -lx };
            default: return new int[] { lx, lz };
        }
    }

    /** Світове зміщення {dx, dz} від майстра -> локальні {lx, lz}. */
    public static int[] toLocal(int k, int dx, int dz) {
        switch (k & 3) {
            case 1: return new int[] { dz, -dx };
            case 2: return new int[] { -dx, -dz };
            case 3: return new int[] { -dz, dx };
            default: return new int[] { dx, dz };
        }
    }

    private static double[] rotUV(int k, double u, double v) {
        double du = u - 0.5D, dv = v - 0.5D;
        switch (k & 3) {
            case 1: return new double[] { 0.5D - dv, 0.5D + du };
            case 2: return new double[] { 0.5D - du, 0.5D - dv };
            case 3: return new double[] { 0.5D + dv, 0.5D - du };
            default: return new double[] { u, v };
        }
    }

    /** Коробка сегмента зі світовим зміщенням (dx, dy, dz) від майстра: {minX,minY,minZ,maxX,maxY,maxZ} у системі блока. */
    public static float[] worldBounds(int dx, int dy, int dz, float angle) {
        int k = quarter(angle);
        int[] l = toLocal(k, dx, dz);
        float[] b = localBox(l[0], dy, l[1]);
        if (b == null) return new float[] { 0.35F, 0.0F, 0.35F, 0.65F, 1.0F, 0.65F };
        double[] a = rotUV(k, b[0], b[2]);
        double[] c = rotUV(k, b[3], b[5]);
        return new float[] {
            (float) Math.min(a[0], c[0]), b[1], (float) Math.min(a[1], c[1]),
            (float) Math.max(a[0], c[0]), b[4], (float) Math.max(a[1], c[1])
        };
    }

    // ---------------------------------------------------------------- порти й точки кріплення

    /** Світові зміщення {dx, 0, dz} трьох кабельних портів. */
    public static int[][] portOffsets(float angle) {
        int k = quarter(angle);
        int[][] r = new int[PORT_LX.length][];
        for (int i = 0; i < PORT_LX.length; i++) {
            int[] o = toWorld(k, PORT_LX[i], LZ_MAX);
            r[i] = new int[] { o[0], 0, o[1] };
        }
        return r;
    }

    /** Світовий одиничний напрямок назовні від кабельного краю (локальний +z): {dx, dz}. */
    public static int[] outOffset(float angle) {
        return toWorld(quarter(angle), 0, 1);
    }

    /** Точки кріплення трьох фаз на порталі: локальні координати (до повороту), відносно центру майстра. */
    public static double[][] mounts() {
        double[][] m = new double[PHASE_X.length][];
        for (int i = 0; i < PHASE_X.length; i++) {
            m[i] = new double[] { PHASE_X[i] + CENTER_X, MOUNT_Y, PORTAL_Z };
        }
        return m;
    }
}
