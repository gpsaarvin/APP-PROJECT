package com.smartmedicare.controllers.doctor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.bson.Document;

import com.smartmedicare.models.Appointment;
import com.smartmedicare.models.Doctor;
import com.smartmedicare.models.MedicalRecord;
import com.smartmedicare.models.Patient;
import com.smartmedicare.models.Prescription;
import com.smartmedicare.services.AppointmentDAO;
import com.smartmedicare.services.MedicalRecordDAO;
import com.smartmedicare.services.PatientDAO;
import com.smartmedicare.services.PrescriptionDAO;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

@SuppressWarnings("unused")
public class PatientProfileController implements DoctorAwareController {
    @FXML private Label patientNameLabel;
    @FXML private Text patientDetailsText;
    @FXML private Text appointmentsCount;
    @FXML private Text prescriptionsCount;
    @FXML private Text pendingFollowUpsCount;
    
    @FXML private TableView<MedicalRecord> medicalHistoryTable;
    @FXML private TableColumn<MedicalRecord, String> dateColumn;
    @FXML private TableColumn<MedicalRecord, String> conditionColumn;
    @FXML private TableColumn<MedicalRecord, String> treatmentColumn;
    @FXML private TableColumn<MedicalRecord, String> notesColumn;
    
    @FXML private TableView<Prescription> prescriptionsTable;
    @FXML private TableColumn<Prescription, String> prescriptionDateColumn;
    @FXML private TableColumn<Prescription, String> medicinesColumn;
    @FXML private TableColumn<Prescription, String> diagnosisColumn;
    @FXML private TableColumn<Prescription, String> followUpColumn;
    @FXML private TableColumn<Prescription, Void> actionsColumn;
    
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> appointmentDateColumn;
    @FXML private TableColumn<Appointment, String> reasonColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private TableColumn<Appointment, String> appointmentNotesColumn;
    
    @FXML private TextArea doctorNotesArea;
    @FXML private Button saveNotesButton;

    private Doctor currentDoctor;
    private Patient currentPatient;
    private AppointmentDAO appointmentDAO;
    private PrescriptionDAO prescriptionDAO;
    private MedicalRecordDAO medicalRecordDAO;
    private PatientDAO patientDAO;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    private void initialize() {
        // Initialize DAOs
        appointmentDAO = new AppointmentDAO();
        prescriptionDAO = new PrescriptionDAO();
        medicalRecordDAO = new MedicalRecordDAO();
        patientDAO = new PatientDAO();

        // Configure table columns
        setupMedicalHistoryTable();
        setupPrescriptionsTable();
        setupAppointmentsTable();
    }

