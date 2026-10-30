package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.CreatePatientDto;
import com.antomino678.medicalclinic.dto.PatientDto;
import com.antomino678.medicalclinic.dto.UpdatePatientDto;
import com.antomino678.medicalclinic.exception.PatientAlreadyExistsException;
import com.antomino678.medicalclinic.exception.PatientNotFoundException;
import com.antomino678.medicalclinic.mapper.PatientMapper;
import com.antomino678.medicalclinic.model.Patient;
import com.antomino678.medicalclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public List<PatientDto> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toDto)
                .toList();
    }

    public PatientDto getPatientByEmail(String email) {
        return patientRepository.findByEmail(email)
                .map(patientMapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException(email));
    }

    public PatientDto addPatient(CreatePatientDto createPatientDto) {
        if (patientRepository.findByEmail(createPatientDto.email()).isPresent()) {
            throw new PatientAlreadyExistsException(createPatientDto.email());
        }
        Patient patient = patientMapper.toPatient(createPatientDto);
        return patientMapper.toDto(patientRepository.save(patient));
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
        patient.update(patientMapper.toPatient(email, updatePatientDto));
        return patientMapper.toDto(patientRepository.save(patient));
    }
}
