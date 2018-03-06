package dx.algorithm.sgm;

import org.apache.commons.math3.util.FastMath;

import java.util.Arrays;

import static org.apache.commons.math3.util.FastMath.abs;

public class DualSGM {

    class Vec3d {
        double[] values = new double[3];
        double value(int i) {
            return values[i];
        }
        Vec3d mul(double m) {
            for (int i=0;i<values.length;i++)
                values[i] *= m;
            return this;
        }
        Vec3d add(Vec3d v) {
            for (int i=0;i<values.length;i++)
                values[i] += v.value(i);
            return this;
        }
        Vec3d div(double v) {
            for (int i=0;i<values.length;i++)
                values[i] /= v;
            return this;
        }
    }

    /**
     * 补偿模型.
     *
     */
    class SGM {
        Vec3d mean;
        Vec3d var;
        double age = 0f;
    }

    SGM[] models = new SGM[2];

    int activeIndex = 0;

    public SGM current() {
        return models[activeIndex];
    }

    public SGM noncurrent() {
        return models[abs(activeIndex - 1)];
    }

    public void swap() {
        activeIndex = abs(activeIndex - 1);
    }

}
