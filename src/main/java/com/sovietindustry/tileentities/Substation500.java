package com.sovietindustry.tileentities;

/**
 * Геометрія трансформаторної підстанції 500 кВ: мультиблок 13 (ширина, вісь X) x 13 (довжина, вісь Z) x 20 (висота).
 *
 * Майстер-блок стоїть у середині КРАЙНЬОГО боку плити, у клітинці (0, 0): це той бік, на який дивиться гравець
 * у момент установки. Плита йде від майстра ДО гравця (lz = -12..0), тому підстанція займає простір перед ним.
 *
 * Компоновка (як у реальному відкритому розподільчому пристрої 500 кВ), уздовж довжини від входу до виходу:
 *   ряд lz=0      : лінійний портал (дві бетонні стійки з блискавкоприймальними щоглами й ґратчаста траверса),
 *                   сюди заходить дріт ЛИШЕ від анкерно-кутової опори 500 кВ (розщеплена фаза 2x4 дроти);
 *   ряди lz=-1..-2: лінійна комірка: на кожну фазу роз'єднувач і обмежувач перенапруг (ОПН);
 *   ряди lz=-3..-7: силовий (авто)трансформатор: бак, радіатори, консерватор, вводи ВН нагору, вводи НН;
 *   ряд lz=-8     : чотири вихідні кабельні порти (повні блоки) для кабелів NTM.
 * Додатково два блискавкоприймальні щогли по кутах і шафа керування.
 *
 * Модель малюється в "центрованій" системі (x', z'): центр плити (0, 0), x' -6.5..+6.5, z' -6.5..+6.5.
 *   локальний z = z' + CENTER_Z (CENTER_Z = -6), локальний x = x'.
 *   Клітинка lz займає z' від lz+3.5 до lz+4.5; вхід z' = +6, вихід z' = -6.
 *
 * Поворот лише кратний 90 градусам (формули ті самі, що й у Substation500).
 */
public final class Substation500 {
    private Substation500() {
    }

    public static final int LX_MIN = -6;
    public static final int LX_MAX = 6;
    public static final int LZ_MIN = -12;
    public static final int LZ_MAX = 0;
    public static final int WIDTH = 13;
    public static final int LENGTH = 13;
    public static final int HEIGHT = 23;

    /** Зсув центру моделі відносно майстер-блока (локальні координати). */
    public static final double CENTER_X = 0.0D;
    public static final double CENTER_Z = -6.0D;
    public static final double SLAB_H = 0.25D;

    // ---------------------------------------------------------------- портал входу 500 кВ (ґратчасті стійки)
    public static final double PORTAL_Z = 5.05D;
    public static final double PORTAL_X = 3.40D;
    /** Півширина основи й верху ґратчастої стійки (до COLUMN_TOP), далі тонка щогла до MAST_TOP. */
    public static final double COLUMN_HALF_BOTTOM = 0.75D;
    public static final double COLUMN_HALF_TOP = 0.12D;
    /** Півширина стійки на рівні траверси: від траверси вниз спершу майже пряма, далі плавно розходиться. */
    public static final double COLUMN_HALF_BEAM = 0.38D;
    public static final double COLUMN_TOP = 18.5D;
    public static final double MAST_TOP = 21.8D;
    public static final double BEAM_TOP = 16.00D;
    public static final double BEAM_LOW = 15.20D;
    public static final double BEAM_X0 = -6.30D;
    public static final double BEAM_X1 = 6.30D;
    public static final double BEAM_PLANE = 0.22D;
    public static final double BRACE_Y = 9.50D;
    public static final double[] PHASE_X = { -4.5D, 0.0D, 4.5D };
    /** Точки кріплення гірлянд на траверсі: крайні майже на краю порталу, центральна посередині. */
    public static final double[] MOUNT_X = { -5.5D, 0.0D, 5.5D };
    public static final double MOUNT_Y = BEAM_LOW - 0.20D;
    /** Така сама довжина гірлянди, як на анкерно-кутовій опорі 500 кВ, щоб дроти двох кінців збігалися. */
    public static final double STRING_LEN = Lep500.A_STRING_LEN;

