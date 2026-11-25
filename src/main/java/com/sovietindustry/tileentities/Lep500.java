package com.sovietindustry.tileentities;

/**
 * Проміжна опора 500 кВ на відтяжках за кресленням ПП500: нахилені ноги (основа ±9.2, верх ±6.4),
 * пряма горизонтальна траверса з вістрями на кінцях і похилі роги тросостійки. Власна геометрія,
 * мультиблок і колізія в LepMultiblock.*P500*.
 */
public final class Lep500 {
    private Lep500() {
    }

    /** Максимальна довжина прольоту між опорами 500 кВ у блоках. */
    public static final double MAX_SPAN_500 = 225.0D;

    // ---------------------------------------------------------------- пучок (як на 750 кВ)
    public static final int STRINGS = Lep750.STRINGS;
    public static final double STRING_SPACING = Lep750.STRING_SPACING;
    public static final int WIRES_PER_STRING = Lep750.WIRES_PER_STRING;
    public static final double BUNDLE_HALF = Lep750.BUNDLE_HALF;
    public static final double WIRE_SPACING = Lep750.WIRE_SPACING;

    // ---------------------------------------------------------------- габарити за кресленням ПП500 (масштаб 0.78 блока на 1 м)
    // Кресленик: верх ніг ±6.4, основа ±9.2, виліт траверси ±12.8, роги до ±8.6, від низу траверси до вершини рогів 5.0.
    public static final int HEIGHT = 25;
    /** Півбаза між п'ятами ніг (креслення: 9.2 м). */
    public static final double LEG_BASE_X = 7.2D;
    /** Півбаза між вершинами ніг (креслення: 6.4 м). */
    public static final double LEG_TOP_X = 5.0D;
    /** Рівень низу траверси = вершина ніг (креслення: h). */
    public static final double LEG_TOP_Y = 20.8D;
    /** Півдовжина траверси (креслення: 12.8 м). */
    public static final double ARM_HALF = 10.0D;
    /** Нижній пояс траверси: пряма горизонталь по всій довжині. */
    public static final double ARM_Y = LEG_TOP_Y;
    /** Верхній пояс на кінцях траверси (вістря консолей). */
    public static final double ARM_TOP_LEG_Y = ARM_Y + 0.5D;
    /** Верхній пояс над проміжком між ногами (плоский). */
    public static final double ARM_TOP_MID_Y = ARM_Y + 1.5D;
    /** Вершина рогів тросостійки (креслення: 5.0 м над низом траверси). */
    public static final double HORN_TOP_Y = ARM_Y + 3.9D;
    public static final double HORN_SPREAD = 1.6D;
    public static final double HORN_TIP_OUT = 0.1D;
    public static final double GROUND_X = LEG_TOP_X + HORN_SPREAD + HORN_TIP_OUT;
    public static final double GROUND_Y = HORN_TOP_Y - 0.15D;
    public static final double[] PHASE_X = { -9.2D, 0.0D, 9.2D };

    // ---------------------------------------------------------------- гірлянда: 18 тарілок
    public static final int DISCS = Lep750.DISCS - 2;
    public static final double DISC_STEP = 0.175D;
    /** Верх гірлянди під нижнім поясом траверси. */
    public static final double STRING_TOP = ARM_Y - 0.10D;
    /** Коромисло (ярмо) під гірляндою. */
    public static final double YOKE_Y = ARM_Y - 3.6D;
    /** Центр пучка проводів: на 0.40 нижче коромисла. */
    public static final double MOUNT_Y = YOKE_Y - 0.40D;

    public static double discY(int k) {
        return STRING_TOP - 0.28D - k * DISC_STEP;
    }

    // ---------------------------------------------------------------- бічні відтяжки (вигляд збоку трикутником)
    public static final double GUY_Z = 8.0D;
    public static final double GUY_ANCHOR_X = LEG_BASE_X;

