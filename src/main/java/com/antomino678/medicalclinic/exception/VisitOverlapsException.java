package com.antomino678.medicalclinic.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class VisitOverlapsException extends MedicalClinicException {
    public VisitOverlapsException(Long doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        super("Doctor with id " + doctorId + " already has a visit between " + startTime + " and " + endTime, HttpStatus.CONFLICT);
    }
}
