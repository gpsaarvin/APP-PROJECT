package com.smartmedicare.controllers.patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bson.types.ObjectId;

import com.smartmedicare.models.Appointment;
import com.smartmedicare.models.Doctor;
import com.smartmedicare.models.Patient;
import com.smartmedicare.services.AppointmentDAO;
import com.smartmedicare.services.AuthenticationService;
import com.smartmedicare.services.DoctorDAO;
import com.smartmedicare.utils.SceneNavigator;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

@SuppressWarnings("unused")
public class AppointmentBookingController {
    @FXML private ComboBox<String> specializationComboBox;
    @FXML private ComboBox<Doctor> doctorComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> timeSlotComboBox;
    @FXML private TextArea reasonTextArea;
    @FXML private Label messageLabel;

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private Patient patient;

    public void initialize() {
        patient = (Patient) AuthenticationService.getInstance().getCurrentUser();
        
        // Initialize specializations
        specializationComboBox.getItems().addAll(
            "General Medicine",
            "Cardiology",
            "Dermatology",
            "Neurology",
            "Pediatrics",
            "Orthopedics"
        );
        
        // Initialize time slots (9 AM to 5 PM)
        timeSlotComboBox.getItems().addAll(
            IntStream.range(9, 17)
                    .mapToObj(hour -> LocalTime.of(hour, 0))
                    .collect(Collectors.toList())
        );

        // Set minimum date to today
        datePicker.setValue(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0);
            }
        });

        // Configure doctor dropdown rendering to show readable names
        doctorComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Doctor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String spec = item.getSpecialization() != null ? (" (" + item.getSpecialization() + ")") : "";
                    setText(getDoctorDisplayName(item) + spec);
                }
            }
        });
        doctorComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Doctor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String spec = item.getSpecialization() != null ? (" (" + item.getSpecialization() + ")") : "";
                    setText(getDoctorDisplayName(item) + spec);
                }
            }
        });
        doctorComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Doctor item) {
                if (item == null) return "";
                String spec = item.getSpecialization() != null ? (" (" + item.getSpecialization() + ")") : "";
                return getDoctorDisplayName(item) + spec;
            }
            @Override
            public Doctor fromString(String string) { return null; }
        });

        // Disable doctor selection until specialization is chosen
        doctorComboBox.setDisable(true);
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleSpecializationChange() {
        String specialization = specializationComboBox.getValue();
        if (specialization != null) {
            List<Doctor> doctors = doctorDAO.findBySpecialization(specialization);
            doctorComboBox.getItems().clear();
            doctorComboBox.getItems().addAll(doctors);
            doctorComboBox.setDisable(false);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleDoctorChange() {
        checkAvailableSlots();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleDateChange() {
        checkAvailableSlots();
    }

    private void checkAvailableSlots() {
        Doctor selectedDoctor = doctorComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();
        
        if (selectedDoctor != null && selectedDate != null) {
            List<Appointment> existingAppointments = appointmentDAO.findByDoctorId(selectedDoctor.getId())
                .stream()
                .filter(apt -> apt.getDateTime().toLocalDate().equals(selectedDate))
                .collect(Collectors.toList());

            // Remove booked time slots
            timeSlotComboBox.getItems().clear();
            timeSlotComboBox.getItems().addAll(
                IntStream.range(9, 17)
                        .mapToObj(hour -> LocalTime.of(hour, 0))
                        .filter(time -> existingAppointments.stream()
                            .noneMatch(apt -> apt.getDateTime().toLocalTime().equals(time)))
                        .collect(Collectors.toList())
            );
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleBookAppointment() {
        if (!validateFields()) {
            return;
        }

        Doctor selectedDoctor = doctorComboBox.getValue();
        LocalDateTime appointmentDateTime = LocalDateTime.of(
            datePicker.getValue(),
            timeSlotComboBox.getValue()
        );

        Appointment appointment = new Appointment();
        appointment.setId(new ObjectId());
        appointment.setPatientId(patient.getId());
        appointment.setDoctorId(selectedDoctor.getId());
        appointment.setDateTime(appointmentDateTime);
        appointment.setReason(reasonTextArea.getText());
        appointment.setStatus("SCHEDULED");

        try {
            appointmentDAO.insert(appointmentDAO.entityToDocument(appointment));
            String doctorLabel = doctorComboBox.getValue() != null ?
                getDoctorDisplayName(doctorComboBox.getValue()) :
                "Doctor";
            showMessage("Appointment with " + doctorLabel + " booked successfully!", false);
            clearFields();
        } catch (Exception e) {
            showMessage("Error booking appointment: " + e.getMessage(), true);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleClear() {
        clearFields();
    }

    private boolean validateFields() {
        if (specializationComboBox.getValue() == null ||
            doctorComboBox.getValue() == null ||
            datePicker.getValue() == null ||
            timeSlotComboBox.getValue() == null ||
            reasonTextArea.getText().trim().isEmpty()) {
            
            showMessage("Please fill in all fields", true);
            return false;
        }
        return true;
    }

    private void clearFields() {
        specializationComboBox.setValue(null);
        doctorComboBox.setValue(null);
        datePicker.setValue(LocalDate.now());
        timeSlotComboBox.setValue(null);
        reasonTextArea.clear();
        messageLabel.setVisible(false);
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("error-message", "success-message");
        messageLabel.getStyleClass().add(isError ? "error-message" : "success-message");
        messageLabel.setVisible(true);
    }

    public static void show() {
        SceneNavigator.getInstance().navigateTo("patient/appointment-booking.fxml");
    }

    private String getDoctorDisplayName(Doctor doctor) {
        if (doctor == null) return "";
        String name = doctor.getName();
        if (name != null && !name.isBlank()) return "Dr. " + name.trim();
        String username = doctor.getUsername();
        return username != null ? ("Dr. " + username.trim()) : "";
    }
}