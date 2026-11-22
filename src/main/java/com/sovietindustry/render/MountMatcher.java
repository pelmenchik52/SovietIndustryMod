package com.sovietindustry.render;

/**
 * Зіставлення точок кріплення двох опор по ПОЛОЖЕННЮ, а не за номером. Раніше дріт від точки i однієї опори
 * йшов до точки i другої, тому дроти перехрещувалися, коли опори стояли "задом наперед" або мали різний порядок
 * точок. Тепер кожна точка однієї опори отримує найближчу за положенням точку іншої так, щоб сума відстаней
 * була мінімальною (поперек прольоту й по висоті). Для рівнобіжних рядів це завжди пара без перетинів.
 *
 * Чиста Java без залежностей від Minecraft, тому легко перевіряється окремо.
 */
public final class MountMatcher {
    private MountMatcher() {
    }

    /** Вага висоти у вартості пари (по висоті дріт може йти похило, перетин по горизонталі гірший). */
    private static final double Y_WEIGHT = 0.30D;
    /** Для великої кількості точок перебір не робимо (6 точок = 720 варіантів, цього досить для всіх наших опор). */
    private static final int MAX_BRUTE = 7;

    /**
     * Повертає перестановку p: точка i першого списку (a) з'єднується з точкою p[i] другого (b).
     * la, lb: положення поперек прольоту; ya, yb: висоти. Кожна сторона центрується окремо (віднімається середнє),
     * тому зсув однієї опори вбік чи різна висота не псують вибір.
     * Результат детермінований: ті самі вхідні дані завжди дають ту саму перестановку.
     */
    public static int[] match(double[] la, double[] ya, double[] lb, double[] yb) {
        int n = la.length;
        int[] id = new int[n];
        for (int i = 0; i < n; i++) id[i] = i;
        if (n != lb.length || n <= 1 || n > MAX_BRUTE) return id;

        double[] a = center(la), b = center(lb);
        double[] va = center(ya), vb = center(yb);
        double[][] cost = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double dl = a[i] - b[j];
                double dv = va[i] - vb[j];
                cost[i][j] = dl * dl + Y_WEIGHT * dv * dv;
            }
        }
        int[] best = id.clone();
        double[] bestCost = { total(cost, id) };
        int[] cur = new int[n];
        boolean[] used = new boolean[n];
        search(cost, 0, 0.0D, cur, used, best, bestCost);
        return best;
    }

    private static void search(double[][] cost, int i, double acc, int[] cur, boolean[] used,
                               int[] best, double[] bestCost) {
        int n = cost.length;
        if (acc >= bestCost[0] - 1.0E-9D) return; // строго краща лише з істотним виграшем: при рівності лишається перший варіант
        if (i == n) {
            bestCost[0] = acc;
            System.arraycopy(cur, 0, best, 0, n);
            return;
        }
        for (int j = 0; j < n; j++) {
            if (used[j]) continue;
            used[j] = true;
            cur[i] = j;
            search(cost, i + 1, acc + cost[i][j], cur, used, best, bestCost);
            used[j] = false;
        }
    }

    private static double total(double[][] cost, int[] p) {
        double s = 0.0D;
        for (int i = 0; i < p.length; i++) s += cost[i][p[i]];
        return s;
    }

    private static double[] center(double[] v) {
        double m = 0.0D;
        for (double x : v) m += x;
        m /= v.length;
        double[] r = new double[v.length];
        for (int i = 0; i < v.length; i++) r[i] = v[i] - m;
        return r;
    }

    /**
     * Перевпорядковує списки індексів точок двох опор так, щоб k-та точка першого з'єднувалась із k-тою другого.
     *
     * @param firstIsRows true, якщо a (idxA) це "перша" опора канонічної пари. Обидві опори мусять викликати це
     *                    з однаковою канонічною парою (нижча за координатами = перша), інакше при рівній вартості
     *                    вони могли б вибрати різні пари, тому порядок аргументів задає рендерер.
     * @return {новий idxA, новий idxB}; довжини різні -> без змін.
     */
    public static int[][] reorder(int[] idxA, double[] latA, double[] yA, int[] idxB, double[] latB, double[] yB) {
        if (idxA.length != idxB.length || idxA.length <= 1) return new int[][] { idxA, idxB };
        int[] p = match(latA, yA, latB, yB);
        int[] outB = new int[idxB.length];
        for (int i = 0; i < p.length; i++) outB[i] = idxB[p[i]];
        return new int[][] { idxA, outB };
    }
}
