package dx.algorithm.video.vibe;

import org.opencv.core.Mat;

/**
 * Created by dongxuan on 11/05/2017.
 */
public class BackgroundSubtractGrayViBe extends BackgroundSubtractViBe {


    @Override
    protected Mat getInitImg(Mat initImg) {
        return initImg;
    }

    @Override
    protected int getImgChannelNum() {
        return 1;
    }
}
