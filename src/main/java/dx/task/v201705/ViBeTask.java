package dx.task.v201705;

import dx.algorithm.video.BackgroundSubtractorLBSP;
import dx.algorithm.video.BackgroundSubtractorSuBSENSE;
import dx.algorithm.video.vibe.BackgroundSubtractRGBViBe;
import dx.framework.camera.*;
import org.opencv.core.Mat;

/**
 * Created by dongxuan on 11/05/2017.
 */
public class ViBeTask implements FrameTask {

    @Override
    public void execute(Frame frame) {

    }

    public static void main(String[] args) {
        //params
        CameraParams params = new CameraParams();
        params.setFrameWidth(320);
        params.setFrameHeight(240);
        params.disableAutoFocus();
        Camera camera = new Camera(1, params, true, true);
        //environment
        CameraEnvironment environment = new CameraEnvironment();
        environment.frameType = FrameType.Array;
        camera.setEnvironment(environment);
        //handler
        DefaultFrameTaskHandler handler = new DefaultFrameTaskHandler();
        handler.addFrameTaskContainer(new FrameTaskContainer(new ViBeTask()));
        camera.setFrameTaskHandler(handler);
        //start
        camera.start();
    }
}
