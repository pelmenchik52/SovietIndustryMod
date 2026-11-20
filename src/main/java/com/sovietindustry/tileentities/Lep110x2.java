package com.sovietindustry.tileentities;

/**
 * Спільні розміри 6-контактних (дволанцюгових) опор 110 кВ за ескізом УП110-2.
 * Висота 16 блоків, ширина по траверсах 5 блоків (середня траверса +-2.5, верхня й нижня +-1.75).
 * Траверси на 13.6 / 11.5 / 9.4: це і фото проміжної опори, і ескіз анкерно-кутової (лінії проводів 13.3 / 11.4 / 9.5).
 * Трос на вершині.
 *
 * Індекси точок кріплення: 0..2 це +X бік (верх, середина, низ), 3..5 це -X бік.
 * Група 0 = {0,1,2}, група 1 = {3,4,5}: кожна група це окреме 3-фазне коло.
 */
public final class Lep110x2 {
    private Lep110x2() {
    }

    public static final int HEIGHT = 16;
    /** Висоти траверс: верхня, середня, нижня. */
    public static final double[] ARM_Y = { 13.6D, 11.5D, 9.4D };
    /** Виліт траверс від осі: верхня, середня, нижня. */
    public static final double[] ARM_LEN = { 1.75D, 2.5D, 1.75D };
    /** Підвісна гірлянда проміжної опори висить на цю глибину під траверсою. */
    public static final double SUSPENSION_DROP = 1.15D;
    /** Точка кріплення натяжної гірлянди анкерної опори під траверсою. */
    public static final double TENSION_DROP = 0.15D;
    public static final double GROUND_Y = 16.3D;

    public static final int[][] GROUPS = { { 0, 1, 2 }, { 3, 4, 5 } };

    /** Півширина ніг (блоки від осі) на висоті y; для проміжної опори це радіус бетонної стійки. Спільна для моделі й колізії. */
    public static double legHalf(double y, boolean angle) {
        double[][] k = angle
            ? new double[][] { { 0.0D, 1.30D }, { 1.9D, 1.07D }, { 8.5D, 0.35D }, { 14.0D, 0.35D }, { 16.0D, 0.06D } }
            : new double[][] { { 0.0D, 0.30D }, { 16.0D, 0.12D } }; // бетонна стійка: радіус внизу 0.30, вгорі 0.12
        if (y <= k[0][0]) return k[0][1];
        for (int i = 0; i < k.length - 1; i++) {
            if (y <= k[i + 1][0]) {
                double f = (y - k[i][0]) / (k[i + 1][0] - k[i][0]);
                return k[i][1] + (k[i + 1][1] - k[i][1]) * f;
            }
        }
        return k[k.length - 1][1];
    }

    /** Локальна точка кріплення [x,y,z] за індексом 0..5. */
    public static double[] mount(int index, double drop) {
        int arm = index % 3;
        double side = index < 3 ? 1.0D : -1.0D;
        return new double[] { side * ARM_LEN[arm], ARM_Y[arm] - drop, 0.0D };
    }
}
