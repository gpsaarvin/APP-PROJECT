package com.smartmedicare.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;

public class Prescription {
    private final StringProperty id;
    private final StringProperty patientId;
    private final StringProperty doctorId;
    private final StringProperty doctorName;
    private final ObjectProperty<LocalDate> dateIssued;
    private final StringProperty diagnosis;
    private final StringProperty medicines;
    private final StringProperty notes;
    private final ObjectProperty<LocalDate> followUpDate;

    public Prescription() {
        this.id = new SimpleStringProperty();
        this.patientId = new SimpleStringProperty();
        this.doctorId = new SimpleStringProperty();
        this.doctorName = new SimpleStringProperty();
        this.dateIssued = new SimpleObjectProperty<>();
        this.diagnosis = new SimpleStringProperty();
        this.medicines = new SimpleStringProperty();
        this.notes = new SimpleStringProperty();
        this.followUpDate = new SimpleObjectProperty<>();
    }

    // ID Property
    public String getId() { return id.get(); }
    public void setId(String value) { id.set(value); }
    public StringProperty idProperty() { return id; }

    // Patient ID Property
    public String getPatientId() { return patientId.get(); }
    public void setPatientId(String value) { patientId.set(value); }
    public StringProperty patientIdProperty() { return patientId; }

    // Doctor ID Property
    public String getDoctorId() { return doctorId.get(); }
    public void setDoctorId(String value) { doctorId.set(value); }
    public StringProperty doctorIdProperty() { return doctorId; }

    // Doctor Name Property
    public String getDoctorName() { return doctorName.get(); }
    public void setDoctorName(String value) { doctorName.set(value); }
    public StringProperty doctorNameProperty() { return doctorName; }

    // Date Issued Property
    public LocalDate getDateIssued() { return dateIssued.get(); }
    public void setDateIssued(LocalDate value) { dateIssued.set(value); }
    public ObjectProperty<LocalDate> dateIssuedProperty() { return dateIssued; }

    // Diagnosis Property
    public String getDiagnosis() { return diagnosis.get(); }
    public void setDiagnosis(String value) { diagnosis.set(value); }
    public StringProperty diagnosisProperty() { return diagnosis; }

    // Medicines Property
    public String getMedicines() { return medicines.get(); }
    public void setMedicines(String value) { medicines.set(value); }
    public StringProperty medicinesProperty() { return medicines; }

    // Notes Property
    public String getNotes() { return notes.get(); }
    public void setNotes(String value) { notes.set(value); }
    public StringProperty notesProperty() { return notes; }

    // Follow Up Date Property
    public LocalDate getFollowUpDate() { return followUpDate.get(); }
    public void setFollowUpDate(LocalDate value) { followUpDate.set(value); }
    public ObjectProperty<LocalDate> followUpDateProperty() { return followUpDate; }
}