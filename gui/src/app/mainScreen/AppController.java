package app.mainScreen;

import app.circleDisplay.CircleDisplayController;
import app.findAllPaths.FindAllPathsController;
import app.graphTableView.GraphTableViewController;
import app.sideMenu.SideMenuController;
import backend.Engine;
import backend.Execution;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
    private final String FIND_ALL_PATHS_FXML_FILE = "/resources/findAllPaths.fxml";
    private final String CIRCLE_DISPLAY_FXML_FILE = "/resources/circleDisplay.fxml";

    @FXML
    public void initialize() {
        if (sideMenuComponentController != null && graphTableViewComponentController != null) {
            sideMenuComponentController.setAppController(this);
            graphTableViewComponentController.setAppController(this);
            setAllComponentsToDisabled();
        }
    }

    protected void setAllComponentsToDisabled() {
        sideMenuComponentController.setAllComponentsToDisabled(false);
        graphTableViewComponentController.setAllComponentsToDisabled();
    }

    public void loadXML(File selectedFile) {
        try {
            execution.xmlFileLoadingHandler(selectedFile.getAbsolutePath());
            sideMenuComponentController.setAllComponentsToEnabled();
            graphTableViewComponentController.setAllComponentsToEnabled();
            graphTableViewComponentController.loadGraphToTableView(execution.getInfoAboutAllTargets());
            graphTableViewComponentController.loadSummaryToTableView(execution.getGraphInfo());
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

    public void findAllPaths() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(FIND_ALL_PATHS_FXML_FILE);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            FindAllPathsController PathFindPopUpWindow = fxmlLoader.getController();
            PathFindPopUpWindow.setAppController(this);

            PathFindPopUpWindow.loadComboBoxes(execution.getAllTargetNames(), execution);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Find all paths");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findAllCircles() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(CIRCLE_DISPLAY_FXML_FILE);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            CircleDisplayController circleDisplay = fxmlLoader.getController();
            circleDisplay.setAppController(this);

            circleDisplay.displayCircles(execution.getAllTargetNames(), execution);

            Stage stage = new Stage();
            stage.setTitle("Find all circles");
            stage.setScene(new Scene(root));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/app/circleDisplay/diplayCircle.css").toExternalForm());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
