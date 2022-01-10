package backend.argumentsDTO;

import backend.GraphManager;

public class CompilationArgs extends TaskArgs {
    public CompilationArgs(boolean isWhatIf, int numOfThreads, TaskType taskType, boolean isIncremental,
                           GraphManager.RelationType relationType) {
        super(isWhatIf, numOfThreads, taskType, isIncremental, relationType);
    }
}
