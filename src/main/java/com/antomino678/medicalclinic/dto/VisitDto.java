package com.antomino678.medicalclinic.dto;

import java.time.LocalDateTime;

public record VisitDto(LocalDateTime startTime, LocalDateTime endTime, DoctorDto doctor, PatientDto patient, Long id) {
}
