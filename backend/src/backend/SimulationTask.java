package backend;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SimulationTask implements Task, Serializable {

    private String path;
    private int msToRun;
    private boolean isRandom;
    private double successRate;
    private double successfulWithWarningRate;
    private List<String> waitingList = new LinkedList<>();
    private Map<String, SimulationTarget> graph = new HashMap<>();
    private List<accumulatorForWritingToFile> logData = new LinkedList<>();
    boolean allGraphHasBeenProcessed;


    public SimulationTask(int msToRun, boolean isRandom, double successRate, double successfulWithWarningRate,
                          GraphManager graphManager, String pathToLogFile) {
        this.allGraphHasBeenProcessed = false;
        this.path = pathToLogFile;
        this.msToRun = msToRun;
        this.isRandom = isRandom;
        this.successRate = successRate;
        this.successfulWithWarningRate = successfulWithWarningRate;
        buildsSimulationGraph(graphManager);
    }

    @Override
    public boolean getAllGraphHasBeenProcessed() {
        return allGraphHasBeenProcessed;
    }

    public void buildsSimulationGraph(GraphManager graphManager) {
        for (Target target : graphManager.getTargetArray()) {
            graph.put(target.getName(), new SimulationTarget(target));
        }
    }

    @Override
    public void getReadyForIncrementalRun(boolean isRandom, int msToRun, double successRate, double successfulWithWarningRate) {

        this.msToRun = msToRun;
        this.isRandom = isRandom;
        this.successRate = successRate;
        this.successfulWithWarningRate = successfulWithWarningRate;

        // remove all succeeded targets from waiting list
        waitingList = waitingList.stream()
                .filter(targetName -> graph.get(targetName).state == Target.TargetState.FAILURE ||
                        graph.get(targetName).state == Target.TargetState.WAITING)
                .collect(Collectors.toCollection(LinkedList::new));

        // RESETTING ALL SKIPPED TARGET TO THEIR ACTUAL STATE
        for (SimulationTarget target : graph.values()) {
            if (target.state == Target.TargetState.SKIPPED) {
                target.nameOfFailedOrSkippedDependencies.forEach(skippDep -> {
                    if (!target.dependsOn.contains(skippDep))
                        graph.get(skippDep).dependsOn.remove(target.name);
                });
                target.state = Target.TargetState.FROZEN;
                target.nameOfFailedOrSkippedDependencies.clear();
                if (target.dependsOn.isEmpty()) {
                    target.state = Target.TargetState.WAITING;
                    waitingList.add(target.name);
                }
            }
            if (target.state == Target.TargetState.FAILURE) {
                target.state = Target.TargetState.WAITING;
            }
        }

        logData.clear();
    }

    @Override
    public void run(Consumer<String> print) {
        Random random = new Random();
        long graphRunStartTime = System.currentTimeMillis();
        String fullPath = createDirectoryToLogData(graphRunStartTime);
        accumulatorForWritingToFile resOfTargetTaskRun;

        for (int i = 0; i < waitingList.size(); i++) {
            SimulationTarget targetToExecute = graph.get(waitingList.get(i));
            resOfTargetTaskRun = new accumulatorForWritingToFile();
            if (targetToExecute.state == Target.TargetState.WAITING) { // redundant check maybe needs to be != FAILURE instead
                runTaskOnTarget(targetToExecute, random, isRandom, resOfTargetTaskRun, print);
                writeTargetResultsToLogFile(resOfTargetTaskRun, fullPath);
                logData.add(resOfTargetTaskRun);
                targetSummary(resOfTargetTaskRun, print);
            }
        }
        long graphRunEndTime = System.currentTimeMillis();
        print.accept("Simulation finished in " +
                (graphRunEndTime - graphRunStartTime) / 1000 +
                "." + (graphRunEndTime - graphRunStartTime) % 1000 +
                " s");
        simulationRunSummary(print);
    }

    private void targetSummary(accumulatorForWritingToFile resOfTargetTaskRun, Consumer<String> print) {
        print.accept("");
        for (String lineInSummary : resOfTargetTaskRun.outPutData) {
            print.accept(lineInSummary);
        }
        print.accept("");
    }

    private void simulationRunSummary(Consumer<String> print) {

        int skipped = 0, Failed = 0, warning = 0, success = 0;

        //count all targets that participated in the simulation with results
        for (accumulatorForWritingToFile res : logData) {
            if (res.targetState == Target.TargetState.FAILURE) Failed++;
            if (res.targetState == Target.TargetState.WARNING) warning++;
            if (res.targetState == Target.TargetState.SUCCESS) success++;
        }
        //count all targets that didn't participated in the simulation i.e . they were skipped
        for (SimulationTarget target : graph.values())
            if (target.state == Target.TargetState.SKIPPED) skipped++;

        if (skipped == 0 && Failed == 0) allGraphHasBeenProcessed = true;

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

    private String createDirectoryToLogData(long graphRunStartTime) {
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

    private void writeTargetResultsToLogFile(accumulatorForWritingToFile resOfTargetTaskRun, String fullPath) {
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

    private void runTaskOnTarget(SimulationTarget targetToExecute, Random random, boolean isRandom,
                                 accumulatorForWritingToFile resOfTargetTaskRun, Consumer<String> print) {
        double randomNumber = random.nextDouble();
        resOfTargetTaskRun.outPutData.add("1. task started, running on - " + targetToExecute.name);

        resOfTargetTaskRun.outPutData.add("2. text on target - " +
                (targetToExecute.userData.isEmpty() ? "no text" : targetToExecute.userData));

        performSimulation(random, isRandom, resOfTargetTaskRun, print);

        updateGraphAccordingToTheResults(targetToExecute, random, resOfTargetTaskRun, randomNumber);
        resOfTargetTaskRun.targetState = targetToExecute.state;
        resOfTargetTaskRun.targetName = targetToExecute.name;

        invokeConsumer(targetToExecute, resOfTargetTaskRun, print);
    }

    private void performSimulation(Random random, boolean isRandom,
                                   accumulatorForWritingToFile resOfTargetTaskRun,
                                   Consumer<String> print) {
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

    private void updateGraphAccordingToTheResults(SimulationTarget targetToExecute, Random random,
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

    private void updateOpenTargets(SimulationTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun) {
        for (String father : targetToExecute.requiredFor) {
            graph.get(father).dependsOn.remove(targetToExecute.name);
            if (graph.get(father).dependsOn.isEmpty())
                resOfTargetTaskRun.targetOpened.add(father);
        }
    }

    private void invokeConsumer(SimulationTarget targetToExecute, accumulatorForWritingToFile
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

    private void removeAndUpdateDependenciesAfterSuccess(SimulationTarget targetToExecute,
                                                         accumulatorForWritingToFile resOfTargetTaskRun) {

        targetToExecute.requiredFor.forEach(neighbour -> {
            graph.get(neighbour).dependsOn.remove(targetToExecute.name);
            if (graph.get(neighbour).dependsOn.isEmpty()) {
                if (!graph.get(neighbour).state.equals(Target.TargetState.SKIPPED)) {
                    waitingList.add(neighbour);
                    graph.get(neighbour).state = Target.TargetState.WAITING;
                }
                resOfTargetTaskRun.targetOpened.add(neighbour);
            }
        });
    }

    private void notifyAllAncestorToBeSkipped(SimulationTarget targetToExecute,
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

    private class SimulationTarget implements Serializable {

        private final String name;
        private final String userData;
        private Target.TargetType type;
        private Target.TargetState state;
        private final List<String> dependsOn;
        private final List<String> requiredFor;
        private final List<String> nameOfFailedOrSkippedDependencies = new ArrayList<>();

        //ctor
        public SimulationTarget(Target target) {
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
        }
    }
}