    // ---------------------------------------------------------------- форма
    public static double legCentreX(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, y / LEG_TOP_Y));
        return LEG_BASE_X + (LEG_TOP_X - LEG_BASE_X) * f;
    }

    /** Нога вужча, ніж на 750 кВ, однакової ширини, внизу звужується гострим конусом до п'яти. */
    public static double legHalf(double y) {
        if (y < 2.0D) {
            return 0.08D + (0.48D - 0.08D) * (y / 2.0D);
        }
        return 0.48D;
    }

    /** Нижній пояс прямий, тому профіль (0..1) потрібен лише для півглибини ферми: 1 в центрі, спадає до кінців. */
    public static double armTri(double ax) {
        ax = Math.abs(ax);
        if (ax <= LEG_TOP_X) return 1.0D;
        return Math.max(0.0D, 1.0D - (ax - LEG_TOP_X) / (ARM_HALF - LEG_TOP_X));
    }

    /** Нижній пояс траверси плоский. */
    public static double armBottomY(double x) {
        return ARM_Y;
    }

    /** Верхній пояс: плаский над ногами, далі косо вниз до вістря консолі. */
    public static double armTopY(double x) {
        double ax = Math.abs(x);
        if (ax <= LEG_TOP_X) return ARM_TOP_MID_Y;
        double f = Math.min(1.0D, (ax - LEG_TOP_X) / (ARM_HALF - LEG_TOP_X));
        return ARM_TOP_MID_Y + (ARM_TOP_LEG_Y - ARM_TOP_MID_Y) * f;
    }

    public static double[][] pMounts() {
        return new double[][] {
            { PHASE_X[0], MOUNT_Y, 0.0D },
            { PHASE_X[1], MOUNT_Y, 0.0D },
            { PHASE_X[2], MOUNT_Y, 0.0D }
        };
    }

    // ---------------------------------------------------------------- анкерно-кутова 500 кВ (три однакові вежі)
    // Усі три вежі мають тіло як середня вежа АКО 750 кВ (плоска голівка). На лівій і правій зверху ще гостра
    // антенка з гачком для грозотроса. Окрім неї, на лівій і правій жодних консолей.
    public static final int A_HEIGHT = 24;
    public static final double A_NECK_Y = 14.0D;
    /** Верх плоскої голівки (середня вежа закінчується тут). */
    public static final double A_MID_TOP = 15.0D;
    /** Вістря антенки на лівій і правій вежах. */
    public static final double A_ANT_TOP = 23.0D;
    public static final double A_BASE_HALF = 2.9D;
    public static final double A_NECK_HALF = 0.62D;
    public static final double A_ANT_HALF = 0.07D;
    public static final double A_TOWER_X = 12.0D;
    public static final double A_MOUNT_Y = 13.6D;
    public static final double A_STRING_LEN = 2.9D;
    public static final double A_STANDOFF = 1.2D;
    public static final double A_GROUND_Y = A_ANT_TOP + 0.35D;
    /** Скільки блоків від осі вежі вершина дуги шлейфу (на допоміжній гірлянді сусіда). */
    public static final double A_LOOP_R = 6.0D;
    public static final double A_LOOP_RISE = 0.8D;
    public static final double A_SUP_STUB = 0.65D;
    /** Вільна дуга шлейфу крайньої вежі (без сусіда): виліт убік і просідання. */
    public static final double A_FREE_R = 4.2D;
    public static final double A_FREE_DROP = 1.1D;

    public static double angleHalf(double y) {
        if (y <= A_NECK_Y) {
            double f = Math.max(0.0D, y / A_NECK_Y);
            return A_BASE_HALF + (A_NECK_HALF - A_BASE_HALF) * f;
        }
        if (y <= A_MID_TOP) return A_NECK_HALF * 0.92D;
        // антенка: основа ледь ширша за верх голівки (повністю її перекриває, без площадки-«зубця»), далі швидко звужується до вістря
        double f = Math.min(1.0D, (y - A_MID_TOP) / (A_ANT_TOP - A_MID_TOP));
        return A_NECK_HALF + (A_ANT_HALF - A_NECK_HALF) * Math.pow(f, 0.8D);
    }

    /** X вежі з номером 0, 1, 2 (ліва, середня, права) у локальних координатах. */
    public static double towerX(int i) {
        return (i - 1) * A_TOWER_X;
    }

    public static double[][] aMounts() {
        return new double[][] {
            { towerX(0), A_MOUNT_Y, 0.0D },
            { towerX(1), A_MOUNT_Y, 0.0D },
            { towerX(2), A_MOUNT_Y, 0.0D }
        };
    }

    public static double[][] aGround() {
        return new double[][] {
            { towerX(0), A_GROUND_Y, 0.0D },
            { towerX(2), A_GROUND_Y, 0.0D }
        };
    }

    /**
     * Допоміжна гірлянда шлейфу фази mount: кріпиться на грані СУСІДНЬОЇ вежі в бік dir (+1 праворуч, -1 ліворуч),
     * вільний кінець гірлянди (вершина дуги) лежить над власною вежею в той самий бік. null, якщо сусіда немає.
     * Повертає { точка на сусідній вежі {x,y,z}, вершина дуги {x,y,z} }.
     */
    public static double[][] aSupport(int mount, int dir) {
        int n = mount + dir;
        if (n < 0 || n > 2) return null;
        double attachX = towerX(n) - dir * (angleHalf(A_MOUNT_Y) + A_SUP_STUB);
        double endX = towerX(mount) + dir * A_LOOP_R;
        return new double[][] {
            { attachX, A_MOUNT_Y, 0.0D },
            { endX, A_MOUNT_Y - A_LOOP_RISE, 0.0D }
        };
    }

    // ---------------------------------------------------------------- окремі щогли 500 кВ: середня й бічна
    /** Скільки блоків від упору до вершини дуги шлейфу (горизонтальна допоміжна гірлянда на власній щоглі). */
    public static final double S_SUP_REACH = 4.2D;
    /** Допоміжна гірлянда є лише коли кут між гірляндами не менший за це значення (180 = пряма лінія). */
    public static final double S_AUX_MIN_ANGLE_DEG = 120.0D;

    /** Одна точка кріплення на осі щогли. */
    public static double[][] sMounts() {
        return new double[][] { { 0.0D, A_MOUNT_Y, 0.0D } };
    }

    /** Упор на грані +X, до якого кріпиться допоміжна гірлянда шлейфу (для іншого боку щоглу просто повертають). */
    public static double[] sSupportAttach() {
        return new double[] { angleHalf(A_MOUNT_Y) + A_SUP_STUB, A_MOUNT_Y, 0.0D };
    }

    public static double[] sSupportEnd() {
        double[] a = sSupportAttach();
        return new double[] { a[0] + S_SUP_REACH, A_MOUNT_Y - A_LOOP_RISE, 0.0D };
    }

    /** Бічна щогла має антенку з гачком грозотроса, середня ні. */
    public static double[][] sGround() {
        return new double[][] { { 0.0D, A_GROUND_Y, 0.0D } };
    }

    /** Вершина вільної дуги шлейфу крайньої вежі (там, де сусіда в бік dir нема). */
    public static double[] aFreeApex(int mount, int dir) {
        return new double[] { towerX(mount) + dir * A_FREE_R, A_MOUNT_Y - A_FREE_DROP, 0.0D };
    }
}
