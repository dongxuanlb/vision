package dx.core.struct;

public interface LifeCycle {

    LifeCycle.State getState();

    void initialize();

    void start();

    void stop();

    boolean isStarted();

    boolean isStopped();

    enum State {
        INITIALIZING,
        INITIALIZED,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED;

        State() {}
    }
}