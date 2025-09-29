package com.smartmedicare.controllers;

import java.io.IOException;

import com.smartmedicare.models.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SuppressWarnings("unused")
public class LandingController {
    
    @FXML
    @SuppressWarnings("unused")
    private void handlePatientLogin(javafx.event.ActionEvent event) {
        navigateToLogin(User.UserType.PATIENT, event);
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleDoctorLogin(javafx.event.ActionEvent event) {
        navigateToLogin(User.UserType.DOCTOR, event);
    }

    private void navigateToLogin(User.UserType userType, javafx.event.ActionEvent event) {
        try {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            
            String loginPath = switch (userType) {
                case PATIENT -> "/fxml/patient/login.fxml";
                case DOCTOR -> "/fxml/doctor/login.fxml";
            };

            FXMLLoader loader = new FXMLLoader(getClass().getResource(loginPath));
            Parent root = loader.load();
            
            System.out.println("Loading login page for " + userType);
            
            Scene scene = new Scene(root);
            stage.setUserData(userType); // Set the user type before changing scene
            stage.setScene(scene);

        } catch (IOException | RuntimeException e) {
            String errorMessage = "Error navigating to login: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += "\nCause: " + e.getCause().getMessage();
            }
            System.err.println(errorMessage);
        }
    }

    // No extra navigation handlers needed for simplified landing
}