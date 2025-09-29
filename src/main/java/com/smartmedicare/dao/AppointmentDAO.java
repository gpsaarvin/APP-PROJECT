package com.smartmedicare.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.smartmedicare.models.Appointment;
import com.smartmedicare.utils.DatabaseConnection;

public class AppointmentDAO {
    private final MongoCollection<Appointment> appointments;

    public AppointmentDAO() {
        this.appointments = DatabaseConnection.getInstance().getDatabase()
            .getCollection("appointments", Appointment.class);
    }

    public LocalDateTime findLastVisit(ObjectId patientId) {
        Appointment lastAppointment = appointments
            .find(Filters.eq("patientId", patientId))
            .sort(Sorts.descending("dateTime"))
            .limit(1)
            .first();
        
        return lastAppointment != null ? lastAppointment.getDateTime() : null;
    }
    
    public String getPatientStatus(ObjectId patientId) {
        Appointment upcomingAppointment = appointments
            .find(Filters.and(
                Filters.eq("patientId", patientId),
                Filters.gte("dateTime", LocalDateTime.now())
            ))
            .sort(Sorts.ascending("dateTime"))
            .first();
        
        if (upcomingAppointment != null) {
            return "Scheduled";
        }

        Appointment lastAppointment = appointments
            .find(Filters.eq("patientId", patientId))
            .sort(Sorts.descending("dateTime"))
            .first();
            
        if (lastAppointment == null) {
            return "New";
        }
        
        return "Follow-up Required";
    }

    public List<Appointment> findByPatient(ObjectId patientId) {
        List<Appointment> patientAppointments = new ArrayList<>();
        appointments.find(Filters.eq("patientId", patientId))
            .sort(Sorts.descending("dateTime"))
            .into(patientAppointments);
        return patientAppointments;
    }
}