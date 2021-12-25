package app.mainScreen;

import app.grapTableView.GraphTableViewController;
import app.sideMenu.SideMenuController;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;

public class AppController {

    @FXML
    private ScrollPane sideMenuComponent;
    @FXML
    private SideMenuController sideMenuComponentController;
    @FXML
    private ScrollPane graphTableViewComponent;
    @FXML
    private GraphTableViewController graphTableViewComponentController;

    @FXML
    public void initialize() {
        if (sideMenuComponentController != null && graphTableViewComponentController != null) {
            sideMenuComponentController.setAppController(this);
            graphTableViewComponentController.setAppController(this);
        }
    }


}
