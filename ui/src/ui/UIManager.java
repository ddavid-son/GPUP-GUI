package ui;

import backend.Engine;
import backend.Execution;

import java.util.*;
import java.util.function.Consumer;

public class UIManager {
    private boolean exitApp = false;
    Engine engine = new Execution();
    boolean isRunning = false;

    public void runApp() {
        while (!exitApp) {
            try {
                switch (startMenu()) {
                    case LOAD_GRAPH:
                        loadGraphFromFile();
                        break;
                    case INFO_ABOUT_GRAPH:
                        getInfoAboutGraph();
                        break;
                    case INFO_ABOUT_NODE:
                        getInfoAboutTarget();
                        break;
                    case PATH_EXIST:
                        getAllPathsBetweenTargets();
                        break;
                    case RUN_TASK:
                        runTask();
                        break;
                    case FIND_CIRCLE:
                        findCircleWithTarget();
                        break;
                    case SAVE_TO_FILE:
                        saveCurrentState();
                        break;
                    case LOAD_FROM_FILE:
                        loadStateFromFile();
                        break;
                    case EXIT_APP:
                        exitApp = true;
                        break;
                    default:
                        break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    //----------------------------------------8th menu option----------------------------------------//
    private void loadStateFromFile() {
        String fileNameQuery = "please enter file path to load the state from: ";
        Scanner readln = new Scanner(System.in);
        boolean isValidOrExit = false;
        while (!isValidOrExit) {
            String fileName = askAndReturnFromUser(fileNameQuery, readln);
            try {
                engine.readObjectFromFile(fileName);
                System.out.println("\nSuccessfully loaded the state from the file.\n");
                isRunning = true;
                isValidOrExit = true;
            } catch (Exception e) {
                if (e.getMessage().equals("Exit!") || e.getMessage().equals("you need to load a valid XML file first"))
                    throw new IllegalArgumentException(e.getMessage() + ", you have been redirected to the main menu.");
                System.out.println(e.getMessage());
            }
        }
    }
    //----------------------------------------8th menu option----------------------------------------//


    //----------------------------------------7th menu option----------------------------------------//
    private void saveCurrentState() {
        graphIsLoadedOrThrow();
        Scanner readln = new Scanner(System.in);
        boolean isValidOrExit = false;
        while (!isValidOrExit) {
            String fileName = askAndReturnFromUser(
                    "Please enter file path to save the current state of the project to: ", readln);
            try {
                engine.writeObjectToFile(fileName);
                System.out.println("\nSuccessfully saved the state to the file.\n");
                isValidOrExit = true;
            } catch (Exception e) {
                if (e.getMessage().equals("Exit!") || e.getMessage().equals("you need to load a valid XML file first"))
                    throw new IllegalArgumentException(e.getMessage() + ", you have been redirected to the main menu.");
                System.out.println(e.getMessage());
            }
        }
    }
    //----------------------------------------7th menu option----------------------------------------//


    //----------------------------------------6th menu option----------------------------------------//
    private void findCircleWithTarget() {
        graphIsLoadedOrThrow();
        boolean isValidOrExit = false;
        String targetName;
        Scanner readln = new Scanner(System.in);

        while (!isValidOrExit) {
            try {
                targetName = askAndReturnFromUser("please enter the name of the target you want to find the circle for", readln);
                List<String> circle = engine.findIfTargetIsInACircle(targetName);
                System.out.println(circle.isEmpty() ?
                        "\nthere is no circle for this target\n" : "\n" + circle + "\n");
                isValidOrExit = true;
            } catch (Exception e) {
                if (e.getMessage().equals("Exit!") || e.getMessage().equals("you need to load a valid XML file first"))
                    throw new IllegalArgumentException(e.getMessage() + ", you have been redirected to the main menu.");
                System.out.println(e.getMessage());
            }
        }
    }
    //----------------------------------------6th menu option----------------------------------------//


    //----------------------------------------5th menu option----------------------------------------//
    private void runTask() {
        graphIsLoadedOrThrow();
        boolean isValidOrExit = false;
        int msToRun;
        boolean isRandom, isIncremental, isSimulation = true;
        double successRate, successfulWithWarningRate;
        Scanner readln = new Scanner(System.in);
        String incrementalOrNewString;
        Consumer<String> print = System.out::println;

        runTaskWorkerLoop(isValidOrExit, isSimulation, readln, print);
    }

    private void runTaskWorkerLoop(boolean isValidOrExit, boolean isSimulation, Scanner readln, Consumer<String> print) {
        boolean isIncremental, isRandom;
        String incrementalOrNewString;
        double successfulWithWarningRate, successRate;
        int msToRun;

        while (!isValidOrExit) {
            try {
                msToRun = Integer.parseInt(askAndReturnFromUser("please enter number of ms for task duration(1000ms = 1 sec)", readln));
                isRandom = askAndReturnFromUser("for random sleep time enter 'random' otherwise the time you chose above will be applied", readln)
                        .equalsIgnoreCase("random");
                successRate = checkRange(Double.parseDouble(askAndReturnFromUser("please enter success rate(0.0 - 1.0)", readln)), 0, 1);
                successfulWithWarningRate = checkRange(Double.parseDouble(askAndReturnFromUser("please enter successful with warning rate(0.0 - 1.0)", readln)), 0, 1);
                incrementalOrNewString = askAndReturnFromUser("please enter:\n for incremental run - inc \n for running new task from scratch - new\n", readln);
                isIncremental = setIsIncremental(incrementalOrNewString);

                engine.runTaskOnGraph(isRandom, msToRun, successRate, successfulWithWarningRate, isIncremental, print, isSimulation);
                isValidOrExit = true;
            } catch (Exception e) {
                if (e.getMessage().equals("Exit!") || e.getMessage().equals("you need to load a valid XML file first"))
                    throw new IllegalArgumentException(e.getMessage() + ", you have been redirected to the main menu.");
                System.out.println(e.getMessage());
            }
        }
    }

    private boolean setIsIncremental(String incrementalOrNewString) {
        if (incrementalOrNewString.toUpperCase(Locale.ROOT).equals("INC")) {
            return true;
        } else {
            if (incrementalOrNewString.toUpperCase(Locale.ROOT).equals("NEW")) {
                return false;
            } else
                throw new IllegalArgumentException("wrong input in the if run needs to be incremental or from scratch");
        }
    }

    double checkRange(double value, double min, double max) {
        if (value < min || value > max)
            throw new IllegalArgumentException("value must be between " + min + " and " + max);

        return value;
    }
    //----------------------------------------5th menu option----------------------------------------//


    //----------------------------------------4th menu option----------------------------------------//
    private void getAllPathsBetweenTargets() {
        graphIsLoadedOrThrow();
        String firstTargetQuery = "please enter first Target's name";
        String secondTargetQuery = "please enter second Target's name";
        String relationTypeQuery = "please enter the wanted relation between these targets,\n" +
                RelationType.DEPENDS_ON.ordinal() + " - " + RelationType.DEPENDS_ON.name() + "\n" +
                RelationType.REQUIRED_FOR.ordinal() + " - " + RelationType.REQUIRED_FOR.name();

        getAllPathsWorker(firstTargetQuery, secondTargetQuery, relationTypeQuery);
    }

    private void getAllPathsWorker(String firstTargetQuery, String secondTargetQuery, String relationTypeQuery) {
        RelationType relationType;
        String secondTarget, firstTarget;
        Scanner readln = new Scanner(System.in);
        boolean isValidOrExit = false;

        while (!isValidOrExit) {
            try {
                firstTarget = askAndReturnFromUser(firstTargetQuery, readln);
                secondTarget = askAndReturnFromUser(secondTargetQuery, readln);
                int relationInt = relationTypeRangeCheck(askAndReturnFromUser(relationTypeQuery, readln));
                relationType = RelationType.values()[relationInt]; // not recommended
                isValidOrExit = pathDirectionSwitch(engine, firstTarget, secondTarget, relationType);
            } catch (Exception e) {
                if (e.getMessage().equals("Exit!") || e.getMessage().equals("you need to load a valid XML file first"))
                    throw new IllegalArgumentException(e.getMessage() + ", you have been redirected to the main menu.");
                System.out.println(e.getMessage());
            }
        }
    }

    private int relationTypeRangeCheck(String relationString) {
        int relationInt;

        try {
            relationInt = Integer.parseInt(relationString); //
            if (relationInt < 0 || relationInt >= RelationType.values().length)
                throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) { // NumberFormatException is a subclass of IllegalArgumentException
            throw new IllegalArgumentException("relation type needs to be an integer between 0 - " +
                    (RelationType.values().length - 1));
        }

        return relationInt;
    }

    private boolean pathDirectionSwitch(Engine engine, String firstTarget, String secondTarget, RelationType
            relationType) {
        String noPathExist = "no path exist from " +
                (relationType == RelationType.DEPENDS_ON ? firstTarget + " to " + secondTarget :
                        secondTarget + " to " + firstTarget);

        Set<List<String>> allPaths = new HashSet<>();
        switch (relationType) {
            case DEPENDS_ON:
                allPaths = engine.findAllPathsBetweenTargets(firstTarget, secondTarget);
                break;
            case REQUIRED_FOR:
                allPaths = engine.findAllPathsBetweenTargets(secondTarget, firstTarget);
                break;
        }
        if (allPaths.isEmpty()) {
            System.out.println("\n" + noPathExist + "\n");
        } else {
            allPaths.forEach(path -> System.out.println("\n" + String.join(" -> ", path) + "\n"));
        }
        return true;
    }

    public enum RelationType {
        DEPENDS_ON,
        REQUIRED_FOR
    }
    //----------------------------------------4th menu option----------------------------------------//


    //----------------------------------------3rd menu option----------------------------------------//
    private void getInfoAboutTarget() {
        graphIsLoadedOrThrow();
        Scanner readln = new Scanner(System.in);
        boolean isValidOrQuit = false;
        while (!isValidOrQuit) {
            try {
                System.out.println("please enter a Target's name (or 'Exit!' to go back to main menu):");
                String targetName = readln.next();
                if (!targetName.equals("Exit!")) {
                    System.out.println("\n" +
                            engine.getInfoAboutTarget(targetName.toUpperCase(Locale.ROOT)).toString() + "\n");
                }
                isValidOrQuit = true;
            } catch (IllegalArgumentException e) {
                if (e.getMessage().equals("you need to load a valid XML file first")) {
                    throw new IllegalArgumentException(e.getMessage() + ", you have been redirected to main menu.");
                }
                System.out.println(e.getMessage());
            }
        }
    }
    //----------------------------------------3rd menu option----------------------------------------//


    //----------------------------------------2nd menu option----------------------------------------//
    private void getInfoAboutGraph() {
        System.out.println("\n" + engine.getGraphInfo().toString() + "\n");
    }
    //----------------------------------------2nd menu option----------------------------------------//


    //----------------------------------------1st menu option----------------------------------------//
    public void loadGraphFromFile() {
        String xmlFilePath;
        Scanner s = new Scanner(System.in);
        boolean isValidOrExit = false;

        while (!isValidOrExit) {
            try {
                /*"C:\\Users\\USER\\IdeaProjects\\GPUP\\src\\resources\\ex1-big.xml"*/
                xmlFilePath = askAndReturnFromUser("please enter a path for the xml file:", s);
                engine.xmlFileLoadingHandler(xmlFilePath);
                isValidOrExit = true;
                isRunning = true;
            } catch (IllegalArgumentException e) {
                if (e.getMessage().equals("Exit!") || e.getMessage().equals("you need to load a valid XML file first"))
                    throw new IllegalArgumentException(e.getMessage() + ", you have been redirected to the main menu.");
                System.out.println(e.getMessage());
            }
        }
    }
    //----------------------------------------1st menu option----------------------------------------//


    //----------------------------------------Utils for main menu----------------------------------------//
    private MenuOption startMenu() {
        Scanner readln = new Scanner(System.in);
        boolean isValidUserChoice = false;
        int userChoice = -1;
        String userChoiceString;

        while (!isValidUserChoice) {
            printMainMenu();
            try {
                userChoiceString = readln.next();
                userChoice = Integer.parseInt(userChoiceString);
            } catch (NumberFormatException e) {
                userChoice = -1;
            }
            if (validateUserChoice(userChoice)) {
                isValidUserChoice = true;
            } else {
                printValueOutOfRangeError(userChoice);
            }
        }

        return convertIntToEnum(userChoice);
    }

    private void graphIsLoadedOrThrow() {
        if (!isRunning) throw new IllegalArgumentException("you need to load a valid XML file first");
    }

    private String askAndReturnFromUser(String question, Scanner readln) {
        System.out.println(question + " (or Exit! to go back to main menu):");
        String s = readln.next();
        if (s.equalsIgnoreCase("Exit!")) {
            throw new IllegalArgumentException("Exit!");
        }
        return s;
    }

    private void printValueOutOfRangeError(int userChoice) {
        String error = userChoice == -1 ?
                "Input needs to be an integer, " : "Input was out of range, ";
        System.out.println(error + "please enter numbers between 1 - " + (MenuOption.EXIT_APP.ordinal() + 1));
    }

    private void printMainMenu() {
        System.out.println("---please enter a number according to the menu below---");
        System.out.println("1. load graph from file");
        System.out.println("2. get general information about the graph");
        System.out.println("3. get details on a specific node in the graph");
        System.out.println("4. find if there is a path between two nodes");
        System.out.println("5. run task on graph");
        System.out.println("6. find if target is a part of a circle in the graph");
        System.out.println("7. save current state to file");
        System.out.println("8. load previous state from file");
        System.out.println("9. exit app");
    }

    private MenuOption convertIntToEnum(int userChoice) {
        switch (userChoice) {
            case 1:
                return MenuOption.LOAD_GRAPH;
            case 2:
                return MenuOption.INFO_ABOUT_GRAPH;
            case 3:
                return MenuOption.INFO_ABOUT_NODE;
            case 4:
                return MenuOption.PATH_EXIST;
            case 5:
                return MenuOption.RUN_TASK;
            case 6:
                return MenuOption.FIND_CIRCLE;
            case 7:
                return MenuOption.SAVE_TO_FILE;
            case 8:
                return MenuOption.LOAD_FROM_FILE;
            case 9:
                return MenuOption.EXIT_APP;
            default:
                return MenuOption.RANGE_ERROR_OCCURRED; // should not be reached
        }
    }

    private boolean validateUserChoice(int userChoice) {
        return userChoice <= MenuOption.EXIT_APP.ordinal() + 1 && userChoice > MenuOption.LOAD_GRAPH.ordinal();
    }

    public enum MenuOption {
        LOAD_GRAPH,
        INFO_ABOUT_GRAPH,
        INFO_ABOUT_NODE,
        PATH_EXIST,
        RUN_TASK,
        FIND_CIRCLE,
        SAVE_TO_FILE,
        LOAD_FROM_FILE,
        EXIT_APP,
        RANGE_ERROR_OCCURRED,
    }
    //----------------------------------------Utils for main menu----------------------------------------//
}
