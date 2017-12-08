package dx.algorithm.video.vibe;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by dongxuan on 11/05/2017.
 */
public class BackgroundSubtractRGBViBe extends BackgroundSubtractViBe {

    @Override
    protected Mat getInitImg(Mat initImg) {
        if (initImg.type() == CvType.CV_8UC3) {
            return initImg;
        } else {
            Mat initRGBImg = new Mat();
            Imgproc.cvtColor(initImg, initRGBImg, Imgproc.COLOR_GRAY2BGR);
            return initRGBImg;
        }
    }

    @Override
    protected int getImgChannelNum() {
        return 3;
    }
}
