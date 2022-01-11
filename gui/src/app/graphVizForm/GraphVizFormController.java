package app.graphVizForm;

import app.mainScreen.AppController;
import backend.Engine;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class GraphVizFormController {
    public Button runGraphVizBtn;
    @FXML
    private Button outputFolderBtn;

    @FXML
    private Label folderNameLabel;

    @FXML
    private TextField fileNameTA;

    @FXML
    private Label fileNameLabel;

    @FXML
    private Button displayImageBtn;

    @FXML
    private TextField dotFilePathTF;

    @FXML
    private TextField imagePathTF;

    @FXML
    private Button copyTextFilePathBtn;

    @FXML
    private Button copyImagePathBtn;
    private AppController appController;
    private File outPutPath;
    private Engine execution;

    @FXML
    void onCopyImagePathBtnClicked(ActionEvent event) {

    }

    @FXML
    void onCopyTextFilePathBtnClicked(ActionEvent event) {

    }

    @FXML
    void onDisplayImageBtnClicked(ActionEvent event) {
        // open imagePathTF in OS image-viewer maybe should be in a simple javaFX window
        try {
            appController.openFile(imagePathTF.getText());
        } catch (Exception e) {
            appController.handleErrors(
                    e,
                    "Error - Could not open image file",
                    "Error - Could not open image file"
            );
        }
    }

    @FXML
    void onFileNameTAClicked(ActionEvent event) {

    }

    @FXML
    void onFileNameChanged(InputMethodEvent event) {

    }

    @FXML
    void onRunGraphVizBtnClicked(ActionEvent event) {
        if (outPutPath != null && !fileNameTA.getText().equals("")) {
            String fileName = fileNameTA.getText();
            String dotFilePath = outPutPath.getAbsolutePath() + File.separator + fileName;
            String imagePath = outPutPath.getAbsolutePath() + File.separator + fileName;

            new Thread(() -> {
                execution.makeGraphUsingGraphViz(outPutPath.toString(), fileName);
                Platform.runLater(() -> updateUIAfterGraphviz(dotFilePath, imagePath));
            }).start();

        } else {
            appController.handleErrors(
                    null,
                    outPutPath == null ? "Please select an output folder" : "Please specify a file name",
                    outPutPath == null ? "Error - Output Folder Missing" : "Error - File Name is missing"
            );
        }
    }

    private void updateUIAfterGraphviz(String dotFilePath, String imagePath) {
        dotFilePathTF.setText(dotFilePath + ".viz");
        imagePathTF.setText(imagePath + ".png");
        disableCopyAndDisplayBtns(false);
    }

    private void disableCopyAndDisplayBtns(boolean disable) {
        copyTextFilePathBtn.setDisable(disable);
        copyImagePathBtn.setDisable(disable);
        displayImageBtn.setDisable(disable);
    }

    @FXML
    void onOutputFolderBtnClicked(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Source Folder");
        outPutPath = directoryChooser.showDialog(fileNameTA.getScene().getWindow());
        folderNameLabel.setText(outPutPath == null ? "No Folder Was Selected" : outPutPath.getName());
        disableCopyAndDisplayBtns(true);
    }

    public void setAppController(AppController appController, Engine execution) {
        this.appController = appController;
        this.execution = execution;
    }

    public void setGraphVizController() {
        outputFolderBtn.setGraphic(appController.getIcon("/icons/UploadIcon.png"));
        copyImagePathBtn.setGraphic(appController.getIcon("/icons/CopyIcon.png", 20));
        copyTextFilePathBtn.setGraphic(appController.getIcon("/icons/copyIcon.png", 20));

        this.copyTextFilePathBtn.setOnAction(event -> {
                    appController.copyTextToClipboard(this.dotFilePathTF.getText());
                }
        );
        this.copyImagePathBtn.setOnAction(event -> {
                    appController.copyTextToClipboard(this.imagePathTF.getText());
                }
        );
    }
}
