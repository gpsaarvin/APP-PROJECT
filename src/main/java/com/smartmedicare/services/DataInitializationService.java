package com.smartmedicare.services;

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;

import com.smartmedicare.models.Doctor;

public class DataInitializationService {
    private final DoctorDAO doctorDAO = new DoctorDAO();
    
    public void initializeSampleData() {
        // Check if doctors already exist
        if (doctorDAO.findBySpecialization("General Medicine").isEmpty()) {
            System.out.println("Initializing sample doctors...");
            createSampleDoctors();
        } else {
            System.out.println("Sample doctors already exist in database.");
        }
    }
    
    private void createSampleDoctors() {
        // General Medicine Doctors
        createDoctor("dr_smith", "password123", "Dr. John Smith", "john.smith@hospital.com", 
                    "+1-555-0101", "General Medicine", "Mon-Fri 9:00-17:00");
        
        createDoctor("dr_johnson", "password123", "Dr. Emily Johnson", "emily.johnson@hospital.com", 
                    "+1-555-0102", "General Medicine", "Mon-Wed-Fri 9:00-17:00");
        
        // Cardiology Doctors
        createDoctor("dr_heart", "password123", "Dr. Michael Heart", "michael.heart@hospital.com", 
                    "+1-555-0201", "Cardiology", "Tue-Thu 10:00-16:00");
        
        createDoctor("dr_cardiac", "password123", "Dr. Sarah Cardiac", "sarah.cardiac@hospital.com", 
                    "+1-555-0202", "Cardiology", "Mon-Wed-Fri 8:00-16:00");
        
        // Dermatology Doctors
        createDoctor("dr_skin", "password123", "Dr. David Skin", "david.skin@hospital.com", 
                    "+1-555-0301", "Dermatology", "Mon-Fri 9:00-15:00");
        
        // Neurology Doctors
        createDoctor("dr_brain", "password123", "Dr. Lisa Brain", "lisa.brain@hospital.com", 
                    "+1-555-0401", "Neurology", "Tue-Thu-Sat 10:00-18:00");
        
        // Pediatrics Doctors
        createDoctor("dr_kids", "password123", "Dr. Robert Kids", "robert.kids@hospital.com", 
                    "+1-555-0501", "Pediatrics", "Mon-Wed-Fri 8:00-16:00");
        
        createDoctor("dr_child", "password123", "Dr. Maria Child", "maria.child@hospital.com", 
                    "+1-555-0502", "Pediatrics", "Tue-Thu 9:00-17:00");
        
        // Orthopedics Doctors
        createDoctor("dr_bone", "password123", "Dr. James Bone", "james.bone@hospital.com", 
                    "+1-555-0601", "Orthopedics", "Mon-Fri 8:00-16:00");
        
        System.out.println("Sample doctors created successfully!");
    }
    
    private void createDoctor(String username, String password, String name, String email, 
                             String contact, String specialization, String schedule) {
        Doctor doctor = new Doctor();
        doctor.setId(new ObjectId());
        doctor.setUsername(username);
        doctor.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        doctor.setName(name);
        doctor.setEmail(email);
        doctor.setContact(contact);
        doctor.setSpecialization(specialization);
        doctor.setSchedule(schedule);
        doctor.setAssignedPatients(new ArrayList<>());
        doctor.setAppointments(new ArrayList<>());
        
        try {
            doctorDAO.insert(doctorDAO.entityToDocument(doctor));
            System.out.println("Created doctor: " + name + " (" + specialization + ")");
        } catch (Exception e) {
            System.err.println("Error creating doctor " + name + ": " + e.getMessage());
        }
    }
}