package com.smartmedicare.controllers;

import com.smartmedicare.models.Doctor;

public interface DoctorAwareController {
    void setDoctor(Doctor doctor);
}