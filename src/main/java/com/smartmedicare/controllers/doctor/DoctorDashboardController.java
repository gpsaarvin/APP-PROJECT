package com.smartmedicare.controllers.doctor;

import java.io.IOException;

import com.smartmedicare.controllers.DoctorAwareController;
import com.smartmedicare.models.Doctor;
import com.smartmedicare.services.AppointmentDAO;
import com.smartmedicare.services.AuthenticationService;
import com.smartmedicare.services.PatientDAO;
import com.smartmedicare.services.PrescriptionDAO;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

@SuppressWarnings("unused")
public class DoctorDashboardController {
    @FXML private Label doctorNameLabel;
    @FXML private Label specialtyLabel;
    @FXML private StackPane contentArea;
    @FXML private Text todayAppointmentsCount;
    @FXML private Text totalPatientsCount;
    @FXML private Text pendingPrescriptionsCount;
    
    private Doctor doctor;
    private AppointmentDAO appointmentDAO;
    private PatientDAO patientDAO;
    private PrescriptionDAO prescriptionDAO;

    @FXML
    public void initialize() {
        doctor = (Doctor) AuthenticationService.getInstance().getCurrentUser();
        doctorNameLabel.setText("Dr. " + doctor.getName());
        specialtyLabel.setText(doctor.getSpecialization());
        
        // Initialize DAOs
        appointmentDAO = new AppointmentDAO();
        patientDAO = new PatientDAO();
        prescriptionDAO = new PrescriptionDAO();
        
        // Load dashboard statistics
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        try {
            // Count today's appointments
            long todayAppts = appointmentDAO.countTodayAppointments(doctor.getId());
            todayAppointmentsCount.setText(String.valueOf(todayAppts));

            // Count total patients
            long totalPatients = patientDAO.countPatientsByDoctor(doctor.getId());
            totalPatientsCount.setText(String.valueOf(totalPatients));

            // Count pending prescriptions
            long pendingPrescriptions = prescriptionDAO.countPendingPrescriptions(doctor.getId());
            pendingPrescriptionsCount.setText(String.valueOf(pendingPrescriptions));
        } catch (Exception e) {
            System.err.println("Error loading dashboard stats: " + e.getMessage());
            showError("Dashboard Error", "Failed to load dashboard statistics", "There was a problem loading your dashboard information. Please try again later.");
        }
    }

    @FXML
    private void handleLogout() {
        AuthenticationService.getInstance().logout();
        navigateToLogin();
    }

    @FXML
    private void showPatients() {
        loadContent("patient-list");
    }

    @FXML
    private void showPatientRecords() {
        loadContent("patient-records");
    }

    @FXML
    private void showSchedule() {
        loadContent("schedule");
    }

    @FXML
    private void showAppointments() {
        loadContent("appointments");
    }

    @FXML
    private void showPrescriptionForm() {
        loadContent("prescription-form");
    }

    @FXML
    private void showPrescriptions() {
        loadContent("prescriptions");
    }

    @FXML
    private void showDiseaseDetection() {
        loadContent("disease-detection");
    }

    @FXML
    private void showDiagnoses() {
        loadContent("diagnoses");
    }

    @FXML
    private void showDiseaseAnalysis() {
        loadContent("disease-analysis");
    }

    @FXML
    private void showProfile() {
        loadContent("profile");
    }

    private void loadContent(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/doctor/" + page + ".fxml")
            );
            Parent content = loader.load();

            // Pass doctor ID to controllers that need it
            var controller = loader.getController();
            if (controller instanceof DoctorAwareController doctorAwareController) {
                doctorAwareController.setDoctor(doctor);
            }

            contentArea.getChildren().setAll(content);
        } catch (IOException | SecurityException e) {
            showError("Navigation Error", "Could not load " + page + " page", e.getMessage());
        } catch (IllegalStateException e) {
            showError("System Error", "An unexpected error occurred", "The application is in an invalid state. Please restart.");
        }
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void navigateToLogin() {
        try {
            // Navigate back to the landing page where user can choose role
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/landing.fxml"));
            Stage stage = (Stage) doctorNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException | SecurityException e) {
            showError("Navigation Error", "Could not return to login page", e.getMessage());
        } catch (IllegalStateException e) {
            showError("System Error", "An unexpected error occurred", "The application is in an invalid state. Please restart.");
        }
    }
}