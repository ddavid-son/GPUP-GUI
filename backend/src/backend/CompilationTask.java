package backend;

import backend.argumentsDTO.TaskArgs;

import java.util.function.Consumer;

public class CompilationTask implements Task {


    @Override
    public void run(Consumer<String> print) {

    }

    @Override
    public void getReadyForIncrementalRun(TaskArgs taskArgs) {

    }

    @Override
    public boolean getAllGraphHasBeenProcessed() {
        return false;
    }
}
