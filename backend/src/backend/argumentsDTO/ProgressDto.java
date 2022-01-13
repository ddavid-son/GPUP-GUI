package backend.argumentsDTO;

import backend.Target;

public class ProgressDto {
    String UserData;
    String targetName;
    Target.TargetState targetState;

    public ProgressDto(String userData, String targetName, Target.TargetState targetState) {
        this.UserData = userData;
        this.targetName = targetName;
        this.targetState = targetState;
    }

    public ProgressDto(String targetName, Target.TargetState targetState) {
        this.targetName = targetName;
        this.targetState = targetState;
    }

    //getters
    public final String getUserData() {
        return UserData;
    }

    public final String getTargetName() {
        return targetName;
    }

    public final Target.TargetState getTargetState() {
        return targetState;
    }
}
