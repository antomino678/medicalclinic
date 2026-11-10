package com.antomino678.medicalclinic.exception;

import org.springframework.http.HttpStatus;

abstract class MedicalClinicException extends RuntimeException {
    private final HttpStatus status;

    public MedicalClinicException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
