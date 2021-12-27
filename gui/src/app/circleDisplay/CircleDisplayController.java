package app.circleDisplay;

import app.mainScreen.AppController;
import backend.Engine;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class CircleDisplayController {
    @FXML
    private HBox targetChooserBox;

    @FXML
    private HBox circleDisplayBox;

    private AppController appController;


    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void displayCircles(List<String> allTargetNames, Engine execution) {
        // create text fields for each target
        targetChooserBox.getChildren().addAll(createTextFields(allTargetNames));
    }

    private List<Text> createTextFields(List<String> allTargetNames) {
        List<Text> textFields = new ArrayList<>();
        for (String targetName : allTargetNames) {
            Text textField = new Text(targetName);
            textFields.add(textField);
        }
        return textFields;
    }
}
