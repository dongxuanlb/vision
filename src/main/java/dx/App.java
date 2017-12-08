package dx;

import dx.framework.camera.Camera;
import dx.framework.camera.CameraParams;
import dx.framework.camera.DefaultFrameTaskHandler;
import dx.framework.camera.FrameTaskContainer;
import dx.task.HSVTuneTask;

public class App {

    public static void main(String[] args) {
        DefaultFrameTaskHandler handler = new DefaultFrameTaskHandler();
        handler.addFrameTaskContainer(new FrameTaskContainer(new HSVTuneTask()));

        CameraParams params = new CameraParams();
        params.setFrameWidth(640);
        params.setFrameHeight(480);
        params.disableAutoFocus();
        Camera camera = new Camera(0, params, false);
        camera.setFrameTaskHandler(handler);
        camera.start();
    }
}
