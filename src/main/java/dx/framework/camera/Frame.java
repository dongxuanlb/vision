package dx.framework.camera;

import dx.framework.utils.Imshow;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by 洞玄 on 24/04/2017.
 *
 * @author 洞玄
 * @date 2017/04/24
 */
public abstract class Frame {

    static Map<String, Pair<Imshow, Mat>> monitors = new HashMap<>();
    //此帧的原始Mat内容
    private final Mat source;
    //图像通道数量, BGR3, GRAY1
    protected final int channels;
    protected final int width;
    protected final int height;
    //图像深度, bit
    protected final int depthBit;
    //创建此帧的时间戳
    private long timeMillis = System.currentTimeMillis();

    private static Map<Integer, Integer> BitsMapping = new HashMap(){{
        put(0, 8);//CV_8U
        put(1, 8);//CV_8S
        put(2, 16);//CV_16U
        put(3, 16);//CV_16S
        put(4, 32);//CV_32U
        put(5, 32);//CV_32S
    }};

    public Frame(Mat source) {
        this.source = source;
        this.channels = source.channels();
        this.width = source.width();
        this.height = source.height();
        this.depthBit = BitsMapping.get(source.depth());
    }

    /**
     *
     * @param row    高度; [0, height), 高度最大值减1
     * @param column 宽度; [0, column), 宽度最大值减1
     * @param getByMat 是否从原始的Mat对象获取; 以目前测试来看, 读取原生的Mat性能比较差.
     * @return
     */
    public abstract double[] getPixelData(int row, int column, boolean getByMat);

    /**
     *  @param row
     * @param column
     * @param data
     * @param setToMat  是否同时修改原始的Mat对象; 如果修改会比较慢.
     */
    public abstract void putPixelData(int row, int column, double[] data, boolean setToMat);

    /**
     *
     * @param source
     * @return
     */
    public abstract Frame newFrame(Mat source);

    public Frame pushMat(String windowName, Mat mat) {
        monitors.computeIfAbsent(windowName, wn -> MutablePair.of(new Imshow(wn), mat)).setValue(mat);
        return this;
    }

    public Map<Imshow, Mat> getMonitors() {
        return monitors.values().stream().collect(Collectors.toMap(p -> p.getLeft(), p -> p.getRight()));
    }

    public Mat getSource() {
        return source;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

}
