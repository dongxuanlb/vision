package dx.core.module.nerual;

import java.util.concurrent.Callable;

/**
 *
 */
public interface NerveCell<Input, Output> extends Runnable {

    /**
     * 激活神经细胞
     * @return
     */
    boolean active();

    /**
     * 取消激活神经细胞
     * @return
     */
    boolean deActive();

    /**
     * 输入数据
     * @param input
     * @return 细胞是否受理
     */
    boolean onInputData(Input input);

    boolean registerOutputNerveCell(NerveCell<Output, ?> outputNerveCell);

}
