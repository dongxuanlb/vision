package dx.framework.camera;

import org.opencv.core.Mat;

import java.math.BigInteger;

/**
 * 内部使用一维数组.
 *
 * Created by dongxuan on 12/05/2017.
 */
public class ArrayFrame extends Frame {

    /**
     * Mat对象数据全部读取出来, 放到一维数组中
     * 目前摄像头提供的是8bit的深度, 所有使用byte就够了
     */
    final byte[] data;

    public ArrayFrame(Mat source) {
        super(source);
        data = new byte[source.channels() * source.cols() * source.rows()];
        //使用source填充data一维数据数组
        source.get(0, 0, data);
    }

    @Override
    public double[] getPixelData(int row, int column, boolean getByMat) {
        if (getByMat) {
            return getSource().get(row, column);
        } else {
            double[] rt = new double[channels];
            int start = getStartIndex(row, column);
            for (int i=0;i<channels;i++) {
                //to unsigned byte
                rt[i] = data[start+i] & 0xFF;
            }
            return rt;
        }
    }

    @Override
    public void putPixelData(int row, int column, double[] data, boolean setToMat) {
        if (setToMat) {
            getSource().put(row, column, data);
        }
        int start = getStartIndex(row, column);
        //按照给定的data长度更新(不以数据宽度channel来更新, 防止越界),
        for (int i=0;i<data.length;i++) {
            this.data[start + i] = (byte) data[i];
        }
    }

    /**
     * 获得数据(row, col)在一维数据中的起点
     * @param row
     * @param col
     * @return
     */
    private int getStartIndex(int row, int col) {
        //行数 * 每一行的数据长度
        int rowIndex = row * width * channels;
        //行起点 + 列起点(列号 * 数据宽度)
        return rowIndex + col * channels;
    }

    @Override
    public Frame newFrame(Mat source) {
        return new ArrayFrame(source);
    }

}
