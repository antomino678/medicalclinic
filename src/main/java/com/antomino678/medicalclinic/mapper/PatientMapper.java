package com.antomino678.medicalclinic.mapper;

import com.antomino678.medicalclinic.dto.CreatePatientDto;
import com.antomino678.medicalclinic.dto.PatientDto;
import com.antomino678.medicalclinic.dto.UpdatePatientDto;
import com.antomino678.medicalclinic.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientDto toDto(Patient patient);

    @Mapping(source = "email", target = "email")
    Patient toPatient(String email, CreatePatientDto createPatientDto);

    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "idCardNo", ignore = true)
    Patient toPatient(UpdatePatientDto updatePatientDto);
}
