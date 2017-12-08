package dx.framework.camera;

import dx.framework.utils.Utils;
import org.opencv.core.Mat;
import sun.misc.Unsafe;

/**
 * Created by dongxuan on 15/05/2017.
 */
public class UnsafeFrame extends Frame {

    final long dataAddress;
    final Unsafe unsafe;

    public UnsafeFrame(Mat source) {
        super(source);
        dataAddress = source.dataAddr();
        try {
            unsafe = Utils.getUnsafe();
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public double[] getPixelData(int row, int column, boolean getByMat) {
        double[] rt = new double[channels];
        int start = Utils.getStartIndex(row, column, width, channels);
        for (int i=0;i<channels;i++) {
            //by directBuffer
            rt[i] = unsafe.getByte(dataAddress + ((start+i)<<0)) & 0xFF;
        }
        return rt;
    }

    @Override
    public void putPixelData(int row, int column, double[] data, boolean setToMat) {
        int start = Utils.getStartIndex(row, column, width, channels);
        //按照给定的data长度更新(不以数据宽度channel来更新, 防止越界),
        for (int i=0;i<data.length;i++) {
            unsafe.putByte(dataAddress + ((start+i)<<0), (byte) data[i]);
        }
    }

    @Override
    public Frame newFrame(Mat source) {
        return new UnsafeFrame(source);
    }
}
