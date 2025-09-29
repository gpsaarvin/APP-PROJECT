package com.smartmedicare.controllers.patient;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.smartmedicare.models.Doctor;
import com.smartmedicare.models.MedicalRecord;
import com.smartmedicare.models.Patient;
import com.smartmedicare.services.AuthenticationService;
import com.smartmedicare.services.DoctorDAO;
import com.smartmedicare.services.MedicalRecordDAO;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

@SuppressWarnings("unused")
public class MedicalHistoryController {
    @FXML private ComboBox<String> severityFilter;
    @FXML private TableView<MedicalRecord> recordsTable;
    @FXML private TableColumn<MedicalRecord, String> dateColumn;
    @FXML private TableColumn<MedicalRecord, String> diagnosisColumn;
    @FXML private TableColumn<MedicalRecord, String> severityColumn;
    @FXML private TableColumn<MedicalRecord, String> aiPredictionColumn;
    @FXML private TableColumn<MedicalRecord, String> doctorColumn;
    @FXML private TableColumn<MedicalRecord, Void> actionsColumn;
    
    @FXML private VBox detailsSection;
    @FXML private TextArea symptomsArea;
    @FXML private TextArea vitalSignsArea;
    @FXML private TextArea treatmentPlanArea;
    @FXML private TextArea notesArea;
    @FXML private Label messageLabel;

    private final MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private Patient patient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    public void initialize() {
        patient = (Patient) AuthenticationService.getInstance().getCurrentUser();
        
        // Initialize severity filter
        severityFilter.getItems().addAll("All", "MILD", "MODERATE", "SEVERE");
        severityFilter.setValue("All");
        severityFilter.setOnAction(e -> loadRecords());

        // Configure table columns
        dateColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getRecordDate().format(formatter)));
        
        diagnosisColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDiagnosis()));
        
        severityColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getSeverity()));
        
        aiPredictionColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getAiPrediction()));
        
        doctorColumn.setCellValueFactory(data -> {
            MedicalRecord record = data.getValue();
            Doctor doctor = doctorDAO.findById(record.getDoctorId());
            return new SimpleStringProperty(doctor != null ? doctor.getName() : "");
        });

        // Configure actions column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("View Details");
            
            {
                viewButton.setOnAction(e -> showDetails(getTableView().getItems().get(getIndex())));
                viewButton.getStyleClass().add("secondary-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });

        // Load initial data
        loadRecords();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleUploadReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Medical Report");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            // TODO: Implement file upload and AI analysis
            showMessage("Report upload functionality coming soon...", false);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleCloseDetails() {
        detailsSection.setVisible(false);
        detailsSection.setManaged(false);
    }

    private void showDetails(MedicalRecord record) {
        // Format symptoms
        String symptoms = record.getSymptoms().entrySet().stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .collect(Collectors.joining("\n"));
        symptomsArea.setText(symptoms);

        // Format vital signs
        String vitalSigns = record.getVitalSigns().entrySet().stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .collect(Collectors.joining("\n"));
        vitalSignsArea.setText(vitalSigns);

        treatmentPlanArea.setText(record.getTreatmentPlan());
        notesArea.setText(record.getNotes());

        detailsSection.setVisible(true);
        detailsSection.setManaged(true);
    }

    private void loadRecords() {
        String severity = severityFilter.getValue();
        List<MedicalRecord> records;

        if ("All".equals(severity)) {
            records = medicalRecordDAO.findByPatientId(patient.getId());
        } else {
            records = medicalRecordDAO.findByPatientIdAndSeverity(patient.getId(), severity);
        }

        recordsTable.getItems().setAll(records);
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("error-message", "success-message");
        messageLabel.getStyleClass().add(isError ? "error-message" : "success-message");
        messageLabel.setVisible(true);
    }
}