package backend.serialSets;

import backend.Target;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerialSetManger {
    private final Map<String, SerialSet> ssName2serialSet = new HashMap<>();
    private final Map<String, List<String>> target2SerialSetNames = new HashMap<>();


    public SerialSetManger() {
    }

    public void addSerialSet(Map.Entry<String, SerialSet> serialSetEntry) {
        ssName2serialSet.put(serialSetEntry.getKey(), serialSetEntry.getValue());
    }

    public void addSerialSet(String key, SerialSet value) {
        ssName2serialSet.put(key, value);
    }

    public void createMapFromTarget2SerialSetNames(List<Target> targetList) {
        for (Target target : targetList) {
            this.target2SerialSetNames.put(target.getName(), target.getSerialSetNames());
            // TODO remember target.getSerialSetNames() returns a FINAL LIST !!!!
        }
    }

    public boolean canIRun(String targetName) {
        if (target2SerialSetNames.get(targetName).isEmpty()) {
            return true;
        }
        if (target2SerialSetNames.get(targetName).stream().
                allMatch(ssName -> ssName2serialSet.get(ssName).canIRun(targetName))) {
            blockAllMySerialSets(targetName);
            return true;
        }

        return false;
    }

    private void blockAllMySerialSets(String targetName) {
        target2SerialSetNames.get(targetName).forEach(ssName ->
                ssName2serialSet.get(ssName).setState(targetName, SerialSet.SerialSetState.BLOCKED));
    }

    public synchronized void finishRunning(String targetName) {
        target2SerialSetNames.get(targetName).forEach(ssName ->
                ssName2serialSet.get(ssName).setState(targetName, SerialSet.SerialSetState.OPENED));
    }
}
