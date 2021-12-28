package app.circleDisplay;

import app.mainScreen.AppController;
import backend.Engine;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        //targetChooserBox.getChildren().addAll(createTextFields(allTargetNames, execution));
        targetChooserBox.getChildren().addAll(createCircleNode(allTargetNames, execution));
    }

    private List<StackPane> createCircleNode(List<String> allTargetNames, Engine execution) {
        List<StackPane> circleNodes = new ArrayList<>();
        for (String targetName : allTargetNames) {
            Text text = new Text(targetName);
            text.setId("txt");
            Circle circle = new Circle(20, Paint.valueOf("#FF0000"));
            text.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
            StackPane stack = new StackPane();
            stack.setId(targetName);
            stack.getChildren().addAll(circle, text);
            circleNodes.add(stack);
        }

        circleNodes.forEach(pane -> pane.setOnMouseClicked(event -> {
            List<String> selectedTargets = execution.findIfTargetIsInACircle(pane.getId());
            circleDisplayBox.getChildren().setAll(
                    selectedTargets.stream()
                            .map(Text::new)
                            .collect(Collectors.toList()));
            if (selectedTargets.isEmpty())
                circleDisplayBox.getChildren().setAll(new Text(pane.getId() + " is not in a circle"));
        }));
        
        return circleNodes;
    }

    private List<Text> createTextFields(List<String> allTargetNames, Engine execution) {
        List<Text> textFields = new ArrayList<>();
        for (String targetName : allTargetNames) {
            Text textField = new Text(targetName);
            textField.setId("txt");
            textFields.add(textField);
        }

        textFields.forEach(textField -> textField.setOnMouseClicked(event -> {
            List<String> selectedTargets = execution.findIfTargetIsInACircle(textField.getText());
            circleDisplayBox.getChildren().setAll(
                    selectedTargets.stream()
                            .map(Text::new)
                            .collect(Collectors.toList()));
            if (selectedTargets.isEmpty())
                circleDisplayBox.getChildren().setAll(new Text(textField.getText() + " is not in a circle"));
        }));

        return textFields;
    }
}
