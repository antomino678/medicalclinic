package com.antomino678.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class UserHasDoctorException extends MedicalClinicException {
    public UserHasDoctorException(String email) {
        super("User with email " + email + " has an assigned doctor and cannot be deleted", HttpStatus.CONFLICT);
    }
}
