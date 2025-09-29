package com.smartmedicare.models;

import java.time.LocalDateTime;
import java.util.Map;

import org.bson.types.ObjectId;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MedicalRecord {
    private ObjectId id;
    private ObjectId patientId;
    private ObjectId doctorId;
    private LocalDateTime recordDate;
    private Map<String, String> symptoms;
    private Map<String, Double> vitalSigns;
    private final StringProperty diagnosis = new SimpleStringProperty();
    private final StringProperty aiPrediction = new SimpleStringProperty();
    private final StringProperty severity = new SimpleStringProperty();
    private final StringProperty treatmentPlan = new SimpleStringProperty();
    private final StringProperty notes = new SimpleStringProperty();

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getPatientId() { return patientId; }
    public void setPatientId(ObjectId patientId) { this.patientId = patientId; }

    public ObjectId getDoctorId() { return doctorId; }
    public void setDoctorId(ObjectId doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDateTime recordDate) { this.recordDate = recordDate; }

    public Map<String, String> getSymptoms() { return symptoms; }
    public void setSymptoms(Map<String, String> symptoms) { this.symptoms = symptoms; }

    public Map<String, Double> getVitalSigns() { return vitalSigns; }
    public void setVitalSigns(Map<String, Double> vitalSigns) { this.vitalSigns = vitalSigns; }

    public String getDiagnosis() { return diagnosis.get(); }
    public void setDiagnosis(String diagnosis) { this.diagnosis.set(diagnosis); }
    public StringProperty diagnosisProperty() { return diagnosis; }

    public String getAiPrediction() { return aiPrediction.get(); }
    public void setAiPrediction(String aiPrediction) { this.aiPrediction.set(aiPrediction); }
    public StringProperty aiPredictionProperty() { return aiPrediction; }

    public String getSeverity() { return severity.get(); }
    public void setSeverity(String severity) { this.severity.set(severity); }
    public StringProperty severityProperty() { return severity; }

    public String getTreatmentPlan() { return treatmentPlan.get(); }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan.set(treatmentPlan); }
    public StringProperty treatmentPlanProperty() { return treatmentPlan; }

    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }
}