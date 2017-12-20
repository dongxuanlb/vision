package dx.core.module.device.capture;

import dx.core.module.device.capture.DxCapture;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;

public class DxCaptureUtils {

    static public  final int MAX_TEST = 10;

    public static List<VideoCapture> initialize(int requireNum) {
        List<VideoCapture> captures = new ArrayList<>(requireNum);
        int[] captureIndex = new int[requireNum];
        int i = -1;
        for(int index=0;index<requireNum;index++) {
            for (;i<MAX_TEST;) {
                i++;
                VideoCapture vc = new VideoCapture(i);
                if (vc.isOpened()) {
                    captureIndex[index] = i;
                    captures.add(index, vc);
                    break;
                }
            }
        }
        if (captures.isEmpty())
            throw new IllegalStateException("No Enough Captures");
        return captures;
    }

    public static List<VideoCapture> initialize(int... specifiedDxCaptureSystemIndexs) {
        List<VideoCapture> captures = new ArrayList<>(specifiedDxCaptureSystemIndexs.length);
        int[] captureIndex = new int[specifiedDxCaptureSystemIndexs.length];
        for(int index = 0; index<captureIndex.length;index++) {
            VideoCapture vc = new VideoCapture(specifiedDxCaptureSystemIndexs[index]);
            if (!vc.isOpened()) {
                throw new IllegalStateException("Video Capture " + specifiedDxCaptureSystemIndexs[index] + " not open!");
            }
            captures.add(index, vc);
        }
        return captures;
    }
}
