package dx.algorithm.video;

import dx.framework.utils.Utils;
import org.opencv.core.Mat;

/**
 * Created by dongxuan on 16/05/2017.
 */
public class SuBSENSE {

    int UCHAR_MIN = 0;
    int UCHAR_MAX = 255;

    //! LBSP internal threshold offset value, used to reduce texture noise in dark regions
    public int m_nLBSPThresholdOffset;
    //! LBSP relative internal threshold (kept here since we don't keep an LBSP object)
    public float m_fRelLBSPThreshold;
    //! pre-allocated internal LBSP threshold values LUT for all possible 8-bit intensities
    public int[] m_anLBSPThreshold_8bitLUT;

    public void initialize(final Mat oInitImg, final Mat oROI) {
        for(int t=0; t<=UCHAR_MAX; ++t)
            m_anLBSPThreshold_8bitLUT[t] = Utils.preventOverflow(m_nLBSPThresholdOffset + t * m_fRelLBSPThreshold, UCHAR_MIN, UCHAR_MAX);

    }

}
