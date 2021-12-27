package app.findAllPaths;

import app.mainScreen.AppController;
import backend.Engine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FindAllPathsController {
    @FXML
    private ComboBox<String> srcComboBox;

    @FXML
    private ComboBox<String> dstComboBox;

    @FXML
    private Button swapDstSrcBtn;

    @FXML
    private ListView<String> pathsListView;

    private AppController appController;

    @FXML
    void OnDstCBDrop(ActionEvent event) {
    }

    @FXML
    void OnSrcCBDrop(ActionEvent event) {
    }

    public void setAppController(AppController appController) {

        this.appController = appController;
    }

    @FXML
    public void OnSwapSrcToDestBtn(ActionEvent event) {
        String src = srcComboBox.getValue();
        srcComboBox.setValue(dstComboBox.getValue());
        dstComboBox.setValue(src);
    }

    public void loadComboBoxes(List<String> allTargetNames, Engine execution) {
        srcComboBox.getItems().addAll(allTargetNames);
        dstComboBox.getItems().addAll(allTargetNames);

        setSrcComboBoxListener(execution);
        setDstComboBoxListener(execution);

        pathsListView.setPlaceholder((Node) new Text("No paths found"));
    }

    private void setSrcComboBoxListener(Engine execution) {
        srcComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (!newValue.equals(oldValue) && !newValue.isEmpty()) {
                try {
                    Set<List<String>> allPaths = execution.findAllPathsBetweenTargets(newValue, dstComboBox.getValue());
                    List<String> res = allPaths.stream()
                            .map(path -> String.join(" -> ", path))
                            .collect(Collectors.toList());
                    pathsListView.getItems().setAll(res);
                } catch (Exception e) {
                    //TODO: handle exception - not really needed
                }
            }
        });
    }

    private void setDstComboBoxListener(Engine execution) {
        dstComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (!newValue.equals(oldValue) && !newValue.isEmpty()) {
                try {
                    Set<List<String>> allPaths = execution.findAllPathsBetweenTargets(srcComboBox.getValue(), newValue);
                    List<String> res = allPaths.stream()
                            .map(path -> String.join(" -> ", path))
                            .collect(Collectors.toList());
                    pathsListView.getItems().setAll(res);
                } catch (Exception e) {
                    //TODO: handle exception not really needed
                }
            }
        });
    }

}

