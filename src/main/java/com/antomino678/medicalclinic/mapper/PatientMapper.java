package com.antomino678.medicalclinic.mapper;

import com.antomino678.medicalclinic.dto.CreatePatientDto;
import com.antomino678.medicalclinic.dto.PatientDto;
import com.antomino678.medicalclinic.dto.UpdatePatientDto;
import com.antomino678.medicalclinic.model.Patient;
import com.antomino678.medicalclinic.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    @Mapping(source = "user.email", target = "email")
    PatientDto toDto(Patient patient);

    @Mapping(target = "idCardNo", ignore = true)
    @Mapping(target = "user", ignore = true)
    Patient toPatient(UpdatePatientDto updatePatientDto);

    @Mapping(source = "user", target = "user")
    Patient toPatient(User user, CreatePatientDto createPatientDto);
}
