package dx.core.module.nerual;

/**
 * 由"自身细胞获取数据"驱动的细胞实现.
 * @param <Input>
 * @param <Output>
 */
public abstract class AbstractSelfDriveNerveCell<Input, Output> extends AbstractTransferToNerveCell<Input, Output> {

    @Override
    public boolean onInputData(Input input) {
        // 永远返回真
        return true;
    }

    @Override
    protected Input takeFrom() {
        return null;
    }
}
