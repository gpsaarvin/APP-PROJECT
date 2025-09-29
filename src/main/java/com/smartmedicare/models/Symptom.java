package com.smartmedicare.models;

public class Symptom {
    private String name;
    private String severity;
    private String duration;
    private String notes;

    public Symptom(String name, String severity, String duration, String notes) {
        this.name = name;
        this.severity = severity;
        this.duration = duration;
        this.notes = notes;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}