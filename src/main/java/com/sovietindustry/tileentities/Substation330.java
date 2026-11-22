package com.sovietindustry.tileentities;

/**
 * Геометрія трансформаторної підстанції 330 кВ: мультиблок 7 (ширина, вісь X) x 6 (довжина, вісь Z) x 8 (висота).
 *
 * Компоновка (як у реальному відкритому розподільчому пристрої 330 кВ), уздовж довжини від входу до виходу:
 *   ряд lz=-3 : лінійний портал (дві бетонні стійки з блискавкоприймальними щоглами й ґратчаста траверса),
 *               сюди заходить дріт від анкерно-кутової опори 330 кВ;
 *   ряд lz=-2 : лінійна комірка: на кожну фазу роз'єднувач і обмежувач перенапруг (ОПН);
 *   ряди lz=-1..1 : силовий трансформатор (бак, радіатори, консерватор, вводи ВН нагору, вводи НН), шафа керування;
 *   ряд lz=+2 : три вихідні кабельні порти (повні блоки) для кабелів NTM.
 * Ланцюг: лінія -> портал -> роз'єднувач -> шлейф -> ввід ВН трансформатора, ОПН висить на шлейфі як шунт,
 * із боку НН шини йдуть від вводів до вихідних портів.
 *
 * Локальна система координат (до повороту): майстер-блок у клітинці (0, 0), крок клітинки 1 блок.
 *   lx = -3..+3 (7 клітинок), lz = -3..+2 (6 клітинок), висота 8 блоків.
 * Модель малюється в "центрованій" системі (x', z'): центр плити в (0, 0), x' -3.5..+3.5, z' -3..+3.
 *   локальний x = x' + CENTER_X (CENTER_X = 0), локальний z = z' + CENTER_Z (CENTER_Z = -0.5, довжина 6 парна).
 *   Клітинка lz займає z' від lz до lz+1, клітинка lx займає x' від lx-0.5 до lx+0.5.
 *
 * Поворот лише кратний 90 градусам. Формули ті самі, що й у TileEntityLepPylon.toWorldOffset.
 */
public final class Substation330 {
    private Substation330() {
    }

    public static final int LX_MIN = -3;
    public static final int LX_MAX = 3;
    public static final int LZ_MIN = -3;
    public static final int LZ_MAX = 2;
    public static final int WIDTH = 7;
    public static final int LENGTH = 6;
    public static final int HEIGHT = 8;

    /** Зсув центру моделі відносно майстер-блока (локальні координати). */
    public static final double CENTER_X = 0.0D;
    public static final double CENTER_Z = -0.5D;
    public static final double SLAB_H = 0.25D;

    // ---------------------------------------------------------------- портал входу 330 кВ (центровані x', z')
    public static final double PORTAL_Z = -2.5D;
    /** Осі двох бетонних стійок: x' = +-PORTAL_X. */
    public static final double PORTAL_X = 3.0D;
    /** Верх бетонної частини стійки, далі ґратчаста щогла до MAST_TOP. */
    public static final double COLUMN_TOP = 6.0D;
    public static final double MAST_TOP = 7.9D;
    public static final double COLUMN_HALF_BOTTOM = 0.30D;
    public static final double COLUMN_HALF_TOP = 0.20D;
    /** Ґратчаста траверса: верхній і нижній пояс, виступ праворуч за стійку (як на фото). */
    public static final double BEAM_TOP = 5.95D;
    public static final double BEAM_LOW = 5.45D;
    public static final double BEAM_X0 = -3.20D;
    public static final double BEAM_X1 = 3.45D;
    public static final double BEAM_PLANE = 0.13D;
    /** Нижні кінці двох розкосів, що сходяться в центрі під траверсою. */
    public static final double BRACE_Y = 3.90D;
    public static final double[] PHASE_X = { -2.0D, 0.0D, 2.0D };
    public static final double MOUNT_Y = 5.30D;
    public static final double STRING_LEN = 1.15D;

    // ---------------------------------------------------------------- лінійна комірка (ряд lz = -2)
    public static final double DISC_Z = -1.60D;
    public static final double ARR_Z = -1.15D;
    public static final double DISC_BASE_Y = 1.00D;
    public static final double DISC_POST_TOP = 2.30D;
    public static final double DISC_BLADE_Y = 2.38D;
    public static final double DISC_HALF = 0.40D;
    public static final double ARR_TOP_Y = 2.90D;

    // ---------------------------------------------------------------- трансформатор (ряди lz = -1..1, клітинки lx = -1..1)
    public static final double TANK_HX = 1.50D;
    public static final double TANK_HZ = 0.70D;
    public static final double TANK_CZ = 0.50D;
    public static final double TANK_BOT = 0.35D;
    public static final double TANK_TOP = 3.00D;
    public static final double LID_Y = 3.10D;

