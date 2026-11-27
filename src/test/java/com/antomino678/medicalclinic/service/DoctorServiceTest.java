package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.CreateDoctorDto;
import com.antomino678.medicalclinic.dto.DoctorDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.dto.UpdateDoctorDto;
import com.antomino678.medicalclinic.exception.DoctorAlreadyAssignedException;
import com.antomino678.medicalclinic.exception.DoctorAlreadyExistsException;
import com.antomino678.medicalclinic.exception.DoctorNotFoundException;
import com.antomino678.medicalclinic.exception.FacilityNotFoundException;
import com.antomino678.medicalclinic.exception.UserNotFoundException;
import com.antomino678.medicalclinic.mapper.DoctorMapper;
import com.antomino678.medicalclinic.model.Doctor;
import com.antomino678.medicalclinic.model.Facility;
import com.antomino678.medicalclinic.model.User;
import com.antomino678.medicalclinic.repository.DoctorRepository;
import com.antomino678.medicalclinic.repository.FacilityRepository;
import com.antomino678.medicalclinic.repository.UserRepository;
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

public class DoctorServiceTest {
    private DoctorService doctorService;
    private DoctorRepository doctorRepository;
    private UserRepository userRepository;
    private FacilityRepository facilityRepository;
    private DoctorMapper doctorMapper;

    @BeforeEach
    void setUp() {
        this.doctorRepository = Mockito.mock(DoctorRepository.class);
        this.userRepository = Mockito.mock(UserRepository.class);
        this.facilityRepository = Mockito.mock(FacilityRepository.class);
        this.doctorMapper = Mappers.getMapper(DoctorMapper.class);
        this.doctorService = new DoctorService(doctorRepository, userRepository, facilityRepository, doctorMapper);
    }

    @Test
    void getAllDoctors_DoctorsExist_DoctorsReturned() {
        // given
        List<Doctor> doctors = List.of(
                new Doctor("jan", "kowalski", "kardiolog", new User("jan@test.pl", "pass")),
                new Doctor("anna", "nowak", "dentysta", new User("anna@test.pl", "pass")));
        Pageable pageable = PageRequest.of(0, 10);
        when(doctorRepository.findAll(pageable)).thenReturn(new PageImpl<>(doctors, pageable, doctors.size()));

        // when
        PageDto<DoctorDto> result = doctorService.getAllDoctors(pageable);
        List<DoctorDto> doctorDtos = result.content();

        // then
        assertAll(
                () -> assertEquals(2, result.totalElements()),
                () -> assertEquals(2, doctorDtos.size()),
                () -> assertEquals("jan", doctorDtos.get(0).firstName()),
                () -> assertEquals("kowalski", doctorDtos.get(0).lastName()),
                () -> assertEquals("kardiolog", doctorDtos.get(0).speciality()),
                () -> assertEquals("jan@test.pl", doctorDtos.get(0).user().email()),
                () -> assertEquals("anna", doctorDtos.get(1).firstName()),
                () -> assertEquals("anna@test.pl", doctorDtos.get(1).user().email())
        );
    }

    @Test
    void getAllDoctors_NoDoctors_EmptyListReturned() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(doctorRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        PageDto<DoctorDto> result = doctorService.getAllDoctors(pageable);
        List<DoctorDto> doctorDtos = result.content();

        // then
        assertTrue(doctorDtos.isEmpty());
    }

