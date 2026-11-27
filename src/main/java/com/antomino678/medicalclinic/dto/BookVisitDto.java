package com.antomino678.medicalclinic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BookVisitDto(
        @Schema(example = "jan@test.pl") String patientEmail) {
}
