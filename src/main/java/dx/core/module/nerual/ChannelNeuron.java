package dx.core.module.nerual;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

/**
 * 通道型神经元, 负责把数据从一端传送至另外一(或多)端.
 * @param <Input>
 * @param <Output>
 */
public abstract class ChannelNeuron<Input, Output> extends AbstractInputOutputNeuron<Input, Output> {

    private SynchronousQueue<Input> data = new SynchronousQueue<>();

    private List<InputableNeuron<Output>> outputNeurons = new ArrayList<>();

    @Override
    public void register(InputableNeuron<Output> neuron) {
        outputNeurons.add(neuron);
    }

    @Override
    protected void transferTo(Output output) {
        outputNeurons.forEach(neuron -> neuron.receive(output));
    }

    @Override
    public void receive(Input input) {
        // 使用offer函数, 不阻塞
        // 如果系统当前还有没有消费完毕的数据, 则直接返回失败即可.
        data.offer(input);
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
