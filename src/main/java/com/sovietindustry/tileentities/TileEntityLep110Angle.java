package com.sovietindustry.tileentities;

/**
 * Анкерно-кутова опора 110 кВ за ескізом У35-1 (3 фази + трос), масштаб під 13 блоків.
 * Натяжні гірлянди горизонтальні й повертаються в бік кожного проводу.
 *
 * Пропорції з ескізу: висота 17950 мм = 13 блоків (0.724 бл/м),
 * нижня траверса на 10000 мм, верхня на 13000 мм, плечі 2800 / 3500 / 2800 мм.
 */
public class TileEntityLep110Angle extends TileEntityLepPylon {

    public static final double LOW_Y = 7.24D;
    public static final double UP_Y = 9.41D;
    public static final double SHORT_ARM = 2.03D;
    public static final double LONG_ARM = 2.53D;
    public static final double TOP_Y = 13.0D;
    /** Скільки нижче верху траверси висить точка кріплення гірлянди. */
    public static final double MOUNT_DROP = 0.15D;

    // Локальний +X це бік гравця зліва, тому довге плече нижньої траверси (праворуч на ескізі) в -X
    private static final double[][] MOUNTS = {
        {  SHORT_ARM, LOW_Y - MOUNT_DROP, 0.0D },   // нижня коротка консоль
        { -LONG_ARM,  LOW_Y - MOUNT_DROP, 0.0D },   // нижня довга консоль
        { -SHORT_ARM, UP_Y - MOUNT_DROP,  0.0D }    // верхня консоль
    };

    public TileEntityLep110Angle() {
    }

    @Override
    protected double[][] getLocalMounts() {
        return MOUNTS;
    }

    @Override
    public boolean hasAnchorStrings() {
        return true;
    }

    /** Півширина ніг опори (блоки, від осі) на висоті y. Спільна для моделі й колізії. */
    public static double legHalfWidth(double y) {
        double[][] k = { { 0.0D, 1.15D }, { LOW_Y, 0.50D }, { UP_Y, 0.34D }, { TOP_Y, 0.12D } };
        if (y <= k[0][0]) return k[0][1];
        for (int i = 0; i < k.length - 1; i++) {
            if (y <= k[i + 1][0]) {
                double f = (y - k[i][0]) / (k[i + 1][0] - k[i][0]);
                return k[i][1] + (k[i + 1][1] - k[i][1]) * f;
            }
        }
        return k[k.length - 1][1];
    }
}
