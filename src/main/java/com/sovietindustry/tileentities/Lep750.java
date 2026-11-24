package com.sovietindustry.tileentities;

public final class Lep750 {
    private Lep750() {
    }

    public static final double MAX_SPAN_750 = 250.0D;

    // Подвійні гірлянди вздовж лінії + розщеплена фаза з 4 проводів квадратом
    public static final int STRINGS = 2;
    public static final double STRING_SPACING = 0.70D;
    public static final int WIRES_PER_STRING = 4;
    public static final double BUNDLE_HALF = 0.22D;
    public static final double WIRE_SPACING = BUNDLE_HALF * 2.0D;

    // ---------------------------------------------------------------- проміжна опора ПП750 (Креслення 1)
    public static final int HEIGHT = 25;

    // Пропорції з креслення: основа 16600, верх ніг 17000 (половина 8500), виліт консолі 18000
    public static final double LEG_BASE_X = 11.8D;
    public static final double LEG_TOP_X = 6.2D;
    public static final double LEG_TOP_Y = 20.5D;

    public static final double ARM_HALF = 13.4D;
    /** Нижній пояс траверси на краях і в центрі. */
    public static final double ARM_Y = 22.8D;
    /** Верхній пояс траверси над ногами. */
    public static final double ARM_TOP_LEG_Y = 24.2D;
    /** Верхній пояс траверси в центрі (підйом «чайкою» як на кресленні ПП750). */
    public static final double ARM_TOP_MID_Y = 25.0D;

    public static final double HORN_TOP_Y = 29.2D;
    public static final double HORN_SPREAD = 2.4D;
    public static final double HORN_TIP_OUT = 1.35D;
    public static final double GROUND_X = LEG_TOP_X + HORN_SPREAD + HORN_TIP_OUT;
    public static final double GROUND_Y = HORN_TOP_Y - 0.15D;

    public static final double[] PHASE_X = { -13.0D, 0.0D, 13.0D };

    public static final int DISCS = 20;
    public static final double STRING_TOP = ARM_Y - 0.10D;
    public static final double YOKE_Y = ARM_Y - 3.95D;
    public static final double MOUNT_Y = ARM_Y - 4.35D;

    public static double discY(int k) {
        return STRING_TOP - 0.28D - k * 0.175D;
    }

    public static double legCentreX(double y) {
        double f = Math.max(0.0D, Math.min(1.0D, y / LEG_TOP_Y));
        return LEG_BASE_X + (LEG_TOP_X - LEG_BASE_X) * f;
    }

    public static double legHalf(double y) {
        // По всій висоті нога однакової ширини (0.62), а біля самого фундаменту (нижні 2.2 блока)
        // звужується гострим трикутним конусом у точку опори (як на кресленні ПП750)
        if (y < 2.2D) {
            return 0.08D + (0.62D - 0.08D) * (y / 2.2D);
        }
        return 0.62D;
    }

    /** Профіль нижнього пояса траверси: 0 на краях і в центрі, 1 над вершинами ніг. */
    public static double armTri(double ax) {
        ax = Math.abs(ax);
        if (ax <= LEG_TOP_X) return ax / LEG_TOP_X;
        return Math.max(0.0D, 1.0D - (ax - LEG_TOP_X) / (ARM_HALF - LEG_TOP_X));
    }

    /** Висота нижнього пояса траверси в точці x. */
    public static double armBottomY(double x) {
        return ARM_Y - (ARM_Y - LEG_TOP_Y) * armTri(x);
    }

    /** Висота верхнього пояса траверси (двоскатний злам на кресленні ПП750). */
    public static double armTopY(double x) {
        double ax = Math.abs(x);
        if (ax <= LEG_TOP_X) {
            double f = 1.0D - ax / LEG_TOP_X;
            return ARM_TOP_LEG_Y + (ARM_TOP_MID_Y - ARM_TOP_LEG_Y) * f;
        }
        double f = Math.min(1.0D, (ax - LEG_TOP_X) / (ARM_HALF - LEG_TOP_X));
        return ARM_TOP_LEG_Y + (ARM_Y - ARM_TOP_LEG_Y) * f;
    }

