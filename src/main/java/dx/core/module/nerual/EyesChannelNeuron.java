package dx.core.module.nerual;

import dx.core.struct.DxMatFrame;

public class EyesChannelNeuron extends ChannelNeuron<DxMatFrame, DxMatFrame> {

    @Override
    protected DxMatFrame process(DxMatFrame dxMatFrame) {
        return dxMatFrame;
    }
}
