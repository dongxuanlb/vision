package dx.core;

import lombok.Getter;
import lombok.Setter;

/**
 * Capture的设备数据.
 *
 * Created by 洞玄 on 2017/12/4.
 *
 * @author 洞玄
 * @date 2017/12/04
 */

public class DxCaptureData {

    // 当前焦距
    @Getter @Setter
    private int focus;
    // 当前fps
    @Getter @Setter
    private int fps;
    // 当前曝光值
    @Getter @Setter
    private int exposure;
    // 当前增益
    @Getter @Setter
    private int gain;

}
