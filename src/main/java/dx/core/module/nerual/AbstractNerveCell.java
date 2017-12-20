package dx.core.module.nerual;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractNerveCell<Input, Output> implements NerveCell<Input, Output> {

    private AtomicBoolean active = new AtomicBoolean(false);

    // 获取输入数据
    protected abstract Input takeFrom();
    // 处理输入数据返回输出数据
    protected abstract Output process(Input input);
    // 输出数据
    protected abstract void transferTo(Output output);

    protected abstract ExecutorService getNerveCellExecutorService();

    @Override
    public void run() {
        while (active.get())
            transferTo(process(takeFrom()));
    }

    @Override
    public boolean active() {
        active.set(true);
        getNerveCellExecutorService().submit(this);
        return true;
    }

    @Override
    public boolean deActive() {
        return active.compareAndSet(true, false);
    }
}
