package com.smartmedicare.controllers.patient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.smartmedicare.models.Appointment;
import com.smartmedicare.models.Doctor;
import com.smartmedicare.models.Patient;
import com.smartmedicare.services.AppointmentDAO;
import com.smartmedicare.services.AuthenticationService;
import com.smartmedicare.services.DoctorDAO;
import com.smartmedicare.utils.SceneNavigator;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

@SuppressWarnings("unused")
public class AppointmentsController {
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> timeColumn;
    @FXML private TableColumn<Appointment, String> doctorColumn;
    @FXML private TableColumn<Appointment, String> specializationColumn;
    @FXML private TableColumn<Appointment, String> reasonColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private TableColumn<Appointment, Void> actionsColumn;
    @FXML private Label messageLabel;
    @FXML private Button refreshButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private TextField searchField;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private Patient patient;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    public void initialize() {
        patient = (Patient) AuthenticationService.getInstance().getCurrentUser();
        
        // Initialize UI components
        loadingIndicator.setVisible(false);
        
        // Initialize status filter
        statusFilter.getItems().addAll("All", "Scheduled", "Completed", "Cancelled");
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> loadAppointments());

        // Initialize search functionality
        searchField.setPromptText("Search by doctor name or reason...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && !oldValue.equals(newValue)) {
                filterAppointments();
            }
        });

        // Set up refresh button
        refreshButton.setOnAction(e -> loadAppointments());
        refreshButton.getStyleClass().add("primary-button");
        
        // Configure table columns
        dateColumn.setCellValueFactory(data -> {
            LocalDateTime dateTime = data.getValue().getDateTime();
            return new SimpleStringProperty(dateTime.format(dateFormatter));
        });

        timeColumn.setCellValueFactory(data -> {
            LocalDateTime dateTime = data.getValue().getDateTime();
            return new SimpleStringProperty(dateTime.format(timeFormatter));
        });

        // Use safe display with fallback to username when name is missing
        doctorColumn.setCellValueFactory(data -> {
            Doctor doctor = doctorDAO.findById(data.getValue().getDoctorId());
            return new SimpleStringProperty(getDoctorDisplayName(doctor));
        });

        specializationColumn.setCellValueFactory(data -> {
            Doctor doctor = doctorDAO.findById(data.getValue().getDoctorId());
            String spec = (doctor != null && doctor.getSpecialization() != null) ? doctor.getSpecialization() : "";
            return new SimpleStringProperty(spec);
        });

        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Configure actions column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button cancelButton = new Button("Cancel");
            private final HBox actions = new HBox(10, cancelButton);

            {
                cancelButton.setOnAction(e -> handleCancelAppointment(getTableView().getItems().get(getIndex())));
                cancelButton.getStyleClass().add("secondary-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    cancelButton.setVisible(appointment.getStatus().equals("SCHEDULED") &&
                                         appointment.getDateTime().isAfter(LocalDateTime.now()));
                    setGraphic(actions);
                }
            }
        });

        // Load initial data
        loadAppointments();
    }

    private final Consumer<Scene> sceneChangeListener = scene -> {
        if (scene.getRoot().getId().equals("appointmentsView")) {
            loadAppointments();
        }
    };

    @FXML
    @SuppressWarnings("unused")
    private void handleNewAppointment() {
        // Navigate to appointment booking
        AppointmentBookingController.show();
        SceneNavigator.getInstance().onSceneChange(sceneChangeListener);
    }

    @FXML
    public void dispose() {
        SceneNavigator.getInstance().removeSceneChangeListener(sceneChangeListener);
    }

    private void handleCancelAppointment(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Appointment");
        alert.setHeaderText("Are you sure you want to cancel this appointment?");
        alert.setContentText("This action cannot be undone.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                appointment.setStatus("CANCELLED");
                appointmentDAO.update(appointment.getId(), appointmentDAO.entityToDocument(appointment));
                loadAppointments();
                showMessage("Appointment cancelled successfully", false);
            } catch (Exception e) {
                showMessage("Error cancelling appointment: " + e.getMessage(), true);
            }
        }
    }

    private List<Appointment> allAppointments = new ArrayList<>();

    private void loadAppointments() {
        loadingIndicator.setVisible(true);
        String status = statusFilter.getValue();

        // Run database query in background
        new Thread(() -> {
            List<Appointment> appointments;
            try {
                if ("All".equals(status)) {
                    appointments = appointmentDAO.findByPatientId(patient.getId());
                } else {
                    appointments = appointmentDAO.findByPatientIdAndStatus(patient.getId(), status.toUpperCase());
                }

                // Sort appointments by date in descending order
                appointments.sort((a1, a2) -> a2.getDateTime().compareTo(a1.getDateTime()));
                allAppointments = appointments;

                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    filterAppointments();
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showMessage("Error loading appointments: " + e.getMessage(), true);
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void filterAppointments() {
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        List<Appointment> filteredList = allAppointments.stream()
            .filter(appointment -> {
                if (searchText == null || searchText.isEmpty()) {
                    return true;
                }
                Doctor doctor = doctorDAO.findById(appointment.getDoctorId());
                String doctorName = getDoctorDisplayName(doctor).toLowerCase();
                String reason = appointment.getReason() != null ? appointment.getReason().toLowerCase() : "";
                return doctorName.contains(searchText) || reason.contains(searchText);
            })
            .collect(Collectors.toList());

        appointmentsTable.getItems().setAll(filteredList);
        setupTableRowFactory();
    }

    private void setupTableRowFactory() {
        appointmentsTable.setRowFactory(tv -> {
            TableRow<Appointment> row = new TableRow<>();
            
            // Add style class based on appointment status
            row.getStyleClass().add("appointment-row");
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    row.getStyleClass().removeAll("status-scheduled", "status-completed", "status-cancelled");
                    row.getStyleClass().add("status-" + newItem.getStatus().toLowerCase());
                }
            });

            // Add tooltip
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    Appointment appointment = row.getItem();
                    Doctor doctor = doctorDAO.findById(appointment.getDoctorId());
                    String tooltipText = String.format(
                        "Doctor: %s\nSpecialty: %s\nDate: %s\nTime: %s\nStatus: %s\nReason: %s",
                        getDoctorDisplayName(doctor),
                        (doctor != null && doctor.getSpecialization() != null) ? doctor.getSpecialization() : "N/A",
                        appointment.getDateTime().format(dateFormatter),
                        appointment.getDateTime().format(timeFormatter),
                        appointment.getStatus(),
                        appointment.getReason() != null ? appointment.getReason() : ""
                    );
                    row.setTooltip(new Tooltip(tooltipText));
                }
            });
            return row;
        });
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("error-message", "success-message");
        messageLabel.getStyleClass().add(isError ? "error-message" : "success-message");
        messageLabel.setVisible(true);
    }

    // Helper to display doctor's name with fallback to username when name is missing
    private String getDoctorDisplayName(Doctor doctor) {
        if (doctor == null) return "";
        String name = doctor.getName();
        if (name != null && !name.isBlank()) return "Dr. " + name.trim();
        String username = doctor.getUsername();
        return username != null ? ("Dr. " + username.trim()) : "";
    }
}