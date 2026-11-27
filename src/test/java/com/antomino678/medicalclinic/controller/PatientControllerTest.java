package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.CreatePatientDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.dto.PatientDto;
import com.antomino678.medicalclinic.dto.UpdatePatientDto;
import com.antomino678.medicalclinic.exception.PatientAlreadyExistsException;
import com.antomino678.medicalclinic.exception.PatientNotFoundException;
import com.antomino678.medicalclinic.exception.UserNotFoundException;
import com.antomino678.medicalclinic.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientControllerTest {

    @MockitoBean
    PatientService patientService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getPatients_PatientsExist_PatientsReturned() throws Exception {
        // given
        List<PatientDto> patients = List.of(
                new PatientDto(
                        "test@test.pl",
                        "123123",
                        "jan",
                        "kowalski",
                        "500500500",
                        LocalDate.of(2005, 1, 1)
                ),
                new PatientDto(
                        "ziomek@test.pl",
                        "321123",
                        "emil",
                        "kowalski",
                        "300500500",
                        LocalDate.of(2001, 1, 1)
                )
        );
        when(patientService.getAllPatients(any(Pageable.class)))
                .thenReturn(new PageDto<>(patients, 0, 2, 2, 1));

        // when and then
        mockMvc.perform(get("/patients").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].email").value("test@test.pl"))
                .andExpect(jsonPath("$.content[0].idCardNo").value("123123"))
                .andExpect(jsonPath("$.content[0].firstName").value("jan"))
                .andExpect(jsonPath("$.content[0].lastName").value("kowalski"))
                .andExpect(jsonPath("$.content[0].phoneNumber").value("500500500"))
                .andExpect(jsonPath("$.content[0].birthday").value("2005-01-01"))
                .andExpect(jsonPath("$.content[1].email").value("ziomek@test.pl"))
                .andExpect(jsonPath("$.content[1].idCardNo").value("321123"))
                .andExpect(jsonPath("$.content[1].firstName").value("emil"))
                .andExpect(jsonPath("$.content[1].lastName").value("kowalski"))
                .andExpect(jsonPath("$.content[1].phoneNumber").value("300500500"))
                .andExpect(jsonPath("$.content[1].birthday").value("2001-01-01"));
    }

    @Test
    void getPatients_NoPatients_EmptyListReturned() throws Exception {
        // given
        when(patientService.getAllPatients(any(Pageable.class)))
                .thenReturn(new PageDto<>(List.of(), 0, 10, 0, 0));

        // when and then
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getPatient_PatientExists_PatientReturned() throws Exception {
        // given
        PatientDto patientDto = new PatientDto(
                "test@test.pl",
                "ASD123",
                "Jan",
                "Kowalski",
                "500100900",
                LocalDate.of(2001, 1, 1)
        );
        when(patientService.getPatientByEmail("test@test.pl")).thenReturn(patientDto);

        // when and then
        mockMvc.perform(get("/patients/test@test.pl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.pl"))
                .andExpect(jsonPath("$.idCardNo").value("ASD123"))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.phoneNumber").value("500100900"))
                .andExpect(jsonPath("$.birthday").value("2001-01-01"));
    }

    @Test
    void getPatient_PatientNotExists_ThrowsPatientNotFoundException() throws Exception {
        // given
        when(patientService.getPatientByEmail("test@test.pl")).thenThrow(new PatientNotFoundException("test@test.pl"));

        // when and then
        mockMvc.perform(get("/patients/test@test.pl"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPatient_PatientNotExists_PatientAdded() throws Exception {
        // given
        CreatePatientDto createPatientDto = new CreatePatientDto(
                1L,
                "ASD123",
                "Jan",
                "Kowalski",
                "500100900",
                LocalDate.of(2001, 1, 1)
        );
        PatientDto patientDto = new PatientDto(
                "test@test.pl",
                "ASD123",
                "Jan",
                "Kowalski",
                "500100900",
                LocalDate.of(2001, 1, 1)
        );
        when(patientService.addPatient(createPatientDto)).thenReturn(patientDto);

        // when and then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPatientDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.pl"))
                .andExpect(jsonPath("$.idCardNo").value("ASD123"))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.phoneNumber").value("500100900"))
                .andExpect(jsonPath("$.birthday").value("2001-01-01"));
    }

    @Test
    void createPatient_PatientExists_ThrowsPatientAlreadyExistsException() throws Exception {
        // given
        CreatePatientDto createPatientDto = new CreatePatientDto(
                1L,
                "ASD123",
                "Jan",
                "Kowalski",
                "500100900",
                LocalDate.of(2001, 1, 1)
        );
        when(patientService.addPatient(createPatientDto)).thenThrow(new PatientAlreadyExistsException(1L));

        // when and then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPatientDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createPatient_UserNotExists_ThrowsUserNotFoundException() throws Exception {
        // given
        CreatePatientDto createPatientDto = new CreatePatientDto(
                1L,
                "ASD123",
                "Jan",
                "Kowalski",
                "500100900",
                LocalDate.of(2001, 1, 1)
        );
        when(patientService.addPatient(createPatientDto)).thenThrow(new UserNotFoundException(1L));

        // when and then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPatientDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id 1 not found"));
    }

    @Test
    void deletePatient_PatientExists_PatientDeleted() throws Exception {
        // when and then
        mockMvc.perform(delete("/patients/test@test.pl"))
                .andExpect(status().isNoContent());

        verify(patientService).deletePatientByEmail("test@test.pl");
    }

    @Test
    void deletePatient_PatientNotExists_ThrowsPatientNotFoundException() throws Exception {
        // given
        doThrow(new PatientNotFoundException("test@test.pl"))
                .when(patientService).deletePatientByEmail("test@test.pl");

        // when and then
        mockMvc.perform(delete("/patients/test@test.pl"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePatient_PatientExists_PatientUpdated() throws Exception {
        // given
        UpdatePatientDto updatePatientDto = new UpdatePatientDto(
                "jan",
                "ziomalski",
                "500200300",
                LocalDate.of(2003, 1, 1)
        );
        PatientDto patientDto = new PatientDto(
                "test@test.pl",
                "ASD123",
                "jan",
                "ziomalski",
                "500200300",
                LocalDate.of(2003, 1, 1)
        );
        when(patientService.updatePatientByEmail("test@test.pl", updatePatientDto)).thenReturn(patientDto);

        // when and then
        mockMvc.perform(put("/patients/test@test.pl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePatientDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.pl"))
                .andExpect(jsonPath("$.idCardNo").value("ASD123"))
                .andExpect(jsonPath("$.firstName").value("jan"))
                .andExpect(jsonPath("$.lastName").value("ziomalski"))
                .andExpect(jsonPath("$.phoneNumber").value("500200300"))
                .andExpect(jsonPath("$.birthday").value("2003-01-01"));
    }

    @Test
    void updatePatient_PatientNotExists_ThrowsPatientNotFoundException() throws Exception {
        // given
        UpdatePatientDto updatePatientDto = new UpdatePatientDto(
                "jan",
                "ziomalski",
                "500200300",
                LocalDate.of(2003, 1, 1)
        );
        when(patientService.updatePatientByEmail("test@test.pl", updatePatientDto))
                .thenThrow(new PatientNotFoundException("test@test.pl"));

        // when and then
        mockMvc.perform(put("/patients/test@test.pl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePatientDto)))
                .andExpect(status().isNotFound());
    }
}
