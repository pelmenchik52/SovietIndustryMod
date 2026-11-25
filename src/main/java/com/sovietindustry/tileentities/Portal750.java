package com.sovietindustry.tileentities;

/**
 * Геометрія окремого порту-порталу 750 кВ: мультиблок 13 (ширина, вісь X) x 1 (глибина) x 23 (висота).
 * Це той самий лінійний портал, що стоїть на вході підстанції 750 кВ: дві ґратчасті стійки з щоглами-блискавкоприймачами
 * і ґратчаста траверса з трьома точками кріплення фаз. Тільки тут він окремий блок і приймає багато проводів.
 *
 * Майстер-блок стоїть посередині траверси, між стійками (локальна клітинка (0, 0)). Траверса йде вздовж локальної осі X,
 * тому при установці портал розвертається впоперек погляду гравця.
 *
 * Модель малюється в системі з початком у центрі майстер-блока, без додаткового зсуву. Поворот кратний 90 градусам
 * (формули ті самі, що й у Substation750).
 */
public final class Portal750 {
    private Portal750() {
    }

    public static final int LX_MIN = -6;
    public static final int LX_MAX = 6;
    public static final int LZ_MIN = 0;
    public static final int LZ_MAX = 0;
    public static final int HEIGHT = 23;

    /** Висота бетонних фундаментів під ногами стійок. */
    public static final double FOOT_H = 0.25D;

    public static final double PORTAL_X = 3.40D;
    public static final double COLUMN_HALF_BOTTOM = 0.75D;
    public static final double COLUMN_HALF_TOP = 0.12D;
    public static final double COLUMN_HALF_BEAM = 0.38D;
    public static final double MAST_TOP = 21.8D;
    public static final double BEAM_TOP = 16.00D;
    public static final double BEAM_LOW = 15.20D;
    public static final double BEAM_X0 = -6.30D;
    public static final double BEAM_X1 = 6.30D;
    public static final double BEAM_PLANE = 0.22D;

    /** Точки кріплення гірлянд на траверсі: крайні майже на краю порталу, центральна посередині. */
    public static final double[] MOUNT_X = { -5.5D, 0.0D, 5.5D };
    public static final double MOUNT_Y = BEAM_LOW - 0.20D;
    /** Така сама довжина гірлянди, як на анкерно-кутовій опорі 750 кВ, щоб дроти двох кінців збігалися. */
    public static final double STRING_LEN = Lep750.A_STRING_LEN;

    /** Заземлювальна коробка під центром траверси (там стоїть майстер-блок). */
    public static final double PAD_HALF = 0.30D;
    public static final double PAD_H = 0.45D;

    /** Півширина ґратчастої стійки на висоті y (єдина формула для моделі й колізії). */
    public static double columnHalf(double y) {
        if (y <= BEAM_LOW) {
            double u = Math.max(0.0D, Math.min(1.0D, (BEAM_LOW - y) / (BEAM_LOW - FOOT_H)));
            return COLUMN_HALF_BEAM + (COLUMN_HALF_BOTTOM - COLUMN_HALF_BEAM) * u;
        }
        if (y <= BEAM_TOP) return COLUMN_HALF_BEAM;
        double f = Math.min(1.0D, (y - BEAM_TOP) / (MAST_TOP - BEAM_TOP));
        return COLUMN_HALF_BEAM + (COLUMN_HALF_TOP - COLUMN_HALF_BEAM) * f;
    }

    /** Точка на параболічному кабелі (грозозахисний трос між щоглами) при частці довжини f. */
    public static double[] cablePoint(double x1, double y1, double z1, double x2, double y2, double z2,
                                      double sag, double f) {
        return new double[] {
            x1 + (x2 - x1) * f,
            y1 + (y2 - y1) * f - sag * 4.0D * f * (1.0D - f),
            z1 + (z2 - z1) * f
        };
    }

    // ---------------------------------------------------------------- колізійні компоненти: {x0, x1, z0, z1, y0, y1}

    private static final double[][] COMPS = buildComps();

    private static double[][] buildComps() {
        double[][] r = new double[24][];
        int n = 0;
        // дві ґратчасті стійки: колізія ступінчаста за висотою (півширина зменшується вгору)
        double[] bandY = { FOOT_H, 3.0D, 6.0D, 9.0D, 12.0D, 15.0D, 18.0D, MAST_TOP };
        double[] bandH = new double[bandY.length - 1];
        for (int k = 0; k < bandH.length; k++) bandH[k] = columnHalf(bandY[k]) + 0.06D;
        for (int sgn = -1; sgn <= 1; sgn += 2) {
            // фундамент (на всю основу стійки, від землі)
            r[n++] = new double[] { sgn * PORTAL_X - COLUMN_HALF_BOTTOM - 0.1D, sgn * PORTAL_X + COLUMN_HALF_BOTTOM + 0.1D,
                -COLUMN_HALF_BOTTOM - 0.1D, COLUMN_HALF_BOTTOM + 0.1D, 0.0D, FOOT_H };
            for (int k = 0; k < bandH.length; k++) {
                r[n++] = new double[] { sgn * PORTAL_X - bandH[k], sgn * PORTAL_X + bandH[k],
                    -bandH[k], bandH[k], bandY[k], bandY[k + 1] };
            }
        }
        // траверса
        r[n++] = new double[] { BEAM_X0, BEAM_X1, -BEAM_PLANE - 0.06D, BEAM_PLANE + 0.06D, BEAM_LOW, BEAM_TOP };
        // заземлювальна коробка під центром (майстер-блок)
        r[n++] = new double[] { -PAD_HALF, PAD_HALF, -PAD_HALF, PAD_HALF, 0.0D, PAD_H };
        double[][] out = new double[n][];
        System.arraycopy(r, 0, out, 0, n);
        return out;
    }

    /**
     * Коробка сегмента в системі клітинки (0..1 по u, v; висота відносно низу блока) до повороту:
     * {minU, minY, minV, maxU, maxY, maxV}, або null, якщо у клітинці на цій висоті нічого немає.
     * Єдине джерело правди і для розстановки dummy-блоків, і для колізії.
     */
    public static float[] localBox(int lx, int dy, int lz) {
        if (lx < LX_MIN || lx > LX_MAX || lz < LZ_MIN || lz > LZ_MAX || dy < 0 || dy >= HEIGHT) return null;
        double u0 = 9.0D, v0 = 9.0D, y0 = 9.0D, u1 = -9.0D, v1 = -9.0D, y1 = -9.0D;
        boolean any = false;
        for (double[] c : COMPS) {
            double ax0 = Math.max(c[0], lx - 0.5D);
            double ax1 = Math.min(c[1], lx + 0.5D);
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
        if (!any) return null;
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

    // ---------------------------------------------------------------- точки кріплення

    /** Точки кріплення трьох фаз на траверсі: локальні координати (до повороту), відносно центру майстра. */
    public static double[][] mounts() {
        double[][] m = new double[MOUNT_X.length][];
        for (int i = 0; i < MOUNT_X.length; i++) {
            m[i] = new double[] { MOUNT_X[i], MOUNT_Y, 0.0D };
        }
        return m;
    }
}
