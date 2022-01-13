package app.taskView;

import backend.Target;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class TaskCircle {
    @FXML
    private Button transparentBtn;
    private StackPane stackPane;
    private Label targetName;
    private Circle circle;

    public TaskCircle() {
        stackPane = new StackPane();
        targetName = new Label("emptyCtor");
        targetName.resize(90, 30);
        circle = new Circle(50, Paint.valueOf("#FFD700"));
        transparentBtn = new Button();
        transparentBtn.resize(80, 80);
        transparentBtn.opacityProperty().setValue(0);
        stackPane.getChildren().add(circle);
        stackPane.getChildren().add(targetName);
        stackPane.getChildren().add(transparentBtn);
        stackPane.setId("emptyCtor");

    }

    public TaskCircle(String name, Target.TargetState state) {
        stackPane = new StackPane();
        targetName = new Label(name);
        targetName.resize(90, 30);
        circle = new Circle(50, Paint.valueOf("red"));
        transparentBtn = new Button();
        transparentBtn.resize(80, 80);
        transparentBtn.opacityProperty().setValue(0);
        stackPane.getChildren().add(circle);
        stackPane.getChildren().add(targetName);
        stackPane.getChildren().add(transparentBtn);
        stackPane.setId(name);
/*        transparentBtb.setOnAction(event -> {
            if (state == Target.TargetState.IN_PROCESS) {
                circle.setFill(Paint.valueOf("#FFD700"));
            } else if (state == Target.TargetState.FROZEN) {
                circle.setFill(Paint.valueOf("#00FF00"));
            } else if (state == Target.TargetState.FAILURE) {
                circle.setFill(Paint.valueOf("#FF0000"));
            } else if (state == Target.TargetState.SKIPPED) {
                circle.setFill(Paint.valueOf("#FFFFFF"));
            } else if (state == Target.TargetState.SUCCESS || state == Target.TargetState.WARNING) {
                circle.setFill(Paint.valueOf("#FFFF00"));
            }
        });*/

    }

    public StackPane getStackPane() {
        return stackPane;
    }


}
