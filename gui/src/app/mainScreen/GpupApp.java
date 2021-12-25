package app.mainScreen;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class GpupApp extends Application {

    private final String APP_FXML_INCLUDE_RESOURCE = "/resources/mainScreen.fxml";
    //private final String MENU_FXML = "/resources/sideMenu.fxml";
    //private final String TABLE_VIEW_FXML = "/resources/graphTableView.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Gpup");

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(APP_FXML_INCLUDE_RESOURCE);
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
