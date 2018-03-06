package dx.idea;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dx.algorithm.sgm.MotionDetector;
import dx.core.module.camera.DxCaptureConfig;
import dx.framework.utils.Imshow;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Camera {

    ExecutorService videoCaptureReadExecutor = Executors.newFixedThreadPool(1,
            new ThreadFactoryBuilder()
                    .setDaemon(false)
                    .setNameFormat("videoCapture-%d")
                    .setPriority(Thread.MAX_PRIORITY)
                    .build());

    DxVideoCaptureTask task;

    Mat motionMask = new Mat();

    MotionDetector motionDetector = new MotionDetector(new Size(640, 480), new Size(1, 1));

    private class DxVideoCaptureTask implements Callable<Mat> {
        // OpenCV的摄像头封装
        private final VideoCapture videoCapture;
        private Imshow imshow;
        private Mat latest = new Mat();

        public DxVideoCaptureTask(int videoCaptureIndex, DxCaptureConfig config) {
            this.videoCapture = new VideoCapture(videoCaptureIndex);
            if (!this.videoCapture.isOpened()) {
                throw new IllegalStateException("Video Capture " + videoCaptureIndex + " not open!");
            }
            imshow = new Imshow("VC" + videoCaptureIndex);
            config.operateCaptureConfig(videoCapture);
        }

        @Override
        public Mat call() throws Exception {
            while(videoCapture.read(latest)) {
                motionDetector.detectMotion(latest, motionMask);
                show();
            }
            return latest;
        }

        public void show() {
            imshow.showImage(latest);
        }
    }

    public void init(int videoCaptureIndex) {
        DxCaptureConfig config = new DxCaptureConfig();
        config.setFrameSize(new Size(640, 480));
        task = new DxVideoCaptureTask(videoCaptureIndex, config);
    }

    public void start() {
        videoCaptureReadExecutor.submit(task);
    }

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws Exception {
        Camera camera = new Camera();
        camera.init(0);
        camera.start();
    }

}
