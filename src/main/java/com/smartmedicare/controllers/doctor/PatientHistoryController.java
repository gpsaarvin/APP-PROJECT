package com.smartmedicare.controllers.doctor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.smartmedicare.models.Doctor;
import com.smartmedicare.models.Patient;
import com.smartmedicare.services.AppointmentDAO;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

@SuppressWarnings("unused")
public class PatientHistoryController implements DoctorAwareController {
    @FXML private Label nameLabel;
    @FXML private Label ageLabel;
    @FXML private Label genderLabel;
    @FXML private Label bloodGroupLabel;
    @FXML private Label lastVisitLabel;
    @FXML private ListView<String> appointmentsListView;
    @FXML private ListView<String> prescriptionsListView;
    @FXML private ListView<String> diagnosesListView;
    @FXML private VBox notesBox;
    @FXML private TextArea notesArea;
    @FXML private Button saveNotesButton;

    private Patient patient;
    private Doctor currentDoctor;
    private final AppointmentDAO appointmentDAO;
    private final DateTimeFormatter dateFormatter;

    public PatientHistoryController() {
        this.appointmentDAO = new AppointmentDAO();
        this.dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a");
    }

    @FXML
    private void initialize() {
        // Setup list cell factories
        setupListViews();
        
        // Set up notes editing
        notesArea.setWrapText(true);
        saveNotesButton.setOnAction(e -> handleSaveNotes());
    }

    private void setupListViews() {
        appointmentsListView.setCellFactory(createListCellFactory());
        prescriptionsListView.setCellFactory(createListCellFactory());
        diagnosesListView.setCellFactory(createListCellFactory());
    }

    private Callback<ListView<String>, ListCell<String>> createListCellFactory() {
        return listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(true);
                }
            }
        };
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        loadPatientData();
    }

    private void loadPatientData() {
        // Set basic info
        nameLabel.setText(patient.getName());
        ageLabel.setText(String.valueOf(patient.getAge()));
        genderLabel.setText(patient.getGender());
        bloodGroupLabel.setText(patient.getBloodGroup());

        // Load last visit
        LocalDateTime lastVisit = appointmentDAO.findLastVisit(patient.getId());
        lastVisitLabel.setText(lastVisit != null ? 
            lastVisit.format(dateFormatter) : "No visits");

        // Load appointments
        loadAppointments();

        // Load prescriptions
        loadPrescriptions();

        // Load diagnoses
        loadDiagnoses();

        // Load notes
        loadNotes();
    }

    private void loadAppointments() {
        List<String> appointments = appointmentDAO.findByPatient(patient.getId())
            .stream()
            .map(appointment -> String.format("%s - %s\nDoctor: %s\nReason: %s",
                appointment.getDateTime().format(dateFormatter),
                appointment.getStatus(),
                currentDoctor.getName(),
                appointment.getReason()))
            .toList();
        
        appointmentsListView.setItems(FXCollections.observableArrayList(appointments));
    }

    private void loadPrescriptions() {
        // TODO: Implement loading prescriptions
        prescriptionsListView.setItems(FXCollections.observableArrayList(
            "No prescriptions found"
        ));
    }

    private void loadDiagnoses() {
        // TODO: Implement loading diagnoses
        diagnosesListView.setItems(FXCollections.observableArrayList(
            "No diagnoses found"
        ));
    }

    private void loadNotes() {
        // TODO: Implement loading doctor's notes
        notesArea.setText("");
    }

    @FXML
    private void handleSaveNotes() {
        // TODO: Implement saving doctor's notes
    }

    @Override
    public void setDoctor(Doctor doctor) {
        this.currentDoctor = doctor;
        if (patient != null) {
            loadPatientData();
        }
    }
}