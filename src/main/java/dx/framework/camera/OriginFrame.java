package dx.framework.camera;

import org.opencv.core.Mat;

/**
 * Created by dongxuan on 14/05/2017.
 */
public class OriginFrame extends Frame {

    public OriginFrame(Mat source) {
        super(source);
    }

    @Override
    public double[] getPixelData(int row, int column, boolean getByMat) {
        return getSource().get(row, column);
    }

    @Override
    public void putPixelData(int row, int column, double[] data, boolean setToMat) {
        getSource().put(row, column, data);
    }

    @Override
    public Frame newFrame(Mat source) {
        return this;
    }
}