    public static double[][] pMounts() {
        return new double[][] {
            { PHASE_X[0], MOUNT_Y, 0.0D },
            { PHASE_X[1], MOUNT_Y, 0.0D },
            { PHASE_X[2], MOUNT_Y, 0.0D }
        };
    }

    // ---------------------------------------------------------------- анкерно-кутова опора УС750-1 (Креслення 2)
    public static final int A_HEIGHT = 23;
    public static final double A_NECK_Y = 15.5D;
    public static final double A_MID_TOP = 16.2D;
    public static final double A_TOP = 22.2D;
    public static final double A_BASE_HALF = 3.15D;
    public static final double A_NECK_HALF = 0.68D;
    public static final double A_TOP_HALF = 0.36D;
    public static final double A_TOWER_X = 15.0D;

    public static final double A_MOUNT_Y = 15.2D;
    public static final double A_STRING_LEN = 3.2D;
    public static final double A_GROUND_Y = A_TOP + 0.35D;
    public static final double A_CORONA_RING_R = 0.44D;
    public static final double A_STANDOFF = 1.25D;

    /** Ліва вежа (idx = -1): мала трикутна консоль вправо. */
    public static final double A_LEFT_ARM_LEN = 2.8D;
    public static final double A_LEFT_ARM_DEPTH = 1.4D;
    public static final double A_LEFT_JUMP_LEN = 3.8D;

    /** Права вежа (idx = 1): велика консоль-прапор вправо. */
    public static final double A_ARM_Y = A_TOP;
    public static final double A_ARM_DEPTH = 2.1D;
    public static final double A_ARM_LEN = 7.5D;
    public static final double A_ARM_RISE = 0.0D;
    public static final double A_JUMP_Z = 0.35D;
    public static final double A_JUMP_LEN = 3.8D;
    public static final int A_JUMPER_MOUNT = 2;

    public static final double A_SUP_REACH = 3.2D;
    public static final double A_SUP_STUB = 0.65D;

    /** Чи малювати верхні підвісні гірлянди малої консолі лівої вежі (за схемою ліва вежа йде дугою, тому вимкнено). */
    public static final boolean LEFT_JUMPER_ENABLED = false;

    /** Дуга проводу фаз 0 і 1: на скільки блоків вправо від осі своєї вежі виходить вершина дуги. */
    public static final double A_LOOP_R = 7.5D;
    /** На скільки вершина дуги нижча за упор на сусідній вежі (гірлянда-перемичка йде вгору до сусідньої вежі). */
    public static final double A_LOOP_RISE = 0.8D;

    /**
     * Перемичка вершини дуги фази mount (0 або 1): кріпиться до упору на лівій грані СУСІДНЬОЇ вежі праворуч
     * (фаза 0 -> середня вежа, фаза 1 -> права вежа). Упор є в моделі: tx - angleHalf - A_SUP_STUB.
     */
    public static double[] aSupportAttach(int mount) {
        if (mount != 0 && mount != 1) return null;
        double nextX = mount * A_TOWER_X; // x сусідньої вежі справа: -15 + (mount + 1) * 15
        return new double[] { nextX - angleHalf(A_MOUNT_Y) - A_SUP_STUB, A_MOUNT_Y, 0.0D };
    }

    /** Вершина дуги проводу (вільний кінець перемички, до нього кріпиться провід). */
    public static double[] aSupportEnd(int mount) {
        if (mount != 0 && mount != 1) return null;
        double ownX = -A_TOWER_X + mount * A_TOWER_X;
        return new double[] { ownX + A_LOOP_R, A_MOUNT_Y - A_LOOP_RISE, 0.0D };
    }

    public static double angleHalf(double y) {
        if (y <= A_NECK_Y) {
            double f = Math.max(0.0D, y / A_NECK_Y);
            return A_BASE_HALF + (A_NECK_HALF - A_BASE_HALF) * f;
        }
        double f = Math.min(1.0D, (y - A_NECK_Y) / (A_TOP - A_NECK_Y));
        return A_NECK_HALF + (A_TOP_HALF - A_NECK_HALF) * f;
    }

