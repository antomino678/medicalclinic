package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.CreateFacilityDto;
import com.antomino678.medicalclinic.dto.FacilityDto;
import com.antomino678.medicalclinic.exception.FacilityAlreadyExistsException;
import com.antomino678.medicalclinic.exception.FacilityNotFoundException;
import com.antomino678.medicalclinic.mapper.FacilityMapper;
import com.antomino678.medicalclinic.model.Facility;
import com.antomino678.medicalclinic.repository.FacilityRepository;
import com.antomino678.medicalclinic.util.FacilityNameNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityService {
    private final FacilityRepository facilityRepository;
    private final FacilityMapper facilityMapper;

    public List<FacilityDto> getAllFacilities() {
        return facilityRepository.findAll()
                .stream()
                .map(facilityMapper::toDto)
                .toList();
    }

    public FacilityDto getFacilityById(Long id) {
        return facilityRepository
                .findById(id)
                .map(facilityMapper::toDto)
                .orElseThrow(() -> new FacilityNotFoundException(id));
    }

    public FacilityDto addFacility(CreateFacilityDto createFacilityDto) {
        String name = FacilityNameNormalizer.normalize(createFacilityDto.name());
        if (facilityRepository.existsByNameIgnoreCase(name)) {
            throw new FacilityAlreadyExistsException(name);
        }
        Facility facility = facilityMapper.toFacility(name, createFacilityDto);
        return facilityMapper.toDto(facilityRepository.save(facility));
    }
}
