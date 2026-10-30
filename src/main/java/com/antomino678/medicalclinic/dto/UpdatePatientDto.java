package com.antomino678.medicalclinic.dto;

import java.time.LocalDate;

public record UpdatePatientDto(
        String password,
        String idCardNo,
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate birthday) {
}
