package dx.algorithm.video;

import dx.algorithm.video.lbsp.LBSP_16bit_dbcross_s3ch;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static dx.algorithm.utils.Mats.getByteData;
import static dx.algorithm.utils.Mats.putByteData;
import static dx.algorithm.utils.Mats.putUnsignedShortData;
import static dx.framework.utils.Utils.preventOverflow;
import static java.lang.Math.floor;
import static org.apache.commons.lang3.math.NumberUtils.min;
import static org.opencv.core.Core.bitwise_or;
import static org.opencv.core.CvType.*;
import static org.opencv.core.CvType.CV_8UC;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.dilate;

/**
 * Created by dongxuan on 18/05/2017.
 */
public class BackgroundSubtractorSuBSENSE extends BackgroundSubtractorLBSP {

    static final int s_nColorMaxDataRange_1ch = UCHAR_MAX;
    static final int s_nDescMaxDataRange_1ch = LBSP.DESC_SIZE*8;
    static final int s_nColorMaxDataRange_3ch = s_nColorMaxDataRange_1ch*3;
    static final int s_nDescMaxDataRange_3ch = s_nDescMaxDataRange_1ch*3;

    // local define used to specify the color dist threshold offset used for unstable regions
    final int STAB_COLOR_DIST_OFFSET;
    // local define used to specify the desc dist threshold offset used for unstable regions
    final int UNSTAB_DESC_DIST_OFFSET;

    /// total number of ROI pixels before & after border cleanup
    int m_nOrigROIPxCount, m_nFinalROIPxCount;
    /// last calculated non-zero desc ratio
    float m_fLastNonZeroDescRatio;
    //! specifies whether Tmin/Tmax scaling is enabled or not
    boolean m_bLearningRateScalingEnabled;
    //! current learning rate caps
    float m_fCurrLearningRateLowerCap, m_fCurrLearningRateUpperCap;
    //! current kernel size for median blur post-proc filtering
    int m_nMedianBlurKernelSize;
    //! specifies the px update spread range
    boolean m_bUse3x3Spread;
    //! specifies the downsampled frame size used for cam motion analysis
    Size m_oDownSampledFrameSize;

    /// specifies whether the model has been fully initialized or not (must be handled by derived class)
    //表明背景模型是否已经初始化完成
    AtomicBoolean m_bModelInitialized = new AtomicBoolean(false);

    //! absolute minimal color distance threshold ('R' or 'radius' in the original ViBe paper, used as the default/initial 'R(x)' value here)
    int m_nMinColorDistThreshold;
    //! absolute descriptor distance threshold offset
    int m_nDescDistThresholdOffset;
    //! number of different samples per pixel/block to be taken from input frames to build the background model (same as 'N' in ViBe/PBAS)
    int m_nBGSamples;
    //! number of similar samples needed to consider the current pixel/block as 'background' (same as '#_min' in ViBe/PBAS)
    int m_nRequiredBGSamples;
    //! number of samples to use to compute the learning rate of moving averages
    int m_nSamplesForMovingAvgs;


    //! background model pixel color intensity samples (equivalent to 'B(x)' in PBAS)
    Mat[] m_voBGColorSamples;
    //! background model descriptors samples
    Mat[] m_voBGDescSamples;


