package com.smartmedicare.models;

import java.time.LocalDate;
import java.util.List;

import org.bson.types.ObjectId;

public class Patient extends User {
    private List<ObjectId> medicalHistory;
    private List<ObjectId> appointments;
    private List<ObjectId> prescriptions;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String doctorNotes;
    private ObjectId attendingDoctorId;
    private String phone;
    private String address;
    private String emergencyContactName;
    private String emergencyContactRelation;
    private String emergencyContactPhone;
    private String allergies;
    private String currentMedications;
    private String medicalConditions;

    public Patient() {
        super.setUserType(UserType.PATIENT);
    }

    // Getters and Setters
    public List<ObjectId> getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(List<ObjectId> medicalHistory) { this.medicalHistory = medicalHistory; }

    public List<ObjectId> getAppointments() { return appointments; }
    public void setAppointments(List<ObjectId> appointments) { this.appointments = appointments; }

    public List<ObjectId> getPrescriptions() { return prescriptions; }
    public void setPrescriptions(List<ObjectId> prescriptions) { this.prescriptions = prescriptions; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public int getAge() { 
        if (dateOfBirth == null) return 0;
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String name) { this.emergencyContactName = name; }

    public String getEmergencyContactRelation() { return emergencyContactRelation; }
    public void setEmergencyContactRelation(String relation) { this.emergencyContactRelation = relation; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String phone) { this.emergencyContactPhone = phone; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getCurrentMedications() { return currentMedications; }
    public void setCurrentMedications(String medications) { this.currentMedications = medications; }

    public String getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(String conditions) { this.medicalConditions = conditions; }

    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }

    public ObjectId getAttendingDoctorId() { return attendingDoctorId; }
    public void setAttendingDoctorId(ObjectId doctorId) { this.attendingDoctorId = doctorId; }
}