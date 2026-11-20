package com.antomino678.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class DoctorAlreadyAssignedException extends MedicalClinicException {
    public DoctorAlreadyAssignedException(Long doctorId, Long facilityId) {
        super("Doctor with id " + doctorId + " already assigned to facility with id " + facilityId, HttpStatus.CONFLICT);
    }
}
