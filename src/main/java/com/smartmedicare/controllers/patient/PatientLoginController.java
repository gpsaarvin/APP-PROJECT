package com.smartmedicare.controllers.patient;

import java.io.IOException;

import com.smartmedicare.models.User;
import com.smartmedicare.services.AuthenticationService;
import com.smartmedicare.utils.DialogUtils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@SuppressWarnings("unused")
public class PatientLoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    @SuppressWarnings("unused")
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        try {
            User user = AuthenticationService.getInstance().authenticateUser(username, password, User.UserType.PATIENT);

            if (user == null) {
                showError("Invalid username or password");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            
        } catch (IOException e) {
            DialogUtils.showError(
                "Error Loading Dashboard",
                "Could not load dashboard page",
                "There was a problem loading the dashboard. Please try again."
            );
        } catch (Exception e) {
            DialogUtils.showError(
                "Login Error",
                "Error during login",
                "An unexpected error occurred. Please try again."
            );
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleRegisterNavigation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            DialogUtils.showError(
                "Navigation Error",
                "Could not open registration page",
                "There was a problem navigating to the registration page."
            );
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/landing.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            DialogUtils.showError(
                "Navigation Error",
                "Could not return to main menu",
                "There was a problem returning to the main menu. The application will now close."
            );
            Platform.exit();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}