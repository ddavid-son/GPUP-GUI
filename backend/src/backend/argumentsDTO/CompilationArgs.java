package backend.argumentsDTO;

import backend.GraphManager;

public class CompilationArgs extends TaskArgs {
    public CompilationArgs(boolean isWhatIf, int numOfThreads, boolean isIncremental,
                           GraphManager.RelationType relationType) {
        super(isWhatIf, numOfThreads, TaskType.COMPILATION, isIncremental, relationType);
    }
}
