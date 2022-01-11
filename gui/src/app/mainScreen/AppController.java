package app.mainScreen;

import app.circleDisplay.CircleDisplayController;
import app.findAllPaths.FindAllPathsController;
import app.graphTableView.GraphTableViewController;
import app.relatedView.RelatedViewController;
import app.sideMenu.SideMenuController;
import backend.Engine;
import backend.Execution;
import backend.GraphManager;
import backend.argumentsDTO.TaskArgs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppController {

    @FXML
    private ScrollPane sideMenuComponent;
    @FXML
    private SideMenuController sideMenuComponentController;
    @FXML
    private ScrollPane graphTableViewComponent;
    @FXML
    private GraphTableViewController graphTableViewComponentController;

    public List<String> targetFromPreviousRun;


    private final Engine execution = new Execution();
    private final String FIND_ALL_PATHS_FXML_FILE = "/resources/findAllPaths.fxml";
    private final String CIRCLE_DISPLAY_FXML_FILE = "/resources/circleDisplay.fxml";

    private File activeFile;

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
            activeFile = selectedFile;
        } catch (IllegalArgumentException e) {
            handleErrors(
                    e,
                    "",
                    "The file you selected is not a valid XML file"
            );
        }
    }

    public File getActiveFile() {
        return activeFile;
    }

    public void handleErrors(Exception e, String bodyMessage, String headerMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error occurred");
        alert.setHeaderText(headerMessage);
        if (e != null) {
            alert.setContentText(e.getMessage());
        } else {
            alert.setContentText(bodyMessage);
        }
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
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Find all circles");
            stage.setScene(new Scene(root));
            stage.getScene().getStylesheets().add(getClass().getResource("/app/circleDisplay/displayCircle.css").toExternalForm());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Node getIcon(String resourceName) {
        return getIcon(resourceName, 50);
    }

    public Node getIcon(String resourceName, int size) {
        Image image = new Image("/resources" + resourceName);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        return imageView;
    }

    public void displayRelated() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/resources/relatedView.fxml");
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            RelatedViewController relatedViewController = fxmlLoader.getController();
            relatedViewController.setAppController(this, execution);

            relatedViewController.loadTargetList();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("display related");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public boolean taskHasTargetsSelected() {
        if (!graphTableViewComponentController.hasTargetSelected()) {
            handleErrors(
                    null,
                    "You must select at least one target before preforming this action",
                    "Error running task"
            );

            return false;
        }

        return true;
    }

    public Engine getExecution() {

        return execution;
    }

    public void runTask(TaskArgs taskArgs) {
        BorderPane mainScreen = (BorderPane) graphTableViewComponent.getScene().getRoot();
        List<String> targetNames = graphTableViewComponentController.getSelectedTargetNames();
        if (taskArgs.isWhatIf()) {
            targetNames = getAllWhatIfResults(targetNames, taskArgs.getRelationType());
        }
        targetFromPreviousRun = targetNames; // here for the visibility of Incremental button
        taskArgs.getTargetsSelectedForGraph().addAll(targetNames);
        delegateExecutionOfTaskToAnotherThread(taskArgs);

        //TODO: make update methods to get the data from the task and update the graph LIVE
    }

    private List<String> getAllWhatIfResults(List<String> targetNames, GraphManager.RelationType relationType) {
        List<String> whatIfResult = new ArrayList<>();
        targetNames.forEach(targetName -> {
            whatIfResult.addAll(execution.getWhatIf(targetName, relationType).getAllRelated());
        });
        targetNames.addAll(whatIfResult);
        return targetNames.stream().distinct().collect(Collectors.toList());
    }

    private void delegateExecutionOfTaskToAnotherThread(TaskArgs taskArgs) {
        Thread thread = new Thread(() -> {
            try {
                execution.runTaskOnGraph(taskArgs);
            } catch (Exception e) {
                Platform.runLater(() -> handleErrors(
                        e,
                        e.getMessage(),
                        "Error running task"));
            }
            // TODO: maybe down here will handle the summary data fetching with runLater to update the UI
        });
        thread.setName("Task thread");
        thread.start();

    }

    public boolean currentSelectedTargetsAreTheSameAsPreviousRun() {
        List<String> targetNames = graphTableViewComponentController.getSelectedTargetNames();
        return targetNames.equals(targetFromPreviousRun);
        //todo: this is not accurate when the order of the list is changed(maybe because of sorting option of the table)
    }

    public void copyTextToClipboard(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    public void openFile(String imagePath) {
        try {
            Desktop.getDesktop().open(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}