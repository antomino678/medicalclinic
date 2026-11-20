package com.antomino678.medicalclinic.dto;

public record CreateDoctorDto(String firstName, String lastName, String speciality, Long userId) {
}
