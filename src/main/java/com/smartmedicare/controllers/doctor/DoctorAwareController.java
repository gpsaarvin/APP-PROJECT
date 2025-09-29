package com.smartmedicare.controllers.doctor;

import com.smartmedicare.models.Doctor;

public interface DoctorAwareController {
    void setDoctor(Doctor doctor);
}