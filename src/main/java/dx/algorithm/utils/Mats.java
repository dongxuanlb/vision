package dx.algorithm.utils;

import dx.framework.utils.Utils;
import org.opencv.core.Mat;
import sun.misc.Unsafe;

/**
 * Created by dongxuan on 16/05/2017.
 */
public class Mats {

    static Unsafe unsafe;

    static {
        try {
            unsafe = Utils.getUnsafe();
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 16U3C
     *                    1pixel
     *   3ch      R          B          G
     *          / \        / \        / \
     *   16b  byte byte  byte byte  byte byte
     *
     * 8U3C
     *                    1pixel
     *   3ch      R          B          G
     *            |          |          |
     *   16b    byte        byte       byte
     */

    /**
     * 获得Mat对象指定index的unsigned short值.
     * @param mat       图像深度一定要是16bit, 2Byte
     * @param index     从数据点上来看的索引号
     * @return unsigned short. 需要使用int, 否则java的short就越界了.
     */
    public static int getUnsignedShortData(Mat mat, int index) {
        return getShortData(mat, index) & 0xFFFF;
    }

    public static short getShortData(Mat mat, int index) {
        return unsafe.getShort(mat.dataAddr() + 2 * index);
    }

    /**
     * 获得Mat对象指定index的unsigned byte值.
     * @param mat       图像深度一定要是8bit, 1Byte
     * @param index     从数据点上来看的索引号
     * @return
     */
    public static int getUnsignedByteData(Mat mat, int index) {
        return getByteData(mat, index) & 0xFF;
    }

    public static void putUnsignedShortData(Mat mat, int index, int data) {
        putShortData(mat, index, (short)data);
    }

    public static void putShortData(Mat mat, int index, short data) {
        unsafe.putShort(mat.dataAddr() + 2*index, data);
    }

    public static byte getByteData(Mat mat, int index) {
        return unsafe.getByte(mat.dataAddr() + index);
    }

    public static void putUnsignedByteData(Mat mat, int index, int data) {
        putByteData(mat, index, (byte)data);
    }

    public static void putByteData(Mat mat, int index, byte data) {
        unsafe.putByte(mat.dataAddr() + index, data);
    }

    @Deprecated
    public static void byteData(Mat mat, int point, byte data) {
        unsafe.putByte(mat.dataAddr() + (point<<0), data);
    }

    @Deprecated
    public static void putData(Mat mat, int point, short data) {
        unsafe.putShort(mat.dataAddr() + (point<<0), data);
    }

}
