package backend;

import java.util.function.Consumer;

public class CompilationTask implements Task {
    @Override
    public void run(Consumer<String> print) {

    }

    @Override
    public void getReadyForIncrementalRun(boolean isRandom, int msToRun, double successRate, double successfulWithWarningRate) {

    }

    @Override
    public boolean getAllGraphHasBeenProcessed() {
        return false;
    }
}
