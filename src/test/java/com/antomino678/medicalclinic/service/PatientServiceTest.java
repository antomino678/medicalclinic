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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PatientServiceTest {
    private PatientService patientService;
    private PatientRepository patientRepository;
    private PatientMapper patientMapper;

    @BeforeEach
    void setUp() {
        this.patientRepository = Mockito.mock(PatientRepository.class);
        this.patientMapper = Mappers.getMapper(PatientMapper.class);
        this.patientService = new PatientService(patientRepository, patientMapper);
    }

    @Test
    void getAllPatients_PatientsExist_PatientsReturned() {
        // given
        List<Patient> patients = List.of(
                new Patient(
                        "email@test.pl",
                        "pass",
                        "123123",
                        "jan",
                        "kowalski",
                        "502502502",
                        LocalDate.of(2000, 1, 25)),
                new Patient(
                        "test@test.pl",
                        "pass123",
                        "333333",
                        "krzysztof",
                        "kowalski",
                        "900502502",
                        LocalDate.of(2005, 2, 15)));
        when(patientRepository.findAll()).thenReturn(patients);

        // when
        List<PatientDto> patientDtos = patientService.getAllPatients();

        // then
        assertAll(
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
        when(patientRepository.findAll()).thenReturn(List.of());

        // when
        List<PatientDto> patientDtos = patientService.getAllPatients();

        // then
        assertTrue(patientDtos.isEmpty());
    }

    @Test
    void getPatientByEmail_PatientExists_PatientReturned() {
        // given
        Optional<Patient> patient = Optional.of(
                new Patient(
                        "test@test.pl",
                        "pass123",
                        "333333",
                        "krzysztof",
                        "kowalski",
                        "900502502",
                        LocalDate.of(2005, 2, 15)));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(patient);

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
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.empty());

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
                        "test@test.pl",
                        "pass123",
                        "333333",
                        "krzysztof",
                        "kowalski",
                        "900502502",
                        LocalDate.of(2005, 2, 15)));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(patient);

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
    void addPatient_PatientNotExists_PatientAdded() {
        // given
        CreatePatientDto createPatientDto = new CreatePatientDto(
                "test@test.pl",
                "pass123",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        Patient savedPatient = new Patient(
                "test@test.pl",
                "pass123",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.empty());
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
    void addPatient_EmailWithUpperCaseAndSpaces_EmailNormalizedBeforeLookup() {
        // given
        CreatePatientDto createPatientDto = new CreatePatientDto(
                "  TEST@TEST.PL",
                "pass123",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.empty());

        // when
        patientService.addPatient(createPatientDto);

        // then
        verify(patientRepository).findByEmail("test@test.pl");
    }

    @Test
    void updatePatientByEmail_PatientExists_PatientUpdated() {
        // given
        Patient patient = new Patient(
                "test@test.pl",
                "pass123",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        UpdatePatientDto updatePatientDto = new UpdatePatientDto(
                "jan",
                "nowak",
                "111222333",
                LocalDate.of(1999, 5, 5));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.of(patient));
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
                "test@test.pl",
                "pass123",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        UpdatePatientDto updatePatientDto = new UpdatePatientDto(
                "jan",
                "nowak",
                "111222333",
                LocalDate.of(1999, 5, 5));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.of(patient));
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
                "test@test.pl",
                "pass123",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        UpdatePatientDto updatePatientDto = new UpdatePatientDto(
                "jan",
                "nowak",
                "111222333",
                LocalDate.of(1999, 5, 5));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);

        // when
        patientService.updatePatientByEmail("test@test.pl", updatePatientDto);

        // then
        assertEquals("pass123", patient.getPassword());
    }

    @Test
    void updatePatientByEmail_PatientNotExists_ThrowsPatientNotFoundException() {
        // given
        UpdatePatientDto updatePatientDto = new UpdatePatientDto(
                "jan",
                "nowak",
                "111222333",
                LocalDate.of(1999, 5, 5));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.empty());

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
                "test@test.pl",
                "pass123",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.of(patient));

        // when
        patientService.deletePatientByEmail("test@test.pl");

        // then
        verify(patientRepository).deleteByEmail("test@test.pl");
    }

    @Test
    void deletePatientByEmail_EmailWithUpperCaseAndSpaces_PatientDeleted() {
        // given
        Patient patient = new Patient(
                "test@test.pl",
                "pass123",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.of(patient));

        // when
        patientService.deletePatientByEmail("  TEST@TEST.PL  ");

        // then
        verify(patientRepository).deleteByEmail("test@test.pl");
    }

    @Test
    void deletePatientByEmail_PatientNotExists_ThrowsPatientNotFoundException() {
        // given
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.deletePatientByEmail("test@test.pl"));

        // then
        assertEquals("Patient with email test@test.pl not found", exception.getMessage());
        verify(patientRepository, never()).deleteByEmail(any());
    }

    @Test
    void changePassword_PatientExists_PasswordChanged() {
        // given
        Patient patient = new Patient(
                "test@test.pl",
                "pass123",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.of(patient));

        // when
        patientService.changePassword("test@test.pl", new ChangePasswordDto("siemanko"));

        // then
        assertAll(
                () -> verify(patientRepository).save(patient),
                () -> assertEquals("siemanko", patient.getPassword())
        );
    }

    @Test
    void changePassword_EmailWithUpperCaseAndSpaces_PasswordChanged() {
        // given
        Patient patient = new Patient(
                "test@test.pl",
                "pass123",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.of(patient));

        // when
        patientService.changePassword("  TEST@TEST.PL  ", new ChangePasswordDto("siemanko"));

        // then
        assertAll(
                () -> verify(patientRepository).save(patient),
                () -> assertEquals("siemanko", patient.getPassword())
        );
    }

    @Test
    void changePassword_PatientNotExists_ThrowsPatientNotFoundException() {
        // given
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.changePassword("test@test.pl", new ChangePasswordDto("siemanko")));

        // then
        assertEquals("Patient with email test@test.pl not found", exception.getMessage());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void addPatient_PatientExists_ThrowsPatientAlreadyExistsException() {
        // given
        Patient patient = new Patient(
                "test@test.pl",
                "pass123",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        when(patientRepository.findByEmail("test@test.pl")).thenReturn(Optional.of(patient));

        // when
        PatientAlreadyExistsException exception = assertThrows(PatientAlreadyExistsException.class,
                () -> patientService.addPatient(new CreatePatientDto(
                        "test@test.pl",
                        "pass123",
                        "333333",
                        "krzysztof",
                        "kowalski",
                        "900502502",
                        LocalDate.of(2005, 2, 15)))
        );

        // then
        assertEquals("Patient with email test@test.pl already exists", exception.getMessage());
        verify(patientRepository, never()).save(any());
    }
}

