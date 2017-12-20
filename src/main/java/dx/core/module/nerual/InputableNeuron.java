package dx.core.module.nerual;

public interface InputableNeuron<Input> extends Neuron {

    void receive(Input input);

}
