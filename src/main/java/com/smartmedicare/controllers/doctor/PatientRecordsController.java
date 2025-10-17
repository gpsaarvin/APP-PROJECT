package com.smartmedicare.controllers.doctor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.smartmedicare.controllers.DoctorAwareController;
import com.smartmedicare.models.Appointment;
import com.smartmedicare.models.Doctor;
import com.smartmedicare.models.Patient;
import com.smartmedicare.services.AppointmentDAO;
import com.smartmedicare.services.AuthenticationService;
import com.smartmedicare.services.PatientDAO;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class PatientRecordsController implements DoctorAwareController {
    @FXML private TextField searchField;
    @FXML private TableView<Patient> recordsTable;
    @FXML private TableColumn<Patient, String> patientNameColumn;
    @FXML private TableColumn<Patient, String> lastVisitColumn;
    @FXML private TableColumn<Patient, String> diagnosisColumn;
    @FXML private TableColumn<Patient, String> contactColumn;
    @FXML private TableColumn<Patient, String> statusColumn;
    @FXML private Button viewRecordButton;
    @FXML private Button addRecordButton;
    @FXML @SuppressWarnings("unused") // Used by FXML
    private Button exportButton;
    @FXML private Label messageLabel;

    private Doctor doctor;
    private final PatientDAO patientDAO = new PatientDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final ObservableList<Patient> allPatients = FXCollections.observableArrayList();

    public void initialize() {
        doctor = (Doctor) AuthenticationService.getInstance().getCurrentUser();
        if (doctor != null) {
            setupTableColumns();
            loadPatientRecords();
            setupTableSelectionListener();
        }
    }

    @Override
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        loadPatientRecords();
    }

    private void setupTableColumns() {
        // Patient Name column
        patientNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));

        // Last Visit column
        lastVisitColumn.setCellValueFactory(cellData -> {
            try {
                List<Appointment> appointments = appointmentDAO.findByPatientId(cellData.getValue().getId());
                if (!appointments.isEmpty()) {
                    Appointment lastAppointment = appointments.stream()
                        .filter(apt -> doctor.getId().equals(apt.getDoctorId()))
                        .max((a1, a2) -> a1.getDateTime().compareTo(a2.getDateTime()))
                        .orElse(null);
                    
                    if (lastAppointment != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                        return new SimpleStringProperty(lastAppointment.getDateTime().format(formatter));
                    }
                }
                return new SimpleStringProperty("No visits");
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });

        // Diagnosis column
        diagnosisColumn.setCellValueFactory(cellData -> {
            try {
                List<Appointment> appointments = appointmentDAO.findByPatientId(cellData.getValue().getId());
                if (!appointments.isEmpty()) {
                    Appointment lastAppointment = appointments.stream()
                        .filter(apt -> doctor.getId().equals(apt.getDoctorId()))
                        .max((a1, a2) -> a1.getDateTime().compareTo(a2.getDateTime()))
                        .orElse(null);
                    
                    if (lastAppointment != null && lastAppointment.getReason() != null) {
                        return new SimpleStringProperty(lastAppointment.getReason());
                    }
                }
                return new SimpleStringProperty("No diagnosis");
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });

        // Contact column
        contactColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getContact()));

        // Status column
        statusColumn.setCellValueFactory(cellData -> {
            try {
                List<Appointment> appointments = appointmentDAO.findByPatientId(cellData.getValue().getId());
                boolean hasActiveAppointments = appointments.stream()
                    .anyMatch(apt -> doctor.getId().equals(apt.getDoctorId()) && 
                             ("SCHEDULED".equals(apt.getStatus()) || "PENDING".equals(apt.getStatus())));
                
                return new SimpleStringProperty(hasActiveAppointments ? "Active" : "Inactive");
            } catch (Exception e) {
                return new SimpleStringProperty("Unknown");
            }
        });
    }

    private void setupTableSelectionListener() {
        recordsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean hasSelection = newSelection != null;
                viewRecordButton.setDisable(!hasSelection);
                addRecordButton.setDisable(!hasSelection);
            }
        );
    }

    private void loadPatientRecords() {
        try {
            // Get all patients who have had appointments with this doctor
            List<Appointment> doctorAppointments = appointmentDAO.findByDoctorId(doctor.getId());
            List<Patient> patients = doctorAppointments.stream()
                .map(apt -> {
                    try {
                        return patientDAO.findById(apt.getPatientId());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(patient -> patient != null)
                .distinct()
                .collect(Collectors.toList());

            allPatients.setAll(patients);
            recordsTable.setItems(allPatients);
            showMessage("Loaded " + patients.size() + " patient records", false);
        } catch (Exception e) {
            showMessage("Error loading patient records: " + e.getMessage(), true);
        }
    }

    /**
     * Handles search functionality for patient records.
     * Called by FXML when search field is updated.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            recordsTable.setItems(allPatients);
        } else {
            ObservableList<Patient> filteredPatients = allPatients.filtered(
                patient -> patient.getName().toLowerCase().contains(searchText) ||
                          patient.getContact().toLowerCase().contains(searchText)
            );
            recordsTable.setItems(filteredPatients);
        }
    }

    /**
     * Views detailed patient record.
     * Called by FXML when view record button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void viewPatientRecord() {
        Patient selectedPatient = recordsTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            showPatientDetails(selectedPatient);
        }
    }

    /**
     * Adds new medical record for selected patient.
     * Called by FXML when add record button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void addMedicalRecord() {
        Patient selectedPatient = recordsTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            showMessage("Medical record functionality coming soon...", false);
        }
    }

    /**
     * Exports patient records.
     * Called by FXML when export button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void exportRecords() {
        showMessage("Export functionality coming soon...", false);
    }

    /**
     * Refreshes the patient records table.
     * Called by FXML when refresh button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void refreshRecords() {
        loadPatientRecords();
    }

    private void showPatientDetails(Patient patient) {
        try {
            // Create patient details dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Patient Record");
            dialog.setHeaderText("Medical Record for " + patient.getName());

            // Get patient's appointments with this doctor
            List<Appointment> appointments = appointmentDAO.findByPatientId(patient.getId())
                .stream()
                .filter(apt -> doctor.getId().equals(apt.getDoctorId()))
                .sorted((a1, a2) -> a2.getDateTime().compareTo(a1.getDateTime()))
                .collect(Collectors.toList());

            StringBuilder details = new StringBuilder();
            details.append("Patient Information:\n");
            details.append("Name: ").append(patient.getName()).append("\n");
            details.append("Contact: ").append(patient.getContact()).append("\n");
            details.append("Email: ").append(patient.getEmail()).append("\n");
            details.append("Blood Group: ").append(patient.getBloodGroup()).append("\n");
            details.append("Gender: ").append(patient.getGender()).append("\n");
            details.append("\nAppointment History:\n");

            if (appointments.isEmpty()) {
                details.append("No appointments found.\n");
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
                for (Appointment apt : appointments) {
                    details.append("• ").append(apt.getDateTime().format(formatter))
                           .append(" - ").append(apt.getReason())
                           .append(" (").append(apt.getStatus()).append(")\n");
                }
            }

            TextArea textArea = new TextArea(details.toString());
            textArea.setEditable(false);
            textArea.setPrefRowCount(15);
            textArea.setPrefColumnCount(60);
            textArea.setWrapText(true);

            dialog.getDialogPane().setContent(textArea);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (Exception e) {
            showMessage("Error displaying patient details: " + e.getMessage(), true);
        }
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