package com.antomino678.medicalclinic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateDoctorDto(
        @Schema(example = "anna") String firstName,
        @Schema(example = "nowak") String lastName,
        @Schema(example = "dentysta") String speciality) {
}
