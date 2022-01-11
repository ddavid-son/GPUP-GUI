package backend;

import backend.argumentsDTO.TaskArgs;
import backend.serialSets.SerialSetManger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class Task implements Serializable {

    protected final String path;
    List<String> waitingList = new LinkedList<>();
    Map<String, TaskTarget> graph = new HashMap<>();
    List<accumulatorForWritingToFile> logData = new LinkedList<>();
    boolean allGraphHasBeenProcessed;
    final int numberOfThreads;
    final SerialSetManger serialSetManger;
    int numberOfFinishedTargets = 0;
    ThreadPoolExecutor threadPool;
    final LinkedBlockingQueue<Runnable> threadPoolTaskQueue = new LinkedBlockingQueue<>();


    public Task(boolean allGraphHasBeenProcessed, SerialSetManger serialSetManger,
                int numberOfThreads, GraphManager graphManager, String path) {
        this.allGraphHasBeenProcessed = allGraphHasBeenProcessed;
        this.serialSetManger = serialSetManger;
        this.numberOfThreads = numberOfThreads;
        this.path = path;
        buildTaskGraph(graphManager);
    }

    protected synchronized void incrementFinishedThreadsCount() {
        numberOfFinishedTargets++;
    }

    protected static boolean missedTargets(TaskTarget target) {
        return target.state == Target.TargetState.FROZEN;
    }


    protected void buildTaskGraph(GraphManager graphManager) {
        for (Target target : graphManager.getTargetArray()) {
            graph.put(target.getName(), new TaskTarget(target));
        }
    }


    boolean getAllGraphHasBeenProcessed() {
        return allGraphHasBeenProcessed;
    }


    public void run(Consumer<String> print) {

        threadPool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads, 30,
                TimeUnit.SECONDS, threadPoolTaskQueue);
        long graphRunStartTime = System.currentTimeMillis();
        String fullPath = createDirectoryToLogData(graphRunStartTime);
        accumulatorForWritingToFile resOfTargetTaskRun;

        while (numberOfFinishedTargets < waitingList.size()) {
            for (int i = 0; i < waitingList.size(); i++) {
                TaskTarget targetToExecute = graph.get(waitingList.get(i));
                // the order of the statements inside the if () is important - relaying on "&&" short-circuiting feature
                // i.e. if the equals methode evaluates to false canIRun will not be called
                if (targetToExecute.state.equals(Target.TargetState.WAITING) && serialSetManger.canIRun(targetToExecute.name)) {
                    targetToExecute.state = Target.TargetState.IN_PROCESS;
                    resOfTargetTaskRun = new accumulatorForWritingToFile();
                    accumulatorForWritingToFile finalResOfTargetTaskRun = resOfTargetTaskRun;
                    Thread t = new Thread(() -> {
                        runTaskOnTarget(targetToExecute, finalResOfTargetTaskRun, print);
                        writeTargetResultsToLogFile(finalResOfTargetTaskRun, fullPath);
                        logData.add(finalResOfTargetTaskRun);
                        targetSummary(finalResOfTargetTaskRun, print);
                        serialSetManger.finishRunning(targetToExecute.name); // this is a synchronized method
                        incrementFinishedThreadsCount();
                    }, "thread #: " + numberOfFinishedTargets);
                    threadPool.execute(t);
                }
            }
        }

        long graphRunEndTime = System.currentTimeMillis();
        print.accept("Simulation finished in " +
                (graphRunEndTime - graphRunStartTime) / 1000 +
                "." + (graphRunEndTime - graphRunStartTime) % 1000 +
                " s");
        simulationRunSummary(print);
        numberOfFinishedTargets = 0;
    }


    protected void changePoolSize(int newNumberOfThreads) {
        threadPool.setMaximumPoolSize(4);
        threadPool.setCorePoolSize(4);
    }

    protected void targetSummary(accumulatorForWritingToFile resOfTargetTaskRun, Consumer<String> print) {
        print.accept("");
        for (String lineInSummary : resOfTargetTaskRun.outPutData) {
            print.accept(lineInSummary);
        }
        print.accept("");
    }

    protected void simulationRunSummary(Consumer<String> print) {

        int skipped = 0, Failed = 0, warning = 0, success = 0;

        //count all targets that participated in the simulation with results
        for (accumulatorForWritingToFile res : logData) {
            if (res.targetState == Target.TargetState.FAILURE) Failed++;
            if (res.targetState == Target.TargetState.WARNING) warning++;
            if (res.targetState == Target.TargetState.SUCCESS) success++;
        }
        //count all targets that didn't participated in the simulation i.e . they were skipped
        for (TaskTarget target : graph.values())
            if (target.state == Target.TargetState.SKIPPED) skipped++;

        //todo: handle in case of cyclic dependency. maybe throw exception or dont open the incremental option
        boolean hasCyclicDependency = graph.values().stream().anyMatch(SimulationTask::missedTargets);

        if (skipped == 0 && Failed == 0)
            allGraphHasBeenProcessed = true; // todo: need to consider if this is true in case of Circle in Graph

        print.accept("number of Skipped targets: " + skipped +
                "\nnumber of Failed targets: " + Failed +
                "\nnumber of warning targets: " + warning +
                "\nnumber of success targets: " + success + "\n");

        logData.forEach(data -> {
            print.accept(data.targetName +
                    "\nstate: " + data.targetState);

            logData.forEach(target -> {
                if (data.targetName.equals(target.targetName))
                    print.accept("task ran took " + TimeUtil.ltd(target.totalTimeToRun) + "\n");

            });

            data.SkippedTargets.forEach(target -> print.accept(target +
                    "\nstate: SKIPPED\n" +
                    "task ran took 00:00:00.000 \n"));
        });
    }

    protected String createDirectoryToLogData(long graphRunStartTime) {
        Date d = new Date(graphRunStartTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        File logFile = new File(path + "\\" + "simulation - " + sdf.format(d));
        if (!logFile.exists()) {
            try {
                if (!logFile.mkdir()) throw new IOException("Could not create directory");
            } catch (SecurityException | IOException se) {
                throw new IllegalArgumentException("could not find the folder and creating it failed too" +
                        "(could be due to permission issues), please choose another directory");
            }
        }
        return logFile.getAbsolutePath();
    }

    protected void writeTargetResultsToLogFile(accumulatorForWritingToFile resOfTargetTaskRun, String fullPath) {
        File logFile = new File(fullPath + "\\" + resOfTargetTaskRun.targetName + ".log");

        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("cannot create log file");
        }

        try {
            FileWriter fw = new FileWriter(logFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (String str : resOfTargetTaskRun.outPutData) {
                try {
                    bw.write(str + '\n');
                } catch (Exception e) {/**/}
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot write to log file " + e.getMessage());
        }
    }

    // ----------------------------------------- abstract Methods --------------------------------------------------- //

    abstract void runTaskOnTarget(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun,
                                  Consumer<String> print);

    abstract void performSimulation(accumulatorForWritingToFile resOfTargetTaskRun, Consumer<String> print);

    abstract void getReadyForIncrementalRun(TaskArgs taskArgs);
    // ----------------------------------------- abstract Methods --------------------------------------------------- //

    public class TaskTarget implements Serializable {

        protected final String name;
        protected final String userData;
        protected Target.TargetType type;
        protected Target.TargetState state;
        protected List<String> dependsOn;
        protected List<String> requiredFor;
        protected final List<String> nameOfFailedOrSkippedDependencies = new ArrayList<>();
        protected final List<String> serialSetsName = new ArrayList<>();

        //ctor
        public TaskTarget(Target target) {
            this.name = target.getName();
            this.type = target.getType();
            this.userData = target.getUserData();
            if (type == Target.TargetType.INDEPENDENT || type == Target.TargetType.LEAF) {
                state = Target.TargetState.WAITING;
                waitingList.add(name);
            } else {
                this.state = Target.TargetState.FROZEN;
            }

            this.dependsOn = target.getDependsOnNames();
            this.requiredFor = target.getRequiredForNames();
            this.serialSetsName.addAll(target.getSerialSetsName());
        }


    }
}
