package dataTransferObjects;

import backend.Target;
import javafx.scene.control.CheckBox;

import java.util.List;

public class InfoAboutTargetDTO {
    public final String userData;
    public final String targetName;
    public final List<String> dependsOnNames;
    public final int dependsOnCount;
    public final Target.TargetType targetType;
    public boolean isSelected = false;
    public CheckBox CheckBox;

    public CheckBox getCheckBox() {
        return CheckBox;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }


    public final List<String> requiredForNames;
    public final int requiredForCount;

    public final List<String> directRequiredByName;
    public final int directRequiredByCount;

    public final List<String> directDependsOnByName;
    public final int directDependsOnByCount;

    public boolean getIsSelected() {
        return isSelected;
    }

    public String getUserData() {
        return userData;
    }

    public String getTargetName() {
        return targetName;
    }

    public List<String> getDependsOnNames() {
        return dependsOnNames;
    }

    public int getDependsOnCount() {
        return dependsOnCount;
    }

    public Target.TargetType getTargetType() {
        return targetType;
    }

    public List<String> getRequiredForNames() {
        return requiredForNames;
    }

    public int getRequiredForCount() {
        return requiredForCount;
    }

    public List<String> getDirectRequiredByName() {
        return directRequiredByName;
    }

    public int getDirectRequiredByCount() {
        return directRequiredByCount;
    }

    public List<String> getDirectDependsOnByName() {
        return directDependsOnByName;
    }

    public int getDirectDependsOnByCount() {
        return directDependsOnByCount;
    }

    public InfoAboutTargetDTO(String targetName, Target.TargetType targetType, String userData,
                              List<String> allDependsOnNames, List<String> allRequiredForNames,
                              List<String> directDependsOnNames, List<String> directRequiredForNames) {
        this.targetName = targetName;
        this.targetType = targetType;
        this.userData = userData;

        this.dependsOnNames = allDependsOnNames;
        this.dependsOnCount = dependsOnNames.size();

        this.requiredForNames = allRequiredForNames;
        this.requiredForCount = requiredForNames.size();

        this.directRequiredByName = directRequiredForNames;
        this.directRequiredByCount = directRequiredByName.size();

        this.directDependsOnByName = directDependsOnNames;
        this.directDependsOnByCount = directDependsOnByName.size();
    }

    @Override
    public String toString() {
        return "Target name = " + targetName + '\n' +
                " Target type = " + targetType + '\n' +
                " User data = " + (userData.isEmpty() ? "no user data" : userData) + '\n' +
                " Depends on = " + (dependsOnNames.isEmpty() ? "depends on no one  " : dependsOnNames) + '\n' +
                " Required for = " + (requiredForNames.isEmpty() ? "required for no one " : requiredForNames);
    }

}
