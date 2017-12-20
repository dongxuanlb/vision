package dx.core.struct;

import com.google.common.base.Stopwatch;
import dx.core.module.device.capture.DxCapture;
import lombok.Getter;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Stopwatch.createUnstarted;
import static dx.core.module.device.capture.DxCaptureUtils.initialize;

public class DxMatFrame {

    private final List<DxCapture> captures = new ArrayList<>();
    @Getter
    private final List<Mat> mats = new ArrayList<>();
    // 计时器, 每次next函数过后, 这个watch就可以读取到最近一次耗时.
    @Getter
    private final Stopwatch timeConsuming = createUnstarted();

    public DxMatFrame(int num) {
        this(initialize(num));
    }

    public DxMatFrame(List<VideoCapture> videoCaptures) {
        for (int i=0;i<videoCaptures.size();i++) {
            Mat mat = new Mat();
            DxCapture capture = new DxCapture(videoCaptures.get(i) , mat);
            mats.add(mat);
            captures.add(capture);
        }
    }

    public DxMatFrame next() {
        timeConsuming.reset().start();
        captures.parallelStream().forEach(DxCapture::call);
        timeConsuming.stop();
        return this;
    }

}
