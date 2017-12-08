package dx.algorithm.video.vibe;

import dx.algorithm.utils.Maths;
import dx.algorithm.utils.Opencvs;
import dx.framework.camera.Frame;
import org.apache.commons.lang3.time.StopWatch;
import org.opencv.core.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dx.algorithm.utils.Opencvs.random;
import static java.util.stream.IntStream.range;

/**
 * Created by dongxuan on 11/05/2017.
 */
public abstract class BackgroundSubtractViBe {
    //8UC3
    final static Scalar Black3Scalar = new Scalar(0, 0, 0);
    //8UC1
    final static Scalar Black1Scalar = new Scalar(0);
    protected Logger logger = Logger.getLogger("ViBeLogger");
    //从输入图像中获取的每个像素(或区块)的采样点数量, 用于构建背景模型.
    //相当于论文中的变量'N'
    protected int bgSamples;
    //一组背景图像集合
    public Frame[] bgImgs;
    //用于被认为当前像素(或区块)是背景的相似采样点数量.
    //相当于论文中的变量'#min'
    protected int requiredBgSamples;

    //输入图像大小
    protected Size imgSize;
    protected int imgHeight;
    protected int imgWidth;
    //绝对色彩距离阀值
    //相当于论文中的'R'
    protected int colorDistThreshold;
    //是否完整初始化
    private AtomicInteger initialized = new AtomicInteger(-1);

    static int Status_UnInit = -1;
    static int Status_Initing = 0;
    static int Status_Inited = 1;

    public BackgroundSubtractViBe() {
        this(10, 2, 20);
    }

    public BackgroundSubtractViBe(int bgSamples, int requiredBgSamples, int colorDistThreshold) {
        this.bgSamples = bgSamples;
        this.requiredBgSamples = requiredBgSamples;
        this.colorDistThreshold = colorDistThreshold;
    }

    public void initialize(final Frame initFrame) {
        //使用AtomicInteger保证仅在一个线程内完成所有初始化过程
        if (initialized.compareAndSet(Status_UnInit, Status_Initing)) {
            StopWatch sw = new StopWatch();
            sw.start();
            this.imgSize = initFrame.getSource().size();
            this.imgHeight = (int) imgSize.height;
            this.imgWidth = (int) imgSize.width;
            this.bgImgs = new Frame[bgSamples];
            //初始化所有背景模型
            try {
                //使用stream的parallel版本能提高性能
                //正常情况CPU核数不会超过bgSamples数量, 所以在外部使用parallel就可以了
                //内部循环如果再并发反而会增加context切换带来损耗
                range(0, bgSamples).parallel().forEach(i -> {
                    //用全黑填充
                    bgImgs[i] = initFrame.newFrame(new Mat(initFrame.getSource().size(), initFrame.getSource().type(), Black3Scalar));
                    for (int row=0;row<imgHeight;row++) {
                        for (int col=0;col<imgWidth;col++) {
                            Point samplePoint = Opencvs.getRandSamplePosition_7x7(col, row, imgWidth, imgHeight);
                            int x = (int) samplePoint.x;
                            int y = (int) samplePoint.y;
                            bgImgs[i].putPixelData(y, x, initFrame.getPixelData(y, x, false), false);
                        }
                    }
                });
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage());
            }
            initialized.set(Status_Inited);
            logger.info("Initialized cost " + sw.getTime() + "ms");
        }
    }

    /**
     *
     * @param input         输入图像
     * @param learningRate  学习频率
     */
    public Mat apply(Frame input, final double learningRate) {
        Mat fgMask = new Mat(input.getSource().size(), CvType.CV_8UC1, Black1Scalar);
        for (int h=0;h<imgHeight;h++) {
            for (int w=0;w<imgWidth;w++) {
                int bgLikePixelCount = 0, bgImgIndex = 0;
                //当前row, col对应的像素点数据
                double[] currentInputPixelData = input.getPixelData(h, w, false);
                //将当前像素点(row, col)的数据, 与每个背景模型(bgImgs)中相同像素点的数据进行比较
                //如果发现dist下于特定阀值, 则认为这个点是"近似背景点"
                //否则就是前景点了.
                while(bgLikePixelCount < requiredBgSamples && bgImgIndex < bgSamples) {
                    double[] bg = bgImgs[bgImgIndex].getPixelData(h, w, false);
                    if (Maths.l1dist(currentInputPixelData, bg) < colorDistThreshold * getImgChannelNum()) {
                        bgLikePixelCount++;
                    }
                    bgImgIndex++;
                }
                if (bgLikePixelCount < requiredBgSamples) {
                    fgMask.put(h, w, 255);
                } else {
                    if (random.nextInt(Integer.MAX_VALUE)%learningRate == 0) {
                        //随机更新某副背景图片
                        bgImgs[random.nextInt(Integer.MAX_VALUE)%bgSamples].putPixelData(h, w, currentInputPixelData, false);
                    }
                    if (random.nextInt(Integer.MAX_VALUE)%learningRate == 0) {
                        Point point = Opencvs.getNeighorPoistion_3x3(w, h, imgWidth, imgHeight);
                        bgImgs[random.nextInt(Integer.MAX_VALUE)%bgSamples].putPixelData((int) point.y, (int) point.x, currentInputPixelData, false);

                    }
                }
            }
        }
        return fgMask;
    }

    protected abstract Mat getInitImg(Mat initImg);

    protected abstract int getImgChannelNum();

    public boolean isInitialized() {
        return initialized.get() == Status_Inited;
    }

    public boolean isNotInitialized() {
        return initialized.get() == Status_UnInit;
    }
}
