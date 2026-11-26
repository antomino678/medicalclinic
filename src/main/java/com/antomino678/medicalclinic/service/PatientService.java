package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.CreatePatientDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.dto.PatientDto;
import com.antomino678.medicalclinic.dto.UpdatePatientDto;
import com.antomino678.medicalclinic.exception.PatientAlreadyExistsException;
import com.antomino678.medicalclinic.exception.PatientNotFoundException;
import com.antomino678.medicalclinic.exception.UserNotFoundException;
import com.antomino678.medicalclinic.mapper.PatientMapper;
import com.antomino678.medicalclinic.model.Patient;
import com.antomino678.medicalclinic.model.User;
import com.antomino678.medicalclinic.repository.PatientRepository;
import com.antomino678.medicalclinic.repository.UserRepository;
import com.antomino678.medicalclinic.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientMapper patientMapper;

    public PageDto<PatientDto> getAllPatients(Pageable pageable) {
        return PageDto.from(patientRepository.findAll(pageable)
                .map(patientMapper::toDto));
    }

    public PatientDto getPatientByEmail(String rawEmail) {
        String email = EmailNormalizer.normalize(rawEmail);
        return patientRepository.findByUserEmail(email)
                .map(patientMapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException(email));
    }

    public PatientDto addPatient(CreatePatientDto createPatientDto) {
        User user = userRepository
                .findById(createPatientDto.userId())
                .orElseThrow(() -> new UserNotFoundException(createPatientDto.userId()));
        if (patientRepository.existsByUserId(createPatientDto.userId())) {
            throw new PatientAlreadyExistsException(createPatientDto.userId());
        }
        Patient patient = patientMapper.toPatient(user, createPatientDto);
        return patientMapper.toDto(patientRepository.save(patient));
    }

    public void deletePatientByEmail(String rawEmail) {
        String email = EmailNormalizer.normalize(rawEmail);
        if (patientRepository.findByUserEmail(email).isEmpty()) {
            throw new PatientNotFoundException(email);
        }
        patientRepository.deleteByUserEmail(email);
    }

    public PatientDto updatePatientByEmail(String rawEmail, UpdatePatientDto updatePatientDto) {
        String email = EmailNormalizer.normalize(rawEmail);
        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new PatientNotFoundException(email));
        patient.update(patientMapper.toPatient(updatePatientDto));
        return patientMapper.toDto(patientRepository.save(patient));
    }
}