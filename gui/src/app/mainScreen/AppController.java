package app.mainScreen;

import app.graphTableView.GraphTableViewController;
import app.sideMenu.SideMenuController;
import backend.Engine;
import backend.Execution;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;

import java.io.File;

public class AppController {

    @FXML
    private ScrollPane sideMenuComponent;
    @FXML
    private SideMenuController sideMenuComponentController;
    @FXML
    private ScrollPane graphTableViewComponent;
    @FXML
    private GraphTableViewController graphTableViewComponentController;

    private final Engine execution = new Execution();

    @FXML
    public void initialize() {
        if (sideMenuComponentController != null && graphTableViewComponentController != null) {
            sideMenuComponentController.setAppController(this);
            graphTableViewComponentController.setAppController(this);
            setAllComponentsToDisabled();
        }
    }

    protected void setAllComponentsToDisabled() {
        sideMenuComponentController.setAllComponentsToDisabled();
        graphTableViewComponentController.setAllComponentsToDisabled();
    }

    public void loadXML(File selectedFile) {
        try {
            execution.xmlFileLoadingHandler(selectedFile.getAbsolutePath());
            sideMenuComponentController.setAllComponentsToEnabled();
            graphTableViewComponentController.setAllComponentsToEnabled();
            graphTableViewComponentController.loadGraphToTableView(execution.getInfoAboutAllTargets());
            System.out.println("XML file loaded");
        } catch (IllegalArgumentException e) {
            handleErrors(e, "The file you selected is not a valid XML file");
        }
    }

    private void handleErrors(Exception e, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error occurred");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
