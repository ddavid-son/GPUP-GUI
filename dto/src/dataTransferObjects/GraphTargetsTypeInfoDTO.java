package dataTransferObjects;

import backend.Target;

public class GraphTargetsTypeInfoDTO {
    private int totalNumberOfRoots;
    private int totalNumberOfLeaves;
    private int totalNumberOfMiddles;
    private int totalNumberOfTargets;
    private int totalNumberOfIndependents;

    public GraphTargetsTypeInfoDTO(int totalNumberOfTargets, int totalNumberOfLeaves,
                                   int totalNumberOfMiddles, int totalNumberOfRoots,
                                   int totalNumberOfIndependents) {
        this.totalNumberOfTargets = totalNumberOfTargets;
        this.totalNumberOfLeaves = totalNumberOfLeaves;
        this.totalNumberOfMiddles = totalNumberOfMiddles;
        this.totalNumberOfRoots = totalNumberOfRoots;
        this.totalNumberOfIndependents = totalNumberOfIndependents;
    }

    public void countTargetsByType(Target.TargetType type) {
        totalNumberOfTargets++;
        switch (type) {
            case LEAF:
                totalNumberOfLeaves++;
                break;
            case MIDDLE:
                totalNumberOfMiddles++;
                break;
            case ROOT:
                totalNumberOfRoots++;
                break;
            case INDEPENDENT:
                totalNumberOfIndependents++;
                break;
        }
    }

    public GraphTargetsTypeInfoDTO() {
        this(0, 0, 0, 0, 0);
    }

    public int getTotalNumberOfTargets() {
        return totalNumberOfTargets;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Distribution of targets by type:")
                .append("\n Total Number Of Targets: ").append(totalNumberOfTargets)
                .append("\n Total Number Of Roots: ").append(totalNumberOfRoots)
                .append("\n Total number of leaves: ").append(totalNumberOfLeaves)
                .append("\n Total Number Of Middles: ").append(totalNumberOfMiddles)
                .append("\n Total Number Of Independents: ").append(totalNumberOfIndependents);
        return sb.toString();
    }

}
