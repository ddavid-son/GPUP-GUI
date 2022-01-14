package app.taskView;

import app.mainScreen.AppController;
import app.taskView.summaryWindow.SummaryController;
import backend.*;
import backend.argumentsDTO.CompilationArgs;
import backend.argumentsDTO.ProgressDto;
import backend.argumentsDTO.SimulationArgs;
import backend.argumentsDTO.TaskArgs;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TaskViewController {

    @FXML
    private ListView<StackPane> frozenList;

    @FXML
    private GridPane userInputGridPane;

    @FXML
    private ListView<StackPane> waitingList;

    @FXML
    private ListView<StackPane> failedList;

    @FXML
    private ListView<StackPane> skippedList;

    @FXML
    private ListView<StackPane> finishedList;

    @FXML
    private ListView<StackPane> inProcessList;

    @FXML
    private TextArea logListViw;

    @FXML
    private Button goHomeBtn;

    @FXML
    private Button playPauseBtn;

    @FXML
    private Spinner<Integer> numberOfThreadsSpinner;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label taskTypeHeaderLabel;

    @FXML
    private Label numberOfThreadsLabel;

    @FXML
    private Label isIncrementalLabel;

    @FXML
    private Button setNewThreadsBtn;

    private Label srcFolderLabel;
    private Label destFolderLabel;
    private Label successRateLabel;
    private Label warningRateLabel;
    private Label sleepTimeLabel;
    private Label isRandomLabel;

    private AppController appController;
    private Execution execution;
    private int totalNumberOfTargets;
    private int finishedNumberTargets = 0;

    List<StackPane> frozenListItems = new ArrayList<>();
    List<StackPane> waitingListItems = new ArrayList<>();
    List<StackPane> failedListItems = new ArrayList<>();
    List<StackPane> skippedListItems = new ArrayList<>();
    List<StackPane> finishedListItems = new ArrayList<>();
    List<StackPane> inProcessListItems = new ArrayList<>();
    List<String> allFinishedTasks = new ArrayList<>(); // dedicated to the progress bar
    List<StackPane> allTargets = new ArrayList<>();

    ObservableList<StackPane> obsFrozenList;
    ObservableList<StackPane> obsWaitingList;
    ObservableList<StackPane> obsFailedList;
    ObservableList<StackPane> obsSkippedList;
    ObservableList<StackPane> obsFinishedList;
    ObservableList<StackPane> obsInProcessList;
    ObservableList<StackPane> obsAllTargets;
    ObservableList<String> obsAllFinishedTasks;

    private boolean paused = false;
    private SimpleBooleanProperty runEnded = new SimpleBooleanProperty(false);
    List<String> summery = new ArrayList<>();
    private final Random r = new Random();

    @FXML
    void onGoHomeBtnClicked(ActionEvent event) {
        appController.goToMainScreen();
    }

    @FXML
    void onPlayPauseBtnClicked(ActionEvent event) {
        if (!paused) {
            appController.pauseExecution();
            playPauseBtn.setGraphic(appController.getIcon("/icons/playBtnIcon.png", 35));
            paused = true;
        } else {
            appController.resumeExecution();
            playPauseBtn.setGraphic(appController.getIcon("/icons/pauseBtnIcon.png", 35));
            paused = false;
        }
    }

    @FXML
    void onSetNewThreadsBtnClicked(ActionEvent event) {
        appController.setNumberOfThreads(numberOfThreadsSpinner.getValue());
    }

    public void setAppController(AppController appController, Engine execution) {
        this.appController = appController;
        this.execution = (Execution) execution;
        setMeInAppController();
    }

    private void setMeInAppController() {
        appController.setTaskViewController(this);
    }

    public void setTaskView(TaskArgs taskArgs) {
        totalNumberOfTargets = taskArgs.getTargetsSelectedForGraph().size();
        setLabelsAccordingToUserInput(taskArgs);
        handleListAndObservables(taskArgs);

        numberOfThreadsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1,
                        execution.getMaxThreadCount(),
                        1)
        );

        playPauseBtn.setGraphic(appController.getIcon("/icons/pauseBtnIcon.png", 35));
        playPauseBtn.setDisable(false);

        progressBar.setProgress(0F);
    }

    private void setLabelsAccordingToUserInput(TaskArgs taskArgs) {
        taskTypeHeaderLabel.setText(taskArgs.getTaskType().toString());
        numberOfThreadsLabel.setText("Number of threads: " + taskArgs.getNumOfThreads());
        isIncrementalLabel.setText("Task will be performed: " + (taskArgs.isIncremental() ?
                "Incrementally" :
                "From scratch")
        );

        if (taskArgs.getTaskType() == TaskArgs.TaskType.COMPILATION) {
            CompilationArgs compilationArgs = (CompilationArgs) taskArgs;
            srcFolderLabel = new Label("Files Will Be Taken From: " + compilationArgs.getSrcPath());
            destFolderLabel = new Label("Compiled Files Will Be Saved In: " + compilationArgs.getDstPath());

            userInputGridPane.add(srcFolderLabel, 4, 0);
            userInputGridPane.add(destFolderLabel, 4, 1);

            GridPane.setValignment(srcFolderLabel, VPos.TOP);
            GridPane.setValignment(destFolderLabel, VPos.TOP);
            GridPane.setColumnSpan(srcFolderLabel, 6);
            GridPane.setColumnSpan(destFolderLabel, 6);

            GridPane.setHalignment(srcFolderLabel, HPos.LEFT);
            GridPane.setHalignment(destFolderLabel, HPos.LEFT);
        } else {
            SimulationArgs simulationArgs = (SimulationArgs) taskArgs;
            successRateLabel = new Label("Success Rate: " + simulationArgs.getSuccessRate());
            warningRateLabel = new Label("Warning Rate: " + simulationArgs.getWarningRate());
            sleepTimeLabel = new Label("Sleep Time" +
                    (simulationArgs.isRandom() ? " <= " : ": ") +
                    simulationArgs.getSleepTime()
            );

            userInputGridPane.add(successRateLabel, 4, 0);
            userInputGridPane.add(warningRateLabel, 4, 1);
            userInputGridPane.add(sleepTimeLabel, 4, 2);

            GridPane.setValignment(successRateLabel, VPos.TOP);
            GridPane.setValignment(warningRateLabel, VPos.TOP);
            GridPane.setValignment(sleepTimeLabel, VPos.TOP);
            GridPane.setColumnSpan(successRateLabel, 6);
            GridPane.setColumnSpan(warningRateLabel, 6);
            GridPane.setColumnSpan(sleepTimeLabel, 6);

            GridPane.setHalignment(successRateLabel, HPos.LEFT);
            GridPane.setHalignment(warningRateLabel, HPos.LEFT);
            GridPane.setHalignment(sleepTimeLabel, HPos.LEFT);
        }
    }

    private void handleListAndObservables(TaskArgs taskArgs) {
        obsAllTargets = FXCollections.observableList(allTargets);
        obsFailedList = FXCollections.observableList(failedListItems);
        obsFrozenList = FXCollections.observableList(frozenListItems);
        obsInProcessList = FXCollections.observableList(inProcessListItems);
        obsSkippedList = FXCollections.observableList(skippedListItems);
        obsWaitingList = FXCollections.observableList(waitingListItems);
        obsFinishedList = FXCollections.observableList(finishedListItems);

        inProcessList.setItems(obsInProcessList);
        finishedList.setItems(obsFinishedList);
        skippedList.setItems(obsSkippedList);
        failedList.setItems(obsFailedList);
        waitingList.setItems(obsWaitingList);
        frozenList.setItems(obsFrozenList);

        taskArgs.getTargetsSelectedForGraph().forEach(target -> {
            obsAllTargets.add(new TaskCircle(target, Target.TargetState.FROZEN).getStackPane());
        });

        obsFrozenList.addAll(obsAllTargets);
    }

    private void handelLogOfTask(accumulatorForWritingToFile targetLog) {

        targetLog.outPutData.forEach(s -> {
            logListViw.appendText("\n" + TimeUtil.ltn(System.currentTimeMillis()) + " " + s);
            logListViw.positionCaret(0);
        });
        logListViw.appendText("\n");
    }

    private void handelFinishedTask(ProgressDto progressDto) {
        updateProgressBar(progressDto);
        if (progressDto.getTargetState() == Target.TargetState.FAILURE)
            handelFailedAndSkipped(progressDto.getTargetName());
        else if (progressDto.getTargetState() == Target.TargetState.SUCCESS ||
                progressDto.getTargetState() == Target.TargetState.WARNING)
            handelSuccessAndOpening(progressDto.getTargetName(), progressDto.getTargetState());
        else
            manageListsMovement(progressDto);
    }

    private void handelSuccessAndOpening(String nameOfSuccessAndOpening, Target.TargetState targetState) {
        String[] failedAndSkipped = nameOfSuccessAndOpening.split(",");
        manageListsMovement(new ProgressDto(failedAndSkipped[0], targetState));

        for (int i = 1; i < failedAndSkipped.length; i++) {
            manageListsMovement(new ProgressDto(failedAndSkipped[i], Target.TargetState.WAITING));
        }
    }

    private void handelFailedAndSkipped(String namesOfFailedAndSkipped) {
        String[] failedAndSkipped = namesOfFailedAndSkipped.split(",");
        manageListsMovement(new ProgressDto(failedAndSkipped[0], Target.TargetState.FAILURE));

        for (int i = 1; i < failedAndSkipped.length; i++) {
            manageListsMovement(new ProgressDto(failedAndSkipped[i], Target.TargetState.SKIPPED));
        }
    }

    private void manageListsMovement(ProgressDto targetLog) {
        // switch on the state of the task
        StackPane temp = new TaskCircle().getStackPane();
        this.summery.add(targetLog.getTargetName() + " " + targetLog.getTargetState() + " visited switch");
        switch (targetLog.getTargetState()) {
            case WAITING:
            case SKIPPED:
                handelListInCaseOfStart(targetLog, temp);// the common ground here is -[frozen list -> else]
                break;
            case FAILURE:
            case WARNING:
            case SUCCESS:
                handelListInCaseOfFinished(targetLog, temp); //the common ground here is -[inProcess list -> else]
                break;
            case IN_PROCESS:
                handleListInCasOfInProcess(targetLog, temp);
                break;
        }
    }

    private void handleListInCasOfInProcess(ProgressDto targetLog, StackPane temp) {
        for (StackPane stackPane : obsWaitingList) {
            if (stackPane.getId().equals(targetLog.getTargetName())) {
                temp = stackPane;
                ((Circle) temp.getChildren().get(0)).fillProperty().setValue(Color.ORANGE);
                if (!inProcessListItems.contains(temp)) {
                    obsInProcessList.add(temp);
                }
                break;
            }
        }
        obsWaitingList.remove(temp);
    }

    private void handelListInCaseOfStart(ProgressDto targetLog, StackPane temp) {
        for (StackPane stackPane : obsFrozenList) {
            if (stackPane.getId().equals(targetLog.getTargetName())) {
                temp = stackPane;
                switch (targetLog.getTargetState()) {
                    case WAITING:
                        if (!waitingListItems.contains(temp)) {
                            ((Circle) temp.getChildren().get(0)).fillProperty().setValue(Color.PINK);
                            obsWaitingList.add(temp);
                        }
                        break;
                    case SKIPPED:
                        if (!skippedListItems.contains(temp)) {
                            ((Circle) temp.getChildren().get(0)).fillProperty().setValue(Color.GRAY);
                            obsSkippedList.add(temp);
                        }
                        break;
                }
                break;
            }
        }
        obsFrozenList.remove(temp);
    }

    private void handelListInCaseOfFinished(ProgressDto targetLog, StackPane temp) {
        for (StackPane stackPane : obsInProcessList) {
            if (stackPane.getId().equals(targetLog.getTargetName())) {
                temp = stackPane;
                switch (targetLog.getTargetState()) {
                    case FAILURE:
                        if (!failedListItems.contains(temp)) {
                            ((Circle) temp.getChildren().get(0)).fillProperty().setValue(Color.RED);
                            obsFailedList.add(temp);
                        }
                        break;
                    case WARNING:
                    case SUCCESS:
                        if (!finishedListItems.contains(temp)) {
                            ((Circle) temp.getChildren().get(0)).fillProperty().setValue(
                                    targetLog.getTargetState() == Target.TargetState.WARNING ?
                                            Color.YELLOW :
                                            Color.GREEN);
                            obsFinishedList.add(temp);
                        }
                        break;
                }
                break;
            }
        }
        obsInProcessList.remove(temp);
    }

    private void updateProgressBar(ProgressDto progressDto) {
        if (progressDto.getTargetState() == Target.TargetState.FAILURE ||
                progressDto.getTargetState() == Target.TargetState.WARNING ||
                progressDto.getTargetState() == Target.TargetState.SUCCESS) {
            allFinishedTasks.addAll(Arrays.asList(progressDto.getTargetName().split(",")));
        }
        finishedNumberTargets = allFinishedTasks.size();
        progressBar.setProgress((double) finishedNumberTargets / (double) totalNumberOfTargets);
    }

    public void delegateExecutionOfTaskToAnotherThread(TaskArgs taskArgs) {
        Thread thread = new Thread(() -> {
            try {
                playPauseBtn.setDisable(false);
                long start = System.currentTimeMillis();
                execution.runTaskOnGraph(taskArgs, this::handelLogOfTask, this::handelFinishedTask);
                long end = System.currentTimeMillis();
                Platform.runLater(() -> {
                    playPauseBtn.setDisable(true);
                    showSummaryWindow(end - start);
                });
            } catch (Exception e) {
                Platform.runLater(() -> appController.handleErrors(
                        e,
                        e.getMessage(),
                        "Error running task"));
            }
            // TODO: maybe down here will handle the summary data fetching with runLater to update the UI
        });
        thread.setName("Task thread");
        thread.start();
    }

    private void showSummaryWindow(long time) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/resources/summayWindow.fxml");
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            SummaryController summayController = fxmlLoader.getController();

            summayController.setSummaryWindow(
                    obsFailedList,
                    obsSkippedList,
                    obsFinishedList,
                    taskTypeHeaderLabel.getText(),
                    time
            );

            Stage stage = new Stage();
            stage.setTitle("Summary");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            //
        }
    }

    public void resetAllLists(boolean isIncremental) {
        if (isIncremental) {
            obsFrozenList.clear();
            obsWaitingList.clear();

            obsFrozenList.addAll(obsSkippedList);
            obsSkippedList.clear();

            obsFrozenList.addAll(obsFailedList);
            obsFailedList.clear();

            obsFinishedList.clear();
        } else {
            obsFrozenList.clear();

            obsFrozenList.addAll(obsSkippedList);
            obsSkippedList.clear();

            obsFrozenList.addAll(obsFinishedList);
            obsFinishedList.clear();

            obsFrozenList.addAll(obsFailedList);
            obsFailedList.clear();
        }
        allFinishedTasks.clear();
        totalNumberOfTargets = obsFrozenList.size();
        progressBar.progressProperty().setValue(0);
    }

    public void disablePlayPauseBtn() {
        playPauseBtn.setDisable(true);
    }
}
