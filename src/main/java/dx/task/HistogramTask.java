package dx.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

import dx.framework.camera.Camera;
import dx.framework.camera.CameraParams;
import dx.framework.camera.DefaultFrameTaskHandler;
import dx.framework.camera.Frame;
import dx.framework.camera.FrameTask;
import dx.framework.camera.FrameTaskContainer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by 洞玄 on 28/04/2017.
 *
 * @author 洞玄
 * @date 2017/04/28
 */
public class HistogramTask implements FrameTask {

    @Override
    public void execute(Frame frame) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(frame.getSource(), hsv, Imgproc.COLOR_BGR2HSV);
        List<Mat> splitHsv = new ArrayList<>();
        Core.split(hsv, splitHsv);


        //HSV图像Hue, Saturation, Value三个通道
        MatOfInt histChannels = new MatOfInt(0, 2);
        //每个通道的Bin数量
        int hbins = 180, sbins = 256, vbins = 256;
        MatOfInt histSize = new MatOfInt(hbins, vbins);
        //每个通道的值范围
        MatOfFloat ranges = new MatOfFloat(0, 179, 0, 255);

        Mat hist = new Mat();
        Imgproc.calcHist(splitHsv, histChannels, new Mat(), hist, histSize, ranges, true);

        //不同Hue值分布下的总像素
        Map<Integer/*hueBin*/, Integer/*pixelAmount*/> hueIntensity = new TreeMap<>();
        //不同Value值分布下的总像素
        Map<Integer/*valueBin*/, Integer/*pixelAmount*/> valueIntensity = new TreeMap<>();
        //根据像素的多少进行排序
        for (int h=0;h<hbins;h++) {
            for (int v=0; v<vbins;v++) {
                //Hue, Value横竖轴的像素强度(即个数)
                Integer pixelAmount = new Double(hist.get(h, v)[0]).intValue();
                if (isNeedToCalculate(h, v, pixelAmount)) {
                    //用于计算像素总数
                    BiFunction<Integer, Integer, Integer> pixelAmountBiFunction = (key, current) -> current == null ? pixelAmount : (current + pixelAmount);
                    hueIntensity.compute(h, pixelAmountBiFunction);
                    valueIntensity.compute(v, pixelAmountBiFunction);
                }
            }
        }

        TreeMap<Integer/*pixelAmount*/, List<Integer/*hueBin*/>> hue = new TreeMap<>();
        Map<Integer/*pixelAmount*/, List<Integer/*valueBin*/>> value = new TreeMap<>();
        hueIntensity.forEach((bin, pixelAmount) -> hue.computeIfAbsent(pixelAmount, amount -> new ArrayList<>()).add(bin));
        valueIntensity.forEach((bin, pixelAmount) -> value.computeIfAbsent(pixelAmount, amount -> new ArrayList<>()).add(bin));


        double maxVal = Core.minMaxLoc(hist).maxVal;
        int scale = 3;
        Mat histImg = new Mat(vbins * scale, hbins * scale, CvType.CV_8UC3);

        for (int h=0;h<hbins;h++) {
            for (int s=0;s<vbins;s++) {
                double binVal = hist.get(h, s)[0];
                long intensity = Math.round(binVal*255/maxVal);
                Imgproc.rectangle(histImg, new Point(h*scale, s*scale), new Point((h+1)*scale-1, (s+1)*scale-1), new Scalar(0, intensity, 0));
            }
        }
        frame.pushMat("dst", histImg);
    }

    /**
     * 判断是否需要计算.
     * 这个函数可以实现:Hue或Value的低通及忽略像素值小于某阀值
     * @param hbin  Hue的Bin值.
     * @param vbin  Value的Bin值.
     * @param pixelAmount   H, V对应的像素值
     * @return
     */
    protected boolean isNeedToCalculate(Integer hbin, Integer vbin, Integer pixelAmount) {
        if (pixelAmount == null) return false;
        //FIXME 不知道为啥, 0,0像素值非常高,直接写成0
        //if (hbin == 0 && vbin == 0) return false;
        return true;
    }

    public static void main(String[] args) {
        DefaultFrameTaskHandler handler = new DefaultFrameTaskHandler();
        handler.addFrameTaskContainer(new FrameTaskContainer(new HistogramTask()));

        CameraParams params = new CameraParams();
        params.setFrameWidth(320);
        params.setFrameHeight(240);
        params.disableAutoFocus();
        Camera camera = new Camera(1, params);
        camera.setFrameTaskHandler(handler);
        camera.start();
    }
}