    // ---------------------------------------------------------------- лінійна комірка (на кожну фазу три ряди)
    /** Роз'єднувач, далі трансформатор струму, далі ОПН (на шлейфі). Усе на ґратчастих підставках. */
    public static final double DISC_Z = 4.70D;
    public static final double DISC_BASE_Y = 2.20D;
    public static final double DISC_POST_TOP = 4.40D;
    public static final double DISC_BLADE_Y = 4.50D;
    public static final double DISC_HALF = 0.60D;
    public static final double CT_Z = 3.00D;
    public static final double CT_BASE_Y = 2.30D;
    public static final double CT_TOP_Y = 4.90D;
    public static final double ARR_Z = 1.60D;
    public static final double ARR_TOP_Y = 5.20D;

    // ---------------------------------------------------------------- трансформатор (той самий, що був)
    public static final double TANK_HX = 3.30D;
    public static final double TANK_HZ = 1.20D;
    public static final double TANK_CZ = -2.00D;
    public static final double TANK_BOT = 0.35D;
    public static final double TANK_TOP = 4.00D;
    public static final double LID_Y = 4.10D;

    public static final double[] HV_X = { -3.0D, 0.0D, 3.0D };
    public static final double HV_Z = -1.00D;
    public static final double HV_TOP_Y = 8.20D;
    public static final double HV_TILT = 1.20D;

    public static final double[] LV_X = { -3.0D, -1.0D, 1.0D, 3.0D };
    public static final double LV_Z = -2.80D;
    public static final double LV_TOP_Y = 6.20D;

    public static final double CONS_CX = 0.0D;
    public static final double CONS_CY = 5.25D;
    public static final double CONS_CZ = -1.90D;
    public static final double CONS_R = 0.55D;
    public static final double CONS_HALF = 2.20D;

    public static final double CAB_X = 5.20D;
    public static final double CAB_Z = -2.00D;

    /** Блискавкоприймальні щогли по виходному краю. */
    public static final double LIGHT_X = 5.9D;
    public static final double LIGHT_Z = -4.6D;
    public static final double LIGHT_TOP = 14.0D;

    // ---------------------------------------------------------------- кабельні порти (ряд lz = -12, повні блоки)
    public static final int[] PORT_LX = { -3, -1, 1, 3 };
    public static final double PORT_Z = -6.0D;
    public static final double END_Z = -6.5D;
    public static final double PORT_PAD_HALF = 2.0D / 16.0D;
    public static final double PORT_PAD_Y = 0.5D;

    /** Центрований x' клітинки lx. */
    public static double cellX(int lx) {
        return lx - CENTER_X;
    }

    /** Центрована z' ЦЕНТРА клітинки lz. */
    public static double cellZ(int lz) {
        return lz - CENTER_Z;
    }

    /** Точка на параболічному кабелі (та сама формула, що й у моделі) при частці довжини f. */
    public static double[] cablePoint(double x1, double y1, double z1, double x2, double y2, double z2,
                                      double sag, double f) {
        return new double[] {
            x1 + (x2 - x1) * f,
            y1 + (y2 - y1) * f - sag * 4.0D * f * (1.0D - f),
            z1 + (z2 - z1) * f
        };
    }

    // ---------------------------------------------------------------- колізійні компоненти: {x0, x1, z0, z1, y0, y1} у центрованих x', z'
    /** Півширина ґратчастої стійки порталу на висоті y (єдина формула для моделі й колізії). */
    public static double columnHalf(double y) {
        if (y <= BEAM_LOW) {
            double u = Math.max(0.0D, Math.min(1.0D, (BEAM_LOW - y) / (BEAM_LOW - SLAB_H)));
            return COLUMN_HALF_BEAM + (COLUMN_HALF_BOTTOM - COLUMN_HALF_BEAM) * u;
        }
        if (y <= BEAM_TOP) return COLUMN_HALF_BEAM;
        double f = Math.min(1.0D, (y - BEAM_TOP) / (MAST_TOP - BEAM_TOP));
        return COLUMN_HALF_BEAM + (COLUMN_HALF_TOP - COLUMN_HALF_BEAM) * f;
    }

    private static final double[][] COMPS = buildComps();

