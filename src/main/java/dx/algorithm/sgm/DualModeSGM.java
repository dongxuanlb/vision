package dx.algorithm.sgm;

import org.apache.commons.math3.util.FastMath;

import static java.lang.Math.floor;

public abstract class DualModeSGM {

    static protected int NUM_MODELS = 2;
    static protected int BLOCK_SIZE = 4;

    static int VARIANCE_INTERPOLATE_PARAM = 1;

    int[] modelIdx;

    double[][] MEAN = new double[NUM_MODELS][];
    double[][] VAR = new double[NUM_MODELS][];
    double[][] AGE = new double[NUM_MODELS][];

    double[][] _MEAN = new double[NUM_MODELS][];
    double[][] _VAR = new double[NUM_MODELS][];
    double[][] _AGE = new double[NUM_MODELS][];

    // 模型图像的宽高. 为图像实际宽高/BLOCK_SIZE
    // 核心的目的是为了减少计算工作量, 但同时牺牲的是精度(或者说敏感度)
    int gridWidth;
    int gridHeight;

    // 图像实际的宽高. 比如我们常说的分辨率800*600
    int imageWidth;
    int imageHeight;

    /**
     *
     * @param homography
     */
    protected void compensate(float[] homography) {
        // compensate models for the current view
        // 双层for循环基本遵循外小内大原则, 图像一般高度小于宽度
        for (int gridY = 0; gridY < gridHeight; ++gridY) {
            for (int gridX = 0; gridX < gridWidth; ++gridX) {

                // 一些临时变量
                double[][] temp_mean = new double[4][NUM_MODELS];
                double[][] temp_var = new double[4][NUM_MODELS];
                double[][] temp_age = new double[4][NUM_MODELS];

                float sumOfWeight = 0.0f;

                // 根据当前的模型坐标转换出图像(BLOCK_SIZE中心)的坐标
                float imageX = BLOCK_SIZE * gridX + BLOCK_SIZE / 2.0f;
                float imageY = BLOCK_SIZE * gridY + BLOCK_SIZE / 2.0f;
                // 根据单应性投影计算新的图像坐标
                // 由于投影换算后的新图像坐标可能是浮点数, 所以计算出来的图像坐标也是浮点数
                float W = homography[6] * imageX + homography[7] * imageY + homography[8];
                float newImageX = (homography[0] * imageX + homography[1] * imageY + homography[2]) / W;
                float newImageY = (homography[3] * imageX + homography[4] * imageY + homography[5]) / W;
                // 投影后图像坐标以浮点形式换算成Grid单位
                float _newGridX = newImageX / BLOCK_SIZE;
                float _newGridY = newImageY / BLOCK_SIZE;
                // 投影后图像坐标以整数形式换算成Grid单位
                int newGridX = (int) floor(_newGridX);
                int newGridY = (int) floor(_newGridY);

                // 需要补偿的距离. 一定小于1
                float distanceGridHorizontal = _newGridX - (newGridX + 0.5f);
                float distanceGridVertical = _newGridY - (newGridY + 0.5f);

                // 当前模型X,Y坐标下的数组下标值
                int gridIndex = gridX + gridY * gridWidth;
                // 新的模型X,Y坐标下的数组下标值
                int newGridIndex = newGridX + newGridY * gridWidth;
                // TODO 为什么???
                float area = 0.0f;

                // 计算MEAN
                // 计算重叠横向上的面积
                if (distanceGridHorizontal != 0) {

                }


                if (isWithinModelBorder(newGridX, newGridY)) {
                    area = (1 - distanceGridHorizontal) * (1 - distanceGridVertical);
                    sumOfWeight += area;
                    for (int m = 0; m < NUM_MODELS; ++m) {
                        temp_mean[3][m] = area * MEAN[m][newGridIndex];
                        temp_age[3][m] = area * AGE[m][newGridIndex];
                    }
                }
                if (sumOfWeight > 0) {
                    for (int m = 0; m < NUM_MODELS; ++m) {
                        MEAN[m][gridIndex] = temp_mean[3][m] / sumOfWeight;
                        AGE[m][gridIndex] = temp_age[3][m] / sumOfWeight;
                    }
                }
                // 计算VAR
                if (isWithinModelBorder(newGridX, newGridY) && sumOfWeight > 0) {
                    for (int m = 0; m < NUM_MODELS; ++m) {
                        _VAR[m][gridIndex] = (area / sumOfWeight) * (VAR[m][newGridIndex] + VARIANCE_INTERPOLATE_PARAM * (Math.pow(_MEAN[m][gridIndex] - MEAN[m][newGridIndex], 2)));
                    }
                }
            }
        }
    }

    protected boolean isWithinModelBorder(int theGridX, int theGridY) {
        return theGridX >= 0 && theGridX < gridWidth && theGridY >= 0 && theGridY < gridHeight;
    }
}
