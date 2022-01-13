package backend;

import backend.argumentsDTO.CompilationArgs;
import backend.argumentsDTO.ProgressDto;
import backend.argumentsDTO.TaskArgs;
import backend.serialSets.SerialSetManger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.function.Consumer;

public class CompilationTask extends Task {

    private String srcFolderPath;
    private String dstFolderPath;


    CompilationTask(TaskArgs taskArgs, GraphManager graphManager, String pathToLogFile, SerialSetManger serialSetManger
            , Consumer<accumulatorForWritingToFile> finishedTargetLog, Consumer<ProgressDto> finishedTarget) {
        super(false, serialSetManger, taskArgs.getNumOfThreads(), graphManager, pathToLogFile,
                finishedTargetLog, finishedTarget);
        CompilationArgs compilationArgs = (CompilationArgs) taskArgs;
        this.srcFolderPath = compilationArgs.getSrcPath();
        this.dstFolderPath = compilationArgs.getDstPath();
    }

    @Override
    void updateMembersAccordingToTask(TaskArgs taskArgs) {
        CompilationArgs simulationArgs = (CompilationArgs) taskArgs;
        this.srcFolderPath = simulationArgs.getSrcPath();
        this.dstFolderPath = simulationArgs.getDstPath();
    }

    // ---------------------------- includes internal logic specific to CompilationTask -----------------------------  //
    @Override
    protected void runTaskOnTarget(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun,
                                   Consumer<String> print) {
        try {
            resOfTargetTaskRun.startTime = System.currentTimeMillis();
            String fullCommand = getFullCommand(targetToExecute, resOfTargetTaskRun);
            Process p = Runtime.getRuntime().exec(fullCommand);
            p.waitFor();
            resOfTargetTaskRun.endTime = System.currentTimeMillis();
            String javacErrorMessage = "";
            if (p.exitValue() == 0) { // 0 == success
                targetToExecute.state = Target.TargetState.SUCCESS;
                removeAndUpdateDependenciesAfterSuccess(targetToExecute, resOfTargetTaskRun);
            } else {
                targetToExecute.state = Target.TargetState.FAILURE;
                javacErrorMessage = new BufferedReader(
                        new InputStreamReader(p.getErrorStream())).readLine();
                notifyAllAncestorToBeSkipped(targetToExecute, resOfTargetTaskRun);
                updateOpenTargets(targetToExecute, resOfTargetTaskRun);
            }
            updateAccumulator(resOfTargetTaskRun, targetToExecute, fullCommand, javacErrorMessage);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void updateAccumulator(accumulatorForWritingToFile resOfTargetTaskRun, TaskTarget targetToExecute,
                                   String fullCommand, String javacErrorMessage) {
        Timestamp ts = new Timestamp(resOfTargetTaskRun.startTime);

        resOfTargetTaskRun.targetName = targetToExecute.name;
        resOfTargetTaskRun.targetState = targetToExecute.state;
        resOfTargetTaskRun.UserData = targetToExecute.userData;

        resOfTargetTaskRun.totalTimeToRun = resOfTargetTaskRun.endTime - resOfTargetTaskRun.startTime;
        resOfTargetTaskRun.outPutData.add("* Task working on target: " + targetToExecute.name);
        resOfTargetTaskRun.outPutData.add("* Task started compiling: " + ts.toString().substring(10));
        resOfTargetTaskRun.outPutData.add("* File being compiled: " + targetToExecute.userData);
        resOfTargetTaskRun.outPutData.add("* The command used to compile: " + fullCommand);
        ts.setTime(resOfTargetTaskRun.endTime);
        resOfTargetTaskRun.outPutData.add("* The task finished compiling " + ts.toString().substring(10));
        resOfTargetTaskRun.outPutData.add("* Outcome of the task: " + targetToExecute.state);
        resOfTargetTaskRun.outPutData.add("* Time taken to compile: " + TimeUtil.ltd(resOfTargetTaskRun.totalTimeToRun));
        if (!javacErrorMessage.isEmpty())
            resOfTargetTaskRun.outPutData.add("* Error message from javac: " + javacErrorMessage);
    }

    private String getFullCommand(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun) {
        String FQN = targetToExecute.userData.replace(".", "\\");
        String srcFileToCompile = srcFolderPath + "\\" + FQN + ".java";
        CheckIfFileExists(srcFileToCompile);
        String saveCompiledFilesTo = dstFolderPath;
        // spacing are important!!!!!! motherfucker
        return "javac " + "-d " + saveCompiledFilesTo + " -cp " + saveCompiledFilesTo + " " + srcFileToCompile;
    }

    private void CheckIfFileExists(String srcFileToCompile) {
        File file = new File(srcFileToCompile);
        if (file.exists() && file.isFile() && file.canRead())
            throw new IllegalArgumentException("File " + srcFileToCompile + " does not exist and/or cannot be read");
    }
    // ---------------------------- includes internal logic specific to CompilationTask -----------------------------  //
}
