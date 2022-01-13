package app.sideMenu;

import app.graphVizForm.GraphVizFormController;
import app.mainScreen.AppController;
import app.taskForm.TaskFormController;
import backend.Engine;
import backend.argumentsDTO.TaskArgs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SideMenuController {

    private AppController appController;
    @FXML
    public Button graphVizBtn;

    @FXML
    private Button loadXMLBtn;

    @FXML
    private Button findPathBtn;

    @FXML
    private Button findCircleBtn;

    @FXML
    private Button displayRelatedBtn;

    @FXML
    private Button runTaskBtn;

    @FXML
    private Button settingBtn;

    private final String TASK_FORM_FXML = "/resources/TaskForm.fxml";
    private final String GRAPH_VIZ_FORM = "/resources/graphVizForm.fxml";

    private Engine execution;
    public TaskArgs.TaskType taskType;

    private File currentFileInTask = new File("");

    @FXML
    private void OnLoadBtnClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load XML File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML Files", "*.XML")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            appController.loadXML(selectedFile);
        }
        execution = appController.getExecution();
    }

    @FXML
    private void OnDisplayRelatedBtnClick(ActionEvent event) {
        //execution.makeGraphUsingGraphViz(); // TODO: uncomment this line - GraphViz
        appController.displayRelated();
    }

    @FXML
    private void OnFindCircleBtnClick(ActionEvent event) {

        appController.findAllCircles();
    }

    @FXML
    private void OnFindPathBtnClick(ActionEvent event) {

        appController.findAllPaths();
    }

    @FXML
    private void OnRunTaskBtnClick(ActionEvent event) {
        if (!appController.taskHasTargetsSelected())
            return;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(TASK_FORM_FXML);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            TaskFormController taskFormController = fxmlLoader.getController();

            taskFormController.setAppController(appController, this);
            taskFormController.setTaskController(
                    execution.getMaxThreadCount(),
                    execution.incrementalAvailable(),
                    currentFileInTask.equals(appController.getActiveFile())
            );

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Task Form");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAllComponentsToDisabled(boolean disableXmlLoadBtn) {
        loadXMLBtn.setDisable(disableXmlLoadBtn);
        findPathBtn.setDisable(true);
        findCircleBtn.setDisable(true);
        displayRelatedBtn.setDisable(true);
        runTaskBtn.setDisable(true);
        graphVizBtn.setDisable(true);
        settingBtn.setDisable(true);
    }

    public void setAllComponentsToEnabled() {
        loadXMLBtn.setDisable(false);
        findPathBtn.setDisable(false);
        findCircleBtn.setDisable(false);
        displayRelatedBtn.setDisable(false);
        runTaskBtn.setDisable(false);
        graphVizBtn.setDisable(false);
        settingBtn.setDisable(false);
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
        setButtonsIcon();
    }

    private void setButtonsIcon() {
        loadXMLBtn.setGraphic(appController.getIcon("/icons/xmlIcon1.png"));
        findPathBtn.setGraphic(appController.getIcon("/icons/pathIcon1.png"));
        findCircleBtn.setGraphic(appController.getIcon("/icons/circleIcon.png"));
        displayRelatedBtn.setGraphic(appController.getIcon("/icons/relatedIcon1.png"));
        runTaskBtn.setGraphic(appController.getIcon("/icons/launchIcon.png"));
        settingBtn.setGraphic(appController.getIcon("/icons/settingIcon.png"));
        graphVizBtn.setGraphic(appController.getIcon("/icons/gvIcon.png"));
    }

    @FXML
    private void OnSettingBtnClicked(ActionEvent event) {
    }

    public void setNewFileForTask() {

        currentFileInTask = appController.getActiveFile();
    }

    public void graphVizBtnClicked(ActionEvent actionEvent) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(GRAPH_VIZ_FORM);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            GraphVizFormController graphVizFormController = fxmlLoader.getController();

            graphVizFormController.setAppController(appController, execution);
            graphVizFormController.setGraphVizController(

            );

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("GraphViz");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //execution.makeGraphUsingGraphViz();
    }
}
