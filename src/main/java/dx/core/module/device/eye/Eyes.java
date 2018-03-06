package dx.core.module.device.eye;

import dx.core.module.camera.DxCaptureConfig;
import dx.core.module.nerual.EyesChannelNeuron;
import dx.core.module.nerual.InputableNeuron;
import dx.core.struct.AbstractLifeCycle;
import dx.core.struct.DxMatFrame;
import dx.framework.utils.Imshow;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;

import java.io.IOException;
import java.util.List;

import static dx.core.module.device.capture.DxCaptureUtils.initialize;

public class Eyes extends AbstractLifeCycle {

    private List<VideoCapture> captures;
    private DxMatFrame frame;
    private EyesChannelNeuron eyesChannelNeuron;

    private Eyes() {}

    public Eyes(int numberOfCapture, EyesChannelNeuron eyesChannelNeuron) {
        this(numberOfCapture, new DxCaptureConfig(), eyesChannelNeuron);
    }

    public Eyes(int numberOfCapture, DxCaptureConfig config, EyesChannelNeuron eyesChannelNeuron) {
        // 这里完成Capture的初始化
        this.captures = initialize(numberOfCapture);
        // 然后完成Capture的配置
        this.captures.forEach(config::operateCaptureConfig);
        // 最后生成对应的DxMatFrame实例
        this.frame = new DxMatFrame(captures);
        //
        this.eyesChannelNeuron = eyesChannelNeuron;
    }

    @Override
    public void start() {
        // 先启动视觉数据通道神经元
        if (null != eyesChannelNeuron)
            eyesChannelNeuron.start();
        // 再启动视觉数据拉取
        super.start();
    }

    @Override
    protected void loop() {
        // 获取最新的视频数据并通过神经元进行传递.
        frame.next();
        if (null != eyesChannelNeuron)
            eyesChannelNeuron.receive(frame);
    }

    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        InputableNeuron<DxMatFrame> monitor = new InputableNeuron<DxMatFrame>() {
            Mat dst = new Mat();
            Imshow imshow1 = new Imshow("m1");
            Imshow imshow2 = new Imshow("m2");
            Imshow imshowDst = new Imshow("dst");
            @Override
            public void receive(DxMatFrame dxMatFrame) {
                List<Mat> mats = dxMatFrame.getMats();
                imshow1.showImage(mats.get(0));
                imshow2.showImage(mats.get(1));
                Core.addWeighted(mats.get(0), 0.5d, mats.get(1), 0.5d, 0.0d, dst);
                imshowDst.showImage(dst);
            }
        };

        EyesChannelNeuron eyesChannelNeuron = new EyesChannelNeuron();
        eyesChannelNeuron.register(monitor);

        DxCaptureConfig config = new DxCaptureConfig();
        config.setFrameSize(new Size(640,480));
        config.setFps(30);
        Eyes eyes = new Eyes(2, config, eyesChannelNeuron);
        eyes.start();
        System.in.read();
    }
}
