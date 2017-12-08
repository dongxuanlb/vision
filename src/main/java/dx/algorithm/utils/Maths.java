package dx.algorithm.utils;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

/**
 * Created by dongxuan on 11/05/2017.
 */
public class Maths {

    /**
     * 使用FastMath做abs运算
     * @param x
     * @return
     */
    public static double abs(double x) {
        return FastMath.abs(x);
    }

    public static int l1dist(final int a, final int b) {
        return FastMath.abs(a-b);
    }

    /**
     * L1距离(曼哈顿距离)
     * @param a
     * @param b
     * @return
     */
    public static double l1dist(final double[] a, final double[] b) {
        double result = .0;
        for (int i = 0; i < a.length; i++) {
            result += abs(a[i] - b[i]);
        }
        return result;
    }

    public static double l2dist(final double[] a, final double[] b) {
        double result = .0;
        for (int i = 0; i < a.length; i++) {
            result += FastMath.pow(a[i] - b[i], 2);
        }
        return result;
    }


}
