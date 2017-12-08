package dx.framework.camera;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * 帧任务容器
 * @author dongxuan
 */
public class FrameTaskContainer {

    private List<FrameTask> tasks = new ArrayList<>();

    public FrameTaskContainer(FrameTask task) {
        this(Lists.newArrayList(task));
    }

    public FrameTaskContainer(List<FrameTask> tasks) {
        this.tasks = tasks;
    }

    public List<FrameTask> getTasks() {
        return tasks;
    }
}
