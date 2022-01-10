package app.taskForm;

import app.mainScreen.AppController;
import app.sideMenu.SideMenuController;
import backend.GraphManager;
import backend.argumentsDTO.SimulationArgs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;

public class TaskFormController {

    @FXML
    private Slider successSlider;

    @FXML
    private Node taskStage;

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
    private RadioButton whatIfDownOnRB;

    @FXML
    private RadioButton whatIfUpOnRB;

    private AppController appController;

    private File currentFileInTask;

    private SideMenuController sideMenuController;

    // -------------------------------------------- what if methods -------------------------------------------------- /
    @FXML
    void whatIfOffRBClicked(ActionEvent event) {
        whatIfOffRB.setSelected(true);
        whatIfDownOnRB.setSelected(false);
        whatIfUpOnRB.setSelected(false);
    }

    @FXML
    void whatIfOnRBClicked(ActionEvent event) {
        whatIfDownOnRB.setSelected(true);
        whatIfOffRB.setSelected(false);
        whatIfUpOnRB.setSelected(false);
    }

    @FXML
    void whatIfUpOnRBClicked(ActionEvent event) {
        whatIfUpOnRB.setSelected(true);
        whatIfDownOnRB.setSelected(false);
        whatIfOffRB.setSelected(false);
    }

    public boolean getWhatIfDownIsSelected() {
        return whatIfDownOnRB.isSelected();
    }

    public boolean getWhatIfUpIsSelected() {
        return whatIfUpOnRB.isSelected();
    }

    // -------------------------------------------- what if methods -------------------------------------------------- /

    @FXML
    private void OnNewRunBtnClicked(ActionEvent event) {
        sideMenuController.setNewFileForTask();
        runTask(false);
    }

    @FXML
    private void OnIncrementalRunBtnClicked(ActionEvent event) {
        runTask(true);
    }

    private void runTask(boolean isIncremental) {
        ((Stage) newRunBtn.getScene().getWindow()).close();

        appController.runTask(new SimulationArgs(
                ((double) successField.getValue()) / 100,
                ((double) warningField.getValue()) / 100,
                sleepTimeField.getValue(),
                threadCountField.getValue(),
                yesRandomRB.isSelected(),
                !whatIfOffRB.isSelected(),
                isIncremental,
                whatIfDownOnRB.isSelected() ?
                        GraphManager.RelationType.DEPENDS_ON :
                        GraphManager.RelationType.REQUIRED_FOR
        ));
    }

    public void setTaskController(int maxThreadCount, boolean incrementalAvailable, boolean filesAreTheSame) {

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
        whatIfUpOnRB.setSelected(false);
        whatIfDownOnRB.setSelected(false);

        incrementalRunBtn.setDisable(
                !incrementalAvailable || !filesAreTheSame ||
                        !appController.currentSelectedTargetsAreTheSameAsPreviousRun()

        );
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

    public void setAppController(AppController appController, SideMenuController sideMenuController) {
        this.appController = appController;
        this.sideMenuController = sideMenuController;
    }
}
