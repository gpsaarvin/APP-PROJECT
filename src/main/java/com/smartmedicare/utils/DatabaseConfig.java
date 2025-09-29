package com.smartmedicare.utils;

public class DatabaseConfig {
    private static final String DATABASE_NAME = "smart_medicare";
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    
    // Collection names
    public static final String USERS_COLLECTION = "users";
    public static final String PATIENTS_COLLECTION = "patients";
    public static final String DOCTORS_COLLECTION = "doctors";
    public static final String APPOINTMENTS_COLLECTION = "appointments";
    public static final String MEDICAL_RECORDS_COLLECTION = "medical_records";
    public static final String PRESCRIPTIONS_COLLECTION = "prescriptions";
    
    public static String getDatabaseName() {
        return DATABASE_NAME;
    }
    
    public static String getConnectionString() {
        return CONNECTION_STRING;
    }
}