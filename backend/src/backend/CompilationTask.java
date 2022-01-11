package backend;

import backend.argumentsDTO.CompilationArgs;
import backend.argumentsDTO.TaskArgs;
import backend.serialSets.SerialSetManger;

import java.util.function.Consumer;

public class CompilationTask extends Task {

    private String srcFolderPath;
    private String dstFolderPath;

    CompilationTask(TaskArgs taskArgs, GraphManager graphManager, String pathToLogFile, SerialSetManger serialSetManger) {
        super(false, serialSetManger, taskArgs.getNumOfThreads(), graphManager, pathToLogFile);
        CompilationArgs compilationArgs = (CompilationArgs) taskArgs;
        this.srcFolderPath = compilationArgs.getSrcPath();
        this.dstFolderPath = compilationArgs.getDstPath();

    }

    @Override
    protected void performSimulation(accumulatorForWritingToFile resOfTargetTaskRun, Consumer<String> print) {

    }

    @Override
    protected void runTaskOnTarget(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun,
                                   Consumer<String> print) {
    }

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
