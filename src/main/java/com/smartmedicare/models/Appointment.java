package com.smartmedicare.models;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Appointment {
    private ObjectId id;
    private ObjectId patientId;
    private ObjectId doctorId;
    private LocalDateTime dateTime;
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty reason = new SimpleStringProperty();
    private final StringProperty notes = new SimpleStringProperty();

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getPatientId() { return patientId; }
    public void setPatientId(ObjectId patientId) { this.patientId = patientId; }

    public ObjectId getDoctorId() { return doctorId; }
    public void setDoctorId(ObjectId doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

    public String getReason() { return reason.get(); }
    public void setReason(String reason) { this.reason.set(reason); }
    public StringProperty reasonProperty() { return reason; }

    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }
}