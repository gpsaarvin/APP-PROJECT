package com.smartmedicare.controllers.patient;

import com.smartmedicare.models.Patient;
import com.smartmedicare.services.AuthenticationService;
import com.smartmedicare.services.PatientService;
import com.smartmedicare.utils.DialogUtils;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

@SuppressWarnings("unused")
public class ProfileController {
    @FXML private TextField nameField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressArea;
    
    @FXML private TextField emergencyNameField;
    @FXML private TextField relationshipField;
    @FXML private TextField emergencyPhoneField;
    
    @FXML private TextArea allergiesArea;
    @FXML private TextArea medicationsArea;
    @FXML private TextArea conditionsArea;
    
    @FXML private Label messageLabel;
    
    private Patient patient;
    private final PatientService patientService;
    
    public ProfileController() {
        this.patientService = new PatientService();
    }
    
    @FXML
    public void initialize() {
        setupComboBoxes();
        loadPatientData();
    }
    
    private void setupComboBoxes() {
        genderCombo.setItems(FXCollections.observableArrayList(
            "Male", "Female", "Other"
        ));
        
        bloodGroupCombo.setItems(FXCollections.observableArrayList(
            "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"
        ));
    }
    
    private void loadPatientData() {
        try {
            patient = (Patient) AuthenticationService.getInstance().getCurrentUser();
            
            nameField.setText(patient.getName());
            dobPicker.setValue(patient.getDateOfBirth());
            genderCombo.setValue(patient.getGender());
            bloodGroupCombo.setValue(patient.getBloodGroup());
            phoneField.setText(patient.getPhone());
            emailField.setText(patient.getEmail());
            addressArea.setText(patient.getAddress());
            
            emergencyNameField.setText(patient.getEmergencyContactName());
            relationshipField.setText(patient.getEmergencyContactRelation());
            emergencyPhoneField.setText(patient.getEmergencyContactPhone());
            
            allergiesArea.setText(patient.getAllergies());
            medicationsArea.setText(patient.getCurrentMedications());
            conditionsArea.setText(patient.getMedicalConditions());
            
        } catch (Exception e) {
            DialogUtils.showError(
                "Error Loading Profile",
                "Failed to load patient profile",
                "There was a problem loading your profile information. Please try again.");
        }
    }
    
    @FXML
    private void handleSave() {
        try {
            patient.setName(nameField.getText().trim());
            patient.setDateOfBirth(dobPicker.getValue());
            patient.setGender(genderCombo.getValue());
            patient.setBloodGroup(bloodGroupCombo.getValue());
            patient.setPhone(phoneField.getText().trim());
            patient.setEmail(emailField.getText().trim());
            patient.setAddress(addressArea.getText().trim());
            
            patient.setEmergencyContactName(emergencyNameField.getText().trim());
            patient.setEmergencyContactRelation(relationshipField.getText().trim());
            patient.setEmergencyContactPhone(emergencyPhoneField.getText().trim());
            
            patient.setAllergies(allergiesArea.getText().trim());
            patient.setCurrentMedications(medicationsArea.getText().trim());
            patient.setMedicalConditions(conditionsArea.getText().trim());
            
            patientService.updatePatient(patient);
            
            messageLabel.setText("Profile updated successfully!");
            messageLabel.getStyleClass().setAll("message-label", "success-message");
            
        } catch (Exception e) {
            DialogUtils.showError(
                "Error Saving Profile",
                "Failed to save profile changes",
                "There was a problem saving your profile information. Please try again.");
        }
    }
    
    @FXML
    private void handleCancel() {
        loadPatientData(); // Reload original data
    }
}