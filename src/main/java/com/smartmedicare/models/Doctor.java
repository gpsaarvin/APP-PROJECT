package com.smartmedicare.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

public class Doctor extends User {
    private String specialization;
    private List<ObjectId> assignedPatients = new ArrayList<>();
    private String schedule;
    private List<ObjectId> appointments = new ArrayList<>();

    public Doctor() {
        super.setUserType(UserType.DOCTOR);
    }

    // Getters and Setters
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public List<ObjectId> getAssignedPatients() { return assignedPatients; }
    public void setAssignedPatients(List<ObjectId> assignedPatients) { this.assignedPatients = assignedPatients; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public List<ObjectId> getAppointments() { return appointments; }
    public void setAppointments(List<ObjectId> appointments) { this.appointments = appointments; }
}