package dx.core.module.nerual;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractTransferToNerveCell<Input, Output> extends AbstractNerveCell<Input, Output> {

    // 注册到这个神经细胞的输出神经细胞列表(这个神经细胞的Ouput作为这些输出细胞的Input)
    private Collection<NerveCell<Output, ?>> transferToNerveCells = new ArrayList<>();

    public void connectTransferToNerveCells(NerveCell<Output, ?>... nerveCells) {
        this.transferToNerveCells = Lists.newArrayList(nerveCells);
    }

    @Override
    protected void transferTo(Output output) {
        transferToNerveCells.forEach(cell -> cell.onInputData(output));
    }

}
