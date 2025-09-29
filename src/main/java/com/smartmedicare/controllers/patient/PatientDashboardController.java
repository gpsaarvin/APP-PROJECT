package com.smartmedicare.controllers.patient;

import com.smartmedicare.models.Patient;
import com.smartmedicare.services.AuthenticationService;
import com.smartmedicare.utils.DialogUtils;
import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.geometry.Pos;

public class PatientDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private StackPane contentArea;
    
    private Patient patient;

    public void initialize() {
        patient = (Patient) AuthenticationService.getInstance().getCurrentUser();
        welcomeLabel.setText("Welcome, " + patient.getName());
    }

    @FXML
    private void handleLogout() {
        AuthenticationService.getInstance().logout();
        navigateToLogin();
    }

    @FXML
    public void showAppointmentBooking() {
        loadContent("appointment-booking");
    }

    @FXML
    private void showAppointments() {
        loadContent("appointments");
    }

    @FXML
    private void showMedicalHistory() {
        loadContent("medical-history");
    }

    @FXML
    private void showPrescriptions() {
        loadContent("prescriptions");
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
        // Create loading indicator
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(50, 50);
        VBox loadingBox = new VBox(progress, new Label("Loading..."));
        loadingBox.setSpacing(10);
        loadingBox.setAlignment(Pos.CENTER);
        
        // Show loading state
        contentArea.getChildren().setAll(loadingBox);
        
        // Create task for loading content
        Task<Parent> loadTask = new Task<Parent>() {
            @Override
            protected Parent call() throws Exception {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient/" + page + ".fxml"));
                Parent content = loader.load();
                
                // Set this controller as user data for child views to access
                if (content.getScene() != null) {
                    content.getScene().setUserData(PatientDashboardController.this);
                }
                
                if (page.equals("prescriptions")) {
                    PrescriptionsController controller = loader.getController();
                    controller.setCurrentPatientId(patient.getId().toString());
                }
                
                return content;
            }
        };
        
        // Handle task completion
        loadTask.setOnSucceeded(e -> {
            contentArea.getChildren().setAll(loadTask.getValue());
        });
        
        loadTask.setOnFailed(e -> {
            Throwable ex = loadTask.getException();
            if (ex instanceof IOException) {
                DialogUtils.showError(
                    "Error Loading Content",
                    "Failed to load " + page,
                    "The requested page could not be found. Please contact support if the issue persists.");
            } else {
                DialogUtils.showError(
                    "Error Loading Content",
                    "Failed to load " + page,
                    "There was a problem initializing the requested content. Please try again.");
            }
            
            // Show error state with retry button
            Button retryButton = new Button("Retry");
            retryButton.setOnAction(event -> loadContent(page));
            
            VBox errorBox = new VBox(10);
            errorBox.setAlignment(Pos.CENTER);
            
            Label errorLabel = new Label("Failed to load content");
            errorLabel.getStyleClass().add("error-label");
            
            errorBox.getChildren().addAll(errorLabel, retryButton);
            contentArea.getChildren().setAll(errorBox);
        });
        
        // Start loading
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void navigateToLogin() {
        try {
            // Navigate back to the landing page where user can choose role
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/landing.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException | SecurityException e) {
            DialogUtils.showError(
                "Navigation Error",
                "Could not return to main page",
                "There was a problem returning to the main page: " + e.getMessage());
        } catch (IllegalStateException e) {
            DialogUtils.showError(
                "System Error",
                "An unexpected error occurred",
                "The application is in an invalid state. Please try again.");
        }
    }
}