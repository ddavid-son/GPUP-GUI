package backend;

import java.util.List;

public class SerialSetManger {
    private List<String> serialSets;

    public SerialSetManger(List<String> serialSets) {
        this.serialSets = serialSets;
    }

    public boolean canIRun(String target) {
        for (String serialSet : serialSets) {
            if (serialSet.contains(target)) {
                return true;
            }
        }
        return false;
    }
}