    //! per-pixel update rates ('T(x)' in PBAS, which contains pixel-level 'sigmas', as referred to in ViBe)
    Mat m_oUpdateRateFrame;
    //! per-pixel distance thresholds (equivalent to 'R(x)' in PBAS, but used as a relative value to determine both intensity and descriptor variation thresholds)
    Mat m_oDistThresholdFrame;
    //! per-pixel distance variation modulators ('v(x)', relative value used to modulate 'R(x)' and 'T(x)' variations)
    Mat m_oVariationModulatorFrame;
    //! per-pixel mean distances between consecutive frames ('D_last(x)', used to detect ghosts and high variation regions in the sequence)
    Mat m_oMeanLastDistFrame;
    //! per-pixel mean minimal distances from the model ('D_min(x)' in PBAS, used to control variation magnitude and direction of 'T(x)' and 'R(x)')
    Mat m_oMeanMinDistFrame_LT, m_oMeanMinDistFrame_ST;
    //! per-pixel mean downsampled distances between consecutive frames (used to analyze camera movement and control max learning rates globally)
    Mat m_oMeanDownSampledLastDistFrame_LT, m_oMeanDownSampledLastDistFrame_ST;
    //! per-pixel mean raw segmentation results (used to detect unstable segmentation regions)
    Mat m_oMeanRawSegmResFrame_LT, m_oMeanRawSegmResFrame_ST;
    //! per-pixel mean raw segmentation results (used to detect unstable segmentation regions)
    Mat m_oMeanFinalSegmResFrame_LT, m_oMeanFinalSegmResFrame_ST;
    //! a lookup map used to keep track of unstable regions (based on segm. noise & local dist. thresholds)
    Mat m_oUnstableRegionMask;
    //! per-pixel blink detection map ('Z(x)')
    Mat m_oBlinksFrame;
    //! pre-allocated matrix used to downsample the input frame when needed
    Mat m_oDownSampledFrame_MotionAnalysis;
    //! the foreground mask generated by the method at [t-1] (without post-proc, used for blinking px detection)
    Mat m_oLastRawFGMask;

    //! pre-allocated CV_8UC1 matrices used to speed up morph ops
    Mat m_oFGMask_PreFlood;
    Mat m_oFGMask_FloodedHoles;
    Mat m_oLastFGMask_dilated;
    Mat m_oLastFGMask_dilated_inverted;
    Mat m_oCurrRawFGBlinkMask;
    Mat m_oLastRawFGBlinkMask;

    Mat m_defaultMorphologyKernel;

    /**
     * Local Binary Similarity Pattern (LBSP)-based change detection algorithm (abstract version/base class).
     *
     * @param fRelLBSPThreshold
     */
    public BackgroundSubtractorSuBSENSE(float fRelLBSPThreshold
            ,int nDescDistThresholdOffset
            ,int nMinColorDistThreshold
            ,int nBGSamples
            ,int nRequiredBGSamples
            ,int nSamplesForMovingAvgs) {
        super(fRelLBSPThreshold, 0);
        this.m_nMinColorDistThreshold = m_nMinColorDistThreshold;
        STAB_COLOR_DIST_OFFSET = m_nMinColorDistThreshold/5;
        this.m_nDescDistThresholdOffset = m_nDescDistThresholdOffset;
        UNSTAB_DESC_DIST_OFFSET = m_nDescDistThresholdOffset;
        this.m_defaultMorphologyKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
    }

    @Override
    public void initialize(Mat oInitImg) {
        initialize(oInitImg, null);
    }

    @Override
    public void initialize(Mat oInitImg, Mat oROI) {
        if (m_bInitialized.compareAndSet(Status_UnInit, Status_Initing)) {
            StopWatch sw = new StopWatch();
            sw.start();
            _initialize(oInitImg, oROI);
            logger.log(Level.INFO, "SuBSENSE init cost " + sw.getTime() + "ms");
        }
    }

