package com.smartmedicare.controllers.patient;

import com.smartmedicare.dao.PrescriptionDAO;
import com.smartmedicare.models.Prescription;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PrescriptionsController {
    @FXML private TableView<Prescription> prescriptionsTable;
    @FXML private TableColumn<Prescription, String> dateColumn;
    @FXML private TableColumn<Prescription, String> doctorColumn;
    @FXML private TableColumn<Prescription, String> diagnosisColumn;
    @FXML private TableColumn<Prescription, String> medicinesColumn;
    @FXML private TableColumn<Prescription, String> followUpColumn;
    @FXML private TableColumn<Prescription, String> actionsColumn;
    
    @FXML private VBox detailsSection;
    @FXML private TextArea medicinesArea;
    @FXML private TextArea diagnosisArea;
    @FXML private TextArea notesArea;
    @FXML private Label followUpLabel;
    @FXML private Label messageLabel;

    private PrescriptionDAO prescriptionDAO;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private String currentPatientId;

    @FXML
    private void initialize() {
        // Configure table columns
        dateColumn.setCellValueFactory(data -> {
            if (data.getValue().getDateIssued() != null) {
                return new SimpleStringProperty(data.getValue().getDateIssued().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });

        doctorColumn.setCellValueFactory(data -> data.getValue().doctorNameProperty());
        diagnosisColumn.setCellValueFactory(data -> data.getValue().diagnosisProperty());
        medicinesColumn.setCellValueFactory(data -> data.getValue().medicinesProperty());
        
        followUpColumn.setCellValueFactory(data -> {
            if (data.getValue().getFollowUpDate() != null) {
                return new SimpleStringProperty(data.getValue().getFollowUpDate().format(dateFormatter));
            }
            return new SimpleStringProperty("No follow-up");
        });

        // Configure action column with View button
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            {
                viewButton.setOnAction(event -> showDetails(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });

        // Add selection listener to show details
        prescriptionsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showDetails(newSelection);
                }
            }
        );
    }

    public void setCurrentPatientId(String patientId) {
        this.currentPatientId = patientId;
        loadPrescriptions();
    }

    private void loadPrescriptions() {
        try {
            List<Prescription> prescriptions = prescriptionDAO.findByPatientId(currentPatientId);
            prescriptionsTable.getItems().setAll(prescriptions);
            messageLabel.setVisible(prescriptions.isEmpty());
            messageLabel.setText(prescriptions.isEmpty() ? "No prescriptions found" : "");
        } catch (Exception e) {
            messageLabel.setVisible(true);
            messageLabel.setText("Error loading prescriptions: " + e.getMessage());
        }
    }

    private void showDetails(Prescription prescription) {
        if (prescription != null) {
            medicinesArea.setText(prescription.getMedicines());
            diagnosisArea.setText(prescription.getDiagnosis());
            notesArea.setText(prescription.getNotes());
            
            if (prescription.getFollowUpDate() != null) {
                followUpLabel.setText(prescription.getFollowUpDate().format(dateFormatter));
            } else {
                followUpLabel.setText("No follow-up scheduled");
            }

            detailsSection.setVisible(true);
            detailsSection.setManaged(true);
        }
    }

    @FXML
    private void handleCloseDetails() {
        detailsSection.setVisible(false);
        detailsSection.setManaged(false);
        prescriptionsTable.getSelectionModel().clearSelection();
    }
}