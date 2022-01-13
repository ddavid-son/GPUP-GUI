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

    public TaskCircle(String name, Target.TargetState state) {
        stackPane = new StackPane();
        targetName = new Label(name);
        targetName.resize(90, 30);
        circle = new Circle(50, Paint.valueOf("BLUE"));
        transparentBtn = new Button();
        transparentBtn.resize(80, 80);
        transparentBtn.opacityProperty().setValue(0);
        stackPane.getChildren().add(circle);
        stackPane.getChildren().add(targetName);
        stackPane.getChildren().add(transparentBtn);
        stackPane.setId(name);
    }

    public TaskCircle() {
        stackPane = new StackPane();
        targetName = new Label("emptyCtor");
        targetName.resize(90, 30);
        circle = new Circle(50, Paint.valueOf("BLUE"));
        transparentBtn = new Button();
        transparentBtn.resize(80, 80);
        transparentBtn.opacityProperty().setValue(0);
        stackPane.getChildren().add(circle);
        stackPane.getChildren().add(targetName);
        stackPane.getChildren().add(transparentBtn);
        stackPane.setId("emptyCtor");
    }

    public void setStateColor(Target.TargetState state) {
        switch (state) {
            case SUCCESS:
                if (!circle.getFill().equals(Paint.valueOf("YELLOW")))
                    circle.setFill(Paint.valueOf("GREEN"));
                break;
            case FAILURE:
                circle.setFill(Paint.valueOf("RED"));
                break;
            case WARNING:
                circle.setFill(Paint.valueOf("YELLOW"));
                break;
            case IN_PROCESS:
                circle.setFill(Paint.valueOf("ORANGE"));
                break;
            case SKIPPED:
                circle.setFill(Paint.valueOf("GREY"));
                break;
            case FROZEN:
                circle.setFill(Paint.valueOf("BLUE"));
                break;
        }
    }

    public StackPane getStackPane() {
        return stackPane;
    }


}
