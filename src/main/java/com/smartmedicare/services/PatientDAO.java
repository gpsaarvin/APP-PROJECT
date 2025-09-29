package com.smartmedicare.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.model.Filters;
import com.smartmedicare.models.Patient;

public class PatientDAO extends BaseDAO<Patient> {
    
    public PatientDAO() {
        super("patients");
    }

    @Override
    protected Patient documentToEntity(Document doc) {
        Patient patient = new Patient();
        patient.setId(doc.getObjectId("_id"));
        patient.setUsername(doc.getString("username"));
        patient.setPassword(doc.getString("password"));
        patient.setName(doc.getString("name"));
        patient.setEmail(doc.getString("email"));
        patient.setContact(doc.getString("contact"));
        patient.setDateOfBirth(LocalDate.parse(doc.getString("dateOfBirth")));
        patient.setGender(doc.getString("gender"));
        patient.setBloodGroup(doc.getString("bloodGroup"));
        
        @SuppressWarnings("unchecked")
        List<ObjectId> medicalHistory = (List<ObjectId>) doc.get("medicalHistory", List.class);
        patient.setMedicalHistory(medicalHistory != null ? medicalHistory : new ArrayList<>());
        
        @SuppressWarnings("unchecked")
        List<ObjectId> appointments = (List<ObjectId>) doc.get("appointments", List.class);
        patient.setAppointments(appointments != null ? appointments : new ArrayList<>());
        
        @SuppressWarnings("unchecked")
        List<ObjectId> prescriptions = (List<ObjectId>) doc.get("prescriptions", List.class);
        patient.setPrescriptions(prescriptions != null ? prescriptions : new ArrayList<>());
        
        return patient;
    }

    @Override
    public Document entityToDocument(Patient patient) {
        return new Document("_id", patient.getId())
                .append("username", patient.getUsername())
                .append("password", patient.getPassword())
                .append("name", patient.getName())
                .append("email", patient.getEmail())
                .append("contact", patient.getContact())
                .append("dateOfBirth", patient.getDateOfBirth().toString())
                .append("gender", patient.getGender())
                .append("bloodGroup", patient.getBloodGroup())
                .append("medicalHistory", patient.getMedicalHistory())
                .append("appointments", patient.getAppointments())
                .append("prescriptions", patient.getPrescriptions())
                .append("userType", "PATIENT");
    }

    public Patient findByUsername(String username) {
        Document doc = collection.find(Filters.eq("username", username)).first();
        return doc != null ? documentToEntity(doc) : null;
    }

    public List<Patient> findByDoctor(ObjectId doctorId) {
        // First get patient IDs from appointments
        List<ObjectId> patientIds = DatabaseService.getInstance().getDatabase()
            .getCollection("appointments")
            .distinct("patientId", Filters.eq("doctorId", doctorId), ObjectId.class)
            .into(new ArrayList<>());
        
        // Then get patient details
        List<Patient> patients = new ArrayList<>();
        if (!patientIds.isEmpty()) {
            collection.find(Filters.in("_id", patientIds))
                     .forEach(doc -> patients.add(documentToEntity(doc)));
        }
        return patients;
    }

    public List<Patient> searchPatients(String searchTerm, String filter, ObjectId doctorId) {
        List<Patient> patients = findByDoctor(doctorId);
        return patients.stream()
                      .filter(p -> matchesSearchCriteria(p, searchTerm, filter))
                      .toList();
    }

    private boolean matchesSearchCriteria(Patient patient, String searchTerm, String filter) {
        // First check the search term
        boolean matchesSearch = searchTerm.isEmpty() ||
            patient.getName().toLowerCase().contains(searchTerm) ||
            patient.getEmail().toLowerCase().contains(searchTerm) ||
            patient.getContact().toLowerCase().contains(searchTerm);

        if (!matchesSearch) return false;

        // Then apply the filter
        return switch (filter) {
            case "All Patients" -> true;
            case "Recent Visits" -> hasRecentVisit(patient);
            case "Pending Follow-ups" -> hasPendingFollowUp(patient);
            default -> true;
        };
    }

    private boolean hasRecentVisit(Patient patient) {
        // Get the patient's latest appointment within the last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return DatabaseService.getInstance().getDatabase()
            .getCollection("appointments")
            .countDocuments(Filters.and(
                Filters.eq("patientId", patient.getId()),
                Filters.gte("dateTime", thirtyDaysAgo.toString())
            )) > 0;
    }

    private boolean hasPendingFollowUp(Patient patient) {
        // Check if patient has any prescription with future follow-up date
        LocalDate today = LocalDate.now();
        return DatabaseService.getInstance().getDatabase()
            .getCollection("prescriptions")
            .countDocuments(Filters.and(
                Filters.eq("patientId", patient.getId()),
                Filters.gt("followUpDate", today.toString())
            )) > 0;
    }

    public long countPatientsByDoctor(ObjectId doctorId) {
        // First find unique patientIds from appointments collection for this doctor
        List<ObjectId> patientIds = DatabaseService.getInstance().getDatabase()
            .getCollection("appointments")
            .distinct("patientId", Filters.eq("doctorId", doctorId), ObjectId.class)
            .into(new ArrayList<>());

        // Then count those patients
        return !patientIds.isEmpty() ? 
               collection.countDocuments(Filters.in("_id", patientIds)) : 0;
    }
}