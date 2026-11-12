package com.antomino678.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class InvalidEmailException extends MedicalClinicException {
    public InvalidEmailException() {
        super("Email must not be empty", HttpStatus.BAD_REQUEST);
    }
}
