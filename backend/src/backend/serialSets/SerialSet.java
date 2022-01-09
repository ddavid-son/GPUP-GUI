package backend.serialSets;

import java.util.ArrayList;
import java.util.List;

public class SerialSet {
    private final String name;
    private final List<String> targetInSerialSet = new ArrayList<>();
    private SerialSetState state;
    private String activeTarget;

    public SerialSet(String name, List<String> targetInSerialSet) {
        this.name = name;
        this.targetInSerialSet.addAll(targetInSerialSet);
        this.state = SerialSetState.OPENED;
        this.activeTarget = "";
    }

    public boolean canIRun(String target) {
        return !(targetInSerialSet.contains(target) && state == SerialSetState.BLOCKED);
    }

    public void setState(String targetName, SerialSetState newState) {
        if (newState.equals(SerialSetState.BLOCKED))
            activeTarget = targetName;
        else
            activeTarget = "";

        state = newState;
    }

    public SerialSetState getState() {

        return state;
    }

    public String getActiveTarget() {

        return activeTarget;
    }

    public String getSerialSetName() {

        return name;
    }

    public enum SerialSetState {
        BLOCKED,
        OPENED,
    }
}
