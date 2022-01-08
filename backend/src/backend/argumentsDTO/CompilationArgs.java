package backend.argumentsDTO;

public class CompilationArgs extends TaskArgs {
    public CompilationArgs(boolean isWhatIf, int numOfThreads, TaskType taskType, boolean isIncremental) {
        super(isWhatIf, numOfThreads, taskType, isIncremental);
    }
}
