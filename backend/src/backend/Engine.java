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

    void writeObjectToFile(String path);

    GraphTargetsTypeInfoDTO getGraphInfo();

    void readObjectFromFile(String filePath);

    //void makeGraphUsingGraphViz();

    void makeGraphUsingGraphViz(String outPutPath, String filesNames);

    void xmlFileLoadingHandler(String xmlFilePath);

    List<String> findIfTargetIsInACircle(String targetName);

    InfoAboutTargetDTO getInfoAboutTarget(String targetName);

    Set<List<String>> findAllPathsBetweenTargets(String start, String end);

    List<InfoAboutTargetDTO> getInfoAboutAllTargets();

    WhatIfDTO getWhatIf(String targetName, GraphManager.RelationType type);

    void runTaskOnGraph(TaskArgs taskArgs, Consumer<accumulatorForWritingToFile> finishedTargetLog,
                        Consumer<ProgressDto> finishedTarget);

    boolean isGraphAccessible();

    boolean incrementalAvailable();

    List<String> getAllTargetNames();

    int getMaxThreadCount();
}
