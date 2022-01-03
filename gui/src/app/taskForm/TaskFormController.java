package app.taskForm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public class TaskFormController {

    @FXML
    private Slider successSlider;

    @FXML
    private Slider warningSlider;

    @FXML
    private RadioButton yesRandomRB;

    @FXML
    private RadioButton noRandomRB;

    @FXML
    private GridPane simulationTabGridPane;

    @FXML
    private Button runBtn;

    @FXML
    private final IntField successField = new IntField(0, 100, 50);

    @FXML
    private final IntField warningField = new IntField(0, 100, 50);

    @FXML
    private final IntField sleepTimeField = new IntField(0, 20000, 1000);


    @FXML
    private void OnRunBtnClicked(ActionEvent event) {


    }

    public void setTaskController() {

        simulationTabGridPane.add(successField, 4, 1);
        simulationTabGridPane.add(warningField, 4, 1);
        simulationTabGridPane.add(sleepTimeField, 2, 2);

        GridPane.setValignment(successField, VPos.TOP);
        GridPane.setValignment(warningField, VPos.BOTTOM);


        successSlider.valueProperty().bindBidirectional(successField.valueProperty());
        warningSlider.valueProperty().bindBidirectional(warningField.valueProperty());

        // make radiobutton only selectable exclusively
        yesRandomRB.setSelected(true);
        noRandomRB.setSelected(false);
    }

    @FXML
    void noRandomRBSelect(ActionEvent event) {
        noRandomRB.setSelected(true);
        yesRandomRB.setSelected(false);
    }

    @FXML
    void yesRandomRBSelect(ActionEvent event) {
        noRandomRB.setSelected(false);
        yesRandomRB.setSelected(true);
    }

    @FXML
    void successSliderDrag(MouseEvent event) {

    }

    @FXML
    void warningSliderDrag(MouseEvent event) {

    }
}
