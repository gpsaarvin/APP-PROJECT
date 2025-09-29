package com.smartmedicare.controllers;

import java.io.IOException;
import java.time.LocalDate;

import com.smartmedicare.models.User;
import com.smartmedicare.services.RegistrationService;
import com.smartmedicare.utils.DialogUtils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@SuppressWarnings("unused")
public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField contactField;
    
    // Patient specific fields
    @FXML private VBox patientFields;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private ComboBox<String> bloodGroupComboBox;
    
    // Doctor specific fields
    @FXML private VBox doctorFields;
    @FXML private TextField specializationField;
    @FXML private TextArea scheduleArea;
    
    @FXML private Label errorLabel;

    private User.UserType userType;

    public void initialize() {
        // JavaFX components might not be ready in initialize(), so we'll set up a one-time listener
        usernameField.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> {
                    try {
                        Stage stage = (Stage) usernameField.getScene().getWindow();
                        if (stage != null && stage.getUserData() != null) {
                            userType = (User.UserType) stage.getUserData();
                            System.out.println("Initialized RegisterController with userType: " + userType);
                            
                            // Show relevant fields based on user type
                            if (userType == User.UserType.PATIENT) {
                                patientFields.setVisible(true);
                                patientFields.setManaged(true);
                                initializePatientFields();
                            } else {
                                doctorFields.setVisible(true);
                                doctorFields.setManaged(true);
                            }
                        } else {
                            System.err.println("Error: No user type data found");
                            showError("Error: Invalid registration type");
                        }
                    } catch (Exception e) {
                        System.err.println("Error in initialize: " + e.getMessage());
                        showError("Error initializing registration page: " + e.getMessage());
                    }
                });
            }
        });
    }

    private void initializePatientFields() {
        genderComboBox.getItems().addAll("Male", "Female", "Other");
        bloodGroupComboBox.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleRegister() {
        try {
            errorLabel.setVisible(false);
            if (!validateFields()) {
                return;
            }

            RegistrationService registrationService = RegistrationService.getInstance();
            
            boolean success = userType == User.UserType.PATIENT ? 
                registerPatient() : registerDoctor();

            if (success) {
                DialogUtils.showInfo("Registration Successful", 
                    "Account created successfully", 
                    "You can now log in with your credentials.");
                navigateToLogin();
            } else {
                showError(registrationService.getLastError() != null ? 
                    registrationService.getLastError() : 
                    "Registration failed. Please try again.");
            }
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            showError("An unexpected error occurred. Please try again.");
        }
    }

    private boolean validateFields() {
        if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty() || 
            !passwordField.getText().equals(confirmPasswordField.getText()) ||
            nameField.getText().isEmpty() || emailField.getText().isEmpty() || 
            contactField.getText().isEmpty()) {
            showError("Please fill in all required fields and ensure passwords match");
            return false;
        }

        if (userType == User.UserType.PATIENT) {
            if (dobPicker.getValue() == null || genderComboBox.getValue() == null || 
                bloodGroupComboBox.getValue() == null) {
                showError("Please fill in all patient-specific fields");
                return false;
            }
            // Ensure the date of birth is not in the future
            if (dobPicker.getValue().isAfter(LocalDate.now())) {
                showError("Date of birth cannot be in the future");
                return false;
            }
        } else {
            if (specializationField.getText().isEmpty() || scheduleArea.getText().isEmpty()) {
                showError("Please fill in all doctor-specific fields");
                return false;
            }
        }

        return true;
    }

    private boolean registerPatient() {
        try {
            return RegistrationService.getInstance().registerPatient(
                usernameField.getText(),
                passwordField.getText(),
                nameField.getText(),
                emailField.getText(),
                contactField.getText(),
                dobPicker.getValue(),
                genderComboBox.getValue(),
                bloodGroupComboBox.getValue()
            );
        } catch (Exception e) {
            showError("Error during registration: " + e.getMessage());
            return false;
        }
    }

    private boolean registerDoctor() {
        try {
            return RegistrationService.getInstance().registerDoctor(
                usernameField.getText(),
                passwordField.getText(),
                nameField.getText(),
                emailField.getText(),
                contactField.getText(),
                specializationField.getText(),
                scheduleArea.getText()
            );
        } catch (Exception e) {
            showError("Error during registration: " + e.getMessage());
            return false;
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleBack() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            String loginPath = switch (userType) {
                case PATIENT -> "/fxml/patient/login.fxml";
                case DOCTOR -> "/fxml/doctor/login.fxml";
                default -> throw new IllegalStateException("Invalid user type");
            };

            FXMLLoader loader = new FXMLLoader(getClass().getResource(loginPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setUserData(userType); // Pass the user type to login
            stage.setScene(new Scene(root));
            
        } catch (IOException e) {
            System.err.println("Navigation error (IO): " + e.getMessage());
            DialogUtils.showError("Navigation Error", 
                "Could not load login page", 
                "Please check your system resources and try again.");
        } catch (IllegalStateException e) {
            System.err.println("Navigation error (State): " + e.getMessage());
            DialogUtils.showError("Navigation Error", 
                "Invalid application state", 
                "Please restart the application.");
        } catch (RuntimeException e) {
            System.err.println("Navigation error (Runtime): " + e.getMessage());
            DialogUtils.showError("Navigation Error", 
                "Could not return to login page", 
                "Please try again or restart the application.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.getStyleClass().setAll("error-label");
    }
}