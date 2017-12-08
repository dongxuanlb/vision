package dx.task;

import dx.framework.camera.*;
import dx.framework.camera.DefaultFrameTaskHandler;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.findContours;

/**
 * Created by 洞玄 on 27/04/2017.
 *
 * @author 洞玄
 * @date 2017/04/27
 */
public class AdapterTask implements FrameTask {

    @Override
    public void execute(Frame frame) {
        //用于后续图像加工的Mat对象, 从source中克隆而来
        Mat dst = frame.getSource().clone();
        frame.pushMat("dst", dst);
        //先灰度
        Imgproc.cvtColor(frame.getSource(), dst, Imgproc.COLOR_BGR2GRAY);
        //高斯
        //Imgproc.GaussianBlur(dst, dst, new Size(5, 5), 0);
        Imgproc.adaptiveThreshold(dst, dst, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 15, 5);
        //List<MatOfPoint> contours = new ArrayList<>();
        //findContours(dst, contours, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_TC89_KCOS);
        //drawContours(dst, contours, -1, new Scalar(0, 255, 0), 1);
    }

    public static void main(String[] args) {
        DefaultFrameTaskHandler handler = new DefaultFrameTaskHandler();
        handler.addFrameTaskContainer(new FrameTaskContainer(new AdapterTask()));

        CameraParams params = new CameraParams();
        params.setFrameWidth(640);
        params.setFrameHeight(480);
        params.disableAutoFocus();
        Camera camera = new Camera(0, params, false);
        camera.setFrameTaskHandler(handler);
        camera.start();
    }
}
