package com.antomino678.medicalclinic.dto;

import java.time.LocalDateTime;

public record CreateVisitDto(LocalDateTime startTime, LocalDateTime endTime, Long doctorId) {
}
