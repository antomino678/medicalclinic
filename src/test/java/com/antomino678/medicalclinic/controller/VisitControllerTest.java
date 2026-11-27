package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.BookVisitDto;
import com.antomino678.medicalclinic.dto.CreateVisitDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.dto.PatientDto;
import com.antomino678.medicalclinic.dto.VisitDto;
import com.antomino678.medicalclinic.exception.DoctorNotFoundException;
import com.antomino678.medicalclinic.exception.InvalidVisitTimeException;
import com.antomino678.medicalclinic.exception.PatientNotFoundException;
import com.antomino678.medicalclinic.exception.VisitAlreadyBookedException;
import com.antomino678.medicalclinic.exception.VisitNotFoundException;
import com.antomino678.medicalclinic.exception.VisitOverlapsException;
import com.antomino678.medicalclinic.service.VisitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class VisitControllerTest {

    @MockitoBean
    VisitService visitService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private static final LocalDateTime START = LocalDateTime.of(2099, 1, 10, 10, 0);
    private static final LocalDateTime END = LocalDateTime.of(2099, 1, 10, 10, 30);

    @Test
    void getVisits_VisitsExist_VisitsReturned() throws Exception {
        // given
        List<VisitDto> visits = List.of(
                new VisitDto(START, END, null, null, 1L),
                new VisitDto(START.plusHours(1), END.plusHours(1), null, null, 2L)
        );
        when(visitService.getAllVisits(any(Pageable.class))).thenReturn(new PageDto<>(visits, 0, 10, 2, 1));

        // when and then
        mockMvc.perform(get("/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].startTime").value("2099-01-10T10:00:00"))
                .andExpect(jsonPath("$.content[0].endTime").value("2099-01-10T10:30:00"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].startTime").value("2099-01-10T11:00:00"))
                .andExpect(jsonPath("$.content[1].endTime").value("2099-01-10T11:30:00"));
    }

    @Test
    void getVisit_VisitExists_VisitReturned() throws Exception {
        // given
        VisitDto visitDto = new VisitDto(START, END, null, null, 1L);
        when(visitService.findById(1L)).thenReturn(visitDto);

        // when and then
        mockMvc.perform(get("/visits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startTime").value("2099-01-10T10:00:00"))
                .andExpect(jsonPath("$.endTime").value("2099-01-10T10:30:00"));
    }

    @Test
    void getVisit_VisitNotExists_ThrowsVisitNotFoundException() throws Exception {
        // given
        when(visitService.findById(1L)).thenThrow(new VisitNotFoundException(1L));

        // when and then
        mockMvc.perform(get("/visits/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Visit with id 1 not found"));
    }

    @Test
    void getPatientVisits_PatientExists_VisitsReturned() throws Exception {
        // given
        when(visitService.getPatientVisits(eq("test@test.pl"), any(Pageable.class)))
                .thenReturn(new PageDto<>(List.of(new VisitDto(START, END, null, null, 1L)), 0, 10, 1, 1));

        // when and then
        mockMvc.perform(get("/visits/patient/test@test.pl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void getPatientVisits_PatientNotExists_ThrowsPatientNotFoundException() throws Exception {
        // given
        when(visitService.getPatientVisits(eq("test@test.pl"), any(Pageable.class)))
                .thenThrow(new PatientNotFoundException("test@test.pl"));

        // when and then
        mockMvc.perform(get("/visits/patient/test@test.pl"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createVisit_ValidData_VisitCreated() throws Exception {
        // given
        CreateVisitDto createVisitDto = new CreateVisitDto(START, END, 1L);
        when(visitService.addVisit(createVisitDto)).thenReturn(new VisitDto(START, END, null, null, 1L));

        // when and then
        mockMvc.perform(post("/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVisitDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startTime").value("2099-01-10T10:00:00"))
                .andExpect(jsonPath("$.endTime").value("2099-01-10T10:30:00"));
    }

    @Test
    void createVisit_InvalidTime_ThrowsInvalidVisitTimeException() throws Exception {
        // given
        CreateVisitDto createVisitDto = new CreateVisitDto(END, START, 1L);
        when(visitService.addVisit(createVisitDto))
                .thenThrow(new InvalidVisitTimeException("Visit start must be before end"));

        // when and then
        mockMvc.perform(post("/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVisitDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Visit start must be before end"));
    }

    @Test
    void createVisit_DoctorNotExists_ThrowsDoctorNotFoundException() throws Exception {
        // given
        CreateVisitDto createVisitDto = new CreateVisitDto(START, END, 1L);
        when(visitService.addVisit(createVisitDto)).thenThrow(new DoctorNotFoundException(1L));

        // when and then
        mockMvc.perform(post("/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVisitDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createVisit_VisitOverlaps_ThrowsVisitOverlapsException() throws Exception {
        // given
        CreateVisitDto createVisitDto = new CreateVisitDto(START, END, 1L);
        when(visitService.addVisit(createVisitDto)).thenThrow(new VisitOverlapsException(1L, START, END));

        // when and then
        mockMvc.perform(post("/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVisitDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void bookVisit_VisitFree_PatientAssigned() throws Exception {
        // given
        BookVisitDto bookVisitDto = new BookVisitDto("test@test.pl");
        PatientDto patientDto = new PatientDto(
                "test@test.pl",
                "333333",
                "krzysztof",
                "kowalski",
                "900502502",
                LocalDate.of(2005, 2, 15)
        );
        when(visitService.registerPatient(1L, bookVisitDto))
                .thenReturn(new VisitDto(START, END, null, patientDto, 1L));

        // when and then
        mockMvc.perform(patch("/visits/1/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookVisitDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patient.email").value("test@test.pl"))
                .andExpect(jsonPath("$.patient.firstName").value("krzysztof"));
    }

    @Test
    void bookVisit_VisitNotExists_ThrowsVisitNotFoundException() throws Exception {
        // given
        BookVisitDto bookVisitDto = new BookVisitDto("test@test.pl");
        when(visitService.registerPatient(1L, bookVisitDto)).thenThrow(new VisitNotFoundException(1L));

        // when and then
        mockMvc.perform(patch("/visits/1/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookVisitDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void bookVisit_VisitAlreadyBooked_ThrowsVisitAlreadyBookedException() throws Exception {
        // given
        BookVisitDto bookVisitDto = new BookVisitDto("test@test.pl");
        when(visitService.registerPatient(1L, bookVisitDto)).thenThrow(new VisitAlreadyBookedException(1L));

        // when and then
        mockMvc.perform(patch("/visits/1/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookVisitDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Visit with id 1 is already booked"));
    }

    @Test
    void bookVisit_PatientNotExists_ThrowsPatientNotFoundException() throws Exception {
        // given
        BookVisitDto bookVisitDto = new BookVisitDto("test@test.pl");
        when(visitService.registerPatient(1L, bookVisitDto))
                .thenThrow(new PatientNotFoundException("test@test.pl"));

        // when and then
        mockMvc.perform(patch("/visits/1/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookVisitDto)))
                .andExpect(status().isNotFound());
    }
}