    @Test
    void getDoctorById_DoctorExists_DoctorReturned() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("jan@test.pl", "pass"));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // when
        DoctorDto doctorDto = doctorService.getDoctorById(1L);

        // then
        assertAll(
                () -> assertEquals("jan", doctorDto.firstName()),
                () -> assertEquals("kowalski", doctorDto.lastName()),
                () -> assertEquals("kardiolog", doctorDto.speciality()),
                () -> assertEquals("jan@test.pl", doctorDto.user().email())
        );
    }

    @Test
    void getDoctorById_DoctorNotExists_ThrowsDoctorNotFoundException() {
        // given
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class,
                () -> doctorService.getDoctorById(1L));

        // then
        assertEquals("Doctor with id 1 not found", exception.getMessage());
    }

    @Test
    void addDoctor_UserExists_DoctorSavedWithUser() {
        // given
        User user = new User("jan@test.pl", "pass");
        CreateDoctorDto createDoctorDto = new CreateDoctorDto("jan", "kowalski", "kardiolog", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(doctorRepository.existsByUserId(1L)).thenReturn(false);
        ArgumentCaptor<Doctor> doctorCaptor = ArgumentCaptor.forClass(Doctor.class);

        // when
        doctorService.addDoctor(createDoctorDto);

        // then
        verify(doctorRepository).save(doctorCaptor.capture());
        Doctor savedDoctor = doctorCaptor.getValue();
        assertAll(
                () -> assertEquals("jan", savedDoctor.getFirstName()),
                () -> assertEquals("kowalski", savedDoctor.getLastName()),
                () -> assertEquals("kardiolog", savedDoctor.getSpeciality()),
                () -> assertSame(user, savedDoctor.getUser())
        );
    }

    @Test
    void addDoctor_UserNotExists_ThrowsUserNotFoundException() {
        // given
        CreateDoctorDto createDoctorDto = new CreateDoctorDto("jan", "kowalski", "kardiolog", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> doctorService.addDoctor(createDoctorDto));

        // then
        assertEquals("User with id 1 not found", exception.getMessage());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void addDoctor_DoctorExists_ThrowsDoctorAlreadyExistsException() {
        // given
        CreateDoctorDto createDoctorDto = new CreateDoctorDto("jan", "kowalski", "kardiolog", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User("jan@test.pl", "pass")));
        when(doctorRepository.existsByUserId(1L)).thenReturn(true);

        // when
        DoctorAlreadyExistsException exception = assertThrows(DoctorAlreadyExistsException.class,
                () -> doctorService.addDoctor(createDoctorDto));

        // then
        assertEquals("User with id 1 is already a doctor", exception.getMessage());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void deleteDoctorById_DoctorExists_DoctorDeleted() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("jan@test.pl", "pass"));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // when
        doctorService.deleteDoctorById(1L);

        // then
        verify(doctorRepository).delete(doctor);
    }

    @Test
    void deleteDoctorById_DoctorNotExists_ThrowsDoctorNotFoundException() {
        // given
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class,
                () -> doctorService.deleteDoctorById(1L));

        // then
        assertEquals("Doctor with id 1 not found", exception.getMessage());
        verify(doctorRepository, never()).delete(any());
    }

    @Test
    void updateDoctorById_DoctorExists_DoctorUpdated() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("jan@test.pl", "pass"));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);

        // when
        DoctorDto doctorDto = doctorService.updateDoctorById(1L, new UpdateDoctorDto("adam", "nowak", "dentysta"));

        // then
        assertAll(
                () -> assertEquals("adam", doctorDto.firstName()),
                () -> assertEquals("nowak", doctorDto.lastName()),
                () -> assertEquals("dentysta", doctorDto.speciality()),
                () -> assertEquals("jan@test.pl", doctorDto.user().email())
        );
    }

    @Test
    void updateDoctorById_DoctorNotExists_ThrowsDoctorNotFoundException() {
        // given
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class,
                () -> doctorService.updateDoctorById(1L, new UpdateDoctorDto("adam", "nowak", "dentysta")));

        // then
        assertEquals("Doctor with id 1 not found", exception.getMessage());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void addFacility_DoctorAndFacilityExist_FacilityAdded() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("jan@test.pl", "pass"));
        Facility facility = new Facility("szpital nr 1", "warszawa", "00-001", "polna", "10");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(facilityRepository.findById(2L)).thenReturn(Optional.of(facility));

        // when
        doctorService.addFacility(1L, 2L);

        // then
        assertAll(
                () -> assertTrue(doctor.getFacilities().contains(facility)),
                () -> verify(doctorRepository).save(doctor)
        );
    }

    @Test
    void addFacility_DoctorNotExists_ThrowsDoctorNotFoundException() {
        // given
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class,
                () -> doctorService.addFacility(1L, 2L));

        // then
        assertEquals("Doctor with id 1 not found", exception.getMessage());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void addFacility_FacilityNotExists_ThrowsFacilityNotFoundException() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("jan@test.pl", "pass"));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(facilityRepository.findById(2L)).thenReturn(Optional.empty());

        // when
        FacilityNotFoundException exception = assertThrows(FacilityNotFoundException.class,
                () -> doctorService.addFacility(1L, 2L));

        // then
        assertEquals("Facility with id 2 not found", exception.getMessage());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void addFacility_DoctorAlreadyAssigned_ThrowsDoctorAlreadyAssignedException() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("jan@test.pl", "pass"));
        Facility facility = new Facility("szpital nr 1", "warszawa", "00-001", "polna", "10");
        doctor.addFacility(facility);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(facilityRepository.findById(2L)).thenReturn(Optional.of(facility));

        // when
        DoctorAlreadyAssignedException exception = assertThrows(DoctorAlreadyAssignedException.class,
                () -> doctorService.addFacility(1L, 2L));

        // then
        assertEquals("Doctor with id 1 already assigned to facility with id 2", exception.getMessage());
        verify(doctorRepository, never()).save(any());
    }
}
