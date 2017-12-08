package dx.framework.utils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;

/**
 * Created by 洞玄 on 25/04/2017.
 *
 * @author 洞玄
 * @date 2017/04/25
 */
public class Utils {

    public static void drawStr(Mat mat, Point target, String str) {
        Imgproc.putText(mat, str, new Point(target.x+1, target.y+1), FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0), 1);
        Imgproc.putText(mat, str, target, FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 255, 255), 1);
    }

    public static Scalar GetBGRFromHSV(Scalar hsvValue) {
        Mat hsvMat = new Mat(1, 1, CvType.CV_8UC3, hsvValue);
        Mat bgrMat = new Mat();
        Imgproc.cvtColor(hsvMat, bgrMat, Imgproc.COLOR_HSV2BGR);
        double[] bgr = bgrMat.get(0, 0);
        return new Scalar(bgr[0], bgr[1], bgr[2]);
    }

    public static Scalar GetHSVFromRBG(Scalar rgbValue) {
        Mat rbgMat = new Mat(1, 1, CvType.CV_8UC3, rgbValue);
        Mat hsvMat = new Mat();
        Imgproc.cvtColor(rbgMat, hsvMat, Imgproc.COLOR_RGB2HSV);
        double[] hsv = hsvMat.get(0, 0);
        return new Scalar(hsv[0], hsv[1], hsv[2]);
    }

    public static Unsafe getUnsafe() throws SecurityException,
            NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException
    {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        return unsafe;
    }

    public static final int getStartIndex(final int row, final int col, final int width, final int channels) {
        //行数 * 每一行的数据长度
        int rowIndex = row * width * channels;
        //行起点 + 列起点(列号 * 数据宽度)
        return rowIndex + col * channels;
    }

    public static int preventOverflow(float input, int lower, int upper) {
        int v = Math.round(input);
        if (v < lower) {
            v = lower;
        } else if (v > upper) {
            v = upper;
        }
        return v;
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Scalar rgb = GetBGRFromHSV(new Scalar(0, 255, 255));
        System.out.println(rgb);
        Scalar hsv = GetHSVFromRBG(new Scalar(255, 0, 0));
        System.out.println(hsv);
    }
}
