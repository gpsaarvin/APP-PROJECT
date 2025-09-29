package com.smartmedicare.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.smartmedicare.models.Appointment;

public class AppointmentDAO extends BaseDAO<Appointment> {
    
    public AppointmentDAO() {
        super("appointments");
    }

    @Override
    protected Appointment documentToEntity(Document doc) {
        Appointment appointment = new Appointment();
        appointment.setId(doc.getObjectId("_id"));
        appointment.setPatientId(doc.getObjectId("patientId"));
        appointment.setDoctorId(doc.getObjectId("doctorId"));
        appointment.setDateTime(LocalDateTime.parse(doc.getString("dateTime")));
        appointment.setStatus(doc.getString("status"));
        appointment.setReason(doc.getString("reason"));
        appointment.setNotes(doc.getString("notes"));
        return appointment;
    }

    @Override
    public Document entityToDocument(Appointment appointment) {
        return new Document("_id", appointment.getId())
                .append("patientId", appointment.getPatientId())
                .append("doctorId", appointment.getDoctorId())
                .append("dateTime", appointment.getDateTime().toString())
                .append("status", appointment.getStatus())
                .append("reason", appointment.getReason())
                .append("notes", appointment.getNotes());
    }

    public List<Appointment> findByPatientId(ObjectId patientId) {
        List<Appointment> appointments = new ArrayList<>();
        collection.find(Filters.eq("patientId", patientId)).forEach(
            doc -> appointments.add(documentToEntity(doc))
        );
        return appointments;
    }

    public List<Appointment> findByDoctorId(ObjectId doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        collection.find(Filters.eq("doctorId", doctorId)).forEach(
            doc -> appointments.add(documentToEntity(doc))
        );
        return appointments;
    }

    public List<Appointment> findByDate(LocalDateTime date) {
        List<Appointment> appointments = new ArrayList<>();
        collection.find(Filters.eq("dateTime", date.toString())).forEach(
            doc -> appointments.add(documentToEntity(doc))
        );
        return appointments;
    }

    public long countTodayAppointments(ObjectId doctorId) {
        String today = LocalDateTime.now().toLocalDate().toString();
        return collection.countDocuments(
            Filters.and(
                Filters.eq("doctorId", doctorId),
                Filters.regex("dateTime", "^" + today)
            )
        );
    }

    public List<Appointment> findByPatientIdAndStatus(ObjectId patientId, String status) {
        List<Appointment> appointments = new ArrayList<>();
        collection.find(Filters.and(
            Filters.eq("patientId", patientId),
            Filters.eq("status", status)
        )).forEach(doc -> appointments.add(documentToEntity(doc)));
        return appointments;
    }

    // Convenience alias to match some controllers
    public List<Appointment> findByPatient(ObjectId patientId) {
        return findByPatientId(patientId);
    }

    public LocalDateTime findLastVisit(ObjectId patientId) {
        Document last = collection
            .find(Filters.eq("patientId", patientId))
            .sort(Sorts.descending("dateTime"))
            .limit(1)
            .first();
        if (last == null) return null;
        return LocalDateTime.parse(last.getString("dateTime"));
    }

    public String getPatientStatus(ObjectId patientId) {
        // Upcoming appointment?
        Document upcoming = collection
            .find(Filters.and(
                Filters.eq("patientId", patientId),
                Filters.regex("dateTime", ".*"))) // placeholder filter; sort ascending and check >= now in-memory
            .sort(Sorts.ascending("dateTime"))
            .first();

        if (upcoming != null) {
            try {
                LocalDateTime dt = LocalDateTime.parse(upcoming.getString("dateTime"));
                if (!dt.isBefore(LocalDateTime.now())) {
                    return "Scheduled";
                }
            } catch (Exception ignored) {}
        }

        Document last = collection
            .find(Filters.eq("patientId", patientId))
            .sort(Sorts.descending("dateTime"))
            .first();

        if (last == null) return "New";
        return "Follow-up Required";
    }
}