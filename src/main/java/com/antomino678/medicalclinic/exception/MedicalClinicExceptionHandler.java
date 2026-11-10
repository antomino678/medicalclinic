package com.antomino678.medicalclinic.exception;

import com.antomino678.medicalclinic.dto.ErrorMessageDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MedicalClinicExceptionHandler {

    @ExceptionHandler(MedicalClinicException.class)
    ResponseEntity<ErrorMessageDto> handleMedicalClinicException (MedicalClinicException exception) {
        return ResponseEntity.status(exception.getStatus()).body(new ErrorMessageDto(exception.getMessage()));
    }
}
