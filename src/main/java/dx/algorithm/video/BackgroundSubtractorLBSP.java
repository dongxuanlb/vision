package dx.algorithm.video;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by dongxuan on 15/05/2017.
 */
public abstract class BackgroundSubtractorLBSP {

    final static Logger logger = Logger.getLogger("BackgroundSubtractorLBSP");

    //8UC3
    final static Scalar Black3Scalar = new Scalar(255, 255, 255);
    final static Scalar White3Scalar = new Scalar(0, 0, 0);
    //8UC1
    final static Scalar White1Scalar = new Scalar(255);
    final static Scalar Black1Scalar = new Scalar(0);

    int UCHAR_MIN = 0;
    static final int UCHAR_MAX = 255;

    static int Status_UnInit = -1;
    static int Status_Initing = 0;
    static int Status_Inited = 1;

    static Size DEFAULT_FRAME_SIZE = new Size(320,240);
    static float FEEDBACK_T_LOWER = 2.0000f;
    static float FEEDBACK_T_UPPER = 256.00f;
    // local define used to determine the default median blur kernel size
    static int DEFAULT_MEDIAN_BLUR_KERNEL_SIZE = 9;
    //! parameters used to define model reset/learning rate boosts in our frame-level component
//    static int FRAMELEVEL_MIN_COLOR_DIFF_THRESHOLD  = (m_nMinColorDistThreshold/2)
    static int FRAMELEVEL_ANALYSIS_DOWNSAMPLE_RATIO = 8;

    /**
     * Created by dongxuan on 16/05/2017.
     */ //basic info struct used in px model LUTs
    public static class PxInfoBase {
        int nImgCoord_Y;
        int nImgCoord_X;
        int nModelIdx;
    }

    /**
     * 理解nPxIter, nModelIter
     *
     * 在算法初始化时, 有两个核心输入参数
     * oInitImg:用于初始化的输入图像, 数据结构为Mat
     * oROI:用于表示需要算法关注的ROI, 数据结构为Mat
     *
     *
     *
     */

    //! background model ROI used for LBSP descriptor extraction (specific to the input image size)
    Mat m_oROI;
    //! 像素总数 (depends on the input frame size) & 相关像素总数
    int m_nTotPxCount, m_nTotRelevantPxCount;
    //! internal pixel index LUT for all relevant analysis regions (based on the provided ROI)
    int[] m_aPxIdxLUT;
    //! internal pixel info LUT for all possible pixel indexes
    PxInfoBase[] m_aPxInfoLUT;


    //! 输入图像的分辨率
    Size m_oImgSize;
    //! 输入图像的通道数量
    int m_nImgChannels;
    //! 输入图像的类型
    int m_nImgType;
    //! LBSP internal threshold offset value, used to reduce texture noise in dark regions
    //! LBSP 内部阀值偏移量值, 用于减少在黑暗区域的纹理噪音
	int m_nLBSPThresholdOffset;
    //! LBSP relative internal threshold (kept here since we don't keep an LBSP object)
	float m_fRelLBSPThreshold;
    //! current frame index, frame count since last model reset & model reset cooldown counters
    int m_nFrameIndex, m_nFramesSinceLastReset, m_nModelResetCooldown;
    //! pre-allocated internal LBSP threshold values LUT for all possible 8-bit intensities
    int[] m_anLBSPThreshold_8bitLUT;
    //! default kernel size for median blur post-proc filtering
	static int m_nDefaultMedianBlurKernelSize;
    //! specifies whether the algorithm parameters are fully initialized or not (must be handled by derived class)
    //表明算法参数是否已经初始化完成
    AtomicInteger m_bInitialized;
    //! specifies whether automatic model resets are enabled or not
    boolean m_bAutoModelResetEnabled;
    //! specifies whether the camera is considered moving or not
    boolean m_bUsingMovingCamera;
    //! copy of latest pixel intensities (used when refreshing model)
    Mat m_oLastColorFrame;
    //! copy of latest descriptors (used when refreshing model)
    Mat m_oLastDescFrame;
    //! the foreground mask generated by the method at [t-1]
    Mat m_oLastFGMask;


    /**
     * Local Binary Similarity Pattern (LBSP)-based change detection algorithm (abstract version/base class).
     * @param fRelLBSPThreshold
     * @param nLBSPThresholdOffset
     */
    public BackgroundSubtractorLBSP(float fRelLBSPThreshold, int nLBSPThresholdOffset) {
        m_nImgChannels = 0;
        m_nImgType = 0;
        m_nLBSPThresholdOffset = nLBSPThresholdOffset;
        m_fRelLBSPThreshold = fRelLBSPThreshold;
        m_nTotPxCount = 0;
        m_nTotRelevantPxCount = 0;
        m_nFrameIndex = Integer.MAX_VALUE;
        m_nFramesSinceLastReset = 0;
        m_nModelResetCooldown = 0;
        m_aPxIdxLUT = null;
        m_aPxInfoLUT = null;
        m_nDefaultMedianBlurKernelSize = DEFAULT_MEDIAN_BLUR_KERNEL_SIZE;
        m_bInitialized = new AtomicInteger(Status_UnInit);
        m_bAutoModelResetEnabled = true;
        m_bUsingMovingCamera = false;
    }

    public abstract void initialize(Mat oInitImg);

    public abstract void initialize(Mat oInitImg, Mat oROI);

    public void setROI(Mat oROI) {
        if(m_bInitialized.get() == Status_Inited) {
//            Mat oLatestBackgroundImage;
//            getBackgroundImage(oLatestBackgroundImage);
//            initialize(oLatestBackgroundImage,oROI);
        } else {
            m_oROI = oROI.clone();
        }
    }

}