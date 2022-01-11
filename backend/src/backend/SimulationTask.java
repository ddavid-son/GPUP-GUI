package backend;

import backend.argumentsDTO.SimulationArgs;
import backend.argumentsDTO.TaskArgs;
import backend.serialSets.SerialSetManger;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SimulationTask extends Task implements Serializable {


    private int msToRun;
    private boolean isRandom;
    private double successRate;
    private double successfulWithWarningRate;
    Random random;

   /* private List<String> waitingList = new LinkedList<>();
    private Map<String, SimulationTarget> graph = new HashMap<>();
    private List<accumulatorForWritingToFile> logData = new LinkedList<>();
    private boolean allGraphHasBeenProcessed;
    private final int numberOfThreads;
    private final SerialSetManger serialSetManger;
    private int numberOfFinishedTargets = 0;
    private ThreadPoolExecutor threadPool;
    private final LinkedBlockingQueue<Runnable> threadPoolTaskQueue = new LinkedBlockingQueue<>();*/


    public SimulationTask(TaskArgs taskArgs/*int msToRun, boolean isRandom, double successRate, double successfulWithWarningRate*/,
                          GraphManager graphManager, String pathToLogFile, /*int numberOfThreads*/ SerialSetManger serialSetManager) {
        super(false, serialSetManager, taskArgs.getNumOfThreads(), graphManager, pathToLogFile);
        SimulationArgs simulationArgs = (SimulationArgs) taskArgs;
        this.msToRun = simulationArgs.getSleepTime();
        this.isRandom = simulationArgs.isRandom();
        this.successRate = simulationArgs.getSuccessRate();
        this.successfulWithWarningRate = simulationArgs.getWarningRate();
        this.random = new Random();
    }


    @Override
    public void getReadyForIncrementalRun(TaskArgs taskArgs/*boolean isRandom, int msToRun, double successRate, double successfulWithWarningRate*/) {
        SimulationArgs simulationArgs = (SimulationArgs) taskArgs;

        this.msToRun = simulationArgs.getSleepTime();//msToRun;
        this.isRandom = simulationArgs.isRandom();// isRandom;
        this.successRate = simulationArgs.getSuccessRate();// successRate;
        this.successfulWithWarningRate = simulationArgs.getWarningRate();// successfulWithWarningRate;

        //todo check if needed to update number of threads in case of incremental run

        // remove all succeeded targets from waiting list
        waitingList = waitingList.stream()
                .filter(targetName -> graph.get(targetName).state == Target.TargetState.FAILURE)
                .collect(Collectors.toCollection(LinkedList::new));

        waitingList.forEach(targetName -> {
            graph.get(targetName).requiredFor.forEach(reqName ->
                    graph.get(reqName).dependsOn.add(targetName));
            graph.get(targetName).state = Target.TargetState.WAITING;
        });

        // RESETTING ALL SKIPPED TARGET TO THEIR ACTUAL STATE
        for (TaskTarget target : graph.values()) {
            if (target.state == Target.TargetState.SKIPPED) {
                target.nameOfFailedOrSkippedDependencies.forEach(skippDep -> {
                    if (!target.dependsOn.contains(skippDep))
                        graph.get(skippDep).dependsOn.add(target.name);
                    //graph.get(skippDep).dependsOn.remove(target.name); // todo: figure why it says remove?
                });
                target.state = Target.TargetState.FROZEN;
                target.nameOfFailedOrSkippedDependencies.clear();
            }
        }

        logData.clear();
    }


    // ---------------------------- includes internal logic specific to simulationTask -----------------------------  //
    @Override
    protected void runTaskOnTarget(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun,
                                   Consumer<String> print) {
        double randomNumber = random.nextDouble();
        resOfTargetTaskRun.outPutData.add("1. task started, running on - " + targetToExecute.name);

        resOfTargetTaskRun.outPutData.add("2. text on target - " +
                (targetToExecute.userData.isEmpty() ? "no text" : targetToExecute.userData));

        performSimulation(resOfTargetTaskRun, print);

        updateGraphAccordingToTheResults(targetToExecute, random, resOfTargetTaskRun, randomNumber);
        resOfTargetTaskRun.targetState = targetToExecute.state;
        resOfTargetTaskRun.targetName = targetToExecute.name;

        invokeConsumer(targetToExecute, resOfTargetTaskRun, print);
    }

    @Override
    protected void performSimulation(accumulatorForWritingToFile resOfTargetTaskRun, Consumer<String> print) {
        resOfTargetTaskRun.startTime = System.currentTimeMillis();
        Timestamp ts = new Timestamp(resOfTargetTaskRun.startTime);
        try {
            if (isRandom) {
                int randomNumberToWait = random.nextInt(msToRun);
                resOfTargetTaskRun.outPutData.add("  * going to sleep for " + TimeUtil.ltd(randomNumberToWait));

                resOfTargetTaskRun.outPutData.add("  * going to sleep, good night " + ts.toString().substring(10));
                Thread.sleep(randomNumberToWait);
            } else {
                resOfTargetTaskRun.outPutData.add("  * going to sleep for " + TimeUtil.ltd(msToRun));

                resOfTargetTaskRun.outPutData.add("  * going to sleep, good night " + ts.toString().substring(10));
                Thread.sleep(msToRun);
            }
            resOfTargetTaskRun.endTime = System.currentTimeMillis();
            ts.setTime(resOfTargetTaskRun.endTime);

            resOfTargetTaskRun.outPutData.add("  * top of the morning to ya good sir " + ts.toString().substring(10));
            resOfTargetTaskRun.totalTimeToRun = resOfTargetTaskRun.endTime - resOfTargetTaskRun.startTime;

        } catch (InterruptedException e) { /**/ }
        resOfTargetTaskRun.endTime = System.currentTimeMillis();
    }

    private void updateGraphAccordingToTheResults(TaskTarget targetToExecute, Random random,
                                                  accumulatorForWritingToFile resOfTargetTaskRun,
                                                  double randomNumber) {
        if (randomNumber <= successRate) {
            randomNumber = random.nextDouble();
            targetToExecute.state = Target.TargetState.SUCCESS;
            if (randomNumber <= successfulWithWarningRate) {
                targetToExecute.state = Target.TargetState.WARNING;
            }
            removeAndUpdateDependenciesAfterSuccess(targetToExecute, resOfTargetTaskRun);
        } else {
            targetToExecute.state = Target.TargetState.FAILURE;
            notifyAllAncestorToBeSkipped(targetToExecute, resOfTargetTaskRun);
            updateOpenTargets(targetToExecute, resOfTargetTaskRun);
        }
    }
    // ---------------------------- includes internal logic specific to simulationTask -----------------------------  //

    private synchronized void updateOpenTargets(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun) {
        for (String father : targetToExecute.requiredFor) {
            graph.get(father).dependsOn.remove(targetToExecute.name);
            if (graph.get(father).dependsOn.isEmpty())
                resOfTargetTaskRun.targetOpened.add(father);
        }
    }

    private void invokeConsumer(TaskTarget targetToExecute, accumulatorForWritingToFile
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

    private synchronized void removeAndUpdateDependenciesAfterSuccess(TaskTarget targetToExecute,
                                                                      accumulatorForWritingToFile resOfTargetTaskRun) {

        targetToExecute.requiredFor.forEach(neighbour -> {
            graph.get(neighbour).dependsOn.remove(targetToExecute.name);
            if (graph.get(neighbour).dependsOn.isEmpty()) {
                if (!graph.get(neighbour).state.equals(Target.TargetState.SKIPPED) &&
                        !waitingList.contains(neighbour)) {
                    waitingList.add(neighbour);
                    graph.get(neighbour).state = Target.TargetState.WAITING;
                }
                resOfTargetTaskRun.targetOpened.add(neighbour);
            }
        });
    }

    private synchronized void notifyAllAncestorToBeSkipped(TaskTarget targetToExecute,
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
}
