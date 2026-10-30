package com.antomino678.medicalclinic.dto;

import java.time.LocalDate;

public record CreatePatientDto(
        String email,
        String password,
        String idCardNo,
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate birthday) {
}
