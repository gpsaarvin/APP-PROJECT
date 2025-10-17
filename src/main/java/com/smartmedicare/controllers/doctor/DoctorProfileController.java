package com.smartmedicare.controllers.doctor;

import com.smartmedicare.controllers.DoctorAwareController;
import com.smartmedicare.models.Doctor;
import com.smartmedicare.services.AuthenticationService;
import com.smartmedicare.services.DoctorDAO;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class DoctorProfileController implements DoctorAwareController {
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField contactField;
    @FXML private ComboBox<String> specializationComboBox;
    @FXML private TextArea scheduleArea;
    @FXML private Label messageLabel;

    private Doctor doctor;
    private final DoctorDAO doctorDAO = new DoctorDAO();

    /**
     * Updates profile information.
     * Called by FXML when save changes button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void handleSave() {
        updateProfile();
    }

    public void initialize() {
        // Initialize specialization options
        specializationComboBox.getItems().addAll(
            "General Medicine",
            "Cardiology",
            "Dermatology", 
            "Neurology",
            "Pediatrics",
            "Orthopedics",
            "Gynecology",
            "Psychiatry",
            "Oncology"
        );
        
        // Load current doctor data
        doctor = (Doctor) AuthenticationService.getInstance().getCurrentUser();
        if (doctor != null) {
            loadDoctorData();
        }
    }

    @Override
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        loadDoctorData();
    }

    private void loadDoctorData() {
        if (doctor != null) {
            nameField.setText(doctor.getName());
            usernameField.setText(doctor.getUsername());
            emailField.setText(doctor.getEmail());
            contactField.setText(doctor.getContact());
            specializationComboBox.setValue(doctor.getSpecialization());
            scheduleArea.setText(doctor.getSchedule());
        }
    }

    /**
     * Updates profile information.
     * Called by FXML when update profile button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void updateProfile() {
        try {
            // Validate fields
            if (nameField.getText().trim().isEmpty() || 
                emailField.getText().trim().isEmpty() || 
                contactField.getText().trim().isEmpty() ||
                specializationComboBox.getValue() == null ||
                scheduleArea.getText().trim().isEmpty()) {
                showMessage("Please fill in all fields", true);
                return;
            }

            // Update doctor object
            doctor.setName(nameField.getText().trim());
            doctor.setEmail(emailField.getText().trim());
            doctor.setContact(contactField.getText().trim());
            doctor.setSpecialization(specializationComboBox.getValue());
            doctor.setSchedule(scheduleArea.getText().trim());

            // Save to database
            doctorDAO.update(doctor.getId(), doctorDAO.entityToDocument(doctor));
            
            showMessage("Profile updated successfully!", false);
        } catch (Exception e) {
            showMessage("Error updating profile: " + e.getMessage(), true);
        }
    }

    /**
     * Changes password.
     * Called by FXML when change password button is clicked.
     */
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void changePassword() {
        // Create password change dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your new password");

        // Set button types
        ButtonType changeButtonType = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        // Create password fields
        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current Password");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Current Password:"), currentPasswordField,
            new Label("New Password:"), newPasswordField,
            new Label("Confirm Password:"), confirmPasswordField
        );
        dialog.getDialogPane().setContent(content);

        // Convert result when change button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                return newPasswordField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newPassword -> {
            try {
                // For now, just validate that password is not empty
                if (currentPasswordField.getText().trim().isEmpty()) {
                    showMessage("Current password cannot be empty", true);
                    return;
                }

                // Validate new password
                if (newPassword.trim().isEmpty()) {
                    showMessage("New password cannot be empty", true);
                    return;
                }

                if (!newPassword.equals(confirmPasswordField.getText())) {
                    showMessage("Passwords do not match", true);
                    return;
                }

                // Update password
                String hashedPassword = AuthenticationService.getInstance().hashPassword(newPassword);
                doctor.setPassword(hashedPassword);
                doctorDAO.update(doctor.getId(), doctorDAO.entityToDocument(doctor));
                
                showMessage("Password changed successfully!", false);
            } catch (Exception e) {
                showMessage("Error changing password: " + e.getMessage(), true);
            }
        });
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("error-label", "success-label");
        messageLabel.getStyleClass().add(isError ? "error-label" : "success-label");
        messageLabel.setVisible(true);
        
        // Auto-hide message after 3 seconds
        new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(3),
                e -> messageLabel.setVisible(false)
            )
        ).play();
    }
}