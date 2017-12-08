package dx.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import dx.framework.camera.Frame;
import dx.framework.camera.FrameTask;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static com.google.common.collect.Lists.newArrayList;
import static dx.framework.utils.Cores.Pair;
import static dx.framework.utils.Cores.Scalar;
import static dx.framework.utils.Utils.GetBGRFromHSV;
import static org.opencv.core.Core.bitwise_or;
import static org.opencv.core.Core.inRange;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_TC89_KCOS;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.findContours;

/**
 * Created by 洞玄 on 25/04/2017.
 *
 * @author 洞玄
 * @date 2017/04/25
 */
public class HSVTuneTask implements FrameTask {

    private static List<ColorRange> colorRanges = new ArrayList<>();

    static {
        colorRanges.add(new ColorRange(
            "Green",
            Scalar(60, 255, 255),
            newArrayList(
                Pair(Scalar(40, 30, 16), Scalar(80, 255, 255))
            )
        ));
        colorRanges.add(new ColorRange(
            "Red",
            Scalar(10, 255, 255),
            newArrayList(
                Pair(Scalar(0, 25, 128), Scalar(10, 255, 255))
            )
        ));
    }

    @Override
    public void execute(Frame frame) {
        //用于后续图像加工的Mat对象, 从source中克隆而来
        Mat dst = frame.getSource().clone();
        frame.pushMat("dst", dst);
        //一组与source画幅一致的Mat
        Mat empty = new Mat(dst.rows(), dst.width(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        frame.pushMat("Empty", empty);
        //获得HSV空间
        Mat hsvSpace = new Mat();
        cvtColor(frame.getSource(), hsvSpace, Imgproc.COLOR_BGR2HSV);
        //加高斯
        Mat hsvGaS = new Mat();
        GaussianBlur(hsvSpace, hsvGaS, new Size(11, 11), 0);
        colorRanges.parallelStream().forEach(colorRange -> {
            try {
                Scalar contourHueByBGR = GetBGRFromHSV(colorRange.getHue());
                List<MatOfPoint> contours = new ColorRangeDetector(hsvGaS, colorRange).call();
                drawContours(dst, contours, -1, contourHueByBGR, 1);
                drawContours(empty, contours, -1, contourHueByBGR, -1);
            } catch (Exception e) {}
        });
    }

    class ColorRangeDetector implements Callable<List<MatOfPoint>> {

        private Mat hsvSpace;

        private ColorRange colorRange;

        public ColorRangeDetector(Mat hsvSpace, ColorRange colorRange) {
            this.hsvSpace = hsvSpace;
            this.colorRange = colorRange;
        }

        @Override
        public List<MatOfPoint> call() throws Exception {
            Mat combinedMask = new Mat(hsvSpace.rows(), hsvSpace.cols(), CvType.CV_8UC1, new Scalar(0, 0, 0));
            //将一组色域(Hue)的mask合并
            colorRange.getRanges().stream().map(p -> {
                //根据颜色的上下界寻找mask区域
                Mat mask = new Mat();
                //使用inRange函数找出lower和upper区间内的像素
                inRange(hsvSpace, p.getLeft(), p.getRight(), mask);
                return mask;
            }).forEach(_mask -> bitwise_or(_mask, combinedMask, combinedMask));
            //根据mask寻找边缘
            List<MatOfPoint> contours = new ArrayList<>();
            findContours(combinedMask, contours, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_TC89_KCOS);
            //做一些过滤
            contours = contours.stream().filter(contour -> contourArea(contour) > 50).collect(Collectors.toList());
            return contours;
        }
    }

    //代表一组颜色域
    static class ColorRange {
        /**
         * 这组色域的标签名称, 比如绿色, 蓝色;
         */
        private String label;
        /**
         * 该色域的主色调. 用于指定边缘(填充)的颜色
         */
        private Scalar hue = new Scalar(60, 255, 255);
        /**
         * 一组上下限色调(Hue)
         */
        private List<Pair<Scalar, Scalar>> ranges = new ArrayList<>();

        public ColorRange(String label,
                          List<Pair<Scalar, Scalar>> ranges) {
            this.label = label;
            this.ranges = ranges;
            if (!ranges.isEmpty()) {
                this.hue = ranges.get(0).getRight();
            }
        }

        public ColorRange(String label, Scalar hue,
                          List<Pair<Scalar, Scalar>> ranges) {
            this.label = label;
            this.hue = hue;
            this.ranges = ranges;
        }

        public String getLabel() {
            return label;
        }

        public Scalar getHue() {
            return hue;
        }

        public List<Pair<Scalar, Scalar>> getRanges() {
            return ranges;
        }
    }
}
