package app.taskView;

import app.mainScreen.AppController;
import backend.*;
import backend.argumentsDTO.ProgressDto;
import backend.argumentsDTO.TaskArgs;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

    Set<StackPane> frozenListItems = new HashSet<>();
    Set<StackPane> waitingListItems = new HashSet<>();
    Set<StackPane> failedListItems = new HashSet<>();
    Set<StackPane> skippedListItems = new HashSet<>();
    Set<StackPane> finishedListItems = new HashSet<>();
    Set<StackPane> inProcessListItems = new HashSet<>();
    Set<String> allFinishedTasks = new HashSet<>();


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
        insertAllTargetsToFrozenList(taskArgs);

        taskTypeHeaderLabel.setText(taskArgs.getTaskType().toString());
        numberOfThreadsLabel.setText("Max number of threads: " + taskArgs.getNumOfThreads());
        isIncrementalLabel.setText("Task will be performed: " + (taskArgs.isIncremental() ? "Incrementally" : "From scratch"));

        numberOfThreadsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1,
                        execution.getMaxThreadCount(),
                        1)
        );

/*        inProcessList.setItems(FXCollections.observableArrayList(inProcessListItems));
        finishedList.setItems(FXCollections.observableArrayList(finishedListItems));
        skippedList.setItems(FXCollections.observableArrayList(skippedListItems));
        failedList.setItems(FXCollections.observableArrayList(failedListItems));
        waitingList.setItems(FXCollections.observableArrayList(waitingListItems));
        frozenList.setItems(FXCollections.observableArrayList(frozenListItems));*/


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
        for (StackPane stackPane : waitingListItems) {
            if (stackPane.getId().equals(targetLog.getTargetName())) {
                temp = stackPane;
                waitingListItems.remove(stackPane);
                if (!inProcessListItems.contains(temp)) {
                    inProcessListItems.add(temp);
                    inProcessList.getItems().setAll(inProcessListItems);
                }
                return;
            }
        }
        waitingList.getItems().setAll(waitingListItems);
    }

    private void handelListInCaseOfStart(ProgressDto targetLog, StackPane temp) {

        for (StackPane stackPane : frozenListItems) {
            if (stackPane.getId().equals(targetLog.getTargetName())) {
                temp = stackPane;
                frozenListItems.remove(temp);
                switch (targetLog.getTargetState()) {
                    case WAITING:
                        if (!waitingListItems.contains(temp)) {
                            waitingListItems.add(temp);
                            waitingList.getItems().setAll(waitingListItems);
                        }
                        break;
                    case SKIPPED:
                        if (!skippedListItems.contains(temp)) {
                            skippedListItems.add(temp);
                            skippedList.getItems().setAll(skippedListItems);
                        }
                        break;
                }
                return;
            }
        }
        frozenList.getItems().setAll(frozenListItems);
    }

    private void handelListInCaseOfFinished(ProgressDto targetLog, StackPane temp) {
        for (StackPane stackPane : inProcessListItems) {
            if (stackPane.getId().equals(targetLog.getTargetName())) {
                temp = stackPane;
                inProcessListItems.remove(stackPane);
                switch (targetLog.getTargetState()) {
                    case FAILURE:
                        if (!failedListItems.contains(temp)) {
                            failedListItems.add(stackPane);
                            failedList.getItems().setAll(failedListItems);
                        }
                        break;
                    case WARNING:
                    case SUCCESS:
                        if (!finishedListItems.contains(temp)) {
                            finishedListItems.add(temp);
                            finishedList.getItems().setAll(finishedListItems);
                        }
                        break;
                }
                return;
            }
        }
        inProcessList.getItems().setAll(inProcessListItems);
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
