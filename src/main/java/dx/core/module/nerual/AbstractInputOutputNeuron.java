package dx.core.module.nerual;

import dx.core.struct.AbstractLifeCycle;

public abstract class AbstractInputOutputNeuron<Input, Output> extends AbstractLifeCycle implements InputableNeuron<Input>, OutputableNeuron<Output> {

    // 获取输入数据
    protected abstract Input takeFrom();
    // 处理输入数据返回输出数据
    protected abstract Output process(Input input);
    // 输出数据
    protected abstract void transferTo(Output output);

    @Override
    protected void loop() {
        transferTo(process(takeFrom()));
    }
}
