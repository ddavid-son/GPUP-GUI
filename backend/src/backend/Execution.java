package backend;

import backend.argumentsDTO.ProgressDto;
import backend.argumentsDTO.TaskArgs;
import backend.xmlhandler.GPUPDescriptor;
import backend.xmlhandler.GPUPTarget;
import backend.xmlhandler.GPUPTargetDependencies;
import dataTransferObjects.GraphTargetsTypeInfoDTO;
import dataTransferObjects.InfoAboutTargetDTO;
import dataTransferObjects.WhatIfDTO;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;


public class Execution implements Engine, Serializable {
    private Task task;
    private String workingDirectory;
    private GraphManager graphManager;
    GraphManager costumeGraphManager;
    private int maxParallelism;
    private final static String JAXB_XML_GENERATED_CLASSES_PATH = "backend.xmlhandler";
    private Consumer<accumulatorForWritingToFile> finishedTargetLog;
    private Consumer<ProgressDto> finishedTarget;

    //----------------------------------------- read/write state from/to file ----------------------------------------//
    @Override
    public void readObjectFromFile(String filePath) {
        checkIfFileExists(filePath + ".dat");
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath + ".dat"))) {
            this.workingDirectory = (String) in.readObject();
            this.graphManager = (GraphManager) in.readObject();
            this.task = (SimulationTask) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            throw new IllegalArgumentException("could not load instance from file");
        }
    }

    private void checkIfFileExists(String filePath) {
        if (filePath == null || filePath.isEmpty())
            throw new IllegalArgumentException("the path is invalid - it is either null or empty");

        File file = new File(filePath);

        if (!file.exists()) {
            throw new IllegalArgumentException("file does not exist");
        }
    }

    @Override
    public void writeObjectToFile(String path) {
        checkIfGraphIsLoaded();
        path = validatePath(path);
        path += ".dat";
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
            out.writeObject(workingDirectory);
            out.writeObject(graphManager);
            out.writeObject(task);
            out.flush();
        } catch (IOException e) {
            throw new IllegalArgumentException("could not write instance to file" + e.getMessage());
        }
    }

    private String validatePath(String path) {

        if (path == null || path.isEmpty())
            throw new IllegalArgumentException("the path is invalid - it is either null or empty");

        File file = new File(path);

        if (!file.isDirectory())
            file = file.getParentFile();

        if (file == null) {
            throw new IllegalArgumentException("the path is invalid");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException("the path does not exist and could not be created");
        }
        return path;
    }
    //----------------------------------------- read/write state from/to file ----------------------------------------//


    //------------------------------------------- find all paths and circle ------------------------------------------//
    @Override
    public Set<List<String>> findAllPathsBetweenTargets(String start, String end) {
        checkIfGraphIsLoaded();
        if (!graphManager.targetExists(start) || !graphManager.targetExists(end))
            throw new IllegalArgumentException("the target name that was entered does not exist");

        Set<String> visited = new HashSet<>();

        return graphManager.getAllPaths(start.toUpperCase(Locale.ROOT), end.toUpperCase(Locale.ROOT), visited);
    }

    @Override
    public List<String> findIfTargetIsInACircle(String targetName) {
        checkIfGraphIsLoaded();
        if (!graphManager.targetExists(targetName))
            throw new IllegalArgumentException("the target name that was entered does not exist");

        Set<String> visited = new HashSet<>();

        return graphManager.findCircle(targetName.toUpperCase(Locale.ROOT), visited);
    }
    //------------------------------------------- find all paths and circle ------------------------------------------//


    //---------------------------------------------- info about target -----------------------------------------------//
    @Override
    public InfoAboutTargetDTO getInfoAboutTarget(String targetName) {
        checkIfGraphIsLoaded();
        if (!graphManager.targetExists(targetName))
            throw new IllegalArgumentException("the target name that was entered does not exist");

        return new InfoAboutTargetDTO(
                targetName,
                graphManager.getTypeOf(targetName),
                graphManager.getTargetUserData(targetName),
                graphManager.getAllRelatedOn(targetName, GraphManager.RelationType.DEPENDS_ON),
                graphManager.getAllRelatedOn(targetName, GraphManager.RelationType.REQUIRED_FOR),
                graphManager.getDependsOnOfByName(targetName),
                graphManager.getRequiredForOfByName(targetName),
                graphManager.getTargetSerialSets(targetName)
        );
    }

    @Override
    public List<InfoAboutTargetDTO> getInfoAboutAllTargets() {
        List<InfoAboutTargetDTO> infoAboutTargets = new ArrayList<>();

        graphManager.getTargetArray().forEach(target -> {
            infoAboutTargets.add(getInfoAboutTarget(target.getName()));
        });

        return infoAboutTargets;
    }

    @Override
    public WhatIfDTO getWhatIf(String targetName, GraphManager.RelationType type) {
        return new WhatIfDTO(type, targetName, graphManager.getAllRelatedOn(targetName, type),
                type == GraphManager.RelationType.DEPENDS_ON ?
                        graphManager.getDependsOnOfByName(targetName) :
                        graphManager.getRequiredForOfByName(targetName));
    }


    //----------------------------------------------- info about info ------------------------------------------------//


    //--------------------------------------------------- run task --------------------------------------------------//
    //build graphManager from list of targets
    private void buildCostumeGraphManager(List<String> targets) {
        costumeGraphManager = new GraphManager(targets, graphManager);
    }

    @Override
    public void runTaskOnGraph(TaskArgs taskArgs, Consumer<accumulatorForWritingToFile> finishedTargetLog,
                               Consumer<ProgressDto> finishedTarget) {
        this.finishedTargetLog = finishedTargetLog;
        this.finishedTarget = finishedTarget;
        checkIfGraphIsLoaded();
        boolean createNewTask = true;

        if (!taskArgs.isIncremental())
            buildCostumeGraphManager(taskArgs.getTargetsSelectedForGraph());

        if (task == null && taskArgs.isIncremental()) {
            System.out.println("no previous run detected, task will start from scratch. ");
        } else if (task != null && task.getAllGraphHasBeenProcessed() && taskArgs.isIncremental()) {
            System.out.println("all graph has been processed, task will start from scratch. ");
        } else if (!taskArgs.isIncremental()) {
            //do nothing
        } else {
            task.getReadyForIncrementalRun(taskArgs);
            createNewTask = false;
        }

        if (createNewTask) {
            createTask(taskArgs);
        }

        //todo: need to remove this sout after testing
        task.run(System.out::println);
    }

    @Override
    public void pauseTask() {
        if (task != null)
            task.pauseTask();
    }

    @Override
    public void resumeTask() {
        if (task != null)
            task.resumeTask();
    }

    private void createTask(TaskArgs taskArgs) {
        switch (taskArgs.getTaskType()) {
            case SIMULATION:
                task = new SimulationTask(taskArgs, costumeGraphManager, workingDirectory,
                        costumeGraphManager.getSerialSetManager(), finishedTargetLog, finishedTarget,
                        maxParallelism);
                break;
            case COMPILATION:
                task = new CompilationTask(taskArgs, costumeGraphManager, workingDirectory,
                        costumeGraphManager.getSerialSetManager(), finishedTargetLog, finishedTarget,
                        maxParallelism);
                break;
        }
    }

    public void setNumberOfThreads(Integer value) {
        if (task != null)
            task.changeNumberOfThreads(value);
    }
    //--------------------------------------------------- run task ---------------------------------------------------//


    //---------------------------------------------- info about graph ------------------------------------------------//
    @Override
    public GraphTargetsTypeInfoDTO getGraphInfo() {
        checkIfGraphIsLoaded();

        GraphTargetsTypeInfoDTO graphTargetsTypeInfoDTO = new GraphTargetsTypeInfoDTO();
        graphManager.getTargetArray().forEach(target -> graphTargetsTypeInfoDTO.countTargetsByType(target.getType()));

        if (graphTargetsTypeInfoDTO.getTotalNumberOfTargets() != graphManager.getSize())
            throw new IllegalArgumentException(
                    "the number of targets in the graph is not equal to the number of targets counted");

        return graphTargetsTypeInfoDTO;
    }
    //---------------------------------------------- info about graph ------------------------------------------------//


    // ----------------------------------------------- Graph Viz bonus -----------------------------------------------//
    @Override
    public void makeGraphUsingGraphViz(String outPutPath, String filesNames) {

        try {
            GraphViz gv = new GraphViz(getDotExeFromEnvironmentVariables(), outPutPath);
            gv.addln(gv.start_graph());
            gv.addln(graphManager.getAllEdges());
            gv.addln(gv.end_graph());
            gv.increaseDpi(); // 106 dpi
            System.out.println(gv.getDotSource());
            String type = "png";
            //      String type = "dot";
            //      String type = "fig";    // open with xfig
            //      String type = "pdf";
            //      String type = "ps";
            //      String type = "svg";    // open with inkscape
            //      String type = "png";
            //      String type = "plain";

            String representationType = "dot";
            //		String representationType= "neato";
            //		String representationType= "fdp";
            //		String representationType= "sfdp";
            // 		String representationType= "twopi";
            // 		String representationType= "circo";

            File out = new File(outPutPath + File.separator + filesNames + "." + type);
            gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type, representationType), out);
            saveDotTextFile(outPutPath, filesNames, gv);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error - dot executable not found");
        }
    }

    private void saveDotTextFile(String outPutPath, String filesNames, GraphViz gv) {
        //write the dot file to txt file
        try {
            FileWriter fw = new FileWriter(outPutPath + File.separator + filesNames + ".viz");
            fw.write(gv.getDotSource());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Error - could not write the dot txt file");
        }
    }

    private String getDotExeFromEnvironmentVariables() {
        return Arrays.stream(System.getenv("Path").split(";"))
                .filter(path -> path.contains("Graphviz"))
                .map(path -> path.concat("\\dot.exe"))
                .findFirst()
                .orElse("C:\\Program Files\\Graphviz\\bin\\dot.exe");
    }
    // ----------------------------------------------- Graph Viz bonus -----------------------------------------------//

    //---------------------------------------------- load graph from xml ---------------------------------------------//
    @Override
    public void xmlFileLoadingHandler(String xmlFilePath) {
        handleError(fileValidityTests(xmlFilePath));

        GPUPDescriptor gpupDescriptor;
        try {
            InputStream inputStream = new FileInputStream(xmlFilePath);
            gpupDescriptor = deserializeFrom(inputStream);
        } catch (JAXBException | FileNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        workingDirectory = gpupDescriptor.getGPUPConfiguration().getGPUPWorkingDirectory();
        maxParallelism = gpupDescriptor.getGPUPConfiguration().getGPUPMaxParallelism();
        handleError(checkIfDataIsValid(gpupDescriptor));

        List<GPUPTarget> gpupTargets = gpupDescriptor.getGPUPTargets().getGPUPTarget();
        graphManager = new GraphManager(gpupTargets.size(), gpupTargets, gpupDescriptor.getGPUPSerialSets());
        this.task = null;
    }

    private void handleError(String errorMessage) {
        if (!errorMessage.isEmpty())
            throw new IllegalArgumentException(errorMessage);
    }

    private String fileValidityTests(String xmlFilePath) {

        File file = new File(xmlFilePath);
        if (!file.exists())
            return "The file path that was entered does not exist please enter a valid path";

        if (!xmlFilePath.endsWith(".xml"))
            return "The file path that was entered points to a non .xml file, please enter a path to a .xml file";

        return "";
    }

    private static GPUPDescriptor deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_GENERATED_CLASSES_PATH);
        Unmarshaller u = jc.createUnmarshaller();
        return (GPUPDescriptor) u.unmarshal(in);
    }
    //---------------------------------------------- load graph from xml ---------------------------------------------//


    //------------------------------------------------ ctor and utils ------------------------------------------------//
    public void checkIfGraphIsLoaded() {
        getGraphManagerOptional().orElseThrow(() ->
                new IllegalArgumentException("you need to load a valid XML file first"));
    }

    @Override
    public boolean isGraphAccessible() {
        return graphManager == null;
    }

    public Optional<GraphManager> getGraphManagerOptional() {

        return Optional.ofNullable(graphManager);
    }

    public String checkIfDataIsValid(GPUPDescriptor instance) {
        String errorMessage;
        List<GPUPTarget> targetList = instance.getGPUPTargets().getGPUPTarget();
        Map<String, TempTarget> string2TempTargetMap = new HashMap<>(targetList.size());

        for (GPUPTarget target : targetList) {
            if (string2TempTargetMap.containsKey(target.getName()))
                return "error: target: " + target.getName() + " is a duplicated target";

            string2TempTargetMap.put(target.getName(), new TempTarget(target.getName()));
        }

        errorMessage = buildTempGraphForValidation(targetList, string2TempTargetMap);

        errorMessage = validateTempGraphHelper(instance, string2TempTargetMap);

        return errorMessage;
    }

    private String validateTempGraphHelper(GPUPDescriptor instance, Map<String, TempTarget> string2TempTargetMap) {
        String errorMessage = checkForLoopsOfSize2(string2TempTargetMap);
        try {
            if (errorMessage.isEmpty())
                checkAllSerialSetsAreValid(instance, string2TempTargetMap);
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }

        return errorMessage;
    }

    private void checkAllSerialSetsAreValid(GPUPDescriptor instance, Map<String, TempTarget> string2TempTargetMap) {
        if (instance.getGPUPSerialSets() == null) {
            instance.setGPUPSerialSets(new GPUPDescriptor.GPUPSerialSets());
            return;
        }
        List<GPUPDescriptor.GPUPSerialSets.GPUPSerialSet> serialSetList = instance.getGPUPSerialSets().getGPUPSerialSet();
        serialSetList.forEach(s -> {
            Arrays.stream(s.getTargets()
                            .split(","))
                    //.collect(Collectors.toList())
                    .forEach(t -> {
                        if (!string2TempTargetMap.containsKey(t))
                            throw new IllegalArgumentException(
                                    "error: serial set: " + s.getName() +
                                            " contains a target that is not defined in the graph");
                    });
        });
    }

    private String checkForLoopsOfSize2(Map<String, TempTarget> string2TempTargetMap) {
        for (TempTarget tempTarget : string2TempTargetMap.values()) {
            for (String neighbourName : tempTarget.neighboursNames) {
                if (string2TempTargetMap.get(neighbourName).neighboursNames.contains(tempTarget.name)) {
                    System.out.println("error: target: " + tempTarget.name + " has a loop of size 2");
                    return "error: there is a circle of size 2 between " + tempTarget.name + " and " + neighbourName;
                }
            }
        }

        return "";
    }

    private String tempGraphBuilder(Map<String, TempTarget> string2TempTargetMap, GPUPTarget target) {
        for (GPUPTargetDependencies.GPUGDependency edge : target.getGPUPTargetDependencies().getGPUGDependency()) {

            handleError(string2TempTargetMap.containsKey(edge.getValue()) ?
                    "" : "tempGraphBuilder: key isn't unique");

            if (edge.getType().equals("dependsOn")) {
                string2TempTargetMap.get(target.getName()).neighboursNames.add(edge.getValue());
            } else {
                string2TempTargetMap.get(edge.getValue()).neighboursNames.add(target.getName());
            }
        }

        return "";
    }

    private String buildTempGraphForValidation
            (List<GPUPTarget> targetList, Map<String, TempTarget> string2TempTargetMap) {
        for (GPUPTarget target : targetList) {
            if (target.getGPUPTargetDependencies() != null) {
                String errorMessage = tempGraphBuilder(string2TempTargetMap, target);
                if (!errorMessage.isEmpty()) return errorMessage;
            }
        }

        return "";
    }

    @Override
    public boolean incrementalAvailable() {
        if (task != null)
            return !task.getAllGraphHasBeenProcessed();
        return false;
    }

    @Override
    public int getMaxThreadCount() {
        return this.maxParallelism;
    }

    @Override
    public List<String> getAllTargetNames() {

        return graphManager.getAllNamesOfTargets();
    }

    @Override
    public List<String> getSerialSetList() {
        return new ArrayList<>(graphManager.getSerialSetManager().getSerialSetMap().keySet());
    }

    @Override
    public List<String> getSerialSetTarget(String serialSetName) {
        return graphManager.getSerialSetManager().getSerialSetMap().get(serialSetName).getTargetInSerialSet();
    }

    @Override
    public List<String> getInfoAboutTargetInExecution(String targetName, Target.TargetState targetState) {
        List<String> info = new ArrayList<>();
        info.add(targetName);//0
        info.add(costumeGraphManager.getTypeOf(targetName).toString());//1
        info.add(String.join(",", graphManager.getTargetSerialSets(targetName)));//2
        info.add(targetState.toString());//3
        switch (targetState) {
            case WAITING:
                long waiting = System.currentTimeMillis() - task.getWaitingStartTime(targetName);
                info.add(TimeUtil.ltd(waiting));
                break;
            case IN_PROCESS:
                long processing = System.currentTimeMillis() - task.getProcessStartTime(targetName);
                info.add(TimeUtil.ltd(processing));
                break;
            case SKIPPED:
            case FROZEN:
                info.add(String.join(",", costumeGraphManager.getDependsOnOfByName(targetName)));
                break;
            case SUCCESS:
            case WARNING:
            case FAILURE:
                info.add(targetState.toString());
                break;
        }
        return info;
    }
    //------------------------------------------------ ctor and utils ------------------------------------------------//

    private static class TempTarget {
        String name;
        List<String> neighboursNames;

        public TempTarget(String name) {
            this.name = name;
            this.neighboursNames = new ArrayList<>();
        }
    }
}