    protected void _initialize(final Mat oInitImg, final Mat oROI) {
        Mat oNewBGROI;
        //如果入参没有传递ROI, 并且当前ROI大小不等于初始图像大小
        if (oROI == null && !m_oROI.size().equals(oInitImg.size())) {
            oNewBGROI = new Mat(oInitImg.size(), CV_8UC1, Black1Scalar);
        } else if (oROI == null) {
            // reuse last ROI if sizes match, and no new ROI is provided
            oNewBGROI = m_oROI;
        } else {
            oNewBGROI = oROI.clone();
            Mat oTempROI = new Mat();
            dilate(oNewBGROI,oTempROI,m_defaultMorphologyKernel,new Point(-1,-1),LBSP.PATCH_SIZE/2);
            // sets value of pixels close to ROI borders as UCHAR_MAX/2 to help internal bounds check
            //FIXME Java api can't support mat / 2 operation
            //Core.bitwise_or(oNewBGROI, oTempROI/2, oNewBGROI);
            bitwise_or(oNewBGROI, oTempROI, oNewBGROI);
        }
        //ROI区域的总像素数量, ROI一定是8UC1的
        m_nOrigROIPxCount = Core.countNonZero(oNewBGROI);
        //最终的ROI区域的总像素数量, 不考虑m_nROIBorderSize了
        m_nFinalROIPxCount = m_nOrigROIPxCount;

        m_oROI = oNewBGROI;
        m_oImgSize = oInitImg.size();
        m_nImgType = oInitImg.type();
        m_nImgChannels = oInitImg.channels();
        //图像像素总数
        m_nTotPxCount = (int) m_oImgSize.area();
        //实际有关的图像像素总数
        m_nTotRelevantPxCount = m_nFinalROIPxCount;
        m_nFrameIndex = 0;
        m_nFramesSinceLastReset = 0;
        m_nModelResetCooldown = 0;
        m_fLastNonZeroDescRatio = 0.0f;

        if (m_nOrigROIPxCount>=m_nTotPxCount/2 && m_nTotPxCount>=DEFAULT_FRAME_SIZE.area()) {
            m_bLearningRateScalingEnabled = true;
            //开启自动模型重置(automatic model resets)
            m_bAutoModelResetEnabled = true;
            m_bUse3x3Spread = !(m_nTotPxCount>DEFAULT_FRAME_SIZE.area()*2);

            int nRawMedianBlurKernelSize = min((int)(floor(m_nTotPxCount/DEFAULT_FRAME_SIZE.area()+0.5f)+m_nDefaultMedianBlurKernelSize), 14);
            m_nMedianBlurKernelSize = (nRawMedianBlurKernelSize%2==0) ? nRawMedianBlurKernelSize : nRawMedianBlurKernelSize - 1;
            m_fCurrLearningRateLowerCap = FEEDBACK_T_LOWER;
            m_fCurrLearningRateUpperCap = FEEDBACK_T_UPPER;
        } else {
            m_bLearningRateScalingEnabled = false;
            //关闭自动模型重置(automatic model resets)
            m_bAutoModelResetEnabled = false;
            m_bUse3x3Spread = true;
            m_nMedianBlurKernelSize = m_nDefaultMedianBlurKernelSize;
            m_fCurrLearningRateLowerCap = FEEDBACK_T_LOWER*2;
            m_fCurrLearningRateUpperCap = FEEDBACK_T_UPPER*2;
        }


        m_oUpdateRateFrame = new Mat(m_oImgSize, CV_32FC1, new Scalar(m_fCurrLearningRateLowerCap));
        m_oDistThresholdFrame = new Mat(m_oImgSize,CV_32FC1, White1Scalar);
        m_oVariationModulatorFrame = new Mat(m_oImgSize,CV_32FC1, White1Scalar);// should always be >= FEEDBACK_V_DECR
        m_oMeanLastDistFrame = new Mat(m_oImgSize,CV_32FC1, White1Scalar);
        m_oMeanMinDistFrame_LT = new Mat(m_oImgSize,CV_32FC1, White1Scalar);
        m_oMeanMinDistFrame_ST = new Mat(m_oImgSize,CV_32FC1, White1Scalar);
        m_oDownSampledFrameSize = new Size(m_oImgSize.width/FRAMELEVEL_ANALYSIS_DOWNSAMPLE_RATIO,m_oImgSize.height/FRAMELEVEL_ANALYSIS_DOWNSAMPLE_RATIO);
        m_oMeanDownSampledLastDistFrame_LT = new Mat(m_oDownSampledFrameSize, CV_32FC(m_nImgChannels), White1Scalar);
        m_oMeanDownSampledLastDistFrame_ST = new Mat(m_oDownSampledFrameSize,CV_32FC(m_nImgChannels), White1Scalar);
        m_oMeanRawSegmResFrame_LT = new Mat(m_oImgSize,CV_32FC1,White1Scalar);
        m_oMeanRawSegmResFrame_ST = new Mat(m_oImgSize,CV_32FC1,White1Scalar);
        m_oMeanFinalSegmResFrame_LT = new Mat(m_oImgSize,CV_32FC1,White1Scalar);
        m_oMeanFinalSegmResFrame_ST = new Mat(m_oImgSize,CV_32FC1,White1Scalar);
        m_oUnstableRegionMask = new Mat(m_oImgSize,CV_8UC1,White1Scalar);
        m_oBlinksFrame = new Mat(m_oImgSize,CV_8UC1,White1Scalar);
        m_oDownSampledFrame_MotionAnalysis = new Mat(m_oDownSampledFrameSize,CV_8UC(m_nImgChannels),White1Scalar);
        m_oLastColorFrame = new Mat(m_oImgSize,CV_8UC(m_nImgChannels),White1Scalar);
        m_oLastDescFrame = new Mat(m_oImgSize,CV_16UC(m_nImgChannels),White1Scalar);
        m_oLastRawFGMask = new Mat(m_oImgSize,CV_8UC1,White1Scalar);
        m_oLastFGMask = new Mat(m_oImgSize,CV_8UC1,White1Scalar);
        m_oLastFGMask_dilated = new Mat(m_oImgSize,CV_8UC1,White1Scalar);
        m_oLastFGMask_dilated_inverted = new Mat(m_oImgSize,CV_8UC1,White1Scalar);
        m_oFGMask_FloodedHoles = new Mat(m_oImgSize,CV_8UC1,White1Scalar);
        m_oFGMask_PreFlood = new Mat(m_oImgSize,CV_8UC1,White1Scalar);
        m_oCurrRawFGBlinkMask = new Mat(m_oImgSize,CV_8UC1,White1Scalar);
        m_oLastRawFGBlinkMask = new Mat(m_oImgSize,CV_8UC1,White1Scalar);
        m_voBGColorSamples = new Mat[m_nBGSamples];
        m_voBGDescSamples = new Mat[m_nBGSamples];
        for(int s=0; s<m_nBGSamples; ++s) {
            m_voBGColorSamples[s] = new Mat(m_oImgSize,CV_8UC(m_nImgChannels), new Scalar(0));
            m_voBGDescSamples[s] = new Mat(m_oImgSize,CV_16UC(m_nImgChannels), new Scalar(0));
        }

        m_oLastFGMask = new Mat(m_oImgSize, CV_8UC1, White1Scalar);
        //初始化最后一帧Mat对象, 和输入图像的大小, 通道, 深度保持一致
        m_oLastColorFrame = new Mat(m_oImgSize, CV_8UC(m_nImgChannels), White3Scalar);
        m_oLastDescFrame = new Mat(m_oImgSize, CvType.CV_16UC(m_nImgChannels), White3Scalar);
        m_aPxIdxLUT = new int[m_nTotRelevantPxCount];
        m_aPxInfoLUT = new PxInfoBase[m_nTotPxCount];

        m_anLBSPThreshold_8bitLUT = new int[UCHAR_MAX+1];

        //初始化LUT(look-up-table), 即0到255分别对应的threshold
        for(int t=0; t<=UCHAR_MAX; ++t)
            m_anLBSPThreshold_8bitLUT[t] = preventOverflow(m_nLBSPThresholdOffset + t * m_fRelLBSPThreshold, UCHAR_MIN, UCHAR_MAX);

        for(int nPxIter=0, nModelIter=0; nPxIter<m_nTotPxCount; ++nPxIter) {
            //C++版本中的理解应该是!=0进入逻辑实现
            if (0!= getByteData(m_oROI, nPxIter)) {
                m_aPxIdxLUT[nModelIter] = nPxIter;
                //图像一维索引的LUT
                PxInfoBase pxInfoBase = new PxInfoBase();
                pxInfoBase.nImgCoord_Y = (int) (nPxIter/m_oImgSize.width);
                pxInfoBase.nImgCoord_X = (int) (nPxIter%m_oImgSize.width);
                pxInfoBase.nModelIdx = nModelIter;
                m_aPxInfoLUT[nPxIter] = pxInfoBase;

                int nPxRGBIter = nPxIter*m_nImgChannels;
                for(int c=0; c<m_nImgChannels; ++c) {
                    byte bInitImgPxData = getByteData(oInitImg, nPxRGBIter + c);
                    int uInitImgPxData = bInitImgPxData & 0xFF;
                    putByteData(m_oLastColorFrame, nPxRGBIter + c, bInitImgPxData);
                    int desc = LBSP_16bit_dbcross_s3ch.computeSingleRGBDescriptor(
                            uInitImgPxData,
                            oInitImg.dataAddr(),
                            oInitImg.step1(),
                            pxInfoBase.nImgCoord_X,
                            pxInfoBase.nImgCoord_Y,
                            c,
                            m_anLBSPThreshold_8bitLUT[uInitImgPxData]);
                    putUnsignedShortData(m_oLastDescFrame, nPxRGBIter+c, desc);
                }
                ++nModelIter;
            }
        }
    }

