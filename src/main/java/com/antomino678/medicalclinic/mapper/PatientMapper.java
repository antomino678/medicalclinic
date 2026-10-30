package com.antomino678.medicalclinic.mapper;

import com.antomino678.medicalclinic.dto.CreatePatientDto;
import com.antomino678.medicalclinic.dto.PatientDto;
import com.antomino678.medicalclinic.dto.UpdatePatientDto;
import com.antomino678.medicalclinic.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {
    public PatientDto toDto(Patient patient) {
        return new PatientDto(
                patient.getEmail(),
                patient.getIdCardNo(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getPhoneNumber(),
                patient.getBirthday()
        );
    }

    public Patient toPatient(CreatePatientDto createPatientDto) {
        return new Patient(
                createPatientDto.email(),
                createPatientDto.password(),
                createPatientDto.idCardNo(),
                createPatientDto.firstName(),
                createPatientDto.lastName(),
                createPatientDto.phoneNumber(),
                createPatientDto.birthday()
        );
    }

    public Patient toPatient(String email, UpdatePatientDto updatePatientDto) {
        return new Patient(
                email,
                updatePatientDto.password(),
                updatePatientDto.idCardNo(),
                updatePatientDto.firstName(),
                updatePatientDto.lastName(),
                updatePatientDto.phoneNumber(),
                updatePatientDto.birthday()
        );
    }
}
