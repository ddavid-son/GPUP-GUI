package backend;

import java.io.Serializable;
import java.util.function.Consumer;

public interface Task extends Serializable {
    void run(Consumer<String> print);

    void getReadyForIncrementalRun(boolean isRandom, int msToRun, double successRate, double successfulWithWarningRate);

    boolean getAllGraphHasBeenProcessed();
}