    private static double[][] buildComps() {
        double[][] r = new double[72][];
        int n = 0;
        r[n++] = new double[] { -TANK_HX - 0.2D, TANK_HX + 0.2D, TANK_CZ - TANK_HZ - 0.55D, TANK_CZ + TANK_HZ + 0.55D, SLAB_H, LID_Y };
        r[n++] = new double[] { CONS_CX - CONS_HALF, CONS_CX + CONS_HALF, CONS_CZ - CONS_R, CONS_CZ + CONS_R, LID_Y, CONS_CY + CONS_R };
        r[n++] = new double[] { -3.80D, -2.80D, HV_Z - 0.22D, HV_Z + 0.22D, LID_Y, HV_TOP_Y + 0.12D };
        r[n++] = new double[] { -0.22D, 0.22D, HV_Z - 0.22D, HV_Z + 0.22D, LID_Y, HV_TOP_Y + 0.12D };
        r[n++] = new double[] { 2.80D, 3.80D, HV_Z - 0.22D, HV_Z + 0.22D, LID_Y, HV_TOP_Y + 0.12D };
        for (double lvx : LV_X) {
            r[n++] = new double[] { lvx - 0.15D, lvx + 0.15D, LV_Z - 0.15D, LV_Z + 0.15D, LID_Y, LV_TOP_Y };
        }
        // дві ґратчасті стійки порталу: ноги розходяться до основи, угорі стійка тонка й переходить у щоглу;
        // колізія ступінчаста за висотою (півширина зменшується вгору)
        double[] bandY = { SLAB_H, 3.0D, 6.0D, 9.0D, 12.0D, 15.0D, 18.0D, MAST_TOP };
        double[] bandH = new double[bandY.length - 1];
        for (int k = 0; k < bandH.length; k++) bandH[k] = columnHalf(bandY[k]) + 0.06D;
        for (int sgn = -1; sgn <= 1; sgn += 2) {
            for (int k = 0; k < bandH.length; k++) {
                r[n++] = new double[] { sgn * PORTAL_X - bandH[k], sgn * PORTAL_X + bandH[k],
                    PORTAL_Z - bandH[k], PORTAL_Z + bandH[k], bandY[k], bandY[k + 1] };
            }
        }
        for (double px : PHASE_X) {
            // роз'єднувач
            r[n++] = new double[] { px - 0.80D, px + 0.80D, DISC_Z - 0.35D, DISC_Z + 0.35D, SLAB_H, DISC_BLADE_Y + 0.15D };
            // трансформатор струму
            r[n++] = new double[] { px - 0.40D, px + 0.40D, CT_Z - 0.40D, CT_Z + 0.40D, SLAB_H, CT_TOP_Y };
            // ОПН
            r[n++] = new double[] { px - 0.25D, px + 0.25D, ARR_Z - 0.25D, ARR_Z + 0.25D, SLAB_H, ARR_TOP_Y };
        }
        r[n++] = new double[] { CAB_X - 0.22D, CAB_X + 0.22D, CAB_Z - 0.50D, CAB_Z + 0.50D, SLAB_H, 1.60D };
        r[n++] = new double[] { -LIGHT_X - 0.30D, -LIGHT_X + 0.30D, LIGHT_Z - 0.30D, LIGHT_Z + 0.30D, SLAB_H, LIGHT_TOP };
        r[n++] = new double[] { LIGHT_X - 0.30D, LIGHT_X + 0.30D, LIGHT_Z - 0.30D, LIGHT_Z + 0.30D, SLAB_H, LIGHT_TOP };
        for (int lx : PORT_LX) {
            double px = cellX(lx);
            r[n++] = new double[] { px - 0.50D, px + 0.50D, PORT_Z - 0.50D, PORT_Z + 0.50D, 0.0D, 1.0D };
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
            double az0 = Math.max(c[2] + CENTER_Z, lz - 0.5D);
            double az1 = Math.min(c[3] + CENTER_Z, lz + 0.5D);
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

    /** true, якщо клітинка (lx, lz) на рівні землі є кабельним портом (повний блок). */
    public static boolean isPortCell(int lx, int lz) {
        if (lz != LZ_MIN) return false;
        for (int p : PORT_LX) {
            if (p == lx) return true;
        }
        return false;
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
            int[] o = toWorld(k, PORT_LX[i], LZ_MIN);
            r[i] = new int[] { o[0], 0, o[1] };
        }
        return r;
    }

    /** Світовий одиничний напрямок назовні від кабельного краю (локальний -z, бік гравця): {dx, dz}. */
    public static int[] outOffset(float angle) {
        return toWorld(quarter(angle), 0, -1);
    }

    /** Точки кріплення трьох фаз на порталі: локальні координати (до повороту), відносно центру майстра. */
    public static double[][] mounts() {
        double[][] m = new double[MOUNT_X.length][];
        for (int i = 0; i < MOUNT_X.length; i++) {
            m[i] = new double[] { MOUNT_X[i] + CENTER_X, MOUNT_Y, PORTAL_Z + CENTER_Z };
        }
        return m;
    }
}