    public static final double[] HV_X = { -1.2D, 0.0D, 1.2D };
    public static final double HV_Z = -0.05D;
    public static final double HV_TOP_Y = 5.60D;
    /** Зовнішні вводи нахилені назовні (множник x верхівки). */
    public static final double HV_TILT = 1.25D;

    public static final double[] LV_X = { -1.2D, 0.0D, 1.2D };
    public static final double LV_Z = 1.12D;
    public static final double LV_TOP_Y = 4.30D;

    public static final double CONS_CX = -0.40D;
    public static final double CONS_CY = 3.95D;
    public static final double CONS_CZ = 0.65D;
    public static final double CONS_R = 0.36D;
    public static final double CONS_HALF = 1.00D;

    /** Шафа керування збоку бака. */
    public static final double CAB_X = 2.10D;
    public static final double CAB_Z = 0.50D;

    // ---------------------------------------------------------------- кабельні порти (ряд lz = +2, повні блоки)
    /** Клітинки lx портів: через одну (кабелі NTM не злипаються), симетрично відносно осі підстанції. */
    public static final int[] PORT_LX = { -2, 0, 2 };
    /** Центрований z' центра порту і торця плити. */
    public static final double PORT_Z = 2.5D;
    public static final double END_Z = 3.0D;
    /** Півсторона оранжевої плити кабельного виводу: 6x6 пікселів = розмір ядра кабелю NTM (0.375 блока). */
    public static final double PORT_PAD_HALF = 0.1875D;
    /** Висота центра кабелю над низом блока (кабель NTM лежить по центру клітинки). */
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
    private static final double[][] COMPS = buildComps();

    private static double[][] buildComps() {
        double[][] r = new double[32][];
        int n = 0;
        // бак із радіаторами на всю клітинку 3x3, щоб не лишалось щілин
        r[n++] = new double[] { -1.5D, 1.5D, -1.0D, 2.0D, SLAB_H, LID_Y };
        // консерватор разом із опорами
        r[n++] = new double[] { CONS_CX - CONS_HALF, CONS_CX + CONS_HALF, CONS_CZ - CONS_R, CONS_CZ + CONS_R,
            LID_Y, CONS_CY + CONS_R };
        // три вводи ВН (крайні нахилені назовні), габарит від основи до верхівки
        r[n++] = new double[] { -1.75D, -1.00D, HV_Z - 0.20D, HV_Z + 0.20D, LID_Y, HV_TOP_Y + 0.12D };
        r[n++] = new double[] { -0.20D, 0.20D, HV_Z - 0.20D, HV_Z + 0.20D, LID_Y, HV_TOP_Y + 0.12D };
        r[n++] = new double[] { 1.00D, 1.75D, HV_Z - 0.20D, HV_Z + 0.20D, LID_Y, HV_TOP_Y + 0.12D };
        // три вводи НН
        for (double lvx : LV_X) {
            r[n++] = new double[] { lvx - 0.15D, lvx + 0.15D, LV_Z - 0.15D, LV_Z + 0.15D, LID_Y, LV_TOP_Y };
        }
        // дві бетонні стійки порталу разом із щоглами
        r[n++] = new double[] { -PORTAL_X - 0.30D, -PORTAL_X + 0.30D, PORTAL_Z - 0.30D, PORTAL_Z + 0.30D, SLAB_H, MAST_TOP };
        r[n++] = new double[] { PORTAL_X - 0.30D, PORTAL_X + 0.30D, PORTAL_Z - 0.30D, PORTAL_Z + 0.30D, SLAB_H, MAST_TOP };
        // лінійна комірка: роз'єднувач + ОПН на кожну фазу (одна коробка на клітинку фази)
        for (double px : PHASE_X) {
            r[n++] = new double[] { px - 0.50D, px + 0.50D, DISC_Z - 0.38D, ARR_Z + 0.15D, SLAB_H, ARR_TOP_Y };
        }
        // шафа керування збоку бака
        r[n++] = new double[] { CAB_X - 0.22D, CAB_X + 0.22D, CAB_Z - 0.50D, CAB_Z + 0.50D, SLAB_H, 1.50D };
        // три вихідні порти: ПОВНІ блоки 1x1x1, щоб кабель NTM щільно прилягав
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
        if (lz != LZ_MAX) return false;
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
            m[i] = new double[] { PHASE_X[i] + CENTER_X, MOUNT_Y, PORTAL_Z + CENTER_Z };
        }
        return m;
    }
}
