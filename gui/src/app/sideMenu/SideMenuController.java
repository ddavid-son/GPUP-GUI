package app.sideMenu;

import app.mainScreen.AppController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

import java.io.File;

public class SideMenuController {

    private AppController appController;

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
    }

    @FXML
    private void OnDisplayRelatedBtnClick(ActionEvent event) {
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

    }

    public void setAllComponentsToDisabled(boolean disableXmlLoadBtn) {
        loadXMLBtn.setDisable(disableXmlLoadBtn);
        findPathBtn.setDisable(true);
        findCircleBtn.setDisable(true);
        displayRelatedBtn.setDisable(true);
        runTaskBtn.setDisable(true);
    }

    public void setAllComponentsToEnabled() {
        loadXMLBtn.setDisable(false);
        findPathBtn.setDisable(false);
        findCircleBtn.setDisable(false);
        displayRelatedBtn.setDisable(false);
        runTaskBtn.setDisable(false);
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
        setButtonsIcon();
    }

    private void setButtonsIcon() {
        loadXMLBtn.setGraphic(appController.getIcon("/icons/xmlIcon.png"));
        findPathBtn.setGraphic(appController.getIcon("/icons/pathIcon.png"));
        findCircleBtn.setGraphic(appController.getIcon("/icons/circleIcon.png"));
        displayRelatedBtn.setGraphic(appController.getIcon("/icons/relatedIcon.png"));
        runTaskBtn.setGraphic(appController.getIcon("/icons/launchIcon.png"));
    }
}
