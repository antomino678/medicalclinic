package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.CreatePatientDto;
import com.antomino678.medicalclinic.dto.PatientDto;
import com.antomino678.medicalclinic.dto.UpdatePatientDto;
import com.antomino678.medicalclinic.exception.PatientAlreadyExistsException;
import com.antomino678.medicalclinic.exception.PatientNotFoundException;
import com.antomino678.medicalclinic.model.Patient;
import com.antomino678.medicalclinic.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientDto> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public PatientDto getPatientByEmail(String email) {
        return patientRepository.findByEmail(email)
                .map(this::toDto)
                .orElseThrow(() -> new PatientNotFoundException(email));
    }

    public PatientDto addPatient(CreatePatientDto createPatientDto) {
        if (patientRepository.findByEmail(createPatientDto.email()).isPresent()) {
            throw new PatientAlreadyExistsException(createPatientDto.email());
        }
        Patient patient = toPatient(createPatientDto);
        patientRepository.save(patient);
        return toDto(patient);
    }

    public void deletePatientByEmail(String email) {
        if (patientRepository.findByEmail(email).isEmpty()) {
            throw new PatientNotFoundException(email);
        }
        patientRepository.deleteByEmail(email);
    }

    public PatientDto updatePatientByEmail(String email, UpdatePatientDto updatePatientDto) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException(email));

        patient.setPassword(updatePatientDto.password());
        patient.setIdCardNo(updatePatientDto.idCardNo());
        patient.setFirstName(updatePatientDto.firstName());
        patient.setLastName(updatePatientDto.lastName());
        patient.setPhoneNumber(updatePatientDto.phoneNumber());
        patient.setBirthday(updatePatientDto.birthday());

        patientRepository.save(patient);
        return toDto(patient);
    }

    private PatientDto toDto(Patient patient) {
        return new PatientDto(
                patient.getEmail(),
                patient.getIdCardNo(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getPhoneNumber(),
                patient.getBirthday()
        );
    }

    private Patient toPatient(CreatePatientDto createPatientDto) {
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

}
