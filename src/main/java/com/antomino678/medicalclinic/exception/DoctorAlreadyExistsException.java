package com.antomino678.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class DoctorAlreadyExistsException extends MedicalClinicException {
    public DoctorAlreadyExistsException(Long id) {
        super("User with id " + id + " is already a doctor", HttpStatus.CONFLICT);
    }
}
