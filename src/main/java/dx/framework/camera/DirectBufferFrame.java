package dx.framework.camera;

import dx.framework.utils.Utils;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;

/**
 * Created by dongxuan on 14/05/2017.
 */
public class DirectBufferFrame extends Frame {

    ByteBuffer byteBuffer;

    public DirectBufferFrame(Mat source) {
        super(source);
        //分配空间
        int size = source.channels() * source.cols() * source.rows();
        byteBuffer = ByteBuffer.allocateDirect(size);
        byte[] data = new byte[size];
        source.get(0, 0, data);
        byteBuffer.put(data);
    }

    @Override
    public double[] getPixelData(int row, int column, boolean getByMat) {
        double[] rt = new double[channels];
        int start = Utils.getStartIndex(row, column, width, channels);
        for (int i=0;i<channels;i++) {
            //to unsigned byte
            rt[i] = byteBuffer.get(start+i) & 0xFF;
        }
        return rt;
    }

    @Override
    public void putPixelData(int row, int column, double[] data, boolean setToMat) {
        int start = Utils.getStartIndex(row, column, width, channels);
        //按照给定的data长度更新(不以数据宽度channel来更新, 防止越界),
        for (int i=0;i<data.length;i++) {
            byteBuffer.put(start + i, (byte) data[i]);
        }
    }

    @Override
    public Frame newFrame(Mat source) {
        return new DirectBufferFrame(source);
    }
}
