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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PatientServiceTest {
    private PatientService patientService;
    private PatientRepository patientRepository;
    private UserRepository userRepository;
    private PatientMapper patientMapper;

    @BeforeEach
    void setUp() {
        this.patientRepository = Mockito.mock(PatientRepository.class);
        this.userRepository = Mockito.mock(UserRepository.class);
        this.patientMapper = Mappers.getMapper(PatientMapper.class);
        this.patientService = new PatientService(patientRepository, userRepository, patientMapper);
    }

    @Test
    void getAllPatients_PatientsExist_PatientsReturned() {
        // given
        List<Patient> patients = List.of(new Patient(
                        "123123",
                        "jan",
                        "kowalski",
                        "502502502",
                        LocalDate.of(2000, 1, 25),
                        new User("email@test.pl", "pass123")),
                new Patient(
                        "333333",
                        "krzysztof",
                        "kowalski",
                        "900502502",
                        LocalDate.of(2005, 2, 15),
                        new User("test@test.pl", "pass")));
        Pageable pageable = PageRequest.of(0, 2);
        when(patientRepository.findAll(pageable)).thenReturn(new PageImpl<>(patients, pageable, 5));

        // when
        PageDto<PatientDto> result = patientService.getAllPatients(pageable);
        List<PatientDto> patientDtos = result.content();

        // then
        assertAll(
                () -> assertEquals(0, result.page()),
                () -> assertEquals(2, result.size()),
                () -> assertEquals(5, result.totalElements()),
                () -> assertEquals(3, result.totalPages()),
                () -> assertEquals(2, patientDtos.size()),
                () -> assertEquals("email@test.pl", patientDtos.get(0).email()),
                () -> assertEquals("123123", patientDtos.get(0).idCardNo()),
                () -> assertEquals("jan", patientDtos.get(0).firstName()),
                () -> assertEquals("kowalski", patientDtos.get(0).lastName()),
                () -> assertEquals("502502502", patientDtos.get(0).phoneNumber()),
                () -> assertEquals(LocalDate.of(2000, 1, 25), patientDtos.get(0).birthday()),
                () -> assertEquals("test@test.pl", patientDtos.get(1).email()),
                () -> assertEquals("333333", patientDtos.get(1).idCardNo()),
                () -> assertEquals("krzysztof", patientDtos.get(1).firstName()),
                () -> assertEquals("kowalski", patientDtos.get(1).lastName()),
                () -> assertEquals("900502502", patientDtos.get(1).phoneNumber()),
                () -> assertEquals(LocalDate.of(2005, 2, 15), patientDtos.get(1).birthday())
        );
    }

    @Test
    void getAllPatients_NoPatients_EmptyListReturned() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(patientRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        PageDto<PatientDto> result = patientService.getAllPatients(pageable);

        // then
        assertAll(
                () -> assertTrue(result.content().isEmpty()),
                () -> assertEquals(0, result.totalElements())
        );
    }

    @Test
    void getPatientByEmail_PatientExists_PatientReturned() {
        // given
        Optional<Patient> patient = Optional.of(
                new Patient(
                        "333333",
                        "krzysztof",
                        "kowalski",
                        "900502502",
                        LocalDate.of(2005, 2, 15),
                        new User("test@test.pl", "pass123")));
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(patient);

        // when
        PatientDto patientDto = patientService.getPatientByEmail("test@test.pl");

        // then
        assertAll(
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("333333", patientDto.idCardNo()),
                () -> assertEquals("krzysztof", patientDto.firstName()),
                () -> assertEquals("kowalski", patientDto.lastName()),
                () -> assertEquals("900502502", patientDto.phoneNumber()),
                () -> assertEquals(LocalDate.of(2005, 2, 15), patientDto.birthday())
        );
    }

    @Test
    void getPatientByEmail_PatientNotExists_ThrowsPatientNotFoundException() {
        // given
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.getPatientByEmail("test@test.pl"));

        // then
        assertEquals("Patient with email test@test.pl not found", exception.getMessage());
    }

    @Test
    void getPatientByEmail_EmailWithUpperCaseAndSpaces_EmailNormalizedAndPatientReturned() {
        // given
        Optional<Patient> patient = Optional.of(
                new Patient(
                        "333333",
                        "krzysztof",
                        "kowalski",
                        "900502502",
                        LocalDate.of(2005, 2, 15),
                        new User("test@test.pl", "pass123")));
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(patient);

        // when
        PatientDto patientDto = patientService.getPatientByEmail("   TEST@TEST.PL");

        // then
        assertAll(
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("333333", patientDto.idCardNo()),
                () -> assertEquals("krzysztof", patientDto.firstName()),
                () -> assertEquals("kowalski", patientDto.lastName()),
                () -> assertEquals("900502502", patientDto.phoneNumber()),
                () -> assertEquals(LocalDate.of(2005, 2, 15), patientDto.birthday())
        );
    }

    @Test
    void addPatient_UserExists_PatientAdded() {
        // given
        User user = new User("test@test.pl", "pass123");
        CreatePatientDto createPatientDto = new CreatePatientDto(
                1L,
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        Patient savedPatient = new Patient(
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15),
                user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(patientRepository.existsByUserId(1L)).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // when
        PatientDto patientDto = patientService.addPatient(createPatientDto);

        // then
        assertAll(
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("333333", patientDto.idCardNo()),
                () -> assertEquals("krzysztof", patientDto.firstName()),
                () -> assertEquals("kowalski", patientDto.lastName()),
                () -> assertEquals("900502502", patientDto.phoneNumber()),
                () -> assertEquals(LocalDate.of(2005, 2, 15), patientDto.birthday())
        );
    }

    @Test
    void addPatient_UserExists_PatientSavedWithUser() {
        // given
        User user = new User("test@test.pl", "pass123");
        CreatePatientDto createPatientDto = new CreatePatientDto(
                1L,
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(patientRepository.existsByUserId(1L)).thenReturn(false);
        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);

        // when
        patientService.addPatient(createPatientDto);

        // then
        verify(patientRepository).save(patientCaptor.capture());
        Patient savedPatient = patientCaptor.getValue();
        assertAll(
                () -> assertSame(user, savedPatient.getUser()),
                () -> assertEquals("333333", savedPatient.getIdCardNo()),
                () -> assertEquals("krzysztof", savedPatient.getFirstName())
        );
    }

    @Test
    void addPatient_UserNotExists_ThrowsUserNotFoundException() {
        // given
        CreatePatientDto createPatientDto = new CreatePatientDto(
                1L,
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> patientService.addPatient(createPatientDto));

        // then
        assertEquals("User with id 1 not found", exception.getMessage());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void addPatient_PatientExists_ThrowsPatientAlreadyExistsException() {
        // given
        User user = new User("test@test.pl", "pass123");
        CreatePatientDto createPatientDto = new CreatePatientDto(
                1L,
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(patientRepository.existsByUserId(1L)).thenReturn(true);

        // when
        PatientAlreadyExistsException exception = assertThrows(PatientAlreadyExistsException.class,
                () -> patientService.addPatient(createPatientDto));

        // then
        assertEquals("Patient for user with id 1 already exists", exception.getMessage());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void updatePatientByEmail_PatientExists_PatientUpdated() {
        // given
        Patient patient = new Patient(
                "333333",
                "krzysztof",
                "nowak",
                "111222333",
                LocalDate.of(1999, 2, 15),
                new User("test@test.pl", "pass123"));
        UpdatePatientDto updatePatientDto = new UpdatePatientDto(
                "jan",
                "nowak",
                "111222333",
                LocalDate.of(1999, 5, 5));
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);

        // when
        PatientDto patientDto = patientService.updatePatientByEmail("test@test.pl", updatePatientDto);

        // then
        assertAll(
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("333333", patientDto.idCardNo()),
                () -> assertEquals("jan", patientDto.firstName()),
                () -> assertEquals("nowak", patientDto.lastName()),
                () -> assertEquals("111222333", patientDto.phoneNumber()),
                () -> assertEquals(LocalDate.of(1999, 5, 5), patientDto.birthday())
        );
    }

    @Test
    void updatePatientByEmail_EmailWithUpperCaseAndSpaces_PatientUpdated() {
        // given
        Patient patient = new Patient(
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15),
                new User("test@test.pl", "pass123"));
        UpdatePatientDto updatePatientDto = new UpdatePatientDto(
                "jan",
                "nowak",
                "111222333",
                LocalDate.of(1999, 5, 5));
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);

        // when
        PatientDto patientDto = patientService.updatePatientByEmail("  TEST@TEST.PL  ", updatePatientDto);

        // then
        assertAll(
                () -> assertEquals("test@test.pl", patientDto.email()),
                () -> assertEquals("333333", patientDto.idCardNo()),
                () -> assertEquals("jan", patientDto.firstName()),
                () -> assertEquals("nowak", patientDto.lastName()),
                () -> assertEquals("111222333", patientDto.phoneNumber()),
                () -> assertEquals(LocalDate.of(1999, 5, 5), patientDto.birthday())
        );
    }

    @Test
    void updatePatientByEmail_PatientExists_PasswordNotChanged() {
        // given
        Patient patient = new Patient(
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15),
                new User("test@test.pl", "pass123"));
        UpdatePatientDto updatePatientDto = new UpdatePatientDto(
                "jan",
                "nowak",
                "111222333",
                LocalDate.of(1999, 5, 5));
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);

        // when
        patientService.updatePatientByEmail("test@test.pl", updatePatientDto);

        // then
        assertEquals("pass123", patient.getUser().getPassword());
    }

    @Test
    void updatePatientByEmail_PatientNotExists_ThrowsPatientNotFoundException() {
        // given
        UpdatePatientDto updatePatientDto = new UpdatePatientDto(
                "jan",
                "nowak",
                "111222333",
                LocalDate.of(1999, 5, 5));
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.updatePatientByEmail("test@test.pl", updatePatientDto));

        // then
        assertEquals("Patient with email test@test.pl not found", exception.getMessage());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void deletePatientByEmail_PatientExists_PatientDeleted() {
        // given
        Patient patient = new Patient(
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15),
                new User("test@test.pl", "pass123"));
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(Optional.of(patient));

        // when
        patientService.deletePatientByEmail("test@test.pl");

        // then
        verify(patientRepository).deleteByUserEmail("test@test.pl");
    }

    @Test
    void deletePatientByEmail_EmailWithUpperCaseAndSpaces_PatientDeleted() {
        // given
        Patient patient = new Patient(
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15),
                new User("test@test.pl", "pass123"));
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(Optional.of(patient));

        // when
        patientService.deletePatientByEmail("  TEST@TEST.PL  ");

        // then
        verify(patientRepository).deleteByUserEmail("test@test.pl");
    }

    @Test
    void deletePatientByEmail_PatientNotExists_ThrowsPatientNotFoundException() {
        // given
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.deletePatientByEmail("test@test.pl"));

        // then
        assertEquals("Patient with email test@test.pl not found", exception.getMessage());
        verify(patientRepository, never()).deleteByUserEmail(any());
    }
}

