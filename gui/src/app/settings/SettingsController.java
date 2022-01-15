package app.settings;

import app.mainScreen.AppController;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;


public class SettingsController {

    @FXML
    private Button theme1;

    @FXML
    private Button theme2;

    @FXML
    private Button theme3;

    @FXML
    private Button animation1;

    @FXML
    private Button animation2;

    @FXML
    private GridPane settingsGridPane;


    private boolean bigger = true;

    Duration duration = Duration.millis(2000);
    private AppController appController;

    @FXML
    void onAnimation1Clicked(ActionEvent event) {
        animation1.setDisable(true);
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> {
                    animation1.setDisable(false);
                });
            } catch (InterruptedException e) {
                //
            }
        }).start();
        RotateTransition rotateTransition = new RotateTransition(duration, animation1);
        rotateTransition.setByAngle(360);
        rotateTransition.play();
    }

    @FXML
    void onAnimation2Clicked(ActionEvent event) {
        if (bigger) {
            // grow to 1.5x and back to 1x
            ScaleTransition scaleTransition = new ScaleTransition(duration, animation2);
            scaleTransition.setByX(1.5);
            scaleTransition.setByY(1.5);
            scaleTransition.play();
            bigger = false;
        } else {
            ScaleTransition scaleTransition2 = new ScaleTransition(duration, animation2);
            scaleTransition2.setFromX(2.2);
            scaleTransition2.setFromY(2.2);
            scaleTransition2.setToX(1);
            scaleTransition2.setToY(1);
            scaleTransition2.play();
            bigger = true;
        }
    }

    @FXML
    void onTheme1Clicked(ActionEvent event) {
        appController.themeCSSPath = "/resources/css/theme1.css";
        appController.setThemeCSSPath(appController.themeCSSPath);
        this.animation1.getScene().getStylesheets().clear();
        this.animation1.getScene().getStylesheets().add(appController.themeCSSPath);
    }

    @FXML
    void onTheme2Clicked(ActionEvent event) {
        appController.themeCSSPath = "/resources/css/theme2.css";
        appController.setThemeCSSPath(appController.themeCSSPath);
        this.animation1.getScene().getStylesheets().clear();
        this.animation1.getScene().getStylesheets().add(appController.themeCSSPath);
    }

    @FXML
    void onTheme3Clicked(ActionEvent event) {
        appController.themeCSSPath = "/resources/css/theme3.css";
        appController.setThemeCSSPath(appController.themeCSSPath);
        this.animation1.getScene().getStylesheets().clear();
        this.animation1.getScene().getStylesheets().add(appController.themeCSSPath);
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }
}
