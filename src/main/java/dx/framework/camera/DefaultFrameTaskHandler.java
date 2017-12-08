package dx.framework.camera;

import dx.framework.utils.Imshow;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Created by 洞玄 on 25/04/2017.
 *
 * @author 洞玄
 * @date 2017/04/25
 */
public class DefaultFrameTaskHandler implements FrameTaskHandler {

    Logger logger = Logger.getLogger("DefaultFrameTaskHandler");

    /**
     * 相机实例
     */
    private Camera camera;
    /**
     * 任务容器列表
     * 整体串行执行, 每个{@link FrameTaskContainer}中如果存在多个{@link FrameTask}则并行执行
     */
    private List<FrameTaskContainer> taskContainers = new ArrayList<>();

    /**
     * 添加帧任务容器实例
     * @param container 帧任务容器
     */
    public void addFrameTaskContainer(FrameTaskContainer container) {
        taskContainers.add(container);
    }

    @Override
    public Callable<Frame> handle(Mat source) {
        return () -> {
            Frame frame = buildFrame(source);
            for (FrameTaskContainer container : taskContainers) {
                try {
                    for (FrameTask task : container.getTasks()) {
                        task.execute(frame);
                    }
                } catch (Exception ex) {
                    logger.warning("Frame Task Execute Fail");
                }
            }
            Map<Imshow, Mat> monitors = frame.getMonitors();
            if (monitors != null && !monitors.isEmpty()) {
                for (Map.Entry<Imshow, Mat> entry : monitors.entrySet()) {
                    entry.getKey().showImage(entry.getValue());
                }
            }
            return frame;
        };
    }

    @Override
    public void setCameraInstance(Camera camera) {
        this.camera = camera;
    }

    protected Frame buildFrame(Mat source) {
        switch (camera.getEnvironment().frameType) {
            case Nd4j:
                return new Nd4jFrame(source);
            case Opencv:
                return new OriginFrame(source);
            case DirectBuffer:
                return new DirectBufferFrame(source);
            case Unsafe:
                return new UnsafeFrame(source);
            default:
                return new ArrayFrame(source);
        }
    }

}
