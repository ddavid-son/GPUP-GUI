package app.sideMenu;

import app.mainScreen.AppController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SideMenuController {


    @FXML
    private Button loadXMLBtn;

    @FXML
    private Button findPathBtn;

    @FXML
    private Button FindCircleBtn;

    @FXML
    private Button displayRelatedBtn;

    @FXML
    private Button runTaskBtn;

    private AppController appController;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    void OnDisplayRelatedBtnClick(ActionEvent event) {

    }

    @FXML
    void OnFindCircleBtnClick(ActionEvent event) {

    }

    @FXML
    void OnFindPathBtnClick(ActionEvent event) {

    }

    @FXML
    void OnLoadBtnClick(ActionEvent event) {

    }

    @FXML
    void OnRunTaskBtnClick(ActionEvent event) {

    }
}
