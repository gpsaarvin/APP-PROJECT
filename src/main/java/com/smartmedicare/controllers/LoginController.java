package com.smartmedicare.controllers;

import java.io.IOException;
import java.net.URL;

import com.smartmedicare.models.User;
import com.smartmedicare.services.AuthenticationService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
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
                            System.out.println("Initialized LoginController with userType: " + userType);
                        } else {
                            System.err.println("Error: No user type data found");
                            showError("Error: Invalid login type");
                        }
                    } catch (Exception e) {
                        System.err.println("Error in initialize: " + e.getMessage());
                        showError("Error initializing login page: Invalid application state");
                    }
                });
            }
        });
    }

    @FXML
    public void handleLogin() {
        System.out.println("Attempting login with userType: " + userType);
        
        if (userType == null) {
            showError("Please select your role (Patient/Doctor) from the main page");
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Clear any previous error
        errorLabel.setVisible(false);

        // Validate input
        if (username.isEmpty()) {
            showError("Please enter your username");
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password");
            passwordField.requestFocus();
            return;
        }

        try {
            // Show loading state
            disableInputs(true);
            errorLabel.setVisible(false);
            
            User user = AuthenticationService.getInstance().authenticateUser(username, password, userType);

            if (user == null) {
                showError("Invalid username or password");
                passwordField.clear();
                passwordField.requestFocus();
                return;
            }

            String dashboardPath = switch (userType) {
                case PATIENT -> "/fxml/patient/dashboard.fxml";
                case DOCTOR -> "/fxml/doctor/dashboard.fxml";
            };

            System.out.println("Loading dashboard: " + dashboardPath);

            URL url = getClass().getResource(dashboardPath);
            if (url == null) {
                throw new IOException("Could not find resource: " + dashboardPath);
            }
            System.out.println("Resource URL: " + url);

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            System.out.println("Successfully loaded dashboard for " + userType);

        } catch (IOException e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            showError("Could not load dashboard page");
        } catch (IllegalStateException e) {
            System.err.println("Error during login: " + e.getMessage());
            showError("Login failed: Invalid application state");
        } catch (RuntimeException e) {
            System.err.println("Error during login: " + e.getMessage());
            showError("Login failed: An unexpected error occurred");
        }
    }

    @FXML
    public void handleRegisterNavigation() {
        try {
            String registerPath = switch (userType) {
                case PATIENT -> "/fxml/patient/register.fxml";
                case DOCTOR -> "/fxml/doctor/register.fxml";
                default -> throw new IllegalStateException("Invalid user type for registration");
            };

            FXMLLoader loader = new FXMLLoader(getClass().getResource(registerPath));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setUserData(userType); // Pass userType to the next scene
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error loading registration page: " + e.getMessage());
            showError("Could not load registration page");
        } catch (IllegalStateException e) {
            System.err.println("Error navigating to registration: " + e.getMessage());
            showError("Invalid registration type selected");
        } catch (RuntimeException e) {
            System.err.println("Error navigating to registration: " + e.getMessage());
            showError("Could not open registration page");
        }
    }

    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/landing.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error loading landing page: " + e.getMessage());
            showError("Could not load main page");
        } catch (RuntimeException e) {
            System.err.println("Error navigating back: " + e.getMessage());
            showError("Could not return to main page");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        disableInputs(false);
    }

    private void disableInputs(boolean disable) {
        usernameField.setDisable(disable);
        passwordField.setDisable(disable);
        errorLabel.setVisible(!disable && errorLabel.getText() != null && !errorLabel.getText().isEmpty());
    }
}