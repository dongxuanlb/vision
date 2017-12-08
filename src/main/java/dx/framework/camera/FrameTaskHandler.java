package dx.framework.camera;

import java.util.concurrent.Callable;

import org.opencv.core.Mat;

/**
 * Created by 洞玄 on 25/04/2017.
 *
 * @author 洞玄
 * @date 2017/04/25
 */
public interface FrameTaskHandler {

    Callable<Frame> handle(Mat source);

    void setCameraInstance(Camera camera);

}
