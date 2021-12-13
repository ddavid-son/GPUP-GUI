package dataTransferObjects;

import backend.Target;

import java.util.List;

public class InfoAboutTargetDTO {
    private final String userData;
    private final String targetName;
    private final List<String> dependsOnNames;
    private final Target.TargetType targetType;
    private final List<String> requiredForNames;

    public InfoAboutTargetDTO(String targetName, Target.TargetType targetType, String userData,
                              List<String> dependsOnNames, List<String> requiredForNames) {
        this.targetName = targetName;
        this.targetType = targetType;
        this.userData = userData;
        this.dependsOnNames = dependsOnNames;
        this.requiredForNames = requiredForNames;
    }

    @Override
    public String toString() {
        return "Target name = " + targetName + '\n' +
                " Target type = " + targetType + '\n' +
                " User data = " + (userData.isEmpty() ? "no user data" : userData) + '\n' +
                " Depends on = " + (dependsOnNames.isEmpty() ? "depends on no one  " : dependsOnNames) + '\n' +
                " Required for = " + (requiredForNames.isEmpty() ? "required for no one " : requiredForNames);
    }

    //getters
    public String getUserData() {
        return userData;
    }

    public String getTargetName() {
        return targetName;
    }

    public List<String> getDependsOnNames() {
        return dependsOnNames;
    }

    public List<String> getRequiredForNames() {
        return requiredForNames;
    }

    public Target.TargetType getTargetType() {
        return targetType;
    }
}
