package dx.core.module.nerual;

import java.util.concurrent.SynchronousQueue;

/**
 * 由"上游细胞输出数据"驱动的细胞实现.
 *
 * @param <Input>
 * @param <Output>
 */
public abstract class AbstractDataDriveNerveCell<Input, Output> extends AbstractTransferToNerveCell<Input, Output> {

    private SynchronousQueue<Input> data = new SynchronousQueue<>();

    @Override
    public boolean onInputData(Input input) {
        // 使用offer函数, 不阻塞
        // 如果系统当前还有没有消费完毕的数据, 则直接返回失败即可.
        return data.offer(input);
    }

    @Override
    protected Input takeFrom() {
        try {
            // 神经线程会在这里阻塞住, 直到上游细胞调用该细胞的#on方法
            return data.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
