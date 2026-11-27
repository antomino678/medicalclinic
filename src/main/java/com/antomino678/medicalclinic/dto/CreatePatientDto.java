package com.antomino678.medicalclinic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record CreatePatientDto(
        @Schema(example = "1") Long userId,
        @Schema(example = "SAD1123123") String idCardNo,
        @Schema(example = "jan") String firstName,
        @Schema(example = "kowalski") String lastName,
        @Schema(example = "123123123") String phoneNumber,
        @Schema(example = "2005-05-12") LocalDate birthday) {
}
