package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.BookVisitDto;
import com.antomino678.medicalclinic.dto.CreateVisitDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.dto.VisitDto;
import com.antomino678.medicalclinic.exception.DoctorNotFoundException;
import com.antomino678.medicalclinic.exception.InvalidVisitTimeException;
import com.antomino678.medicalclinic.exception.PatientNotFoundException;
import com.antomino678.medicalclinic.exception.VisitAlreadyBookedException;
import com.antomino678.medicalclinic.exception.VisitNotFoundException;
import com.antomino678.medicalclinic.exception.VisitOverlapsException;
import com.antomino678.medicalclinic.mapper.VisitMapper;
import com.antomino678.medicalclinic.model.Doctor;
import com.antomino678.medicalclinic.model.Patient;
import com.antomino678.medicalclinic.model.User;
import com.antomino678.medicalclinic.model.Visit;
import com.antomino678.medicalclinic.repository.DoctorRepository;
import com.antomino678.medicalclinic.repository.PatientRepository;
import com.antomino678.medicalclinic.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class VisitServiceTest {
    private VisitService visitService;
    private VisitRepository visitRepository;
    private DoctorRepository doctorRepository;
    private PatientRepository patientRepository;
    private VisitMapper visitMapper;

    private static final LocalDateTime START = LocalDateTime.of(2099, 1, 10, 10, 0);
    private static final LocalDateTime END = LocalDateTime.of(2099, 1, 10, 10, 30);

    @BeforeEach
    void setUp() {
        this.visitRepository = Mockito.mock(VisitRepository.class);
        this.doctorRepository = Mockito.mock(DoctorRepository.class);
        this.patientRepository = Mockito.mock(PatientRepository.class);
        this.visitMapper = Mockito.mock(VisitMapper.class);
        this.visitService = new VisitService(visitRepository, doctorRepository, patientRepository, visitMapper);
    }

    @Test
    void getAllVisits_VisitsExist_VisitsReturned() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("doctor@test.pl", "pass"));
        Visit firstVisit = new Visit(START, END, doctor);
        Visit secondVisit = new Visit(START.plusHours(1), END.plusHours(1), doctor);
        VisitDto firstVisitDto = new VisitDto(START, END, null, null, 1L);
        VisitDto secondVisitDto = new VisitDto(START.plusHours(1), END.plusHours(1), null, null, 2L);
        Pageable pageable = PageRequest.of(0, 10);
        when(visitRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(firstVisit, secondVisit), pageable, 2));
        when(visitMapper.toDto(firstVisit)).thenReturn(firstVisitDto);
        when(visitMapper.toDto(secondVisit)).thenReturn(secondVisitDto);

        // when
        PageDto<VisitDto> result = visitService.getAllVisits(pageable);
        List<VisitDto> visitDtos = result.content();

        // then
        assertEquals(List.of(firstVisitDto, secondVisitDto), visitDtos);
    }

    @Test
    void getAllVisits_NoVisits_EmptyListReturned() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(visitRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        PageDto<VisitDto> result = visitService.getAllVisits(pageable);
        List<VisitDto> visitDtos = result.content();

        // then
        assertTrue(visitDtos.isEmpty());
    }

    @Test
    void findById_VisitExists_VisitReturned() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("doctor@test.pl", "pass"));
        Visit visit = new Visit(START, END, doctor);
        VisitDto visitDto = new VisitDto(START, END, null, null, 1L);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(visitMapper.toDto(visit)).thenReturn(visitDto);

        // when
        VisitDto result = visitService.findById(1L);

        // then
        assertEquals(visitDto, result);
    }

    @Test
    void findById_VisitNotExists_ThrowsVisitNotFoundException() {
        // given
        when(visitRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        VisitNotFoundException exception = assertThrows(VisitNotFoundException.class,
                () -> visitService.findById(1L));

        // then
        assertEquals("Visit with id 1 not found", exception.getMessage());
    }

    @Test
    void addVisit_ValidData_VisitSaved() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("doctor@test.pl", "pass"));
        CreateVisitDto createVisitDto = new CreateVisitDto(START, END, 1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        ArgumentCaptor<Visit> visitCaptor = ArgumentCaptor.forClass(Visit.class);

        // when
        visitService.addVisit(createVisitDto);

        // then
        verify(visitRepository).save(visitCaptor.capture());
        Visit savedVisit = visitCaptor.getValue();
        assertAll(
                () -> assertEquals(START, savedVisit.getStartTime()),
                () -> assertEquals(END, savedVisit.getEndTime()),
                () -> assertSame(doctor, savedVisit.getDoctor()),
                () -> assertNull(savedVisit.getPatient())
        );
    }

    @Test
    void addVisit_DoctorNotExists_ThrowsDoctorNotFoundException() {
        // given
        CreateVisitDto createVisitDto = new CreateVisitDto(START, END, 1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class,
                () -> visitService.addVisit(createVisitDto));

        // then
        assertEquals("Doctor with id 1 not found", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void addVisit_VisitOverlaps_ThrowsVisitOverlapsException() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("doctor@test.pl", "pass"));
        CreateVisitDto createVisitDto = new CreateVisitDto(START, END, 1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(visitRepository.existsByDoctorIdAndStartTimeLessThanAndEndTimeGreaterThan(doctor.getId(), END, START))
                .thenReturn(true);

        // when
        assertThrows(VisitOverlapsException.class,
                () -> visitService.addVisit(createVisitDto));

        // then
        verify(visitRepository, never()).save(any());
    }

    @Test
    void addVisit_StartAfterEnd_ThrowsInvalidVisitTimeException() {
        // given
        CreateVisitDto createVisitDto = new CreateVisitDto(END, START, 1L);

        // when
        InvalidVisitTimeException exception = assertThrows(InvalidVisitTimeException.class,
                () -> visitService.addVisit(createVisitDto));

        // then
        assertEquals("Visit start must be before end", exception.getMessage());
        verify(doctorRepository, never()).findById(any());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void registerPatient_VisitFree_PatientAssigned() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("doctor@test.pl", "pass"));
        Patient patient = new Patient(
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15),
                new User("test@test.pl", "pass123"));
        Visit visit = new Visit(START, END, doctor);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(Optional.of(patient));

        // when
        visitService.registerPatient(1L, new BookVisitDto("test@test.pl"));

        // then
        assertAll(
                () -> assertSame(patient, visit.getPatient()),
                () -> verify(visitRepository).save(visit)
        );
    }

    @Test
    void registerPatient_EmailWithUpperCaseAndSpaces_PatientAssigned() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("doctor@test.pl", "pass"));
        Patient patient = new Patient(
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15),
                new User("test@test.pl", "pass123"));
        Visit visit = new Visit(START, END, doctor);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(Optional.of(patient));

        // when
        visitService.registerPatient(1L, new BookVisitDto("  TEST@TEST.PL  "));

        // then
        assertSame(patient, visit.getPatient());
    }

    @Test
    void registerPatient_VisitNotExists_ThrowsVisitNotFoundException() {
        // given
        when(visitRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        VisitNotFoundException exception = assertThrows(VisitNotFoundException.class,
                () -> visitService.registerPatient(1L, new BookVisitDto("test@test.pl")));

        // then
        assertEquals("Visit with id 1 not found", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void registerPatient_VisitAlreadyBooked_ThrowsVisitAlreadyBookedException() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("doctor@test.pl", "pass"));
        Patient patient = new Patient(
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15),
                new User("test@test.pl", "pass123"));
        Visit visit = new Visit(START, END, doctor);
        visit.assignPatient(patient);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));

        // when
        VisitAlreadyBookedException exception = assertThrows(VisitAlreadyBookedException.class,
                () -> visitService.registerPatient(1L, new BookVisitDto("other@test.pl")));

        // then
        assertEquals("Visit with id 1 is already booked", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void registerPatient_VisitInPast_ThrowsInvalidVisitTimeException() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("doctor@test.pl", "pass"));
        Visit visit = new Visit(
                LocalDateTime.of(2020, 1, 10, 10, 0),
                LocalDateTime.of(2020, 1, 10, 10, 30),
                doctor);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));

        // when
        InvalidVisitTimeException exception = assertThrows(InvalidVisitTimeException.class,
                () -> visitService.registerPatient(1L, new BookVisitDto("test@test.pl")));

        // then
        assertEquals("Visit must not be in the past", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void registerPatient_PatientNotExists_ThrowsPatientNotFoundException() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("doctor@test.pl", "pass"));
        Visit visit = new Visit(START, END, doctor);
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(patientRepository.findByUserEmail("test@test.pl")).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> visitService.registerPatient(1L, new BookVisitDto("test@test.pl")));

        // then
        assertEquals("Patient with email test@test.pl not found", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void getPatientVisits_PatientExists_VisitsReturned() {
        // given
        Doctor doctor = new Doctor("jan", "kowalski", "kardiolog", new User("doctor@test.pl", "pass"));
        Visit visit = new Visit(START, END, doctor);
        VisitDto visitDto = new VisitDto(START, END, null, null, 1L);
        Pageable pageable = PageRequest.of(0, 10);
        when(patientRepository.existsByUserEmail("test@test.pl")).thenReturn(true);
        when(visitRepository.findByPatientUserEmail("test@test.pl", pageable))
                .thenReturn(new PageImpl<>(List.of(visit), pageable, 1));
        when(visitMapper.toDto(visit)).thenReturn(visitDto);

        // when
        PageDto<VisitDto> result = visitService.getPatientVisits("  TEST@TEST.PL  ", pageable);
        List<VisitDto> visitDtos = result.content();

        // then
        assertEquals(List.of(visitDto), visitDtos);
    }

    @Test
    void getPatientVisits_PatientNotExists_ThrowsPatientNotFoundException() {
        // given
        when(patientRepository.existsByUserEmail("test@test.pl")).thenReturn(false);

        // when
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> visitService.getPatientVisits("test@test.pl", PageRequest.of(0, 10)));

        // then
        assertEquals("Patient with email test@test.pl not found", exception.getMessage());
        verify(visitRepository, never()).findByPatientUserEmail(any(), any());
    }
}
