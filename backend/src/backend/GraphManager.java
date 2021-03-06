package backend;

import backend.serialSets.SerialSet;
import backend.serialSets.SerialSetManger;
import backend.xmlhandler.GPUPDescriptor;
import backend.xmlhandler.GPUPTarget;
import backend.xmlhandler.GPUPTargetDependencies;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class GraphManager implements Serializable {
    private int size;
    private boolean[][] adjMatrix;
    private List<Target> targetArray = new ArrayList<>();
    private Map<String, Integer> targetsMap = new HashMap<>();
    private SerialSetManger serialSetManger = new SerialSetManger();

    public enum RelationType {
        DEPENDS_ON,
        REQUIRED_FOR
    }

    // ---------------------------------------- ctor and Graph initialization ----------------------------------------//
    public GraphManager(int numVertices, List<GPUPTarget> targetList, GPUPDescriptor.GPUPSerialSets gpupSerialSets) {
        this.size = numVertices;
        adjMatrix = new boolean[numVertices][numVertices];

        for (boolean[] row : adjMatrix) {
            Arrays.fill(row, false);
        }

        fillMapAndListWithTargets(targetList);
        setDependenciesOfTargets(targetList);
        setSerialSets(gpupSerialSets);
    }

    private void setSerialSets(GPUPDescriptor.GPUPSerialSets gpupSerialSets) {
        List<String> targetsInSerialSet = new ArrayList<>();
        for (GPUPDescriptor.GPUPSerialSets.GPUPSerialSet ss : gpupSerialSets.getGPUPSerialSet()) {
            Arrays.stream(ss.getTargets().split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .forEach(targetName -> {
                        targetArray.get(targetsMap.get(targetName))
                                .addSerialSetsName(ss.getName());
                        targetsInSerialSet.add(targetName);
                    });
            serialSetManger.addSerialSet(ss.getName(), new SerialSet(ss.getName(), targetsInSerialSet));
            targetsInSerialSet.clear();
        }

        serialSetManger.createMapFromTarget2SerialSetNames(targetArray);
    }

    private void fillMapAndListWithTargets(List<GPUPTarget> targetList) {
        for (GPUPTarget target : targetList) {
            Target newTarget = new Target(target.getName().toUpperCase(Locale.ROOT), target.getGPUPUserData());
            targetArray.add(newTarget);
            targetsMap.put(target.getName().toUpperCase(Locale.ROOT), targetArray.size() - 1);
        }
    }

    private void setDependenciesOfTargets(List<GPUPTarget> targetList) {
        for (GPUPTarget target : targetList) {
            for (GPUPTargetDependencies.GPUGDependency edge : getGpupDependenciesOptional(target).getGPUGDependency()) {
                insertDependenciesToArrayAndMatrix(target, edge);
            }
        }
    }

    private Target getTargetByName(String targetName) {
        if (!targetExists(targetName)) {
            throw new IllegalArgumentException("Target with name " + targetName + " does not exist");
        }
        return targetArray.get(targetsMap.get(targetName));
    }

    private Target getTargetByName(Object target) {
        Class<?> targetClass = target.getClass();
        try {
            switch (targetClass.getSimpleName()) {
                case "GPUPTarget":
                    return getTargetByName(((GPUPTarget) target).getName());
                case "GPUGDependency":
                    return getTargetByName(((GPUPTargetDependencies.GPUGDependency) target).getValue());
                case "String":
                    return getTargetByName((String) target);
                default:
                    throw new IllegalArgumentException("from: default case");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "getTargetByName: name provided dose not exist in the graph.\n" + e.getMessage());
        }
    }

    private void addEdgeToMatrix(int source, int destination) {
        if (source < 0 || source > size || destination < 0 || destination > size)
            throw new ArrayIndexOutOfBoundsException("removeEdgeFromMatrix: source or destination index is out of bounds");
        if (!adjMatrix[source][destination])
            adjMatrix[source][destination] = true;
    }

    private GPUPTargetDependencies getGpupDependenciesOptional(GPUPTarget target) {
        return getGPUPTargetDependenciesOptional(
                target.getGPUPTargetDependencies())
                .orElseGet(GPUPTargetDependencies::new
                );
    }

    public Optional<GPUPTargetDependencies> getGPUPTargetDependenciesOptional(GPUPTargetDependencies d) {
        return Optional.ofNullable(d);
    }

    private void insertDependenciesToArrayAndMatrix(GPUPTarget target, GPUPTargetDependencies.GPUGDependency edge) {
        if (edge.getType().equals("dependsOn")) {
            addEdgeToMatrix(targetsMap.get(target.getName()), targetsMap.get(edge.getValue()));
            getTargetByName(target).addDependsOn(getTargetByName(edge.getValue()));
            getTargetByName(edge.getValue()).addRequiredFor(getTargetByName(target));
        } else/* if (edge.getType().equals("requiredFor")) */ {
            addEdgeToMatrix(targetsMap.get(edge.getValue()), targetsMap.get(target.getName()));
            getTargetByName(target).addRequiredFor(getTargetByName(edge.getValue()));
            getTargetByName(edge.getValue()).addDependsOn(getTargetByName(target));
        }
    }
    // ---------------------------------------- ctor and Graph initialization ----------------------------------------//


    // --------------------------------------------- copy Ctor and utils ---------------------------------------------//
    public GraphManager(List<String> targetList, GraphManager originalGraph) {
        this.size = targetList.size();
        this.serialSetManger = originalGraph.serialSetManger;
        loadListAndMapWithNewGraph(targetList, originalGraph);
        filterOutAllReferencesToNonExcitingTargets();   // filter out targets that are not in the targetList
        //handleDependenciesReferencingOriginalTargets(); // replace all targets in dependencies with the new ones
        setMatrixFromOriginalGraph(originalGraph);
    }

    private void loadListAndMapWithNewGraph(List<String> targetList, GraphManager originalGraph) {
        for (String targetName : targetList) {
            this.targetArray.add(new Target(originalGraph.getTargetByName(targetName)));
            this.targetsMap.put(targetName, targetArray.size() - 1);
        }
    }

    private void filterOutAllReferencesToNonExcitingTargets() {
        List<Target> t;
        for (int i = 0; i < targetArray.size(); i++) {
            t = new ArrayList<>();
            removeUnnecessaryTargetsFromList(t, i);
            t = new ArrayList<>();
            removeUnnecessaryTargetsFromDependsOnList(t, i);
            updateTypeAndStateOfTarget(i);
        }
    }

    private void updateTypeAndStateOfTarget(int i) {
        if (targetArray.get(i).getDependsOnNames().size() == 0 &&
                targetArray.get(i).getRequiredFor().size() == 0)
            targetArray.get(i).setType(Target.TargetType.INDEPENDENT);

        else if (targetArray.get(i).getDependsOnNames().size() != 0 &&
                targetArray.get(i).getRequiredFor().size() != 0)
            targetArray.get(i).setType(Target.TargetType.MIDDLE);

        else if (targetArray.get(i).getDependsOnNames().size() == 0)
            targetArray.get(i).setType(Target.TargetType.LEAF);

        else
            targetArray.get(i).setType(Target.TargetType.ROOT);

        if (targetArray.get(i).getType().equals(Target.TargetType.INDEPENDENT) ||
                targetArray.get(i).getType().equals(Target.TargetType.LEAF))
            targetArray.get(i).setState(Target.TargetState.WAITING);
        else
            targetArray.get(i).setState(Target.TargetState.FROZEN);
    }

    private void removeUnnecessaryTargetsFromDependsOnList(List<Target> t, int i) {
        targetArray.get(i).getDependsOnList().forEach(target -> {
            if (targetsMap.containsKey(target.getName())) {
                t.add(targetArray.get(targetsMap.get(target.getName())));
            }
        });
        targetArray.get(i).setDependsOnList(t);
    }

    private void removeUnnecessaryTargetsFromList(List<Target> t, int i) {
        targetArray.get(i).getRequiredFor().forEach(target -> {
            if (targetsMap.containsKey(target.getName())) {
                t.add(targetArray.get(targetsMap.get(target.getName())));
            }
        });
        this.targetArray.get(i).setRequiredFor(t);
    }

    private void handleDependenciesReferencingOriginalTargets() {
        List<Target> tempDependencyList;
        for (Target target : targetArray) {
            for (int i = 0; i < target.getDependsOnList().size(); i++) {
                tempDependencyList = target.getDependsOnList().stream()
                        .map(this::getTargetByName)
                        .collect(Collectors.toList());
                target.setDependsOnList(tempDependencyList);

                tempDependencyList = target.getRequiredFor().stream()
                        .map(this::getTargetByName)
                        .collect(Collectors.toList());
                target.setRequiredFor(tempDependencyList);
            }
        }
    }

    private void setMatrixFromOriginalGraph(GraphManager originalGraph) {
        adjMatrix = new boolean[size][size];

        for (boolean[] row : adjMatrix) {
            Arrays.fill(row, false);
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (originalGraph.adjMatrix[i][j] &&
                        targetsMap.containsKey(originalGraph.targetArray.get(i).getName())) {
                    addEdgeToMatrix(i, j);
                }
            }
        }
    }

    public String getAllEdges() {
        List<String> edges = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        targetArray.forEach(target -> {
            sb.append(target.getName())
                    .append(target.getDependsOnNames().isEmpty() ? "" : " -> ")
                    .append(String.join(",", target.getDependsOnNames()))
                    .append(";\n");
        });
        return sb.toString();
    }
    // --------------------------------------------- copy Ctor and utils ---------------------------------------------//


    // ----------------------------- for serialization - dont delete or comment out!!! -------------------------------//
    public void writeToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("GraphManager.dat"))) {
            oos.writeObject(size);
            oos.writeObject(adjMatrix);
            oos.writeObject(targetArray);
            oos.writeObject(targetsMap);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write to file - GraphManager");
        }
    }

    public void readFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("GraphManager.dat"))) {
            size = (int) ois.readObject();
            adjMatrix = (boolean[][]) ois.readObject();
            targetArray = (ArrayList<Target>) ois.readObject();
            targetsMap = (HashMap<String, Integer>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not read from file - GraphManager");
        }
    }
    // ----------------------------- for serialization - dont delete or comment out!!! -------------------------------//


    // -------------------------------------------------- all paths --------------------------------------------------//
    public Set<List<String>> getAllPaths(String start, String end, Set<String> visited) {
        Set<List<String>> allPaths = new HashSet<>();
        List<String> currentPath = new LinkedList<>();

        if (start.equals(end)) {
            currentPath.add(start);
            allPaths.add(currentPath);
            return allPaths;
        }

        visited.add(start);
        allPathsBuilder(start, end, visited, allPaths);
        return allPaths;
    }

    private void allPathsBuilder(String start, String end, Set<String> visited, Set<List<String>> allPaths) {
        for (String neighbor : getDependsOnOfByName(start)) {
            if (!visited.contains(neighbor)) {
                Set<List<String>> newPaths = getAllPaths(neighbor, end, visited);
                for (List<String> p : newPaths) {
                    p.add(0, start);
                    allPaths.add(p);
                }
            }
        }
    }
    // -------------------------------------------------- all paths --------------------------------------------------//


    // ------------------------------------------------- find circle -------------------------------------------------//
    public List<String> findCircle(String targetForCircle, Set<String> visited) {
        List<String> targetNeighbours = getDependsOnOfByName(targetForCircle);
        Set<List<String>> allCircles;
        for (String targetNeighbour : targetNeighbours) {
            allCircles = getAllPaths(targetNeighbour, targetForCircle, visited);
            for (List<String> circlePath : allCircles) {
                if (circlePath.size() > 1) {
                    return circlePath;
                }
            }
            visited.clear();
        }

        return new ArrayList<>();
    }
    // ------------------------------------------------- find circle -------------------------------------------------//


    // ---------------------------------------------- getters and utils ----------------------------------------------//
    public List<String> getAllRelatedOn(String targetName, RelationType relationType) {
        List<String> allRelated = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        allRelated.add(targetName);
        visited.add(targetName);
        allRelated.addAll(getAllRelatedOnDFSWorker(targetName, visited, relationType));
        return allRelated.subList(1, allRelated.size());
    }

    public List<String> getAllRelatedOnDFSWorker(String targetName, Set<String> visited, RelationType relationType) {
        List<String> allAncestor = new ArrayList<>();
        List<String> targetNeighbours = relationType.equals(RelationType.REQUIRED_FOR) ?
                getRequiredForOfByName(targetName) : getDependsOnOfByName(targetName);

        for (String targetNeighbour : targetNeighbours) {
            if (!visited.contains(targetNeighbour)) {
                allAncestor.add(targetNeighbour);
                visited.add(targetNeighbour);
                allAncestor.addAll(getAllRelatedOnDFSWorker(targetNeighbour, visited, relationType));
            }
        }

        return allAncestor;
    }

    public List<Target> getTargetArray() {
        if (targetArray == null)
            targetArray = new ArrayList<>();
        return targetArray;
    }

    public List<String> getTargetSerialSets(String targetName) {
        if (targetExists(targetName)) {
            return getTargetByName(targetName).getSerialSetsName();
        }

        return new ArrayList<>();
    }

    public List<String> getAllNamesOfTargets() {
        return getTargetArray().stream().map(Target::getName).collect(Collectors.toList());
    }

    public int getSize() {
        return this.size;
    }

    public List<String> getDependsOnOfByName(String targetName) {
        return getTargetByName(targetName).getDependsOnNames();
    }

    public List<String> getRequiredForOfByName(String targetName) {
        return getTargetByName(targetName).getRequiredForNames();
    }

    public Target.TargetType getTypeOf(String targetName) {
        return getTargetByName(targetName).getType();
    }

    public String getTargetUserData(String targetName) {
        return getTargetByName(targetName).getUserData();
    }

    public SerialSetManger getSerialSetManager() {
        return serialSetManger;
    }

    public boolean targetExists(String name) {
        if (name != null)
            return targetsMap.containsKey(name.toUpperCase(Locale.ROOT));
        return false;
    }
    // ---------------------------------------------- getters and utils ----------------------------------------------//


    // -------------------------------------------- not in use currently ---------------------------------------------//
    public List<Integer> getRow(int rowNumber) {
        List<Integer> indexesOfMyNeighbours = new ArrayList<>();
        if (rowNumber < 0 || rowNumber >= size)
            throw new ArrayIndexOutOfBoundsException("getRow: rowNumber out of bounds");

        for (int i = 0; i < size; i++) {
            if (adjMatrix[rowNumber][i]) {
                indexesOfMyNeighbours.add(i);
            }
        }

        return indexesOfMyNeighbours;
    }

    private List<Integer> getColumn(int columnNumber) {
        List<Integer> indexesOfMyNeighbours = new ArrayList<>();
        if (columnNumber < 0 || columnNumber >= size)
            throw new ArrayIndexOutOfBoundsException("getColumn: columnNumber out of bounds");

        for (int i = 0; i < size; i++) {
            if (adjMatrix[i][columnNumber]) {
                indexesOfMyNeighbours.add(i);
            }
        }

        return indexesOfMyNeighbours;
    }

    private void removeEdgeFromMatrix(int source, int destination) {
        if (source < 0 || source > size || destination < 0 || destination > size)
            throw new ArrayIndexOutOfBoundsException("removeEdgeFromMatrix: source or destination index is out of bounds");
        adjMatrix[source][destination] = false;
    }

    public List<Target> getRequiredForOf(String targetName) {
        return getTargetByName(targetName).getRequiredFor();
    }

    private List<Target> getDependsOnOf(String targetName) {
        return getTargetByName(targetName).getDependsOnList();
    }
    // -------------------------------------------- not in use currently ---------------------------------------------//


    // simple representation of the matrix;
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < size; i++) {
            s.append(i).append(": ");
            for (boolean j : adjMatrix[i]) {
                s.append(j ? 1 : 0).append(" ");
            }
            s.append("\n");
        }

        return s.toString();
    }
}
