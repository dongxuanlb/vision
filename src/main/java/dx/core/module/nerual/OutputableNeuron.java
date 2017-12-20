package dx.core.module.nerual;

/**
 * 支持数据输出的神经元.
 *
 * 这种神经元支持单一数据类型输出, 多接收神经元的场景
 * @param <Output> 神经元输出的数据类型
 */
public interface OutputableNeuron<Output> extends Neuron {

    /**
     * 向该神经元注册一个用于接收输出数据的神经元(作为输入数据)
     * @param neuron 用于接收输出数据的神经元
     */
    void register(InputableNeuron<Output> neuron);

}
