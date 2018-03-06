package dx.algorithm.sgm;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import static dx.algorithm.utils.Maths.abs;
import static org.opencv.imgproc.Imgproc.*;

public class MotionDetector {

    final DualSGM[] backgroundModels;

    final DualSGM[] warpedBackgroundModel;

    final Size targetSize;

    final Size gridSize;

    final Size gridLayoutSize;

    final int gridAmount;

    Mat smallRGBImage = new Mat();
    Mat smallBlurImage = new Mat();
    Mat smallMotionMask = new Mat();

    Mat preSmallGrayImage = new Mat();
    Mat curSmallGrayImage = new Mat();

    boolean preImageLoaded = false;

    public MotionDetector(Size targetSize, Size gridSize) {
        this.targetSize = targetSize;
        this.gridSize = gridSize;
        this.gridLayoutSize = new Size(targetSize.width / gridSize.width, targetSize.height / gridSize.height);
        this.gridAmount = (int) gridLayoutSize.area();
        this.backgroundModels = new DualSGM[gridAmount];
        this.warpedBackgroundModel = new DualSGM[gridAmount];
    }

    public void detectMotion(Mat in_img, Mat out_motion_mask) {
        updateBackgroundModel(in_img);
    }

    void prepare(Mat in_img) {
        resize(in_img, smallRGBImage, targetSize);
        cvtColor(smallRGBImage, curSmallGrayImage, COLOR_BGR2GRAY);
        GaussianBlur(smallRGBImage, smallBlurImage, new Size(7,7),0);
        medianBlur(smallBlurImage, smallBlurImage,5);
        if (!preImageLoaded) {
            curSmallGrayImage.copyTo(preSmallGrayImage);
            preImageLoaded = true;
        }
    }

    void updateBackgroundModel(Mat in_img) {
        prepare(in_img);
        computeBackgroundMotion(preSmallGrayImage, curSmallGrayImage);

        curSmallGrayImage.copyTo(preSmallGrayImage);
    }



    float[] computeBackgroundMotion(Mat pre_img, Mat cur_img) {
        MatOfPoint pre_points = new MatOfPoint();
        MatOfPoint2f cur_points = new MatOfPoint2f();
        MatOfByte point_status = new MatOfByte();
        MatOfFloat point_error = new MatOfFloat();


        int maxCorners = 300;
        float qualityLevel = 0.01f;
        float minDistance = 5f;
        int blockSize = 4;
        boolean useHarrisDetector = false;
        double k = 0.04d;

        Imgproc.goodFeaturesToTrack(pre_img, pre_points, maxCorners, qualityLevel, minDistance, new Mat(), blockSize, useHarrisDetector, k);
        if (pre_points.total() > 1) {
            MatOfPoint2f pre_points_2f = MatOfPoint2f.fromNativeAddr(pre_points.dataAddr());
            Video.calcOpticalFlowPyrLK(pre_img, cur_img, pre_points_2f, cur_points, point_status, point_error, new Size(20, 20), 5);

            MatOfPoint2f prev_corners, cur_corners = new MatOfPoint2f();
            // weed out bad matches
//            for(int i=0; i < point_status.total(); i++) {
//                if(point_status[i]) {
//                    prev_corner2.push_back(pre_points[i]);
//                    cur_corner2.push_back(cur_points[i]);
//                }
//            }
        }

        return null;
    }

