package com.smartmedicare.services;

import org.mindrot.jbcrypt.BCrypt;

import com.smartmedicare.dao.UserDAO;
import com.smartmedicare.models.Doctor;
import com.smartmedicare.models.Patient;
import com.smartmedicare.models.User;

public class AuthenticationService {
    private static AuthenticationService instance;
    private final UserDAO userDAO;
    private User currentUser;

    private AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public User authenticateUser(String username, String password, User.UserType userType) {
        try {
            if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
                System.err.println("Authentication failed: Empty username or password");
                return null;
            }

            final String finalUsername = username.trim();
            
            return userDAO.findByUsernameAndType(finalUsername, userType)
                .filter(foundUser -> verifyPassword(password, foundUser.getPassword()))
                .map(foundUser -> {
                    // Load the full role-specific entity for downstream controllers
                    try {
                        switch (userType) {
                            case DOCTOR -> {
                                DoctorDAO doctorDAO = new DoctorDAO();
                                Doctor doctor = doctorDAO.findByUsername(finalUsername);
                                if (doctor == null) {
                                    // Backfill doctor profile from base user if missing
                                    doctor = new Doctor();
                                    doctor.setId(foundUser.getId());
                                    doctor.setUsername(foundUser.getUsername());
                                    doctor.setPassword(foundUser.getPassword());
                                    doctor.setName(foundUser.getName());
                                    doctor.setEmail(foundUser.getEmail());
                                    doctor.setContact(foundUser.getContact());
                                    try { doctorDAO.insert(doctorDAO.entityToDocument(doctor)); } catch (Exception ignored) {}
                                }
                                currentUser = doctor;
                            }
                            case PATIENT -> {
                                PatientDAO patientDAO = new PatientDAO();
                                Patient patient = patientDAO.findByUsername(finalUsername);
                                if (patient == null) {
                                    // Backfill patient profile from base user if missing
                                    patient = new Patient();
                                    patient.setId(foundUser.getId());
                                    patient.setUsername(foundUser.getUsername());
                                    patient.setPassword(foundUser.getPassword());
                                    patient.setName(foundUser.getName());
                                    patient.setEmail(foundUser.getEmail());
                                    patient.setContact(foundUser.getContact());
                                    try { patientDAO.insert(patientDAO.entityToDocument(patient)); } catch (Exception ignored) {}
                                }
                                currentUser = patient;
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Warning: Failed loading full profile for user '" + finalUsername + "': " + ex.getMessage());
                        currentUser = foundUser;
                    }

                    System.out.println("User authenticated successfully: " + finalUsername);
                    return currentUser;
                })
                .orElseGet(() -> {
                    System.err.println("Authentication failed for user: " + finalUsername);
                    return null;
                });
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            return null;
        }
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            System.err.println("No user currently logged in");
        }
        return currentUser;
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("User logged out: " + currentUser.getUsername());
            currentUser = null;
        }
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User.UserType getCurrentUserType() {
        return currentUser != null ? currentUser.getUserType() : null;
    }

    public boolean isUsernameTaken(String username, User.UserType userType) {
        return userDAO.findByUsernameAndType(username, userType).isPresent();
    }
}