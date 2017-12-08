package dx.framework.camera;

import java.util.HashMap;
import java.util.Map;

import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 * Created by 洞玄 on 25/04/2017.
 *
 * @author 洞玄
 * @date 2017/04/25
 */
public class CameraParams {

    private Map<Integer, Double> params = new HashMap<>();


    public CameraParams setFrameWidth(Integer width) {
        params.put(Videoio.CV_CAP_PROP_FRAME_WIDTH, width.doubleValue());
        return this;
    }

    public CameraParams setFrameHeight(Integer height) {
        params.put(Videoio.CV_CAP_PROP_FRAME_HEIGHT, height.doubleValue());
        return this;
    }

    public CameraParams disableAutoFocus() {
        params.put(Videoio.CAP_PROP_AUTOFOCUS, 0.0);
        return this;
    }

    public void setParams(VideoCapture videoCapture) {
        if (null != videoCapture) {
            params.entrySet().stream().forEach(entry -> videoCapture.set(entry.getKey(), entry.getValue()));
        }
    }

}
