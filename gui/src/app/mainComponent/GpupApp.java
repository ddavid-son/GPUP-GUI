package app.mainComponent;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;

public class GpupApp extends Application {

    private final String ROOT_MAIN_FXML = "/resources/MainScreen.fxml";
    private final String MENU_FXML = "/resources/SideMenu.fxml";
    private final String TABLE_VIEW_FXML = "/resources/GraphTableView.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Gpup");

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(ROOT_MAIN_FXML);
        fxmlLoader.setLocation(url);
        BorderPane rootMain = fxmlLoader.load(url.openStream());
        //HeaderController headerController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(TABLE_VIEW_FXML);
        fxmlLoader.setLocation(url);
        ScrollPane tableViewComponent = fxmlLoader.load(url.openStream());

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(MENU_FXML);
        fxmlLoader.setLocation(url);
        ScrollPane menuComponent = fxmlLoader.load(url.openStream());

        rootMain.setLeft(menuComponent);
        rootMain.setCenter(tableViewComponent);

        Scene scene = new Scene(rootMain);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
