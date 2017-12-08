package dx.framework.utils;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Scalar;

/**
 * Created by 洞玄 on 26/04/2017.
 *
 * @author 洞玄
 * @date 2017/04/26
 */
public class Cores {

    public static Scalar Scalar(double x, double y, double z) {
        return new Scalar(x, y, z);
    }

    public static <X, Y> Pair<X, Y> Pair(X x, Y y) {
        return new MutablePair<>(x, y);
    }

}
