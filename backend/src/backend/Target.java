package backend;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Target implements Serializable {
    private String name;
    private String userData;
    private TargetState state = TargetState.WAITING;
    private TargetType type = TargetType.INDEPENDENT;
    private List<Target> requiredFor = new LinkedList<>();
    private List<Target> dependsOnList = new LinkedList<>();
    private final List<String> serialSetsName = new ArrayList<>();

    //ctor
    public Target(String name, String userData) {
        this.name = name;
        this.userData = userData == null ? "" : userData;
    }


    //for serialization - dont delete or comment!!
    public void writeToFile() {
        //write to File_NAME
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("target" + name + ".dat"))) {
            oos.writeObject(dependsOnList);
            oos.writeObject(requiredFor);
            oos.writeObject(state);
            oos.writeObject(type);
            oos.writeObject(userData);
            oos.writeObject(name);
        } catch (IOException e) {
            throw new IllegalArgumentException("could not write Object to file - Target");
        }
    }

    public void readFromFile() {
        //read from File_NAME
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("target" + name + ".dat"))) {
            dependsOnList = (LinkedList<Target>) ois.readObject();
            requiredFor = (LinkedList<Target>) ois.readObject();
            state = (TargetState) ois.readObject();
            type = (TargetType) ois.readObject();
            userData = (String) ois.readObject();
            name = (String) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("could not read Object from file - Target");
        }
    }


    //required for
    public List<String> getRequiredForNames() {

        return requiredFor
                .stream()
                .map(Target::getName)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public List<Target> getRequiredFor() {
        return requiredFor;
    }

    public void setRequiredFor(List<Target> requiredFor) { // maybe will be helpful in loading from file
        this.requiredFor = requiredFor;
    }

    public void addRequiredFor(Target target) {
        if (!requiredFor.contains(target)) {
            requiredFor.add(target);
        }
        if (type == TargetType.INDEPENDENT) {
            type = TargetType.LEAF;
        }
        if (type == TargetType.ROOT) {
            type = TargetType.MIDDLE;
        }
    }


    // depends on
    public List<String> getDependsOnNames() {
        return dependsOnList
                .stream()
                .map(Target::getName)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public List<Target> getDependsOnList() {
        return dependsOnList;
    }

    public void setDependsOnList(List<Target> dependsOnList) { // maybe will be helpful in loading from file
        this.dependsOnList = dependsOnList;
    }

    public void addDependsOn(Target target) {
        if (!dependsOnList.contains(target)) {
            dependsOnList.add(target);
        }
        if (type == TargetType.INDEPENDENT) {
            type = TargetType.ROOT;
        }
        if (type == TargetType.LEAF) {
            type = TargetType.MIDDLE;
        }
    }

    //copy constructor
    public Target(Target OGTarget) {
        this.name = OGTarget.name;
        this.userData = OGTarget.userData;
        this.state = OGTarget.state;
        this.type = OGTarget.type;
        this.requiredFor = OGTarget.requiredFor;
        this.dependsOnList = OGTarget.dependsOnList;
    }

    //getters and setters

    public final List<String> getSerialSetsName() {
        return serialSetsName;
    }

    public void addSerialSetsName(String ssName) {
        if (!serialSetsName.contains(ssName)) {
            serialSetsName.add(ssName);
        }
    }

    public final List<String> getSerialSetNames() {
        return serialSetsName;
    }

    public String getName() {
        return name;
    }

    public String getUserData() {
        return userData;
    }

    public TargetState getState() {
        return state;
    }

    public TargetType getType() {
        return type;
    }

    protected void setType(TargetType targetType) {
        this.type = targetType;
    }

    protected void setState(TargetState targetState) {
        this.state = targetState;
    }

    //enums
    public enum TargetState {WAITING, SUCCESS, WARNING, FAILURE, FROZEN, IN_PROCESS, SKIPPED, FINISHED}

    public enum TargetType {LEAF, MIDDLE, ROOT, INDEPENDENT}
}
