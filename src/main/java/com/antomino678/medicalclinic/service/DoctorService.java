package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.*;
import com.antomino678.medicalclinic.exception.*;
import com.antomino678.medicalclinic.mapper.DoctorMapper;
import com.antomino678.medicalclinic.model.Doctor;
import com.antomino678.medicalclinic.model.Facility;
import com.antomino678.medicalclinic.model.User;
import com.antomino678.medicalclinic.repository.DoctorRepository;
import com.antomino678.medicalclinic.repository.FacilityRepository;
import com.antomino678.medicalclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final DoctorMapper doctorMapper;

    public PageDto<DoctorDto> getAllDoctors(Pageable pageable) {
        return PageDto.from(doctorRepository.findAll(pageable)
                .map(doctorMapper::toDto));
    }

    public DoctorDto getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .map(doctorMapper::toDto)
                .orElseThrow(() -> new DoctorNotFoundException(id));
    }

    public DoctorDto addDoctor(CreateDoctorDto createDoctorDto) {
        User user = userRepository
                .findById(createDoctorDto.userId())
                .orElseThrow(() -> new UserNotFoundException(createDoctorDto.userId()));
        if (doctorRepository.existsByUserId(createDoctorDto.userId())) {
            throw new DoctorAlreadyExistsException(createDoctorDto.userId());
        }
        Doctor doctor = doctorMapper.toDoctor(user, createDoctorDto);
        return doctorMapper.toDto(doctorRepository.save(doctor));
    }

    public void deleteDoctorById(Long id) {
        Doctor doctor = doctorRepository
                .findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id));
        doctorRepository.delete(doctor);
    }

    public DoctorDto updateDoctorById(Long id, UpdateDoctorDto updateDoctorDto) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id));
        doctor.update(doctorMapper.toDoctor(updateDoctorDto));
        return doctorMapper.toDto(doctorRepository.save(doctor));
    }

    public DoctorDto addFacility(Long doctorId, Long facilityId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException(doctorId));
        Facility facility = facilityRepository.findById(facilityId).orElseThrow(() -> new FacilityNotFoundException(facilityId));
        if (doctor.getFacilities().contains(facility)) {
            throw new DoctorAlreadyAssignedException(doctorId, facilityId);
        }
        doctor.addFacility(facility);
        return doctorMapper.toDto(doctorRepository.save(doctor));
    }
}
