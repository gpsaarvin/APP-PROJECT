package com.smartmedicare.controllers.doctor;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AppointmentManagementController {
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> patientColumn;
    @FXML private TableColumn<Appointment, String> reasonColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    
    @FXML private Button acceptButton;
    @FXML private Button cancelButton;
    @FXML private Label messageLabel;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private Doctor currentDoctor;
    private ObservableList<Appointment> allAppointments = FXCollections.observableArrayList();

    public void initialize() {
        try {
            currentDoctor = (Doctor) AuthenticationService.getInstance().getCurrentUser();
            
            // Initialize status filter
            statusFilterComboBox.getItems().addAll("All", "PENDING", "SCHEDULED", "COMPLETED", "CANCELLED");
            statusFilterComboBox.setValue("All");
            
            // Setup table columns
            setupTableColumns();
            
            // Setup table selection listener
            appointmentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean hasSelection = newSelection != null;
                    acceptButton.setDisable(!hasSelection || !"PENDING".equals(newSelection.getStatus()));
                    cancelButton.setDisable(!hasSelection || 
                        ("COMPLETED".equals(newSelection.getStatus()) || "CANCELLED".equals(newSelection.getStatus())));
                }
            );
            
            // Load appointments
            loadAppointments();
        } catch (Exception e) {
            showMessage("Error initializing appointments view: " + e.getMessage(), true);
        }
    }

    private void setupTableColumns() {
        // Date column
        dateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            return new SimpleStringProperty(cellData.getValue().getDateTime().format(formatter));
        });

        // Patient column
        patientColumn.setCellValueFactory(cellData -> {
            try {
                Patient patient = patientDAO.findById(cellData.getValue().getPatientId());
                String patientName = patient != null ? patient.getName() : "Unknown Patient";
                return new SimpleStringProperty(patientName);
            } catch (Exception e) {
                return new SimpleStringProperty("Error loading patient");
            }
        });

        // Reason column
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));

        // Status column
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadAppointments() {
        try {
            List<Appointment> appointments = appointmentDAO.findByDoctorId(currentDoctor.getId());
            allAppointments.setAll(appointments);
            filterAppointments();
            showMessage("Loaded " + appointments.size() + " appointments", false);
            
            // Auto-hide message after 3 seconds
            new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.seconds(3),
                    e -> messageLabel.setVisible(false)
                )
            ).play();
        } catch (Exception e) {
            showMessage("Error loading appointments: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleStatusFilter() {
        filterAppointments();
    }

    @FXML
    private void handleRefresh() {
        loadAppointments();
    }

    @FXML
    private void acceptAppointment() {
        Appointment selectedAppointment = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null && "PENDING".equals(selectedAppointment.getStatus())) {
            acceptSelectedAppointment(selectedAppointment);
        }
    }

    @FXML
    private void cancelAppointment() {
        Appointment selectedAppointment = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            cancelSelectedAppointment(selectedAppointment);
        }
    }

    private void filterAppointments() {
        String selectedStatus = statusFilterComboBox.getValue();
        if ("All".equals(selectedStatus)) {
            appointmentsTable.setItems(allAppointments);
        } else {
            ObservableList<Appointment> filteredAppointments = allAppointments.filtered(
                appointment -> selectedStatus.equals(appointment.getStatus())
            );
            appointmentsTable.setItems(filteredAppointments);
        }
    }

    private void acceptSelectedAppointment(Appointment appointment) {
        try {
            appointment.setStatus("SCHEDULED");
            appointmentDAO.update(appointment.getId(), appointmentDAO.entityToDocument(appointment));
            
            // Update local data
            allAppointments.stream()
                .filter(apt -> apt.getId().equals(appointment.getId()))
                .findFirst()
                .ifPresent(apt -> apt.setStatus("SCHEDULED"));
            
            appointmentsTable.refresh();
            showMessage("Appointment accepted successfully!", false);
        } catch (Exception e) {
            showMessage("Error accepting appointment: " + e.getMessage(), true);
        }
    }

    private void cancelSelectedAppointment(Appointment appointment) {
        try {
            appointment.setStatus("CANCELLED");
            appointmentDAO.update(appointment.getId(), appointmentDAO.entityToDocument(appointment));
            
            // Update local data
            allAppointments.stream()
                .filter(apt -> apt.getId().equals(appointment.getId()))
                .findFirst()
                .ifPresent(apt -> apt.setStatus("CANCELLED"));
            
            appointmentsTable.refresh();
            showMessage("Appointment cancelled", false);
        } catch (Exception e) {
            showMessage("Error cancelling appointment: " + e.getMessage(), true);
        }
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("error-label", "success-label");
        messageLabel.getStyleClass().add(isError ? "error-label" : "success-label");
        messageLabel.setVisible(true);
    }
}