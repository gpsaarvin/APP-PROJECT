package com.smartmedicare.services;

import java.time.LocalDate;

import org.bson.types.ObjectId;

import com.smartmedicare.dao.UserDAO;
import com.smartmedicare.models.Doctor;
import com.smartmedicare.models.Patient;
import com.smartmedicare.models.User;

public class RegistrationService {
    private static RegistrationService instance;
    private final UserDAO userDAO;
    private final AuthenticationService authService;
    private String lastError;

    private RegistrationService() {
        this.userDAO = new UserDAO();
        this.authService = AuthenticationService.getInstance();
    }

    public static RegistrationService getInstance() {
        if (instance == null) {
            instance = new RegistrationService();
        }
        return instance;
    }

    public boolean registerPatient(String username, String password, String name, String email, 
                                 String contact, LocalDate dateOfBirth, String gender, String bloodGroup) {
        try {
            if (username == null || username.trim().isEmpty()) {
                lastError = "Username cannot be empty";
                return false;
            }

            if (authService.isUsernameTaken(username, User.UserType.PATIENT)) {
                lastError = "Username is already taken";
                return false;
            }

            Patient patient = new Patient();
            patient.setId(new ObjectId());
            patient.setUsername(username.trim());
            patient.setPassword(authService.hashPassword(password));
            patient.setName(name);
            patient.setEmail(email);
            patient.setContact(contact);
            patient.setUserType(User.UserType.PATIENT);
            patient.setDateOfBirth(dateOfBirth);
            patient.setGender(gender);
            patient.setBloodGroup(bloodGroup);

            // Save into unified users collection
            userDAO.save(patient);

            // Also persist into role-specific patients collection for downstream DAOs/controllers
            try {
                PatientDAO patientDAO = new PatientDAO();
                patientDAO.insert(patientDAO.entityToDocument(patient));
            } catch (Exception ex) {
                System.err.println("Warning: Failed to save patient into patients collection: " + ex.getMessage());
            }
            System.out.println("Successfully registered patient: " + username);
            return true;

        } catch (IllegalArgumentException e) {
            lastError = "Invalid input: " + e.getMessage();
            System.err.println(lastError);
            return false;
        } catch (Exception e) {
            String errorMessage = "Registration error: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += "\nCause: " + e.getCause().getMessage();
            }
            lastError = errorMessage;
            System.err.println(errorMessage);
            return false;
        }
    }

    public boolean registerDoctor(String username, String password, String name, String email,
                                String contact, String specialization, String schedule) {
        try {
            if (username == null || username.trim().isEmpty()) {
                lastError = "Username cannot be empty";
                return false;
            }

            if (authService.isUsernameTaken(username, User.UserType.DOCTOR)) {
                lastError = "Username is already taken";
                return false;
            }

            Doctor doctor = new Doctor();
            doctor.setId(new ObjectId());
            doctor.setUsername(username.trim());
            doctor.setPassword(authService.hashPassword(password));
            doctor.setName(name);
            doctor.setEmail(email);
            doctor.setContact(contact);
            doctor.setUserType(User.UserType.DOCTOR);
            doctor.setSpecialization(specialization);
            doctor.setSchedule(schedule);

            // Save into unified users collection
            userDAO.save(doctor);

            // Also persist into role-specific doctors collection for downstream DAOs/controllers
            try {
                DoctorDAO doctorDAO = new DoctorDAO();
                doctorDAO.insert(doctorDAO.entityToDocument(doctor));
            } catch (Exception ex) {
                System.err.println("Warning: Failed to save doctor into doctors collection: " + ex.getMessage());
            }
            System.out.println("Successfully registered doctor: " + username);
            return true;

        } catch (IllegalArgumentException e) {
            lastError = "Invalid input: " + e.getMessage();
            System.err.println(lastError);
            return false;
        } catch (Exception e) {
            String errorMessage = "Registration error: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += "\nCause: " + e.getCause().getMessage();
            }
            lastError = errorMessage;
            System.err.println(errorMessage);
            return false;
        }
    }

    public String getLastError() {
        return lastError;
    }
}