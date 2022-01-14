package app.taskView.summaryWindow;

import backend.TimeUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.util.stream.Collectors;

public class SummaryController {

    @FXML
    private Button closeBtn;

    @FXML
    private Label taskTypeLabel;

    @FXML
    private Label failedLabel;

    @FXML
    private Label warningLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Label skippedLabel;

    @FXML
    private Label successLabel;

    @FXML
    private Label durationLabel;

    @FXML
    private ListView<String> failedList;

    @FXML
    private ListView<String> skippedList;

    @FXML
    private ListView<String> warningList;

    @FXML
    private ListView<String> successList;


    public void setSummaryWindow(ObservableList<StackPane> failed, ObservableList<StackPane> skipped,
                                 ObservableList<StackPane> finished, String taskType, long duration) {
        int success = 0, warning = 0;
        for (StackPane pane : finished) {
            if (((Circle) pane.getChildren().get(0)).getFill() == Paint.valueOf("GREEN"))
                success++;
            else
                warning++;

        }
        taskTypeLabel.setText("Task Type: " + taskType);
        durationLabel.setText("Duration: " + TimeUtil.ltd(duration));
        failedLabel.setText("Failed: " + failed.size());
        skippedLabel.setText("Skipped: " + skipped.size());
        warningLabel.setText("Warning: " + warning);
        successLabel.setText("Success: " + success);
        totalLabel.setText("Total: " + (failed.size() + skipped.size() + finished.size()));

        ObservableList<String> succ = FXCollections.observableArrayList();
        ObservableList<String> warn = FXCollections.observableArrayList();
        for (StackPane pane : finished) {
            if (((Circle) pane.getChildren().get(0)).getFill() == Paint.valueOf("GREEN"))
                succ.add(pane.getId());
            else
                warn.add(pane.getId());
        }
        skippedList.setItems(skipped
                .stream()
                .map(Node::getId)
                .collect(Collectors.toCollection(FXCollections::observableArrayList)));
        failedList.setItems(failed
                .stream()
                .map(Node::getId)
                .collect(Collectors.toCollection(FXCollections::observableArrayList)));
        warningList.setItems(warn);
        successList.setItems(succ);

        closeBtn.setOnAction(event -> closeBtn.getScene().getWindow().hide());
    }

}

