package com.smartmedicare.controllers.doctor;

import com.smartmedicare.controllers.DoctorAwareController;
import com.smartmedicare.models.Appointment;
import com.smartmedicare.models.Doctor;
import com.smartmedicare.models.Patient;
import com.smartmedicare.services.AppointmentDAO;
import com.smartmedicare.services.AuthenticationService;
import com.smartmedicare.services.DoctorDAO;
import com.smartmedicare.services.PatientDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleController implements DoctorAwareController {
    @FXML private TextArea scheduleDisplay;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> timeColumn;
    @FXML private TableColumn<Appointment, String> patientColumn;
    @FXML private TableColumn<Appointment, String> reasonColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private Label messageLabel;

    private Doctor doctor;
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    public void initialize() {
        doctor = (Doctor) AuthenticationService.getInstance().getCurrentUser();
        if (doctor != null) {
            loadScheduleData();
            setupTableColumns();
            loadTodayAppointments();
        }
    }

    @Override
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        loadScheduleData();
        loadTodayAppointments();
    }

    private void loadScheduleData() {
        if (doctor != null) {
            String schedule = doctor.getSchedule();
            if (schedule != null && !schedule.trim().isEmpty()) {
                scheduleDisplay.setText(schedule);
            } else {
                scheduleDisplay.setText("No schedule information available.\nClick 'Edit Schedule' to add your working hours.");
            }
        }
    }

    private void setupTableColumns() {
        // Time column
        timeColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return new SimpleStringProperty(cellData.getValue().getDateTime().format(timeFormatter));
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

    private void loadTodayAppointments() {
        try {
            List<Appointment> allAppointments = appointmentDAO.findByDoctorId(doctor.getId());
            List<Appointment> todayAppointments = allAppointments.stream()
                .filter(apt -> apt.getDateTime().toLocalDate().equals(LocalDate.now()))
                .sorted((a1, a2) -> a1.getDateTime().compareTo(a2.getDateTime()))
                .collect(Collectors.toList());

            appointmentsTable.setItems(FXCollections.observableArrayList(todayAppointments));
            showMessage("Loaded " + todayAppointments.size() + " appointments for today", false);
        } catch (Exception e) {
            showMessage("Error loading today's appointments: " + e.getMessage(), true);
        }
    }

    /**
     * Edits doctor's schedule.
     * Called by FXML when edit schedule button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void editSchedule() {
        // Create schedule edit dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Schedule");
        dialog.setHeaderText("Update your working schedule");

        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create text area for schedule editing
        TextArea scheduleEditArea = new TextArea();
        scheduleEditArea.setText(doctor.getSchedule() != null ? doctor.getSchedule() : "");
        scheduleEditArea.setPrefRowCount(8);
        scheduleEditArea.setPrefColumnCount(50);
        scheduleEditArea.setWrapText(true);
        scheduleEditArea.setPromptText("Enter your working schedule...\nExample:\nMonday: 9:00 AM - 5:00 PM\nTuesday: 9:00 AM - 5:00 PM\n...");

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Schedule Information:"),
            scheduleEditArea,
            new Label("Please include your working days and hours")
        );
        
        dialog.getDialogPane().setContent(content);

        // Convert result when save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return scheduleEditArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newSchedule -> {
            try {
                doctor.setSchedule(newSchedule.trim());
                doctorDAO.update(doctor.getId(), doctorDAO.entityToDocument(doctor));
                
                // Refresh display
                loadScheduleData();
                showMessage("Schedule updated successfully!", false);
            } catch (Exception e) {
                showMessage("Error updating schedule: " + e.getMessage(), true);
            }
        });
    }

    /**
     * Refreshes schedule display.
     * Called by FXML when refresh button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void refreshSchedule() {
        loadScheduleData();
        loadTodayAppointments();
        showMessage("Schedule refreshed", false);
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