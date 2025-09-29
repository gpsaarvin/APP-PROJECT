package com.smartmedicare.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.model.Filters;
import com.smartmedicare.models.MedicalRecord;

public class MedicalRecordDAO extends BaseDAO<MedicalRecord> {
    
    public MedicalRecordDAO() {
        super("medical_records");
    }

    @Override
    protected MedicalRecord documentToEntity(Document doc) {
        MedicalRecord record = new MedicalRecord();
        record.setId(doc.getObjectId("_id"));
        record.setPatientId(doc.getObjectId("patientId"));
        record.setRecordDate(LocalDateTime.parse(doc.getString("recordDate")));
        
        @SuppressWarnings("unchecked")
        Map<String, String> symptoms = (Map<String, String>) doc.get("symptoms");
        record.setSymptoms(symptoms != null ? symptoms : new HashMap<>());
        
        @SuppressWarnings("unchecked")
        Map<String, Double> vitalSigns = (Map<String, Double>) doc.get("vitalSigns");
        record.setVitalSigns(vitalSigns != null ? vitalSigns : new HashMap<>());
        
        record.setDiagnosis(doc.getString("diagnosis"));
        record.setAiPrediction(doc.getString("aiPrediction"));
        record.setSeverity(doc.getString("severity"));
        record.setTreatmentPlan(doc.getString("treatmentPlan"));
        record.setNotes(doc.getString("notes"));
        
        return record;
    }

    @Override
    public Document entityToDocument(MedicalRecord record) {
        return new Document("_id", record.getId())
                .append("patientId", record.getPatientId())
                .append("recordDate", record.getRecordDate().toString())
                .append("symptoms", record.getSymptoms())
                .append("vitalSigns", record.getVitalSigns())
                .append("diagnosis", record.getDiagnosis())
                .append("aiPrediction", record.getAiPrediction())
                .append("severity", record.getSeverity())
                .append("treatmentPlan", record.getTreatmentPlan())
                .append("notes", record.getNotes());
    }

    public List<MedicalRecord> findByPatientId(ObjectId patientId) {
        List<MedicalRecord> records = new ArrayList<>();
        collection.find(Filters.eq("patientId", patientId))
                 .sort(new Document("recordDate", -1))
                 .forEach(doc -> records.add(documentToEntity(doc)));
        return records;
    }

    public List<MedicalRecord> findByPatientIdAndSeverity(ObjectId patientId, String severity) {
        List<MedicalRecord> records = new ArrayList<>();
        collection.find(Filters.and(
            Filters.eq("patientId", patientId),
            Filters.eq("severity", severity)
        )).forEach(doc -> records.add(documentToEntity(doc)));
        return records;
    }
}