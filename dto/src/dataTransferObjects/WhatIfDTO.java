package dataTransferObjects;

import backend.GraphManager;

import java.util.ArrayList;
import java.util.List;

public class WhatIfDTO {
    GraphManager.RelationType targetType = GraphManager.RelationType.DEPENDS_ON;
    String targetName;
    List<String> allRelatedDependsOn = new ArrayList<>();
    List<String> allImmediateDependsOn = new ArrayList<>();
    List<String> allRelatedRequiredFor = new ArrayList<>();
    List<String> allImmediateRequiredFor = new ArrayList<>();

    public WhatIfDTO(GraphManager.RelationType targetType, String targetName,
                     List<String> allRelated, List<String> allImmediate) {
        this.targetType = targetType;
        this.targetName = targetName;
        switch (targetType) {
            case DEPENDS_ON:
                this.allRelatedDependsOn = allRelated;
                this.allImmediateDependsOn = allImmediate;
                break;
            case REQUIRED_FOR:
                this.allRelatedRequiredFor = allRelated;
                this.allImmediateRequiredFor = allImmediate;
                break;
        }
    }

    public WhatIfDTO() {
    }

    public GraphManager.RelationType getTargetType() {
        return targetType;
    }

    public String getTargetName() {
        return targetName;
    }

    public List<String> getAllRelated() {
        switch (targetType) {
            case DEPENDS_ON:
                return allRelatedDependsOn;
            case REQUIRED_FOR:
                return allRelatedRequiredFor;
        }
        return new ArrayList<>(); // should never happen
    }

    public List<String> getAllImmediate() {
        switch (targetType) {
            case DEPENDS_ON:
                return allImmediateDependsOn;
            case REQUIRED_FOR:
                return allImmediateRequiredFor;
        }
        return new ArrayList<>(); // should never happen
    }
}
