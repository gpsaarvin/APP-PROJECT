package com.smartmedicare.controllers.patient;

import com.smartmedicare.models.DiseaseAnalysis;
import com.smartmedicare.services.AnalysisService;
import com.smartmedicare.utils.DialogUtils;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

public class DiseaseAnalysisController {
    @FXML private TextField symptomField;
    @FXML private FlowPane symptomChips;
    @FXML private TableView<DiseaseAnalysis> conditionsTable;
    @FXML private TableColumn<DiseaseAnalysis, String> conditionColumn;
    @FXML private TableColumn<DiseaseAnalysis, Double> probabilityColumn;
    @FXML private TableColumn<DiseaseAnalysis, String> severityColumn;
    @FXML private PieChart probabilityChart;
    
    private final List<String> symptoms = new ArrayList<>();
    private AnalysisService analysisService;
    
    @FXML
    public void initialize() {
        analysisService = new AnalysisService();
        setupTable();
    }
    
    private void setupTable() {
        conditionColumn.setCellValueFactory(new PropertyValueFactory<>("condition"));
        probabilityColumn.setCellValueFactory(new PropertyValueFactory<>("probability"));
        severityColumn.setCellValueFactory(new PropertyValueFactory<>("severity"));
        
        probabilityColumn.setCellFactory(column -> new TableCell<DiseaseAnalysis, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", item * 100));
                }
            }
        });
    }
    
    /**
     * Adds symptom to the list.
     * Called by FXML when add symptom button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void handleAddSymptom() {
        String symptom = symptomField.getText().trim();
        if (!symptom.isEmpty() && !symptoms.contains(symptom)) {
            symptoms.add(symptom);
            updateSymptomChips();
            symptomField.clear();
        }
    }
    
    private void updateSymptomChips() {
        symptomChips.getChildren().clear();
        for (String symptom : symptoms) {
            Button chip = new Button(symptom);
            chip.getStyleClass().add("symptom-chip");
            chip.setOnAction(e -> {
                symptoms.remove(symptom);
                updateSymptomChips();
            });
            symptomChips.getChildren().add(chip);
        }
    }
    
    /**
     * Analyzes symptoms and shows results.
     * Called by FXML when analyze button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void handleAnalyze() {
        if (symptoms.isEmpty()) {
            DialogUtils.showError("No Symptoms", "No symptoms entered", 
                "Please enter at least one symptom to analyze.");
            return;
        }
        
        try {
            List<DiseaseAnalysis> results = analysisService.analyzeSymptoms(symptoms);
            updateResults(results);
        } catch (Exception e) {
            DialogUtils.showError("Analysis Failed", "Failed to analyze symptoms", 
                "An error occurred during the analysis. Please try again.");
        }
    }
    
    private void updateResults(List<DiseaseAnalysis> results) {
        // Update table
        conditionsTable.setItems(FXCollections.observableArrayList(results));
        
        // Update chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (DiseaseAnalysis result : results) {
            pieChartData.add(new PieChart.Data(
                result.getCondition(), 
                result.getProbability() * 100));
        }
        probabilityChart.setData(pieChartData);
    }
    
    /**
     * Clears analysis results.
     * Called by FXML when clear button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void handleClear() {
        symptoms.clear();
        updateSymptomChips();
        conditionsTable.getItems().clear();
        probabilityChart.setData(FXCollections.emptyObservableList());
    }
    
    /**
     * Books appointment based on analysis.
     * Called by FXML when book appointment button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void handleBookAppointment() {
        // Load appointment booking view through the dashboard controller
        ((PatientDashboardController) getParentController()).showAppointmentBooking();
    }
    
    private Object getParentController() {
        return symptomField.getScene().getUserData();
    }
}