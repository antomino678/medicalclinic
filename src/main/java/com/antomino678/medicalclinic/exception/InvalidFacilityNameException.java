package com.antomino678.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class InvalidFacilityNameException extends MedicalClinicException {
    public InvalidFacilityNameException() {
        super("Facility must not be empty", HttpStatus.BAD_REQUEST);
    }
}
