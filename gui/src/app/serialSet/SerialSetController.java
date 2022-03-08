package app.serialSet;

import app.mainScreen.AppController;
import backend.Engine;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

public class SerialSetController {

    @FXML
    private ListView<String> serialSetListView;

    @FXML
    private GridPane serialSetGridPane;

    @FXML
    private ListView<String> targetInSerialSet;

    private AppController appController;

    private Engine execution;


    public void setAppController(AppController appController, Engine execution) {
        this.appController = appController;
        this.execution = execution;
    }

    public void setSerialSet() {
        serialSetListView.getItems().addAll(execution.getSerialSetList());
        serialSetListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            targetInSerialSet.getItems().setAll(execution.getSerialSetTarget(newValue));
        });
    }
}