    /**
     *
     * @param oInputImg                 输入图像
     * @param learningRateOverride      学习频率
     * @return 函数处理后获得的当前前景
     */
    public Mat operator(Mat oInputImg, double learningRateOverride) {
        //确保当前输入的图像类型,大小与初始化时的一致
        Validate.isTrue(oInputImg.type()==m_nImgType && oInputImg.size().equals(m_oImgSize));
        Validate.isTrue(oInputImg.isContinuous());
        //当前输入图像的前景
        Mat oCurrFGMask = new Mat(oInputImg.size(), CvType.CV_8UC1, Black1Scalar);

        int nNonZeroDescCount = 0;

        //roll avg factor 直译是滚动平均因子, 啥意思?
        ++m_nFrameIndex;
        float fRollAvgFactor_LT = 1.0f/min(m_nFrameIndex,m_nSamplesForMovingAvgs/1);
	    float fRollAvgFactor_ST = 1.0f/min(m_nFrameIndex,m_nSamplesForMovingAvgs/4);

	    //循环每一个有关像素(Relevant Pixel)
	    for(int nModelIter=0; nModelIter<m_nTotRelevantPxCount; ++nModelIter) {
	        //从PxIdxLUT中通过"模型索引"找到对应的"像素索引"
            int nPxIter = m_aPxIdxLUT[nModelIter];
            //m_oLastDescFrame"索引", 因为是16bit数据, 所以需要*2
            int nDescIter = nPxIter*2;
            //通过"像素索引"获得其在图片中所在的X,Y坐标值
			int nCurrImgCoord_X = m_aPxInfoLUT[nPxIter].nImgCoord_X;
			int nCurrImgCoord_Y = m_aPxInfoLUT[nPxIter].nImgCoord_Y;
			//获得"像素索引"对应RBG图像在内存中的"RGB内存索引"
			int nPxIterRGB = nPxIter*3;
			//获得"像素索引"对应Desc图像在内存中的"Desc内存索引"(16bit数据)
            int nDescIterRGB = nPxIterRGB*2;
            //32bit数据索引
            int nFloatIter = nPxIter*4;
            long anCurrColor = oInputImg.dataAddr()+nPxIterRGB;
            long pfCurrDistThresholdFactor = m_oDistThresholdFrame.dataAddr()+nFloatIter;
			long pfCurrVariationFactor = m_oVariationModulatorFrame.dataAddr()+nFloatIter;
			long pfCurrLearningRate = (m_oUpdateRateFrame.dataAddr()+nFloatIter);
			long pfCurrMeanLastDist = (m_oMeanLastDistFrame.dataAddr()+nFloatIter);
			long pfCurrMeanMinDist_LT = (m_oMeanMinDistFrame_LT.dataAddr()+nFloatIter);
			long pfCurrMeanMinDist_ST = (m_oMeanMinDistFrame_ST.dataAddr()+nFloatIter);
			long pfCurrMeanRawSegmRes_LT = (m_oMeanRawSegmResFrame_LT.dataAddr()+nFloatIter);
			long pfCurrMeanRawSegmRes_ST = (m_oMeanRawSegmResFrame_ST.dataAddr()+nFloatIter);
			long pfCurrMeanFinalSegmRes_LT = (m_oMeanFinalSegmResFrame_LT.dataAddr()+nFloatIter);
			long pfCurrMeanFinalSegmRes_ST = (m_oMeanFinalSegmResFrame_ST.dataAddr()+nFloatIter);
            long nLastIntraDesc = m_oLastDescFrame.dataAddr()+nDescIter;
            long nLastColor = m_oLastColorFrame.dataAddr()+nPxIter;

            int nGoodSamplesCount=0, nSampleIdx=0;
            while(nGoodSamplesCount<m_nRequiredBGSamples && nSampleIdx<m_nBGSamples) {
                //const ushort* const anBGIntraDesc = (ushort*)(m_voBGDescSamples[nSampleIdx].data+nDescIterRGB);
				//const uchar* const anBGColor = m_voBGColorSamples[nSampleIdx].data+nPxIterRGB;
            }


        }



	    return oCurrFGMask;
    }


}
