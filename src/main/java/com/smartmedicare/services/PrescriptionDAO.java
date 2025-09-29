package com.smartmedicare.services;

import com.mongodb.client.model.Filters;
import com.smartmedicare.models.Prescription;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class PrescriptionDAO extends BaseDAO<Prescription> {
    
    public PrescriptionDAO() {
        super("prescriptions");
    }

    @Override
    protected Prescription documentToEntity(Document doc) {
        Prescription prescription = new Prescription();
        prescription.setId(doc.getObjectId("_id").toString());
        prescription.setPatientId(doc.getObjectId("patientId").toString());
        prescription.setDoctorId(doc.getObjectId("doctorId").toString());
        prescription.setDateIssued(LocalDate.parse(doc.getString("dateIssued")));
        prescription.setDiagnosis(doc.getString("diagnosis"));
        prescription.setMedicines(doc.getString("medicines"));
        prescription.setNotes(doc.getString("notes"));
        
        String followUpDate = doc.getString("followUpDate");
        if (followUpDate != null) {
            prescription.setFollowUpDate(LocalDate.parse(followUpDate));
        }
        
        return prescription;
    }

    @Override
    public Document entityToDocument(Prescription prescription) {
        Document doc = new Document();
        if (prescription.getId() != null) {
            doc.append("_id", new ObjectId(prescription.getId()));
        }
        doc.append("patientId", new ObjectId(prescription.getPatientId()))
           .append("doctorId", new ObjectId(prescription.getDoctorId()))
           .append("dateIssued", prescription.getDateIssued().toString())
           .append("medicines", prescription.getMedicines())
           .append("diagnosis", prescription.getDiagnosis())
           .append("notes", prescription.getNotes());

        if (prescription.getFollowUpDate() != null) {
            doc.append("followUpDate", prescription.getFollowUpDate().toString());
        }

        return doc;
    }

    public List<Prescription> findByPatientId(ObjectId patientId) {
        List<Prescription> prescriptions = new ArrayList<>();
        collection.find(Filters.eq("patientId", patientId))
                 .sort(new Document("dateIssued", -1))
                 .forEach(doc -> prescriptions.add(documentToEntity(doc)));
        return prescriptions;
    }

    public List<Prescription> findByDoctorId(ObjectId doctorId) {
        List<Prescription> prescriptions = new ArrayList<>();
        collection.find(Filters.eq("doctorId", doctorId))
                 .sort(new Document("dateIssued", -1))
                 .forEach(doc -> prescriptions.add(documentToEntity(doc)));
        return prescriptions;
    }
    
    public long countPendingPrescriptions(ObjectId doctorId) {
        return collection.countDocuments(
            Filters.and(
                Filters.eq("doctorId", doctorId.toString()),
                Filters.eq("status", "PENDING")
            )
        );
    }
}