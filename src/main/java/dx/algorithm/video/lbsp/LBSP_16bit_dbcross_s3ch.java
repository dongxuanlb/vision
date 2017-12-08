package dx.algorithm.video.lbsp;

import dx.algorithm.utils.Maths;
import dx.framework.utils.Utils;
import sun.misc.Unsafe;

/**
 * LBSP 16bit double cross single 3 channel
 * Created by dongxuan on 16/05/2017.
 */
public class LBSP_16bit_dbcross_s3ch {

// note: this is the LBSP 16 bit double-cross indiv RGB pattern as used in
// the original article by G.-A. Bilodeau et al.
//
//  O   O   O          4 ..  3 ..  6
//    O O O           .. 15  8 13 ..
//  O O X O O    =>    0  9  X 11  1
//    O O O           .. 12 10 14 ..
//  O   O   O          7 ..  2 ..  5
//          (single/3x)            (single/3x)
//
// must be defined externally:
//      _t              (size_t, absolute threshold used for comparisons)
//      _ref            (uchar, 'central' value used for comparisons)
//      _data           (uchar*, triple-channel data to be covered by the pattern)
//      _y              (int, pattern rows location in the image data)
//      _x              (int, pattern cols location in the image data)
//      _c              (size_t, pattern channel location in the image data)
//      _step_row       (size_t, step size between rows, including padding)
//      _res            (ushort, 16 bit result vector)
//       L1dist         (function, returns the absolute difference between two uchars)

    static Unsafe unsafe;
    static int channel = 3;

    static {
        try {
            unsafe = Utils.getUnsafe();
        } catch (Throwable ex) {}
    }

    static int _val(long dataAddress, long stepRow, int positionX, int positionY, int n, int offsetX, int offsetY) {
        return unsafe.getByte(dataAddress + stepRow*(positionY+offsetY)+channel*(positionX+offsetX)+n) & 0xFF;
    }

    /**
     * 计算两值的曼哈顿距离, 超过阀值返回1否则返回0
     * @param a 点a
     * @param b 点b
     * @param threshold 阀值
     * @return 超过阀值返回1否则返回0
     */
    static int _l1dist_threshold(int a, int b, int threshold) {
        return Maths.l1dist(a, b) > threshold ? 1 : 0;
    }

    public static int computeSingleRGBDescriptor(int ref, long dataAddress, long stepRow, int positionX, int positionY, int n, int threshold) {
        int res = (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, -1, 1), ref, threshold) << 15)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, 1, -1), ref, threshold) << 14)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, 1, 1), ref, threshold) << 13)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, -1, -1), ref, threshold) << 12)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, 1, 0), ref, threshold) << 11)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, 0, -1), ref, threshold) << 10)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, -1, 0), ref, threshold) << 9)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, 0, 1), ref, threshold) << 8)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, -2, -2), ref, threshold) << 7)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, 2, 2), ref, threshold) << 6)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, 2, -2), ref, threshold) << 5)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, -2, 2), ref, threshold) << 4)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, 0, 2), ref, threshold) << 3)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, 0, -2), ref, threshold) << 2)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, 2, 0), ref, threshold) << 1)
                + (_l1dist_threshold(_val(dataAddress, stepRow, positionX, positionY, n, -2, 0), ref, threshold))
                ;
        return res;
    }

}
