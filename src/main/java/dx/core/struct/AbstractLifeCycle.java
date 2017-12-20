package dx.core.struct;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractLifeCycle implements Runnable {

    static Logger logger = LogManager.getLogger(AbstractLifeCycle.class);
    static ExecutorService CommonExecutor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                    .setDaemon(true).setNameFormat("DxLifeCycleCommonPool-T-%d")
                    .build());

    boolean running = false;

    protected ExecutorService getExecutor() {
        return CommonExecutor;
    }

    @Override
    public void run() {
        logger.info("{} starting({})...", getClass().getSimpleName(), Thread.currentThread().getName());
        try {
            while(true) {
                running = true;
                loop();
            }
        } finally {
            running = false;
        }
    }

    public void start() {
        getExecutor().submit(this);
    }

    protected abstract void loop();
}
