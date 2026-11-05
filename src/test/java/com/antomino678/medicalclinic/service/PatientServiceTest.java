package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.CreatePatientDto;
import com.antomino678.medicalclinic.dto.PatientDto;
import com.antomino678.medicalclinic.dto.UpdatePatientDto;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        List<Patient> patients = List.of(new Patient(
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
        List<PatientDto> expected = List.of(
                new PatientDto("email@test.pl", "123123", "jan", "kowalski",
                        "502502502", LocalDate.of(2000, 1, 25)),
                new PatientDto("test@test.pl", "333333", "krzysztof", "kowalski",
                        "900502502", LocalDate.of(2005, 2, 15))
        );
        assertEquals(expected, patientDtos);
    }

    @Test
    void getAllPatients_NoPatients_EmptyListReturned() {
        // given
        when(patientRepository.findAll()).thenReturn(List.of());

        // when
        List<PatientDto> patientDtos = patientService.getAllPatients();

        // then
        List<PatientDto> expected = List.of();
        assertEquals(expected, patientDtos);
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
        when(patientRepository.findByEmail(("test@test.pl"))).thenReturn(patient);

        // when
        PatientDto patientDto = patientService.getPatientByEmail("test@test.pl");

        // then
        PatientDto expected = new PatientDto(
                "test@test.pl",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));

        assertEquals(expected, patientDto);
    }

    @Test
    void getPatientByEmail_PatientNotFound_PatientNotFoundExceptionThrown() {
        // given
        when(patientRepository.findByEmail(("test@test.pl"))).thenReturn(Optional.empty());

        // then
        assertThrows(PatientNotFoundException.class,
                () -> patientService.getPatientByEmail("test@test.pl"));
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
        PatientDto expected = new PatientDto(
                "test@test.pl",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));

        assertEquals(expected, patientDto);
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
        PatientDto expected = new PatientDto(
                "test@test.pl",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        assertEquals(expected, patientDto);
    }

    @Test
    void addPatient_EmailWithUpperCaseAndSpaces_PatientAddedWithNormalizedEmail() {
        // given
        CreatePatientDto createPatientDto = new CreatePatientDto(
                "  TEST@TEST.PL",
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
        PatientDto expected = new PatientDto(
                "test@test.pl",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15));
        assertEquals(expected, patientDto);
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
        PatientDto expected = new PatientDto(
                "test@test.pl",
                "333333",
                "jan",
                "nowak",
                "111222333",
                LocalDate.of(1999, 5, 5));
        assertEquals(expected, patientDto);
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
        PatientDto expected = new PatientDto(
                "test@test.pl",
                "333333",
                "jan",
                "nowak",
                "111222333",
                LocalDate.of(1999, 5, 5));
        assertEquals(expected, patientDto);
    }
}

