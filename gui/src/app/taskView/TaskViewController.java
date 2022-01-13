package app.taskView;

import app.mainScreen.AppController;
import backend.*;
import backend.argumentsDTO.ProgressDto;
import backend.argumentsDTO.TaskArgs;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskViewController {

    @FXML
    private ListView<StackPane> frozenList;

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
    List<String> allFinishedTasks = new ArrayList<>();

    ObservableList<StackPane> obsFrozenList;
    ObservableList<StackPane> obsWaitingList;
    ObservableList<StackPane> obsFailedList;
    ObservableList<StackPane> obsSkippedList;
    ObservableList<StackPane> obsFinishedList;
    ObservableList<StackPane> obsInProcessList;
    ObservableList<String> obsAllFinishedTasks;


    @FXML
    void onGoHomeBtnClicked(ActionEvent event) {
        appController.goToMainScreen();
    }

    @FXML
    void onPlayPauseBtnClicked(ActionEvent event) {

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
        //insertAllTargetsToFrozenList(taskArgs);

        taskTypeHeaderLabel.setText(taskArgs.getTaskType().toString());
        numberOfThreadsLabel.setText("Max number of threads: " + taskArgs.getNumOfThreads());
        isIncrementalLabel.setText("Task will be performed: " + (taskArgs.isIncremental() ? "Incrementally" : "From scratch"));

        numberOfThreadsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1,
                        execution.getMaxThreadCount(),
                        1)
        );

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
            obsFrozenList.add(new TaskCircle(target, Target.TargetState.FROZEN).getStackPane());
        });

        progressBar.setProgress(0);
    }

    private void insertAllTargetsToFrozenList(TaskArgs taskArgs) {
        for (String target : taskArgs.getTargetsSelectedForGraph()) {
            TaskCircle taskCircle = new TaskCircle(target, Target.TargetState.FROZEN);
            frozenListItems.add(taskCircle.getStackPane());
            frozenList.getItems().add(taskCircle.getStackPane());
        }
    }

    private void handelLogOfTask(accumulatorForWritingToFile targetLog) {
        //updateProgressBar(taskTarget);
        //manageListsMovement(targetLog);
    }

/*    private void handelFinishedTask(Task.TaskTarget taskTarget) {
      //updateProgressBar(taskTarget);
        manageListsMovement(taskTarget);
    }*/

    private void handelFinishedTask(ProgressDto progressDto) {
        //updateProgressBar(taskTarget);
        manageListsMovement(progressDto);
    }

    private void manageListsMovement(ProgressDto targetLog) {
        // switch on the state of the task
        StackPane temp = new TaskCircle().getStackPane();
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
                            obsWaitingList.add(temp);
                        }
                        break;
                    case SKIPPED:
                        if (!skippedListItems.contains(temp)) {
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
                            obsFailedList.add(temp);
                        }
                        break;
                    case WARNING:
                    case SUCCESS:
                        if (!finishedListItems.contains(temp)) {
                            obsFinishedList.add(temp);
                        }
                        break;
                }
                break;
            }
        }
        obsInProcessList.remove(temp);
    }

    private void updateProgressBar(Task.TaskTarget taskTarget) {
        allFinishedTasks.add(taskTarget.getName());
        allFinishedTasks.addAll(taskTarget.getNameOfFailedOrSkippedDependencies());
        finishedNumberTargets = allFinishedTasks.size();
        progressBar.setProgress((double) finishedNumberTargets / (double) totalNumberOfTargets);
    }

    public void delegateExecutionOfTaskToAnotherThread(TaskArgs taskArgs) {
        Thread thread = new Thread(() -> {
            try {
                execution.runTaskOnGraph(taskArgs, this::handelLogOfTask, this::handelFinishedTask);
            } catch (Exception e) {
                Platform.runLater(() -> appController.handleErrors(
                        e,
                        Arrays.toString(e.getStackTrace()),
                        "Error running task"));
            }
            // TODO: maybe down here will handle the summary data fetching with runLater to update the UI
        });
        thread.setName("Task thread");
        thread.start();
    }
}
