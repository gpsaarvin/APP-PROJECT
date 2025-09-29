package com.smartmedicare.controllers.doctor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.smartmedicare.models.DiseaseResult;
import com.smartmedicare.models.MedicalRecord;
import com.smartmedicare.models.Patient;
import com.smartmedicare.models.Symptom;
import com.smartmedicare.services.AIDiseaseDetectionService;
import com.smartmedicare.services.MedicalRecordDAO;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

@SuppressWarnings("unused")
public class DiseaseDetectionController implements PatientAwareController {
    @FXML private Label patientNameLabel;
    @FXML private Label dateLabel;
    @FXML private ComboBox<String> symptomComboBox;
    @FXML private ComboBox<String> severityComboBox;
    @FXML private TextField durationField;
    @FXML private TextArea notesArea;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private VBox resultsBox;
    
    @FXML private TableView<Symptom> symptomsTable;
    @FXML private TableColumn<Symptom, String> symptomColumn;
    @FXML private TableColumn<Symptom, String> severityColumn;
    @FXML private TableColumn<Symptom, String> durationColumn;
    @FXML private TableColumn<Symptom, Void> actionsColumn;
    
    @FXML private TableView<DiseaseResult> resultsTable;
    @FXML private TableColumn<DiseaseResult, String> diseaseColumn;
    @FXML private TableColumn<DiseaseResult, Double> probabilityColumn;
    @FXML private TextArea recommendationArea;

    private Patient currentPatient;
    private final AIDiseaseDetectionService aiService;
    private final MedicalRecordDAO medicalRecordDAO;
    private final ObservableList<Symptom> symptoms;
    private final DateTimeFormatter dateFormatter;

    public DiseaseDetectionController() {
        aiService = new AIDiseaseDetectionService();
        medicalRecordDAO = new MedicalRecordDAO();
        symptoms = FXCollections.observableArrayList();
        dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    }

    @FXML
    private void initialize() {
        setupSymptomControls();
        setupTables();
        updateDateTime();
    }

    private void setupSymptomControls() {
        // Initialize symptom options
        symptomComboBox.getItems().addAll(Arrays.asList(
            "Fever", "Cough", "Fatigue", "Shortness of breath", "Headache",
            "Body aches", "Sore throat", "Loss of taste/smell", "Nausea",
            "Diarrhea", "Chest pain", "Dizziness", "Runny nose", "Sneezing",
            "Wheezing", "Abdominal pain", "Joint pain", "Muscle weakness",
            "Vision problems", "Skin rash"
        ));

        // Initialize severity options
        severityComboBox.getItems().addAll(
            "Mild", "Moderate", "Severe"
        );
    }

    private void setupTables() {
        // Setup symptoms table
        symptomColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        severityColumn.setCellValueFactory(new PropertyValueFactory<>("severity"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Remove");
            {
                deleteButton.setOnAction(e -> {
                    Symptom symptom = getTableView().getItems().get(getIndex());
                    symptoms.remove(symptom);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        symptomsTable.setItems(symptoms);

        // Setup results table
        diseaseColumn.setCellValueFactory(new PropertyValueFactory<>("disease"));
        probabilityColumn.setCellValueFactory(data -> 
            new SimpleDoubleProperty(data.getValue().getProbability() * 100).asObject()
        );
        
        // Format probability as percentage
        probabilityColumn.setCellFactory(col -> new TableCell<DiseaseResult, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", value));
                }
            }
        });

        // Add row styling based on probability
        resultsTable.setRowFactory(tv -> new TableRow<DiseaseResult>() {
            @Override
            protected void updateItem(DiseaseResult result, boolean empty) {
                super.updateItem(result, empty);
                getStyleClass().removeAll(
                    "high-probability", 
                    "medium-probability", 
                    "low-probability"
                );
                
                if (result != null) {
                    if (result.getProbability() > 0.7) {
                        getStyleClass().add("high-probability");
                    } else if (result.getProbability() > 0.4) {
                        getStyleClass().add("medium-probability");
                    } else {
                        getStyleClass().add("low-probability");
                    }
                }
            }
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleAddSymptom() {
        String symptomName = symptomComboBox.getValue();
        String severity = severityComboBox.getValue();
        String duration = durationField.getText();

        if (symptomName == null || severity == null || duration.isEmpty()) {
            showAlert("Missing Information", "Please fill in all symptom details.");
            return;
        }

        symptoms.add(new Symptom(symptomName, severity, duration, ""));
        
        // Clear inputs
        symptomComboBox.setValue(null);
        severityComboBox.setValue(null);
        durationField.clear();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleAnalyzeSymptoms() {
        if (symptoms.isEmpty()) {
            showAlert("No Symptoms", "Please add at least one symptom to analyze.");
            return;
        }

        progressIndicator.setVisible(true);
        resultsBox.setVisible(false);

        // Run analysis in background
        new Thread(() -> {
            List<DiseaseResult> results = aiService.analyzeSymptoms(new ArrayList<>(symptoms));
            String recommendation = aiService.getAIRecommendation(results, new ArrayList<>(symptoms));

            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                resultsTable.getItems().setAll(results);
                recommendationArea.setText(recommendation);
                resultsBox.setVisible(true);
                progressIndicator.setVisible(false);
            });
        }).start();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleSaveToRecord() {
        if (resultsTable.getItems().isEmpty()) {
            return;
        }

        MedicalRecord record = new MedicalRecord();
        record.setPatientId(currentPatient.getId());
        record.setRecordDate(LocalDateTime.now());
        
        // Combine symptoms into a document
        StringBuilder symptomsText = new StringBuilder();
        symptoms.forEach(s -> symptomsText.append(String.format(
            "- %s (%s, Duration: %s)\n", 
            s.getName(), s.getSeverity(), s.getDuration()
        )));

        // Get the top disease result
        DiseaseResult topResult = resultsTable.getItems().get(0);
        
        record.setDiagnosis(topResult.getDisease());
        record.setAiPrediction(String.format("%.1f%%", topResult.getProbability() * 100));
        record.setSeverity(calculateOverallSeverity());
        record.setTreatmentPlan(recommendationArea.getText());
        record.setNotes(String.format(
            "Symptoms:\n%s\nAdditional Notes: %s",
            symptomsText.toString(),
            notesArea.getText()
        ));

        try {
            medicalRecordDAO.insert(medicalRecordDAO.entityToDocument(record));
            showAlert(
                "Success", 
                "AI analysis results have been saved to the patient's medical record.",
                Alert.AlertType.INFORMATION
            );
            handleClear();
        } catch (Exception e) {
            showAlert(
                "Error", 
                "Failed to save to medical record: " + e.getMessage(),
                Alert.AlertType.ERROR
            );
        }
    }

    private String calculateOverallSeverity() {
        long severeCases = symptoms.stream()
            .filter(s -> "Severe".equals(s.getSeverity()))
            .count();
        long moderateCases = symptoms.stream()
            .filter(s -> "Moderate".equals(s.getSeverity()))
            .count();

        if (severeCases > symptoms.size() / 3) {
            return "SEVERE";
        } else if (moderateCases > symptoms.size() / 2) {
            return "MODERATE";
        } else {
            return "MILD";
        }
    }

    @FXML
    private void handleClear() {
        symptoms.clear();
        notesArea.clear();
        resultsBox.setVisible(false);
        updateDateTime();
    }

    private void updateDateTime() {
        dateLabel.setText(LocalDateTime.now().format(dateFormatter));
    }

    private void showAlert(String title, String content) {
        showAlert(title, content, Alert.AlertType.WARNING);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        patientNameLabel.setText(patient.getName());
        handleClear();
    }
}