    private void setupMedicalHistoryTable() {
        dateColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getRecordDate().format(dateFormatter))
        );
        conditionColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDiagnosis())
        );
        treatmentColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getTreatmentPlan())
        );
        notesColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNotes())
        );
    }

    private void setupPrescriptionsTable() {
        prescriptionDateColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDateIssued().format(dateFormatter))
        );
        medicinesColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getMedicines())
        );
        diagnosisColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDiagnosis())
        );
        followUpColumn.setCellValueFactory(data -> {
            if (data.getValue().getFollowUpDate() != null) {
                return new SimpleStringProperty(
                    data.getValue().getFollowUpDate().format(dateFormatter)
                );
            }
            return new SimpleStringProperty("No follow-up");
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            {
                editButton.setOnAction(event -> handleEditPrescription(
                    getTableView().getItems().get(getIndex())
                ));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editButton);
            }
        });
    }

    private void setupAppointmentsTable() {
        appointmentDateColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDateTime().format(dateFormatter))
        );
        reasonColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getReason())
        );
        statusColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatus())
        );
        appointmentNotesColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNotes())
        );
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        loadPatientData();
    }

    private void loadPatientData() {
        // Update header information
        patientNameLabel.setText(currentPatient.getName());
        patientDetailsText.setText(String.format(
            "Age: %d | Gender: %s | Blood Group: %s | Contact: %s",
            currentPatient.getAge(),
            currentPatient.getGender(),
            currentPatient.getBloodGroup(),
            currentPatient.getContact()
        ));

        // Load medical history
        List<MedicalRecord> medicalHistory = medicalRecordDAO.findByPatientId(
            currentPatient.getId()
        );
        medicalHistoryTable.setItems(FXCollections.observableArrayList(medicalHistory));

        // Load prescriptions
        List<Prescription> prescriptions = prescriptionDAO.findByPatientId(
            currentPatient.getId()
        );
        prescriptionsTable.setItems(FXCollections.observableArrayList(prescriptions));

        // Load appointments
        List<Appointment> appointments = appointmentDAO.findByPatientId(
            currentPatient.getId()
        );
        appointmentsTable.setItems(FXCollections.observableArrayList(appointments));

        // Update statistics
        appointmentsCount.setText(String.valueOf(appointments.size()));
        prescriptionsCount.setText(String.valueOf(prescriptions.size()));
        
        long pendingFollowUps = prescriptions.stream()
            .filter(p -> p.getFollowUpDate() != null && 
                        p.getFollowUpDate().isAfter(LocalDate.now()))
            .count();
        pendingFollowUpsCount.setText(String.valueOf(pendingFollowUps));

        // Load doctor's notes
        doctorNotesArea.setText(currentPatient.getDoctorNotes());
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleEditNotes() {
        // Only allow editing notes if this is the attending doctor
        if (currentDoctor != null && 
            currentPatient != null && 
            currentDoctor.getId().toString().equals(currentPatient.getAttendingDoctorId().toString())) {
            doctorNotesArea.setEditable(true);
            saveNotesButton.setVisible(true);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleSaveNotes() {
        if (currentPatient != null && currentDoctor != null && doctorNotesArea != null) {
            currentPatient.setDoctorNotes(doctorNotesArea.getText());
            patientDAO.update(currentPatient.getId(), entityToDocument(currentPatient));
            doctorNotesArea.setEditable(false);
            saveNotesButton.setVisible(false);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleNewPrescription() {
        // TODO: Open new prescription form
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleScheduleAppointment() {
        // TODO: Open appointment scheduling dialog
    }

    private void handleEditPrescription(Prescription prescription) {
        // Only the prescribing doctor or current attending doctor can edit prescriptions
        if (currentDoctor != null && currentPatient != null && prescription != null &&
            prescription.getDoctorId() != null && currentPatient.getAttendingDoctorId() != null &&
            (prescription.getDoctorId().equals(currentDoctor.getId().toString()) ||
             currentPatient.getAttendingDoctorId().toString().equals(currentDoctor.getId().toString()))) {
            // TODO: Open prescription edit form
        }
    }

    private Document entityToDocument(Patient patient) {
        org.bson.Document doc = new org.bson.Document();
        doc.put("_id", patient.getId());
        doc.put("name", patient.getName());
        doc.put("email", patient.getEmail());
        doc.put("contact", patient.getContact());
        doc.put("age", patient.getAge());
        doc.put("gender", patient.getGender());
        doc.put("bloodGroup", patient.getBloodGroup());
        doc.put("doctorNotes", patient.getDoctorNotes());
        doc.put("attendingDoctorId", patient.getAttendingDoctorId());
        doc.put("medicalHistory", patient.getMedicalHistory());
        doc.put("appointments", patient.getAppointments());
        doc.put("prescriptions", patient.getPrescriptions());
        doc.put("userType", patient.getUserType().name());
        return doc;
    }

    @Override
    public void setDoctor(Doctor doctor) {
        this.currentDoctor = doctor;
        
        // Update UI to reflect doctor's permissions
        if (doctor != null) {
            doctorNotesArea.setEditable(false); // Initially not editable
            // Add any doctor-specific UI customizations here
        }
    }
}