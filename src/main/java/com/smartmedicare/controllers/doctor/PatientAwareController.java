package com.smartmedicare.controllers.doctor;

import com.smartmedicare.models.Patient;

public interface PatientAwareController {
    void setPatient(Patient patient);
}