package backend;

import backend.argumentsDTO.ProgressDto;
import backend.argumentsDTO.TaskArgs;
import backend.serialSets.SerialSetManger;
import javafx.application.Platform;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Task implements Serializable {

    Thread mainThread;
    int maxParallelism;
    int numberOfThreads;
    boolean flag = false;
    int outOfWaiting = 0;
    protected final String path;
    ThreadPoolExecutor threadPool;
    int numberOfFinishedTargets = 0;
    boolean allGraphHasBeenProcessed;
    Consumer<ProgressDto> finishedTarget;
    private int numberOfThreadActive = 0;
    final SerialSetManger serialSetManger;
    final Object monitorObj = new Object();
    BlockingQueue<Runnable> threadPoolTaskQueue;
    List<String> waitingList = new LinkedList<>();
    Map<String, TaskTarget> graph = new HashMap<>();
    Consumer<accumulatorForWritingToFile> finishedTargetLog;
    List<accumulatorForWritingToFile> logData = new LinkedList<>();


    public void pauseTask() {
        flag = true;
    }

    public void resumeTask() {
        flag = false;
        synchronized (monitorObj) {
            monitorObj.notifyAll();
        }
    }

    private void pauseThreadTask() {
        if (flag) {
            synchronized (monitorObj) {
                try {
                    monitorObj.wait();
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    private synchronized void updateNumberOfActiveThreads(boolean isUp) {
        outOfWaiting += isUp ? 1 : 0;
        numberOfThreadActive = isUp ? numberOfThreadActive + 1 : numberOfThreadActive - 1;
        int idle = numberOfThreads - numberOfThreadActive;
        int waiting = waitingList.size() - outOfWaiting;
        Platform.runLater(() -> finishedTargetLog.accept(new accumulatorForWritingToFile("\n" +
                TimeUtil.ltn(System.currentTimeMillis()) + " there are currently " + idle + " idle threads. " +
                "still in Queue: " + waiting + "\n")));
    }

    // ---------------------------------------------- ctor and utils ------------------------------------------------ //
    public Task(boolean allGraphHasBeenProcessed, SerialSetManger serialSetManger,
                int numberOfThreads, GraphManager graphManager, String path,
                Consumer<accumulatorForWritingToFile> finishedTargetLog, Consumer<ProgressDto> finishedTarget,
                int maxParallelism) {
        this.path = path;
        this.maxParallelism = maxParallelism;
        this.numberOfThreads = numberOfThreads;
        this.serialSetManger = serialSetManger;
        this.allGraphHasBeenProcessed = allGraphHasBeenProcessed;
        setConsumers(finishedTargetLog, finishedTarget);
        buildTaskGraph(graphManager);
    }

    protected void buildTaskGraph(GraphManager graphManager) {
        for (Target target : graphManager.getTargetArray()) {
            TaskTarget taskTarget = new TaskTarget(target);
            if (target.getState() == Target.TargetState.WAITING) {
                Platform.runLater(() -> finishedTarget.accept(new ProgressDto(target.getName(), target.getState())));
            }
            graph.put(target.getName(), taskTarget);
        }
    }

    protected synchronized void incrementFinishedThreadsCount() {

        numberOfFinishedTargets++;
    }

    boolean getAllGraphHasBeenProcessed() {

        return allGraphHasBeenProcessed;
    }

    protected void setPoolSize() {
        if (threadPool == null || threadPool.isShutdown()) {
            threadPool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                    1, TimeUnit.MILLISECONDS, threadPoolTaskQueue == null ?
                    new LinkedBlockingQueue<>() :
                    threadPoolTaskQueue
            );
            return;
        }
        threadPool.setCorePoolSize(numberOfThreads);
        threadPool.setMaximumPoolSize(numberOfThreads);
    }

    public void changeNumberOfThreads(int newThreadsCount) {
        if (newThreadsCount != numberOfThreads && newThreadsCount > 0 && newThreadsCount <= maxParallelism) {
            numberOfThreads = newThreadsCount;
            setPoolSize();
        }
    }

    // ---------------------------------------------- ctor and utils ------------------------------------------------ //


    // --------------------------------------------------- run ------------------------------------------------------ //
    public void run(Consumer<String> print) {
        setPoolSize();
        mainThread = Thread.currentThread();
        accumulatorForWritingToFile resOfTargetTaskRun;
        long graphRunStartTime = System.currentTimeMillis();
        String fullPath = createDirectoryToLogData(graphRunStartTime);

        while (numberOfFinishedTargets < waitingList.size()) {
            for (int i = 0; i < waitingList.size(); i++) {
                pauseThreadTask();
                TaskTarget targetToExecute = graph.get(waitingList.get(i));
                // the order of the statements inside the if () is important - relaying on "&&" short-circuiting feature
                // i.e. if the equals methode evaluates to false canIRun will not be called
                if (targetToExecute.state.equals(Target.TargetState.WAITING) && serialSetManger.canIRun(targetToExecute.name)) {
                    targetToExecute.state = Target.TargetState.IN_PROCESS;
                    targetToExecute.enterProcess = System.currentTimeMillis();
                    resOfTargetTaskRun = new accumulatorForWritingToFile();
                    Platform.runLater(() -> {
                        finishedTarget.accept(new ProgressDto(targetToExecute.name, Target.TargetState.IN_PROCESS));
                    });
                    accumulatorForWritingToFile finalResOfTargetTaskRun = resOfTargetTaskRun;
                    sendToNewThreadAndPushToPool(print, fullPath, targetToExecute, finalResOfTargetTaskRun);
                }
            }
        }
        threadPool.shutdown();
        printRunSummary(print, graphRunStartTime);
        numberOfFinishedTargets = 0;
        numberOfThreadActive = 0;
    }

    public long getWaitingStartTime(String targetName) {
        return graph.get(targetName).enterWaiting;
    }

    public long getProcessStartTime(String targetName) {
        return graph.get(targetName).enterProcess;
    }

    public List<String> getFailedOrSkipped(String targetName) {
        return graph.get(targetName).nameOfFailedOrSkippedDependencies;
    }

    public ProgressDto getInfoAboutTargetInExecution(String targetName) {
        return new ProgressDto(
                "",
                graph.get(targetName).name,
                graph.get(targetName).state,
                graph.get(targetName).enterWaiting,
                graph.get(targetName).enterProcess,
                String.join(",", graph.get(targetName).nameOfFailedOrSkippedDependencies),
                String.join(",", graph.get(targetName).dependsOn)
        );
    }

    private void printRunSummary(Consumer<String> print, long graphRunStartTime) {
        long graphRunEndTime = System.currentTimeMillis();
        print.accept("Simulation finished in " +
                (graphRunEndTime - graphRunStartTime) / 1000 +
                "." + (graphRunEndTime - graphRunStartTime) % 1000 +
                " s");
        simulationRunSummary(print);
    }

    private void sendToNewThreadAndPushToPool(Consumer<String> print, String fullPath, TaskTarget targetToExecute, accumulatorForWritingToFile finalResOfTargetTaskRun) {
        Thread t = new Thread(() -> {
            pauseThreadTask();
            updateNumberOfActiveThreads(true);
            runTaskOnTarget(targetToExecute, finalResOfTargetTaskRun, print);
            Platform.runLater(() -> {
                finishedTargetLog.accept(finalResOfTargetTaskRun);
                finishedTarget.accept(new ProgressDto(getNamesToRunLater(targetToExecute, finalResOfTargetTaskRun), targetToExecute.state));
            });
            writeTargetResultsToLogFile(finalResOfTargetTaskRun, fullPath);
            logData.add(finalResOfTargetTaskRun);
            targetSummary(finalResOfTargetTaskRun, print);
            serialSetManger.finishRunning(targetToExecute.name); // this is a synchronized method
            incrementFinishedThreadsCount();
            updateNumberOfActiveThreads(false);
        }, "thread #: " + numberOfFinishedTargets);
        threadPool.execute(t);
    }

    private String getNamesToRunLater(TaskTarget targetToExecute, accumulatorForWritingToFile finalResOfTargetTaskRun) {
        if (targetToExecute.state == Target.TargetState.FAILURE) {
            return targetToExecute.name + "," + String.join(",", finalResOfTargetTaskRun.SkippedTargets);
        }
        return targetToExecute.name;
    }

    private void setConsumers(Consumer<accumulatorForWritingToFile> finishedTargetLog, Consumer<ProgressDto> finishedTarget) {
        if (this.finishedTargetLog == null && this.finishedTarget == null) {
            this.finishedTargetLog = finishedTargetLog;
            this.finishedTarget = finishedTarget;
        }
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

    protected synchronized void updateOpenTargets(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun) {
        for (String father : targetToExecute.requiredFor) {
            graph.get(father).dependsOn.remove(targetToExecute.name);
            if (graph.get(father).dependsOn.isEmpty())
                resOfTargetTaskRun.targetOpened.add(father);
        }
    }

    protected void invokeConsumer(TaskTarget targetToExecute, accumulatorForWritingToFile
            resOfTargetTaskRun, Consumer<String> print) {
        resOfTargetTaskRun.outPutData.add(
                "3. task finished running on - " + targetToExecute.name + " task results: " + targetToExecute.state);

        resOfTargetTaskRun.outPutData.add(
                "4. targets opened to execution by this operation:\n" + (resOfTargetTaskRun.targetOpened.isEmpty() ?
                        "no targets opened" : resOfTargetTaskRun.targetOpened));

        if (targetToExecute.state.equals(Target.TargetState.FAILURE)) {
            resOfTargetTaskRun.outPutData.add("5. targets that will be skipped due to: " + targetToExecute.name + "'s failure:\n" +
                    (resOfTargetTaskRun.SkippedTargets.isEmpty() ? "no targets skipped" : resOfTargetTaskRun.SkippedTargets));
        }
    }

    protected synchronized void removeAndUpdateDependenciesAfterSuccess(TaskTarget targetToExecute,
                                                                        accumulatorForWritingToFile resOfTargetTaskRun) {

        targetToExecute.requiredFor.forEach(neighbour -> {
            graph.get(neighbour).dependsOn.remove(targetToExecute.name);
            if (graph.get(neighbour).dependsOn.isEmpty()) {
                if (!graph.get(neighbour).state.equals(Target.TargetState.SKIPPED) &&
                        !waitingList.contains(neighbour)) {
                    Platform.runLater(() -> finishedTarget.accept(new ProgressDto(neighbour, Target.TargetState.WAITING)));
                    waitingList.add(neighbour);
                    graph.get(neighbour).state = Target.TargetState.WAITING;
                }
                resOfTargetTaskRun.targetOpened.add(neighbour);
            }
        });
    }

    protected synchronized void notifyAllAncestorToBeSkipped(TaskTarget targetToExecute,
                                                             accumulatorForWritingToFile resOfTargetTaskRun) {
        if (!targetToExecute.state.equals(Target.TargetState.SKIPPED)) {
            for (String ancestor : targetToExecute.requiredFor) {
                if (!graph.get(ancestor).state.equals(Target.TargetState.SKIPPED)) {
                    if (!resOfTargetTaskRun.SkippedTargets.contains(ancestor))
                        resOfTargetTaskRun.SkippedTargets.add(ancestor);
                    notifyAllAncestorToBeSkipped(graph.get(ancestor), resOfTargetTaskRun);
                    graph.get(ancestor).state = Target.TargetState.SKIPPED;
                    graph.get(ancestor).nameOfFailedOrSkippedDependencies.add(targetToExecute.name);
                }
            }
        }
    }
    // --------------------------------------------------- run ------------------------------------------------------ //


    // --------------------------------------------------- run ------------------------------------------------------ //

    // ------------------------------------------------- getReady --------------------------------------------------- //
    public void getReadyForIncrementalRun(TaskArgs taskArgs) {

        updateMembersAccordingToTask(taskArgs);
        numberOfThreads = taskArgs.getNumOfThreads();

        // remove all succeeded targets from waiting list
        waitingList = waitingList.stream()
                .filter(targetName -> graph.get(targetName).state == Target.TargetState.FAILURE)
                .collect(Collectors.toCollection(LinkedList::new));

        waitingList.forEach(targetName -> {
            graph.get(targetName).requiredFor.forEach(reqName ->
                    graph.get(reqName).dependsOn.add(targetName));
            graph.get(targetName).state = Target.TargetState.WAITING;
            Platform.runLater(() -> finishedTarget.accept(new ProgressDto(targetName, Target.TargetState.WAITING)));
        });

        // RESETTING ALL SKIPPED TARGET TO THEIR ACTUAL STATE
        for (TaskTarget target : graph.values()) {
            if (target.state == Target.TargetState.SKIPPED) {
                target.nameOfFailedOrSkippedDependencies.forEach(skippDep -> {
                    if (!target.dependsOn.contains(skippDep))
                        graph.get(skippDep).dependsOn.add(target.name);
                });
                target.state = Target.TargetState.FROZEN;
                target.nameOfFailedOrSkippedDependencies.clear();
            }
        }

        logData.clear();
    }
    // ------------------------------------------------- getReady --------------------------------------------------- //


    // ----------------------------------------- abstract Methods --------------------------------------------------- //
    abstract void updateMembersAccordingToTask(TaskArgs taskArgs);

    abstract void runTaskOnTarget(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun,
                                  Consumer<String> print);

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
        protected long enterWaiting = 0;
        protected long enterProcess = 0;

        //ctor
        public TaskTarget(Target target) {
            this.name = target.getName();
            this.type = target.getType();
            this.userData = target.getUserData();
            if (type == Target.TargetType.INDEPENDENT || type == Target.TargetType.LEAF) {
                state = Target.TargetState.WAITING;
                waitingList.add(name);
                enterWaiting = System.currentTimeMillis();
            } else {
                this.state = Target.TargetState.FROZEN;
            }

            this.dependsOn = target.getDependsOnNames();
            this.requiredFor = target.getRequiredForNames();
            this.serialSetsName.addAll(target.getSerialSetsName());
        }

        public String getName() {
            return this.name;
        }

        public String getUserData() {
            return this.userData;
        }

        public Target.TargetType getType() {
            return this.type;
        }

        public Target.TargetState getState() {
            return this.state;
        }

        public List<String> getDependsOnNames() {
            return this.dependsOn;
        }

        public List<String> getRequiredForNames() {
            return this.requiredFor;
        }

        public List<String> getNameOfFailedOrSkippedDependencies() {
            return this.nameOfFailedOrSkippedDependencies;
        }

        public List<String> getSerialSetsName() {
            return this.serialSetsName;
        }
    }
}
