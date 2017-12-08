package dx.framework.camera;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by dongxuan on 12/05/2017.
 */
public class Nd4jFrame extends Frame {

    /**
     * 高度(row)为height, 宽度为(col)为with * 通道数的二维矩阵
     */
    private INDArray data;

    public Nd4jFrame(Mat source) {
        super(source);
        //把图像深度提到int
        Mat intSource = new Mat();
        source.convertTo(intSource, CvType.CV_32SC3);
        int[] data = new int[intSource.channels() * intSource.cols() * intSource.rows()];
        //使用intSource填充data一维数据数组
        intSource.get(0, 0, data);
        //创建高度(row)为height, 宽度为(col)为with * 通道数的二维矩阵
        this.data = Nd4j.create(Nd4j.createBuffer(data), new int[]{height, width*channels});
    }

    @Override
    public double[] getPixelData(int row, int column, boolean getByMat) {
        if (getByMat) {
            return getSource().get(row, column);
        }
        double[] rt = new double[channels];
        int columnIndex = column * channels;
        for (int i=0;i<3;i++) {
            rt[i] = data.getDouble(row, columnIndex + i);
        }
//        this.data.get(NDArrayIndex.point(row), NDArrayIndex.interval(columnIndex, columnIndex+channels));
        return rt;
    }

    @Override
    public void putPixelData(int row, int column, double[] data, boolean setToMat) {

    }

    @Override
    public Frame newFrame(Mat source) {
        return new Nd4jFrame(source);
    }
}
