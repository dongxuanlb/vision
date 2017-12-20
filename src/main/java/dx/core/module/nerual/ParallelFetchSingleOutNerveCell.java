package dx.core.module.nerual;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public abstract class ParallelFetchSingleOutNerveCell<Source, Output> implements Callable<Output> {

    private List<DataInputNerveCell<Source>> dataInputNerveCells = new ArrayList<>();

    public void register(DataInputNerveCell<Source> dataInputNerveCell) {
        if (dataInputNerveCell != null)
            dataInputNerveCells.add(dataInputNerveCell);
    }

    public boolean begin() {

        return true;
    }

}
