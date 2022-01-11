package backend.argumentsDTO;

import backend.GraphManager;

public class CompilationArgs extends TaskArgs {

    private String srcPath;
    private String dstPath;

    public CompilationArgs(boolean isWhatIf, int numOfThreads, boolean isIncremental,
                           GraphManager.RelationType relationType, String srcPath, String dstPath) {
        super(isWhatIf, numOfThreads, TaskType.COMPILATION, isIncremental, relationType);
        this.dstPath = dstPath;
        this.srcPath = srcPath;
    }
    
    public String getSrcPath() {
        return srcPath;
    }

    public String getDstPath() {
        return dstPath;
    }
}
