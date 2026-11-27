package com.antomino678.medicalclinic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record UpdatePatientDto(
        @Schema(example = "jan") String firstName,
        @Schema(example = "nowak") String lastName,
        @Schema(example = "123123123") String phoneNumber,
        @Schema(example = "2005-05-12") LocalDate birthday) {
}
