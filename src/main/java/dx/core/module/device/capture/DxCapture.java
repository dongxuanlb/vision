package dx.core.module.device.capture;

import dx.core.module.camera.DxCaptureConfig;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.util.concurrent.Callable;

/**
 * 代表一路基于OpenCV的VideoCapture视频流.
 */
public class DxCapture implements Callable<Mat> {

    private VideoCapture capture;
    private Mat mat;

    private DxCapture() {}

    public DxCapture(VideoCapture capture, Mat mat) {
        this();
        this.capture = capture;
        this.mat = mat;
    }

    /**
     * 加载配置, 支持实时生效
     * @param config 配置
     * @return 当前{@link DxCapture}实例
     */
    public DxCapture loadConfig(DxCaptureConfig config) {
        if (null != config)
            config.operateCaptureConfig(capture);
        return this;
    }

    public Mat call() {
        capture.read(mat);
        return mat;
    }

}
