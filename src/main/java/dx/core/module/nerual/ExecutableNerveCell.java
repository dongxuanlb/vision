package dx.core.module.nerual;

import java.util.concurrent.ExecutorService;

public interface ExecutableNerveCell extends NerveCell {

    /**
     * 获取用于该神经元工作的执行器.
     * @return {@link ExecutorService}实例
     */
    ExecutorService getExecutorService();

}
