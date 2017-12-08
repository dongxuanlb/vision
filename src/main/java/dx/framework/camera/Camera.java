package dx.framework.camera;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dx.framework.utils.Imshow;
import dx.framework.utils.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.videoio.VideoCapture;

import static java.lang.System.currentTimeMillis;

/**
 * Created by 洞玄 on 24/04/2017.
 *
 * @author 洞玄
 * @date 2017/04/24
 */
public class Camera implements Runnable {

    //OpenCV的摄像头句柄
    private VideoCapture videoCapture;
    //是否显示原始图像窗口
    private boolean showWindow = true;
    //原始图像的显示窗口
    private Imshow imshow = new Imshow("Camera");
    //相机运行时环境变量
    private CameraEnvironment environment = new CameraEnvironment();

    private FrameTaskHandler frameTaskHandler;

    private Integer frameCounter = 0;
    //首次多少帧直接跳过, 防止摄像头还在初始化中.
    private Integer skipFirstFrameCount = 10;
    //是否启用多线程
    private boolean async = true;

    //存放视频帧的队列
    private BlockingDeque<Future<Frame>> queue = new LinkedBlockingDeque<>(Runtime.getRuntime().availableProcessors());

    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()/2+1,
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("camera-handle-pool-%d")
            .build());

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * 设置相机的帧处理器.
     * @param frameTaskHandler
     */
    public void setFrameTaskHandler(FrameTaskHandler frameTaskHandler) {
        if (null != frameTaskHandler) {
            frameTaskHandler.setCameraInstance(this);
            this.frameTaskHandler = frameTaskHandler;
        }
    }

    /**
     * 获得相机的运行时环境变量
     * @return 运行时环境变量
     */
    public CameraEnvironment getEnvironment() {
        return environment;
    }

    /**
     * 设置相机的运行时环境变量
     * @param environment
     */
    public void setEnvironment(CameraEnvironment environment) {
        this.environment = environment;
    }

    public Camera(int index) {
        this(index, null);
    }

    public Camera(int index, CameraParams params) {
        this(index, params, true, true);
    }

    public Camera(int index, CameraParams params, boolean showWindow) {
        this(index, params, showWindow, true);
    }

    public Camera(int index, CameraParams params, boolean showWindow, boolean async) {
        this.videoCapture = new VideoCapture(index);
        //强制检查VC是否可以使用
        if (!videoCapture.isOpened()) {
            throw new IllegalStateException("Video Capture " + index + " not found!");
        }
        //设置摄像头参数
        if (null != params) {
            params.setParams(videoCapture);
        }
        //设置是否显示原始图像
        this.showWindow = showWindow;
        this.async = async;
    }

    public void start() {
        Thread readingThread = new Thread(this, "CameraReadingThread");
        readingThread.setDaemon(false);
        readingThread.start();
    }

    @Override
    public void run() {
        if (async) {
            runInAsync();
        } else {
            runInSync();
        }
    }

    protected void runInAsync() {
        while (true) {
            Future<Frame> head;
            while (null != (head = queue.peekFirst()) && head.isDone()) {
                try {
                    showWindow(queue.pollFirst().get());
                } catch (Exception e) {
                    //Ignore Exceptions
                }
            }
            //如果队列中还有空余则读取当前视频帧并提交任务
            if (queue.remainingCapacity() > 0) {
                Mat source = new Mat();
                if (videoCapture.read(source)) {
                    if (frameCounter < skipFirstFrameCount) {
                        frameCounter++;
                        continue;
                    }
                    queue.offerLast(executor.submit(frameTaskHandler.handle(source)));
                }
            }
        }
    }

    protected void runInSync() {
        while (true) {
            Mat source = new Mat();
            if (videoCapture.read(source)) {
                if (frameCounter < skipFirstFrameCount) {
                    frameCounter++;
                    continue;
                }
                try {
                    showWindow(frameTaskHandler.handle(source).call());
                } catch (Exception e) {
                    //
                }
            }
        }
    }

    protected void showWindow(Frame frame) {
        if (showWindow && frame != null) {
            Mat source = frame.getSource();
            Utils.drawStr(source, new Point(20, 20), String.format("FPS: %.1f", 1000f / (currentTimeMillis() - frame.getTimeMillis())));
            imshow.showImage(source);
        }
    }
}
