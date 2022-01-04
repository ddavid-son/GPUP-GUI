package app.taskForm;

import app.mainScreen.AppController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;

public class TaskFormController {

    @FXML
    private Slider successSlider;

    @FXML
    private Slider warningSlider;

    @FXML
    private RadioButton yesRandomRB;

    @FXML
    private RadioButton noRandomRB;

    @FXML
    private GridPane simulationTabGridPane;

    @FXML
    private Button newRunBtn;

    @FXML
    private Button incrementalRunBtn;

    @FXML
    private final IntField successField = new IntField(0, 100, 50);

    @FXML
    private final IntField warningField = new IntField(0, 100, 50);

    @FXML
    private final IntField sleepTimeField = new IntField(0, 20000, 1000);

    @FXML
    private Slider threadSlider;

    @FXML
    private IntField threadCountField;

    @FXML
    private Slider sleepTimeSlider;

    @FXML
    private RadioButton whatIfOffRB;

    @FXML
    private RadioButton whatIfOnRB;
    private AppController appController;


    @FXML
    void whatIfOffRBClicked(ActionEvent event) {
        whatIfOffRB.setSelected(true);
        whatIfOnRB.setSelected(false);
    }

    @FXML
    void whatIfOnRBClicked(ActionEvent event) {
        whatIfOnRB.setSelected(true);
        whatIfOffRB.setSelected(false);
    }

    @FXML
    private void OnNewRunBtnClicked(ActionEvent event) {
        runTask();
    }

    @FXML
    private void OnIncrementalRunBtnClicked(ActionEvent event) {
        runTask();
    }

    private void runTask() {
        appController.runTask(
                successField.getValue(),
                warningField.getValue(),
                sleepTimeField.getValue(),
                threadCountField.getValue(),
                yesRandomRB.isSelected(),
                whatIfOnRB.isSelected(),
                true
        );
    }

    public void setTaskController(int maxThreadCount, boolean incrementalAvailable) {

        threadCountField = new IntField(1, maxThreadCount, 1);
        threadSlider.setMax(maxThreadCount);
        sleepTimeSlider.setMax(20_000);//20 sec max
        simulationTabGridPane.add(successField, 4, 1);
        simulationTabGridPane.add(warningField, 4, 1);
        simulationTabGridPane.add(sleepTimeField, 4, 2);
        simulationTabGridPane.add(threadCountField, 4, 3);

        GridPane.setValignment(successField, VPos.TOP);
        GridPane.setValignment(warningField, VPos.BOTTOM);
        GridPane.setValignment(threadCountField, VPos.TOP);

        successSlider.valueProperty().bindBidirectional(successField.valueProperty());
        warningSlider.valueProperty().bindBidirectional(warningField.valueProperty());
        sleepTimeSlider.valueProperty().bindBidirectional(sleepTimeField.valueProperty());
        threadCountField.valueProperty().bindBidirectional(threadSlider.valueProperty());

        yesRandomRB.setSelected(true);
        noRandomRB.setSelected(false);
        whatIfOffRB.setSelected(true);
        whatIfOnRB.setSelected(false);

        incrementalRunBtn.setDisable(!incrementalAvailable);
    }

    @FXML
    void noRandomRBSelect(ActionEvent event) {
        noRandomRB.setSelected(true);
        yesRandomRB.setSelected(false);
    }

    @FXML
    void yesRandomRBSelect(ActionEvent event) {
        noRandomRB.setSelected(false);
        yesRandomRB.setSelected(true);
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }
}
