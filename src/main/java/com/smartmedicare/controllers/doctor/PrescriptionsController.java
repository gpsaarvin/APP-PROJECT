package com.smartmedicare.controllers.doctor;

import com.smartmedicare.controllers.DoctorAwareController;
import com.smartmedicare.models.Doctor;
import com.smartmedicare.services.AuthenticationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PrescriptionsController implements DoctorAwareController {
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TableView<?> prescriptionsTable;
    @FXML @SuppressWarnings("unused") // Used by FXML
    private TableColumn<?, String> dateColumn;
    @FXML @SuppressWarnings("unused") // Used by FXML
    private TableColumn<?, String> patientColumn;
    @FXML @SuppressWarnings("unused") // Used by FXML
    private TableColumn<?, String> medicationColumn;
    @FXML @SuppressWarnings("unused") // Used by FXML
    private TableColumn<?, String> dosageColumn;
    @FXML @SuppressWarnings("unused") // Used by FXML
    private TableColumn<?, String> durationColumn;
    @FXML @SuppressWarnings("unused") // Used by FXML
    private TableColumn<?, String> statusColumn;
    @FXML private Button viewPrescriptionButton;
    @FXML private Button editPrescriptionButton;
    @FXML private Button printButton;
    @FXML private Label messageLabel;

    @SuppressWarnings("unused") // Will be used for prescription management
    private Doctor doctor;

    public void initialize() {
        doctor = (Doctor) AuthenticationService.getInstance().getCurrentUser();
        
        // Initialize status filter
        statusFilterComboBox.getItems().addAll("All", "Active", "Completed", "Cancelled");
        statusFilterComboBox.setValue("All");
        
        // Setup table selection listener
        prescriptionsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean hasSelection = newSelection != null;
                viewPrescriptionButton.setDisable(!hasSelection);
                editPrescriptionButton.setDisable(!hasSelection);
                printButton.setDisable(!hasSelection);
            }
        );
        
        loadPrescriptions();
    }

    @Override
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        loadPrescriptions();
    }

    private void loadPrescriptions() {
        showMessage("Prescription system coming soon...", false);
    }

    /**
     * Handles status filter change.
     * Called by FXML when status filter is changed.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void handleStatusFilter() {
        // Filter prescriptions based on selected status
        showMessage("Filtering prescriptions...", false);
    }

    /**
     * Creates new prescription.
     * Called by FXML when create button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void createPrescription() {
        showMessage("New prescription creation coming soon...", false);
    }

    /**
     * Refreshes prescriptions list.
     * Called by FXML when refresh button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void refreshPrescriptions() {
        loadPrescriptions();
    }

    /**
     * Views selected prescription.
     * Called by FXML when view button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void viewPrescription() {
        showMessage("View prescription details coming soon...", false);
    }

    /**
     * Edits selected prescription.
     * Called by FXML when edit button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void editPrescription() {
        showMessage("Edit prescription coming soon...", false);
    }

    /**
     * Prints selected prescription.
     * Called by FXML when print button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void printPrescription() {
        showMessage("Print prescription coming soon...", false);
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("error-label", "success-label");
        messageLabel.getStyleClass().add(isError ? "error-label" : "success-label");
        messageLabel.setVisible(true);
        
        // Auto-hide message after 3 seconds
        new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(3),
                e -> messageLabel.setVisible(false)
            )
        ).play();
    }
}