    /** Окрема щогла (одна фаза): єдина точка кріплення на осі вежі. */
    public static double[][] sMounts() {
        return new double[][] { { 0.0D, A_MOUNT_Y, 0.0D } };
    }

    // ---------------------------------------------------------------- окремі щогли: ліва / середня / права
    /** Скільки блоків від упору до вершини дуги шлейфу (горизонтальна допоміжна гірлянда на власній щоглі). */
    public static final double S_SUP_REACH = 4.2D;

    /**
     * Допоміжна гірлянда на окремій щоглі з'являється, лише коли кут між передньою й задньою натяжними гірляндами
     * НЕ МЕНШИЙ за це значення (у градусах; 180 = пряма лінія). При гострішому куті допоміжної гірлянди немає, а дріт просто провисає від передньої до задньої натяжної гірлянди.
     */
    public static final double S_AUX_MIN_ANGLE_DEG = 120.0D;

    /**
     * Напрям упору допоміжної гірлянди вздовж локальної осі X: ліва щогла (-1) дивиться в протилежний бік
     * (повернута на 180 градусів), середня й права (0, +1) на +X.
     */
    public static double sSupportDir(int variant) {
        return variant < 0 ? -1.0D : 1.0D;
    }

    /** Упор на грані щогли (+X, а для лівої щогли -X), до якого кріпиться допоміжна гірлянда шлейфу. */
    public static double[] sSupportAttach(int variant) {
        return new double[] { sSupportDir(variant) * (angleHalf(A_MOUNT_Y) + A_SUP_STUB), A_MOUNT_Y, 0.0D };
    }

    /** Вершина дуги шлейфу: вільний кінець допоміжної гірлянди. */
    public static double[] sSupportEnd(int variant) {
        double[] a = sSupportAttach(variant);
        return new double[] { a[0] + sSupportDir(variant) * S_SUP_REACH, A_MOUNT_Y - A_LOOP_RISE, 0.0D };
    }

    /** Ліва і права щогли мають тросостійку із затискачем троса, середня ні (як у складі АКО). */
    public static double[][] sGround() {
        return new double[][] { { 0.0D, A_GROUND_Y, 0.0D } };
    }

    /** Верхні підвісні гірлянди великої консолі правої щогли (шлейф іде через них, як у правої вежі АКО). */
    public static double[][][] sJumperStrings() {
        double x = A_ARM_LEN;
        double top = A_ARM_Y - 0.1D;
        double bot = top - A_JUMP_LEN;
        return new double[][][] {
            { { x, top,  A_JUMP_Z }, { x, bot,  A_JUMP_Z } },
            { { x, top, -A_JUMP_Z }, { x, bot, -A_JUMP_Z } }
        };
    }

    public static double[][] aMounts() {
        return new double[][] {
            { -A_TOWER_X, A_MOUNT_Y, 0.0D },
            {  0.0D,      A_MOUNT_Y, 0.0D },
            {  A_TOWER_X, A_MOUNT_Y, 0.0D }
        };
    }

    /** Верхні підвісні гірлянди правої вежі: {верх, низ}, локальні координати. */
   public static double[][][] aLeftJumperStrings() {
        double x = -A_TOWER_X + A_LEFT_ARM_LEN;
        double top = A_TOP - 0.1D;
        double bot = top - A_LEFT_JUMP_LEN;
        return new double[][][] {
            { { x, top,  A_JUMP_Z }, { x, bot,  A_JUMP_Z } },
            { { x, top, -A_JUMP_Z }, { x, bot, -A_JUMP_Z } }
        };
    }

    /** Верхні підвісні гірлянди правої вежі (mount = 2): {верх, низ}, локальні координати. */
    public static double[][][] aJumperStrings() {
        double x = A_TOWER_X + A_ARM_LEN;
        double top = A_ARM_Y - 0.1D;
        double bot = top - A_JUMP_LEN;
        return new double[][][] {
            { { x, top,  A_JUMP_Z }, { x, bot,  A_JUMP_Z } },
            { { x, top, -A_JUMP_Z }, { x, bot, -A_JUMP_Z } }
        };
    }
}