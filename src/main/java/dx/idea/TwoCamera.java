package dx.idea;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dx.core.module.camera.DxCaptureConfig;
import dx.framework.utils.Imshow;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;

/**
 * Created by 洞玄 on 2017/12/3.
 *
 * @author 洞玄
 * @date 2017/12/03
 */
public class TwoCamera {

    private int videoCaptureNum = 2;

    private List<DxVideoCaptureTask> tasks = new ArrayList<>(videoCaptureNum);

    private ExecutorService videoCaptureReadExecutor = Executors.newFixedThreadPool(videoCaptureNum,
        new ThreadFactoryBuilder()
        .setDaemon(true).setNameFormat("videoCapture-%d").setPriority(Thread.MAX_PRIORITY).build());

    private class DxVideoCaptureTask implements Callable<Mat> {
        // OpenCV的摄像头封装
        private final VideoCapture videoCapture;
        private Imshow imshow;
        private Mat latest = new Mat();

        public DxVideoCaptureTask(int videoCaptureIndex) {
            this(videoCaptureIndex, new DxCaptureConfig());
        }

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
            videoCapture.read(latest);
            return latest;
        }

        public void show() {
            imshow.showImage(latest);
        }
    }

    public void init(int shot1Index, int shot2Index) {
        DxCaptureConfig config = new DxCaptureConfig();
        config.setFrameSize(new Size(1920, 1080));

        tasks.add(new DxVideoCaptureTask(shot1Index, config));
//        tasks.add(new DxVideoCaptureTask(shot2Index, config));
    }

    public void sync_000() throws Exception {

        while (true) {
            //for (DxVideoCaptureTask task : tasks) {
            //    task.call();
            //}
            videoCaptureReadExecutor.invokeAll(tasks);
            tasks.stream().forEach(DxVideoCaptureTask::show);
        }
    }

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws Exception {
        TwoCamera tc = new TwoCamera();
        tc.init(0, 1);
        tc.sync_000();
    }



}
