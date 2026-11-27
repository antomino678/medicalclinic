package com.antomino678.medicalclinic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CreateVisitDto(
        @Schema(example = "2027-01-15T10:00:00") LocalDateTime startTime,
        @Schema(example = "2027-01-15T10:30:00") LocalDateTime endTime,
        @Schema(example = "1") Long doctorId) {
}
