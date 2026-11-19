package com.antomino678.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class UserHasPatientException extends MedicalClinicException {
    public UserHasPatientException(String email) {
        super("User with email " + email + " has an assigned patient and cannot be deleted", HttpStatus.CONFLICT);
    }
}