    /**
     * 补偿.
     * 核心是:
     * 1. 依据入参homography可以计算出新的图像坐标.
     * 2. 由于计算本身存在浮点以及在grid的尺度变化, 会存在"精度补偿"
     * 3. 补偿的极大值是1, 由函数floor决定.
     * @param in_dsgm
     * @param out_dsgm
     * @param homography
     */
    void compensateMotion(final DualSGM[] in_dsgm, final DualSGM[] out_dsgm, float[] homography) {
        for (int r=0, count = 0;r<gridLayoutSize.height;r++) {
            for (int c = 0; c < gridLayoutSize.width; c++, count++) {
                DualSGM current_dsgm_out = out_dsgm[count];

                ////////////////////////////////////////////////
                double imageX = gridSize.width * c + gridSize.width * 0.5;
                double imageY = gridSize.height * r + gridSize.height * 0.5;

                double normalizer = homography[6] * imageX + homography[7] * imageY + homography[8];
                double imageX_new = (homography[0] * imageX + homography[1] * imageY + homography[2]) / normalizer;
                double imageY_new = (homography[3] * imageX + homography[4] * imageY + homography[5]) / normalizer;

                double c_new = imageX_new / gridSize.width;
                double r_new = imageY_new / gridSize.height;

                int c_idx = (int) Math.floor(c_new);
                int r_idx = (int) Math.floor(r_new);

                double dr = r_new - ((float)r_idx + 0.5);
                double dc = c_new - ((float)c_idx + 0.5);

                double area_h;
                double area_v;
                double area_hv;
                double area_self;

                DualSGM tmp_dsgm = new DualSGM();
                float total_area = 0;

                if (dc != 0) {
                    int c_idx_new = c_idx;
                    int r_idx_new = r_idx;
                    c_idx_new += dc > 0 ? 1: -1;
                    if (isWithinModelBorder(c_idx_new, r_idx_new)) {
                        DualSGM selected_dsgm = in_dsgm[gridIndex(c_idx_new, r_idx_new)];
                        area_h = abs(dc) * (1.0 - abs(dr));
                        calucaluateMeanAndAge(tmp_dsgm, selected_dsgm, area_h);
                        total_area += area_h;
                    }
                }

                if (dr != 0) {
                    int c_idx_new = c_idx;
                    int r_idx_new = r_idx;
                    r_idx_new += dr > 0 ? 1 : -1;
                    if (isWithinModelBorder(c_idx_new, r_idx_new)) {
                        DualSGM selected_dsgm = in_dsgm[gridIndex(c_idx_new, r_idx_new)];
                        area_v = abs(dr) * (1.0 - abs(dc));
                        calucaluateMeanAndAge(tmp_dsgm, selected_dsgm, area_v);
                        total_area += area_v;
                    }
                }

                if (dr!=0 && dc!=0){
                    int c_idx_new = c_idx;
                    int r_idx_new = r_idx;
                    c_idx_new += dc > 0 ? 1 : -1;
                    r_idx_new += dr > 0 ? 1 : -1;
                    if (isWithinModelBorder(c_idx_new, r_idx_new)){
                        DualSGM selected_dsgm = in_dsgm[gridIndex(c_idx_new, r_idx_new)];
                        area_hv = abs(dr) * abs(dc);
                        calucaluateMeanAndAge(tmp_dsgm, selected_dsgm, area_hv);
                        total_area += area_hv;
                    }
                }

                if (c_idx >= 0 && c_idx < gridLayoutSize.width && r_idx >=0 && r_idx < gridLayoutSize.height) {
                    DualSGM selected_dsgm = in_dsgm[gridIndex(c_idx, r_idx)];
                    area_self = (1.0 - abs(dr)) * (1.0 - abs(dc));
                    calucaluateMeanAndAge(tmp_dsgm, selected_dsgm, area_self);
                    total_area += area_self;
                }

                if (total_area>0) {
                    tmp_dsgm.current().mean.div(total_area);
                    tmp_dsgm.noncurrent().mean.div(total_area);
                    tmp_dsgm.current().age /= total_area;
                    tmp_dsgm.noncurrent().age /= total_area;
                }

            }
        }
    }

    protected void calucaluateMeanAndAge(DualSGM tmp_dsgm, DualSGM target_dsgm, double area) {
        tmp_dsgm.current().mean.add(target_dsgm.current().mean.mul(area));
        tmp_dsgm.noncurrent().mean.add(target_dsgm.noncurrent().mean.mul(area));
        tmp_dsgm.current().age += area * target_dsgm.current().age;
        tmp_dsgm.noncurrent().age += area * target_dsgm.noncurrent().age;
    }

    protected void calucaluate(DualSGM.Vec3d[] tempMean, double[] tempAge, DualSGM dualSGM, double area) {

    }

    protected boolean isWithinModelBorder(int theGridX, int theGridY) {
        return theGridX >= 0 && theGridX < gridLayoutSize.width && theGridY >= 0 && theGridY < gridLayoutSize.height;
    }

    protected Integer gridIndex(int theGridColumn, int theGridRow) {
        return (int) (theGridRow * gridLayoutSize.width + theGridColumn);
    }

}
