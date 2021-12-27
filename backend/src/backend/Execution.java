package backend;

import backend.xmlhandler.GPUPDescriptor;
import backend.xmlhandler.GPUPTarget;
import backend.xmlhandler.GPUPTargetDependencies;
import dataTransferObjects.GraphTargetsTypeInfoDTO;
import dataTransferObjects.InfoAboutTargetDTO;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Execution implements Engine, Serializable {
    private Task task;
    private String workingDirectory;
    private GraphManager graphManager;
    private final static String JAXB_XML_GENERATED_CLASSES_PATH = "backend.xmlhandler";

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
                graphManager.getRequiredForOfByName(targetName)
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


    //----------------------------------------------- info about info ------------------------------------------------//


    //--------------------------------------------------- run task --------------------------------------------------//
    @Override
    public void runTaskOnGraph(boolean isRandom, int msToRun, double successRate, double successfulWithWarningRate,
                               boolean isIncremental, Consumer<String> print, boolean isSimulation) {

        checkIfGraphIsLoaded();

        if (task == null && isIncremental) {
            print.accept("no previous run detected, task will start from scratch. ");
            task = new SimulationTask(msToRun, isRandom, successRate,
                    successfulWithWarningRate, graphManager, workingDirectory);
        } else if (task != null && task.getAllGraphHasBeenProcessed() && isIncremental) {
            print.accept("all graph has been processed, task will start from scratch. ");
            task = new SimulationTask(msToRun, isRandom, successRate,
                    successfulWithWarningRate, graphManager, workingDirectory);
        } else if (!isIncremental) {
            task = new SimulationTask(msToRun, isRandom, successRate,
                    successfulWithWarningRate, graphManager, workingDirectory);
        } else {
            task.getReadyForIncrementalRun(isRandom, msToRun, successRate, successfulWithWarningRate);
        }

        task.run(print);
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
        handleError(checkIfDataIsValid(gpupDescriptor));

        List<GPUPTarget> gpupTargets = gpupDescriptor.getGPUPTargets().getGPUPTarget();
        graphManager = new GraphManager(gpupTargets.size(), gpupTargets);
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
        if (instance.getGPUPSerialSets() == null) return;
        List<GPUPDescriptor.GPUPSerialSets.GPUPSerialSet> serialSetList = instance.getGPUPSerialSets().getGPUPSerialSet();
        serialSetList.forEach(s -> {
            Arrays.stream(s.getTargets()
                            .split(","))
                    .collect(Collectors.toList())
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
                if (string2TempTargetMap.get(neighbourName).neighboursNames.contains(tempTarget.name))
                    return "error: there is a circle of size 2 between " + tempTarget.name + " and " + neighbourName;
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
    public List<String> getAllTargetNames() {
        return graphManager.getAllNamesOfTargets();
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
