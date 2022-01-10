package app.taskForm;

import app.mainScreen.AppController;
import app.sideMenu.SideMenuController;
import backend.GraphManager;
import backend.argumentsDTO.CompilationArgs;
import backend.argumentsDTO.SimulationArgs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class TaskFormController {


    // ----------------------------------------------- Compilation Components ---------------------------------------- //
    public RadioButton whatIfOffRBComp;
    public Slider threadSliderComp;
    public RadioButton whatIfDownOnRBComp;
    public RadioButton whatIfUpOnRBComp;
    public Button incrementalRunBtnComp;
    public Button newRunBtnComp;
    public Button OutputFolderUploadBnt;
    public Button sourceFolderUploadBnt;
    public GridPane compilationTabGridPane;
    @FXML
    private IntField threadNumberForCompilationField;
    private File srcFolder;
    private File dstFolder;
    private Label srcFolderNameLabel;
    private Label dstFolderNameLabel;
    //private SimpleBooleanProperty srcFolderIsSet = new SimpleBooleanProperty(false);
    //private SimpleBooleanProperty dstFolderIsSet = new SimpleBooleanProperty(false);


    // ----------------------------------------------- Compilation Components ---------------------------------------- //


    // ----------------------------------------------- Simulation Components ---------------------------------------- //
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
    private final IntField successField = new IntField(1, 100, 50);

    @FXML
    private final IntField warningField = new IntField(1, 100, 50);

    @FXML
    private final IntField sleepTimeField = new IntField(1, 20000, 1000);

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
    // ----------------------------------------------- Simulation Components ---------------------------------------- //

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
    public void whatIfUpOnRBClicked(ActionEvent event) {
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


    // --------------------------------------------- Compilation Methods ----------------------------------------------//
    public void onWhatIfOffRBCompClicked(ActionEvent actionEvent) {
        whatIfOffRBComp.setSelected(true);
        whatIfDownOnRBComp.setSelected(false);
        whatIfUpOnRBComp.setSelected(false);
    }

    public void onWhatIfDownOnRBCompClicked(ActionEvent actionEvent) {
        whatIfDownOnRBComp.setSelected(true);
        whatIfUpOnRBComp.setSelected(false);
        whatIfOffRBComp.setSelected(false);
    }

    public void onWhatIfUpOnRBCompClicked(ActionEvent actionEvent) {
        whatIfUpOnRBComp.setSelected(true);
        whatIfDownOnRBComp.setSelected(false);
        whatIfOffRBComp.setSelected(false);
    }

    public void onIncrementalRunBtnCompClicked(ActionEvent actionEvent) {
        runCompilationTask(true);
    }

    public void onNewRunBtnCompClicked(ActionEvent actionEvent) {
        sideMenuController.setNewFileForTask();
        runCompilationTask(false);
    }

    private void runCompilationTask(boolean isIncremental) {
        if (!(srcFolder != null && dstFolder != null)) {
            appController.handleErrors(
                    null,
                    "Source and destination folders must be set before running a compilation task.",
                    "error - source or destination folder wasn't set"
            );
            return;
        }
        ((Stage) newRunBtn.getScene().getWindow()).close();

        //todo: add the paths i got from the user to the CompilationArgs Ctor
        appController.runTask(new CompilationArgs(
                !whatIfOffRB.isSelected(),
                threadNumberForCompilationField.getValue(),
                isIncremental,
                getWhatIfDownIsSelected() ?
                        GraphManager.RelationType.DEPENDS_ON :
                        GraphManager.RelationType.REQUIRED_FOR
        ));
    }

    public void sourceFolderUploadBntClicked(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Source Folder");
        srcFolder = directoryChooser.showDialog(null);
        updateSRCLabel();
        //srcFolderIsSet = new SimpleBooleanProperty(srcFolder != null);
    }

    private void updateSRCLabel() {
        if (srcFolderNameLabel == null) {
            srcFolderNameLabel = new Label(srcFolder == null ?
                    "no folder was selected" :
                    srcFolder.getName());
            compilationTabGridPane.add(srcFolderNameLabel, 3, 1);
        } else {
            srcFolderNameLabel.setText(srcFolder == null ?
                    "no folder was selected" :
                    srcFolder.getName());
        }
    }

    public void onOutputFolderUploadBntClicked(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select destination Folder");
        dstFolder = directoryChooser.showDialog(null);
        updateDSTLabel();
        //dstFolderIsSet = new SimpleBooleanProperty(dstFolder != null);
    }

    private void updateDSTLabel() {
        if (dstFolderNameLabel == null) {
            dstFolderNameLabel = new Label(dstFolder == null ?
                    "no folder was selected" :
                    dstFolder.getName());
            compilationTabGridPane.add(dstFolderNameLabel, 3, 2);
        } else {
            dstFolderNameLabel.setText(dstFolder == null ?
                    "no folder was selected" :
                    dstFolder.getName());
        }
    }

    // --------------------------------------------- Compilation Methods ----------------------------------------------//


    // --------------------------------------------- Simulation Methods ----------------------------------------------//
    @FXML
    private void OnNewRunBtnClicked(ActionEvent event) {
        //todo: add check that all the inputs are valid
        sideMenuController.setNewFileForTask();
        runSimulationTask(false);
    }

    @FXML
    private void OnIncrementalRunBtnClicked(ActionEvent event) {
        //todo: add check that all the inputs are valid
        runSimulationTask(true);
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

    private void runSimulationTask(boolean isIncremental) {
        if (assertSimulationUserInput()) {
            appController.handleErrors(
                    null,
                    "please fill all the fields - dont leave field empty",
                    "error in arguments that was inserted"
            );
            return;
        }

        ((Stage) newRunBtn.getScene().getWindow()).close();
        appController.runTask(new SimulationArgs(
                ((double) successField.getValue()) / 100,
                ((double) warningField.getValue()) / 100,
                sleepTimeField.getValue(),
                threadCountField.getValue(),
                yesRandomRB.isSelected(),
                !whatIfOffRB.isSelected(),
                isIncremental,
                getWhatIfDownIsSelected() ?
                        GraphManager.RelationType.DEPENDS_ON :
                        GraphManager.RelationType.REQUIRED_FOR
        ));
    }

    private boolean assertSimulationUserInput() {
        return !(successField.getValue() > 0 &&
                warningField.getValue() > 0 &&
                sleepTimeField.getValue() > 0 &&
                threadCountField.getValue() > 0);
    }
    // --------------------------------------------- Simulation Methods ----------------------------------------------//

    public void setTaskController(int maxThreadCount, boolean incrementalAvailable, boolean filesAreTheSame) {
        simulationTabSetUp(maxThreadCount, incrementalAvailable, filesAreTheSame);
        compilationTabSetUp(maxThreadCount, incrementalAvailable, filesAreTheSame);
    }

    private void compilationTabSetUp(int maxThreadCount, boolean incrementalAvailable, boolean filesAreTheSame) {
        this.threadNumberForCompilationField = new IntField(1, maxThreadCount, 1);
        this.threadSliderComp.setMax(maxThreadCount);
        compilationTabGridPane.add(threadNumberForCompilationField, 4, 3);
        GridPane.setValignment(threadNumberForCompilationField, VPos.CENTER);
        threadSliderComp.valueProperty().bindBidirectional(threadNumberForCompilationField.valueProperty());
        whatIfOffRBComp.setSelected(true);
        whatIfDownOnRBComp.setSelected(false);
        whatIfUpOnRBComp.setSelected(false);
        incrementalRunBtnComp.setDisable(
                !incrementalAvailable || !filesAreTheSame ||
                        !appController.currentSelectedTargetsAreTheSameAsPreviousRun()

        );
        sourceFolderUploadBnt.setGraphic(appController.getIcon("/icons/UploadIcon.png"));
        OutputFolderUploadBnt.setGraphic(appController.getIcon("/icons/UploadIcon.png"));
/*        newRunBtnComp.disableProperty().bind(Bindings.and(
                new SimpleBooleanProperty(srcFolder != null),
                new SimpleBooleanProperty(dstFolder != null)));*/
    }

    private void simulationTabSetUp(int maxThreadCount, boolean incrementalAvailable, boolean filesAreTheSame) {
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

    public void setAppController(AppController appController, SideMenuController sideMenuController) {
        this.appController = appController;
        this.sideMenuController = sideMenuController;
    }
}
