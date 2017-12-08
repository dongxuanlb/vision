package dx.core;

import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Size;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 * Capture的设备配置.
 *
 * Created by 洞玄 on 2017/12/4.
 *
 * @author 洞玄
 * @date 2017/12/04
 */
public class DxCaptureConfig {

    public static final double DefaultFrameWidth = 320;
    public static final double DefaultFrameHeight = 240;

    // 是否支持Capture设备的自动对焦功能.
    // 默认false, 由系统实现接管
    @Getter @Setter
    private boolean autoFocus = false;
    // 是否支持Capture设备的自动曝光功能.
    // 默认false, 由系统实现接管
    @Getter @Setter
    private boolean autoExposure = true;
    // Capture设备的角度
    @Getter @Setter
    private double focus = 0.2;
    @Getter @Setter
    private double fps = 30;
    @Getter @Setter
    private double brightness = 0.5;
    @Getter @Setter
    private double contrast = 0.5;
    @Getter @Setter
    private double saturation = 0.5;
    // 视频设备的帧分辨率大小.
    @Getter @Setter
    private Size frameSize = new Size(DefaultFrameWidth, DefaultFrameHeight);

    /**
     * 生效设备配置
     */
    public void operateCaptureConfig(VideoCapture videoCapture) {
        if (!autoFocus) {
            videoCapture.set(Videoio.CAP_PROP_AUTOFOCUS, 0.0);
        }
        if (!autoExposure) {
            videoCapture.set(Videoio.CAP_PROP_AUTO_EXPOSURE, 0.25);
        }
        // brightness, contrast, saturation with percent
        // FIXME 这些值需要从系统上次数据读取
        videoCapture.set(Videoio.CAP_PROP_BRIGHTNESS, getBrightness());
        videoCapture.set(Videoio.CAP_PROP_CONTRAST, getContrast());
        videoCapture.set(Videoio.CAP_PROP_SATURATION, getSaturation());
        videoCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, getFrameSize().width);
        videoCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, getFrameSize().height);
        videoCapture.set(Videoio.CAP_PROP_FPS, getFps());
        videoCapture.set(Videoio.CAP_PROP_FOCUS, getFocus());
    }

}
