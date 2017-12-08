package dx.framework.camera;

/**
 * Created by 洞玄 on 25/04/2017.
 *
 * @author 洞玄
 * @date 2017/04/25
 */
public interface FrameTask {

    /**
     * 执行图像(帧)处理
     * @param frame 一帧数据
     */
    void execute(Frame frame);

}
