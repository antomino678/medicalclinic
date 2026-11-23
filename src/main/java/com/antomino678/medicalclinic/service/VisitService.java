package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.BookVisitDto;
import com.antomino678.medicalclinic.dto.CreateVisitDto;
import com.antomino678.medicalclinic.dto.VisitDto;
import com.antomino678.medicalclinic.exception.*;
import com.antomino678.medicalclinic.mapper.VisitMapper;
import com.antomino678.medicalclinic.model.Doctor;
import com.antomino678.medicalclinic.model.Patient;
import com.antomino678.medicalclinic.model.Visit;
import com.antomino678.medicalclinic.repository.DoctorRepository;
import com.antomino678.medicalclinic.repository.PatientRepository;
import com.antomino678.medicalclinic.repository.VisitRepository;
import com.antomino678.medicalclinic.util.EmailNormalizer;
import com.antomino678.medicalclinic.util.VisitTimeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class VisitService {
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitMapper visitMapper;

    public List<VisitDto> getAllVisits() {
        return visitRepository.findAll().stream()
                .map(visitMapper::toDto)
                .toList();
    }

    public VisitDto findById(Long id) {
        return visitRepository.findById(id)
                .map(visitMapper::toDto)
                .orElseThrow(() -> new VisitNotFoundException(id));
    }

    public VisitDto addVisit(CreateVisitDto createVisitDto) {
        VisitTimeValidator.validate(createVisitDto.startTime(), createVisitDto.endTime());
        Doctor doctor = doctorRepository.findById(createVisitDto.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(createVisitDto.doctorId()));
        Visit visit = new Visit(createVisitDto.startTime(), createVisitDto.endTime(), doctor);
        if (visitRepository.existsByDoctorIdAndStartTimeLessThanAndEndTimeGreaterThan(
                doctor.getId(),
                visit.getEndTime(),
                visit.getStartTime())) {
            throw new VisitOverlapsException(doctor.getId(), visit.getStartTime(), visit.getEndTime());
        }

        return visitMapper.toDto(visitRepository.save(visit));
    }

    public VisitDto registerPatient(Long id, BookVisitDto bookVisitDto) {
        Visit visit = visitRepository.findById(id).orElseThrow(() -> new VisitNotFoundException(id));
        if (visit.isBooked()) {
            throw new VisitAlreadyBookedException(id);
        }
        VisitTimeValidator.validateNotInPast(visit.getStartTime());
        String email = EmailNormalizer.normalize(bookVisitDto.patientEmail());
        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new PatientNotFoundException(email));
        visit.assignPatient(patient);
        return visitMapper.toDto(visitRepository.save(visit));
    }

    public List<VisitDto> getPatientVisits(String rawEmail) {
        String email = EmailNormalizer.normalize(rawEmail);
        if (!patientRepository.existsByUserEmail(email)) {
            throw new PatientNotFoundException(email);
        }        return visitRepository.findByPatientUserEmail(email).stream()
                .map(visitMapper::toDto)
                .toList();
    }
}
