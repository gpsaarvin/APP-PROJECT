package com.smartmedicare.dao;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.smartmedicare.models.Prescription;
import com.smartmedicare.services.BaseDAO;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrescriptionDAO extends BaseDAO<Prescription> {
    public PrescriptionDAO() {
        super("prescriptions");
    }

    private LocalDate toLocalDate(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }

    private Date toDate(LocalDate localDate) {
        return localDate != null ? Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
    }

    @Override
    public Document entityToDocument(Prescription prescription) {
        Document doc = new Document()
                .append("patientId", prescription.getPatientId())
                .append("doctorId", prescription.getDoctorId())
                .append("doctorName", prescription.getDoctorName())
                .append("dateIssued", toDate(prescription.getDateIssued()))
                .append("diagnosis", prescription.getDiagnosis())
                .append("medicines", prescription.getMedicines())
                .append("notes", prescription.getNotes());

        if (prescription.getFollowUpDate() != null) {
            doc.append("followUpDate", toDate(prescription.getFollowUpDate()));
        }

        return doc;
    }

    @Override
    protected Prescription documentToEntity(Document doc) {
        Prescription prescription = new Prescription();
        prescription.setId(doc.getObjectId("_id").toString());
        prescription.setPatientId(doc.getString("patientId"));
        prescription.setDoctorId(doc.getString("doctorId"));
        prescription.setDoctorName(doc.getString("doctorName"));
        prescription.setDateIssued(toLocalDate(doc.getDate("dateIssued")));
        prescription.setDiagnosis(doc.getString("diagnosis"));
        prescription.setMedicines(doc.getString("medicines"));
        prescription.setNotes(doc.getString("notes"));
        
        Date followUpDate = doc.getDate("followUpDate");
        if (followUpDate != null) {
            prescription.setFollowUpDate(toLocalDate(followUpDate));
        }
        
        return prescription;
    }

    public void save(Prescription prescription) {
        Document doc = entityToDocument(prescription);
        if (prescription.getId() == null || prescription.getId().isEmpty()) {
            InsertOneResult result = insert(doc);
            if (result != null && result.getInsertedId() != null && result.getInsertedId().asObjectId() != null) {
                prescription.setId(result.getInsertedId().asObjectId().getValue().toString());
            } else {
                throw new IllegalStateException("Failed to get inserted ID for prescription");
            }
        } else {
            update(new ObjectId(prescription.getId()), doc);
        }
    }

    public List<Prescription> findByPatientId(String patientId) {
        List<Prescription> result = new ArrayList<>();
        collection.find(Filters.eq("patientId", patientId))
                 .forEach(doc -> result.add(documentToEntity(doc)));
        return result;
    }

    public Prescription findById(String id) {
        Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        return doc != null ? documentToEntity(doc) : null;
    }

    public void delete(String id) {
        delete(new ObjectId(id));
    }

    public long countPendingPrescriptions(ObjectId doctorId) {
        // Count prescriptions with follow-up dates in the future
        LocalDate today = LocalDate.now();
        return collection.countDocuments(Filters.and(
            Filters.eq("doctorId", doctorId.toString()),
            Filters.gt("followUpDate", toDate(today))
        ));
    }
}