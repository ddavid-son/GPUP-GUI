package app.graphTableView;

import app.mainScreen.AppController;
import dataTransferObjects.GraphTargetsTypeInfoDTO;
import dataTransferObjects.InfoAboutTargetDTO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class GraphTableViewController {
    private AppController appController;

    @FXML
    private TableView<InfoAboutTargetDTO> graphTable;

    @FXML
    private TableColumn<InfoAboutTargetDTO, String> nameCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, String> typeCol;

    @FXML
    private TableColumn<?, ?> dependsOnCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, Integer> dependsOnImmediateCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, Integer> dependsOnRelatedCol;

    @FXML
    private TableColumn<?, ?> RequiredForCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, Integer> RequiredForImmediateCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, Integer> RequiredForRelatedCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, Integer> serialSetCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, String> dataCol;

    @FXML
    private TableView<GraphTargetsTypeInfoDTO> stateTable;

    @FXML
    private TableColumn<GraphTargetsTypeInfoDTO, Integer> independentCol;

    @FXML
    private TableColumn<GraphTargetsTypeInfoDTO, Integer> rootCol;

    @FXML
    private TableColumn<GraphTargetsTypeInfoDTO, Integer> leafCol;

    @FXML
    private TableColumn<GraphTargetsTypeInfoDTO, Integer> middleCol;

    @FXML
    private TableColumn<GraphTargetsTypeInfoDTO, Integer> totalCol;

    private ObservableList<InfoAboutTargetDTO> dataForTable;

    private ObservableList<GraphTargetsTypeInfoDTO> dataForStateTable;


    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void setAllComponentsToDisabled() {
        graphTable.setDisable(true);
        stateTable.setDisable(true);
    }

    public void setAllComponentsToEnabled() {
        graphTable.setDisable(false);
        stateTable.setDisable(false);
    }

    public void loadGraphToTableView(List<InfoAboutTargetDTO> allTargets) {
        dataForTable = FXCollections.observableArrayList(allTargets);

        nameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTargetName()));
        typeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTargetType().toString()));
        dependsOnImmediateCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDirectDependsOnByCount()).asObject());
        dependsOnRelatedCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDependsOnCount()).asObject());
        RequiredForImmediateCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDirectRequiredByCount()).asObject());
        RequiredForRelatedCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getRequiredForCount()).asObject());
        //serialSetCol.setCellValueFactory(cellData ->
        //      new SimpleStringProperty(cellData.getValue().getSerialSet()));
        dataCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUserData()));

        graphTable.setItems(dataForTable);
    }

    public void loadSummaryToTableView(GraphTargetsTypeInfoDTO graphStateSummary) {
        dataForStateTable = FXCollections.observableArrayList(graphStateSummary);
        independentCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(graphStateSummary.getTotalNumberOfIndependents()).asObject());
        rootCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(graphStateSummary.getTotalNumberOfRoots()).asObject());
        leafCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(graphStateSummary.getTotalNumberOfLeaves()).asObject());
        middleCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(graphStateSummary.getTotalNumberOfMiddles()).asObject());
        totalCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(graphStateSummary.getTotalNumberOfTargets()).asObject());

        stateTable.setItems(dataForStateTable);
    }

    public TableView<InfoAboutTargetDTO> getTargetsGraph() {
        return graphTable;
    }
}
