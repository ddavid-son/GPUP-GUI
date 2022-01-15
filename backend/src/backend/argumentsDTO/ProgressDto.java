package backend.argumentsDTO;

import backend.Target;

public class ProgressDto {
    String UserData;
    String targetName;
    Target.TargetState targetState;
    Target.TargetType targetType;
    long waiting;
    long processing;
    String skippedOrFailed;
    String waitsTo;

    public ProgressDto() {

    }

    public ProgressDto(String userData, String targetName, Target.TargetState targetState) {
        this.UserData = userData;
        this.targetName = targetName;
        this.targetState = targetState;
    }

    public ProgressDto(String userData, String targetName, Target.TargetState targetState, long waiting,
                       long processing, String skippedOrFailed, String waitsTo) {
        this.UserData = userData;
        this.targetName = targetName;
        this.targetState = targetState;
        this.waiting = waiting;
        this.processing = processing;
        this.skippedOrFailed = skippedOrFailed;
        this.waitsTo = waitsTo;
    }

    public ProgressDto(String targetName, Target.TargetState targetState) {
        this.targetName = targetName;
        this.targetState = targetState;
    }
    // setters


    public void setUserData(String userData) {
        UserData = userData;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void setTargetState(Target.TargetState targetState) {
        this.targetState = targetState;
    }

    public void setTargetType(Target.TargetType targetType) {
        this.targetType = targetType;
    }

    public void setWaiting(long waiting) {
        this.waiting = waiting;
    }

    public void setProcessing(long processing) {
        this.processing = processing;
    }

    public void setSkippedOrFailed(String skippedOrFailed) {
        this.skippedOrFailed = skippedOrFailed;
    }

    public void setWaitsTo(String waitsTo) {
        this.waitsTo = waitsTo;
    }

    //getters
    public Target.TargetType getTargetType() {
        return targetType;
    }

    public long getWaiting() {
        return waiting;
    }

    public long getProcessing() {
        return processing;
    }

    public String getSkippedOrFailed() {
        return skippedOrFailed;
    }

    public String getWaitsTo() {
        return waitsTo;
    }

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
