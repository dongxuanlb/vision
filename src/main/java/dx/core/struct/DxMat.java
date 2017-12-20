package dx.core.struct;

import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Mat;

public class DxMat {

    @Getter @Setter
    private int index;
    @Getter @Setter
    private Mat mat;

    public DxMat(int index, Mat mat) {
        this.index = index;
        this.mat = mat;
    }
}
