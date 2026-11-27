package com.antomino678.medicalclinic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateDoctorDto(
        @Schema(example = "anna") String firstName,
        @Schema(example = "nowak") String lastName,
        @Schema(example = "kardiolog") String speciality,
        @Schema(example = "2") Long userId) {
}
