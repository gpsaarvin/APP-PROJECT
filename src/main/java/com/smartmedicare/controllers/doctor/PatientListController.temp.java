package com.smartmedicare.controllers.doctor;

import com.smartmedicare.services.AppointmentDAO;
import com.smartmedicare.models.Doctor;
import com.smartmedicare.models.Patient;
import com.smartmedicare.services.PatientDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings("unused")
class PatientListControllerTemp implements DoctorAwareController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> nameColumn;
    @FXML private TableColumn<Patient, Integer> ageColumn;
    @FXML private TableColumn<Patient, String> genderColumn;
    @FXML private TableColumn<Patient, String> lastVisitColumn;
    @FXML private TableColumn<Patient, String> statusColumn;
    @FXML private TableColumn<Patient, Void> actionsColumn;
    @FXML private Pagination pagination;
    @FXML private Label messageLabel;

    private Doctor currentDoctor;
    private final PatientDAO patientDAO;
    private final AppointmentDAO appointmentDAO;
    private final ObservableList<Patient> patients;
    private static final int ITEMS_PER_PAGE = 10;
    private final DateTimeFormatter dateFormatter;

    public PatientListControllerTemp() {
        this.patientDAO = new PatientDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.patients = FXCollections.observableArrayList();
        this.dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    }

    @FXML
    private void initialize() {
        // Initialize filter options
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All Patients", "Recent Visits", "Pending Follow-ups"
        ));
        filterComboBox.setValue("All Patients");

        // Configure table columns
        setupTableColumns();

        // Add search listener
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.isEmpty()) {
                handleSearch();
            } else {
                loadAllPatients();
            }
        });

        // Configure pagination
        pagination.setPageCount(1);
        pagination.currentPageIndexProperty().addListener(
            (obs, oldIndex, newIndex) -> updateTableContent()
        );
    }

    private void setupTableColumns() {
        // Name column
        nameColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getName())
        );

        // Age column
        ageColumn.setCellValueFactory(data ->
            new SimpleObjectProperty<>(data.getValue().getAge())
        );

        // Gender column
        genderColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getGender())
        );

        // Last Visit column
        lastVisitColumn.setCellValueFactory(data -> {
            LocalDateTime lastVisit = appointmentDAO.findLastVisit(data.getValue().getId());
            return new SimpleStringProperty(
                lastVisit != null ? lastVisit.format(dateFormatter) : "No visits"
            );
        });

        // Status column
        statusColumn.setCellValueFactory(data ->
            new SimpleStringProperty(appointmentDAO.getPatientStatus(data.getValue().getId()))
        );

        // Actions column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button historyButton = new Button("History");
            
            {
                viewButton.setOnAction(event -> handleViewPatient(
                    getTableView().getItems().get(getIndex())
                ));
                historyButton.setOnAction(event -> handleViewHistory(
                    getTableView().getItems().get(getIndex())
                ));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, viewButton, historyButton);
                    buttons.setStyle("-fx-alignment: CENTER;");
                    setGraphic(buttons);
                }
            }
        });
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().toLowerCase();
        String filter = filterComboBox.getValue();

        List<Patient> filteredPatients = patientDAO.searchPatients(
            searchTerm, filter, currentDoctor.getId()
        );
        
        patients.setAll(filteredPatients);
        updatePagination();
        updateTableContent();
    }

    private void loadAllPatients() {
        List<Patient> allPatients = patientDAO.findByDoctor(currentDoctor.getId());
        patients.setAll(allPatients);
        updatePagination();
        updateTableContent();
    }

    private void updatePagination() {
        int pageCount = (patients.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        pagination.setPageCount(Math.max(1, pageCount));
    }

    private void updateTableContent() {
        int startIndex = pagination.getCurrentPageIndex() * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, patients.size());

        patientsTable.setItems(FXCollections.observableArrayList(
            patients.subList(startIndex, endIndex)
        ));
    }

    private void handleViewPatient(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor/patient-profile.fxml"));
            Parent root = loader.load();

            PatientProfileController controller = loader.getController();
            controller.setPatient(patient);
            controller.setDoctor(currentDoctor);

            Stage stage = new Stage();
            stage.setTitle("Patient Profile - " + patient.getName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading patient profile: " + e.getMessage());
            showMessage("Could not open patient profile", true);
        }
    }

    private void handleViewHistory(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor/patient-history.fxml"));
            Parent root = loader.load();

            PatientHistoryController controller = loader.getController();
            controller.setPatient(patient);
            controller.setDoctor(currentDoctor);

            Stage stage = new Stage();
            stage.setTitle("Medical History - " + patient.getName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading patient history: " + e.getMessage());
            showMessage("Could not open medical history", true);
        }
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().setAll(isError ? "error-message" : "info-message");
        messageLabel.setVisible(true);
    }

    @Override
    public void setDoctor(Doctor doctor) {
        this.currentDoctor = doctor;
        loadAllPatients();
    }
}