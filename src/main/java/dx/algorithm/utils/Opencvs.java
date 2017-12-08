package dx.algorithm.utils;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.util.MathUtils;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.Random;

/**
 * Created by dongxuan on 11/05/2017.
 */
public class Opencvs {

    public final static Random random = new JDKRandomGenerator();

    final static int[][] SamplesInitPattern_7x7 = new int[][]{
            { 2, 4, 6, 7, 6, 4, 2},
            { 4, 8,12,14,12, 8, 4,},
            { 6,12,21,25,21,12, 6,},
            { 7,14,25,28,25,14, 7,},
            { 6,12,21,25,21,12, 6,},
            { 4, 8,12,14,12, 8, 4,},
            { 2, 4, 6, 7, 6, 4, 2,},
    };

    final static int[][] NeighborPattern_3x3 = {
            {-1, 1},  {0, -1},  {1, 1},
            {-1, 0},            {1, 0},
            {-1, -1}, {0, -1},  {1, -1}
    };

    //获得给定坐标受7*7矩阵随机计算出的新坐标
    public static Point getRandSamplePosition_7x7(int origCoordX, int origCoordY, int imgWidth, int imgHeight) {
        return getRandSamplePosition(SamplesInitPattern_7x7, 7, 7, 512, origCoordX, origCoordY, imgWidth, imgHeight);
    }

    public static Point getNeighorPoistion_3x3(int origCoordX, int origCoordY, int imgWidth, int imgHeight) {
        return getRandNeighborPosition(NeighborPattern_3x3, 8, origCoordX, origCoordY, imgWidth, imgHeight);
    }

    public static Point getRandSamplePosition(
            int[][] samplesInitPattern,
            int kernelWidth,
            int kernelHeight,
            int samplesInitPatternTot,
            int origCoordX,
            int origCoordY,
            int imgWidth,
            int imgHeight) {
        //半径长度
        int r = 1 + random.nextInt(Integer.MAX_VALUE) % samplesInitPatternTot;
        Integer sampleCoordX = 0;
        Integer sampleCoordY;
        L:
        for(sampleCoordY=0; sampleCoordY<kernelHeight; sampleCoordY++) {
            for(sampleCoordX=0; sampleCoordX<kernelWidth; sampleCoordX++) {
                r -= samplesInitPattern[sampleCoordY][sampleCoordX];
                if(r<=0)
                    break L;
            }
        }
        sampleCoordX += (origCoordX - kernelWidth/2);
        sampleCoordY += (origCoordY - kernelHeight/2);
        //修正边界
        sampleCoordX = sampleCoordX > 0 ? sampleCoordX : 0;
        sampleCoordY = sampleCoordY > 0 ? sampleCoordY : 0;
        sampleCoordX = sampleCoordX >= imgWidth ?  (imgWidth - 1) : sampleCoordX;
        sampleCoordY = sampleCoordY >= imgHeight ?  (imgHeight - 1) : sampleCoordY;

        return new Point(sampleCoordX, sampleCoordY);
    }

    public static Point getRandNeighborPosition(
            int[][] neighborPattern,
            int neighborCount,
            int origCoordX,
            int origCoordY,
            int imgWidth,
            int imgHeight) {
        int r = random.nextInt(Integer.MAX_VALUE)%neighborCount;
        Integer neighborCoordX = origCoordX + neighborPattern[r][0];
        Integer neighborCoordY = origCoordY + neighborPattern[r][1];

        if (neighborCoordX < 0) {
            neighborCoordX = 0;
        } else if (neighborCoordX >= imgWidth) {
            neighborCoordX = imgWidth - 1;
        }
        if (neighborCoordY < 0) {
            neighborCoordY = 0;
        } else if (neighborCoordY >= imgHeight) {
            neighborCoordY = imgHeight - 1;
        }
        return new Point(neighborCoordX, neighborCoordY);
    }

}
