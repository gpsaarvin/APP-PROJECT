package com.smartmedicare.services;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.model.Filters;
import com.smartmedicare.models.Doctor;

public class DoctorDAO extends BaseDAO<Doctor> {
    
    public DoctorDAO() {
        super("doctors");
    }

    @Override
    protected Doctor documentToEntity(Document doc) {
        Doctor doctor = new Doctor();
        doctor.setId(doc.getObjectId("_id"));
        doctor.setUsername(doc.getString("username"));
        doctor.setPassword(doc.getString("password"));
        doctor.setName(doc.getString("name"));
        doctor.setEmail(doc.getString("email"));
        doctor.setContact(doc.getString("contact"));
        doctor.setSpecialization(doc.getString("specialization"));
        doctor.setSchedule(doc.getString("schedule"));
        
        @SuppressWarnings("unchecked")
        List<ObjectId> assignedPatients = (List<ObjectId>) doc.get("assignedPatients", List.class);
        doctor.setAssignedPatients(assignedPatients != null ? assignedPatients : new ArrayList<>());
        
        @SuppressWarnings("unchecked")
        List<ObjectId> appointments = (List<ObjectId>) doc.get("appointments", List.class);
        doctor.setAppointments(appointments != null ? appointments : new ArrayList<>());
        
        return doctor;
    }

    @Override
    public Document entityToDocument(Doctor doctor) {
        return new Document("_id", doctor.getId())
                .append("username", doctor.getUsername())
                .append("password", doctor.getPassword())
                .append("name", doctor.getName())
                .append("email", doctor.getEmail())
                .append("contact", doctor.getContact())
                .append("specialization", doctor.getSpecialization())
                .append("schedule", doctor.getSchedule())
                .append("assignedPatients", doctor.getAssignedPatients())
                .append("appointments", doctor.getAppointments())
                .append("userType", "DOCTOR");
    }

    public Doctor findByUsername(String username) {
        Document doc = collection.find(Filters.eq("username", username)).first();
        return doc != null ? documentToEntity(doc) : null;
    }

    public List<Doctor> findBySpecialization(String specialization) {
        List<Doctor> doctors = new ArrayList<>();
        collection.find(Filters.eq("specialization", specialization)).forEach(
            doc -> doctors.add(documentToEntity(doc))
        );
        return doctors;
    }

    public Doctor findById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        return doc != null ? documentToEntity(doc) : null;
    }
}