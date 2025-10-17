package com.smartmedicare.services;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.smartmedicare.models.Patient;
import com.smartmedicare.utils.DatabaseConnection;

public class PatientService {
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;
    
    public PatientService() {
        this.database = DatabaseConnection.getInstance().getDatabase();
        this.collection = database.getCollection("patients");
    }
    
    public void updatePatient(Patient patient) {
        try {
            Document update = new Document()
                .append("$set", new Document()
                    .append("name", patient.getName())
                    .append("email", patient.getEmail())
                    .append("dateOfBirth", patient.getDateOfBirth())
                    .append("gender", patient.getGender())
                    .append("bloodGroup", patient.getBloodGroup())
                    .append("phone", patient.getPhone())
                    .append("address", patient.getAddress())
                    .append("emergencyContactName", patient.getEmergencyContactName())
                    .append("emergencyContactRelation", patient.getEmergencyContactRelation())
                    .append("emergencyContactPhone", patient.getEmergencyContactPhone())
                    .append("allergies", patient.getAllergies())
                    .append("currentMedications", patient.getCurrentMedications())
                    .append("medicalConditions", patient.getMedicalConditions())
                );
            
            collection.updateOne(
                Filters.eq("_id", patient.getId()),
                update
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to update patient profile: " + e.getMessage());
        }
    }
}