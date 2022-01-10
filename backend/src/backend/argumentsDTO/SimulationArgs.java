package backend.argumentsDTO;

import backend.GraphManager;

public class SimulationArgs extends TaskArgs {
    private final double successRate;
    private final double warningRate;
    private final int sleepTime;
    private final boolean isRandom;

    public SimulationArgs(double successRate, double warningRate, int sleepTime, int numOfThreads,
                          boolean isRandom, boolean isWhatIf, boolean isIncremental, GraphManager.RelationType relationType) {
        super(isWhatIf, numOfThreads, TaskType.SIMULATION, isIncremental, relationType);
        this.successRate = successRate;
        this.warningRate = warningRate;
        this.sleepTime = sleepTime;
        this.isRandom = isRandom;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public double getWarningRate() {
        return warningRate;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public boolean isRandom() {
        return isRandom;
    }
}
