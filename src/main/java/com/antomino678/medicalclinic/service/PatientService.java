package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.ChangePasswordDto;
import com.antomino678.medicalclinic.dto.CreatePatientDto;
import com.antomino678.medicalclinic.dto.PatientDto;
import com.antomino678.medicalclinic.dto.UpdatePatientDto;
import com.antomino678.medicalclinic.exception.PatientAlreadyExistsException;
import com.antomino678.medicalclinic.exception.PatientNotFoundException;
import com.antomino678.medicalclinic.mapper.PatientMapper;
import com.antomino678.medicalclinic.model.Patient;
import com.antomino678.medicalclinic.repository.PatientRepository;
import com.antomino678.medicalclinic.util.EmailNormalizer;
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

    public PatientDto getPatientByEmail(String rawEmail) {
        String email = EmailNormalizer.normalize(rawEmail);
        return patientRepository.findByEmail(email)
                .map(patientMapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException(email));
    }

    public PatientDto addPatient(CreatePatientDto createPatientDto) {
        String email = EmailNormalizer.normalize(createPatientDto.email());
        if (patientRepository.findByEmail(email).isPresent()) {
            throw new PatientAlreadyExistsException(email);
        }
        Patient patient = patientMapper.toPatient(email, createPatientDto);
        return patientMapper.toDto(patientRepository.save(patient));
    }

    public void deletePatientByEmail(String rawEmail) {
        String email = EmailNormalizer.normalize(rawEmail);
        if (patientRepository.findByEmail(email).isEmpty()) {
            throw new PatientNotFoundException(email);
        }
        patientRepository.deleteByEmail(email);
    }

    public PatientDto updatePatientByEmail(String rawEmail, UpdatePatientDto updatePatientDto) {
        String email = EmailNormalizer.normalize(rawEmail);
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException(email));
        patient.update(patientMapper.toPatient(updatePatientDto));
        return patientMapper.toDto(patientRepository.save(patient));
    }

    public void changePassword(String rawEmail, ChangePasswordDto changePasswordDto) {
        String email = EmailNormalizer.normalize(rawEmail);
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException(email));
        patient.changePassword(changePasswordDto.newPassword());
        patientRepository.save(patient);
    }
}