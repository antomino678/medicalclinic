package com.antomino678.medicalclinic.dto;

import java.util.Set;

public record DoctorDto(String firstName,
                        String lastName,
                        String speciality,
                        Long id,
                        UserDto user,
                        Set<FacilityDto> facilities) {
}
