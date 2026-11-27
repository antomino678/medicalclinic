package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.CreateFacilityDto;
import com.antomino678.medicalclinic.dto.FacilityDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.exception.FacilityAlreadyExistsException;
import com.antomino678.medicalclinic.exception.FacilityNotFoundException;
import com.antomino678.medicalclinic.exception.InvalidFacilityNameException;
import com.antomino678.medicalclinic.mapper.FacilityMapper;
import com.antomino678.medicalclinic.model.Facility;
import com.antomino678.medicalclinic.repository.FacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FacilityServiceTest {
    private FacilityService facilityService;
    private FacilityRepository facilityRepository;
    private FacilityMapper facilityMapper;

    @BeforeEach
    void setUp() {
        this.facilityRepository = Mockito.mock(FacilityRepository.class);
        this.facilityMapper = Mappers.getMapper(FacilityMapper.class);
        this.facilityService = new FacilityService(facilityRepository, facilityMapper);
    }

    @Test
    void getAllFacilities_FacilitiesExist_FacilitiesReturned() {
        // given
        List<Facility> facilities = List.of(
                new Facility("szpital nr 1", "warszawa", "00-001", "polna", "10"),
                new Facility("przychodnia", "krakow", "30-001", "lesna", "5"));
        Pageable pageable = PageRequest.of(0, 10);
        when(facilityRepository.findAll(pageable)).thenReturn(new PageImpl<>(facilities, pageable, facilities.size()));

        // when
        PageDto<FacilityDto> result = facilityService.getAllFacilities(pageable);
        List<FacilityDto> facilityDtos = result.content();

        // then
        assertAll(
                () -> assertEquals(2, result.totalElements()),
                () -> assertEquals(2, facilityDtos.size()),
                () -> assertEquals("szpital nr 1", facilityDtos.get(0).name()),
                () -> assertEquals("warszawa", facilityDtos.get(0).city()),
                () -> assertEquals("00-001", facilityDtos.get(0).zipCode()),
                () -> assertEquals("polna", facilityDtos.get(0).street()),
                () -> assertEquals("10", facilityDtos.get(0).buildingNumber()),
                () -> assertEquals("przychodnia", facilityDtos.get(1).name()),
                () -> assertEquals("krakow", facilityDtos.get(1).city())
        );
    }

    @Test
    void getAllFacilities_NoFacilities_EmptyListReturned() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(facilityRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        PageDto<FacilityDto> result = facilityService.getAllFacilities(pageable);
        List<FacilityDto> facilityDtos = result.content();

        // then
        assertTrue(facilityDtos.isEmpty());
    }

    @Test
    void getFacilityById_FacilityExists_FacilityReturned() {
        // given
        Facility facility = new Facility("szpital nr 1", "warszawa", "00-001", "polna", "10");
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));

        // when
        FacilityDto facilityDto = facilityService.getFacilityById(1L);

        // then
        assertAll(
                () -> assertEquals("szpital nr 1", facilityDto.name()),
                () -> assertEquals("warszawa", facilityDto.city()),
                () -> assertEquals("00-001", facilityDto.zipCode()),
                () -> assertEquals("polna", facilityDto.street()),
                () -> assertEquals("10", facilityDto.buildingNumber())
        );
    }

    @Test
    void getFacilityById_FacilityNotExists_ThrowsFacilityNotFoundException() {
        // given
        when(facilityRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        FacilityNotFoundException exception = assertThrows(FacilityNotFoundException.class,
                () -> facilityService.getFacilityById(1L));

        // then
        assertEquals("Facility with id 1 not found", exception.getMessage());
    }

    @Test
    void addFacility_FacilityNotExists_FacilityAdded() {
        // given
        CreateFacilityDto createFacilityDto = new CreateFacilityDto("szpital nr 1", "warszawa", "00-001", "polna", "10");
        Facility savedFacility = new Facility("szpital nr 1", "warszawa", "00-001", "polna", "10");
        when(facilityRepository.existsByNameIgnoreCase("szpital nr 1")).thenReturn(false);
        when(facilityRepository.save(any(Facility.class))).thenReturn(savedFacility);

        // when
        FacilityDto facilityDto = facilityService.addFacility(createFacilityDto);

        // then
        assertAll(
                () -> assertEquals("szpital nr 1", facilityDto.name()),
                () -> assertEquals("warszawa", facilityDto.city()),
                () -> assertEquals("00-001", facilityDto.zipCode()),
                () -> assertEquals("polna", facilityDto.street()),
                () -> assertEquals("10", facilityDto.buildingNumber())
        );
    }

    @Test
    void addFacility_NameWithExtraSpaces_FacilitySavedWithNormalizedName() {
        // given
        CreateFacilityDto createFacilityDto = new CreateFacilityDto("  szpital   nr   1  ", "warszawa", "00-001", "polna", "10");
        when(facilityRepository.existsByNameIgnoreCase("szpital nr 1")).thenReturn(false);
        ArgumentCaptor<Facility> facilityCaptor = ArgumentCaptor.forClass(Facility.class);

        // when
        facilityService.addFacility(createFacilityDto);

        // then
        verify(facilityRepository).existsByNameIgnoreCase("szpital nr 1");
        verify(facilityRepository).save(facilityCaptor.capture());
        assertEquals("szpital nr 1", facilityCaptor.getValue().getName());
    }

    @Test
    void addFacility_FacilityExists_ThrowsFacilityAlreadyExistsException() {
        // given
        CreateFacilityDto createFacilityDto = new CreateFacilityDto("szpital nr 1", "warszawa", "00-001", "polna", "10");
        when(facilityRepository.existsByNameIgnoreCase("szpital nr 1")).thenReturn(true);

        // when
        FacilityAlreadyExistsException exception = assertThrows(FacilityAlreadyExistsException.class,
                () -> facilityService.addFacility(createFacilityDto));

        // then
        assertEquals("Facility with name szpital nr 1 already exists", exception.getMessage());
        verify(facilityRepository, never()).save(any());
    }

    @Test
    void addFacility_EmptyName_ThrowsInvalidFacilityNameException() {
        // given
        CreateFacilityDto createFacilityDto = new CreateFacilityDto("   ", "warszawa", "00-001", "polna", "10");

        // when
        InvalidFacilityNameException exception = assertThrows(InvalidFacilityNameException.class,
                () -> facilityService.addFacility(createFacilityDto));

        // then
        assertEquals("Facility must not be empty", exception.getMessage());
        verify(facilityRepository, never()).save(any());
    }
}
