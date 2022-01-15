package backend;

import backend.argumentsDTO.ProgressDto;
import backend.argumentsDTO.TaskArgs;
import dataTransferObjects.GraphTargetsTypeInfoDTO;
import dataTransferObjects.InfoAboutTargetDTO;
import dataTransferObjects.WhatIfDTO;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;


public interface Engine extends Serializable {
    void pauseTask();

    void resumeTask();

    int getMaxThreadCount();

    boolean isGraphAccessible();

    boolean incrementalAvailable();

    List<String> getSerialSetList();

    List<String> getAllTargetNames();

    void writeObjectToFile(String path);

    void setNumberOfThreads(Integer value);

    GraphTargetsTypeInfoDTO getGraphInfo();

    void readObjectFromFile(String filePath);

    void xmlFileLoadingHandler(String xmlFilePath);

    List<InfoAboutTargetDTO> getInfoAboutAllTargets();

    List<String> getSerialSetTarget(String serialSetName);

    List<String> findIfTargetIsInACircle(String targetName);

    InfoAboutTargetDTO getInfoAboutTarget(String targetName);

    void makeGraphUsingGraphViz(String outPutPath, String filesNames);

    Set<List<String>> findAllPathsBetweenTargets(String start, String end);

    WhatIfDTO getWhatIf(String targetName, GraphManager.RelationType type);

    List<String> getInfoAboutTargetInExecution(String targetName, Target.TargetState targetState);

    void runTaskOnGraph(TaskArgs taskArgs, Consumer<accumulatorForWritingToFile> finishedTargetLog,
                        Consumer<ProgressDto> finishedTarget);
}
