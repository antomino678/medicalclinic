package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.CreateDoctorDto;
import com.antomino678.medicalclinic.dto.DoctorDto;
import com.antomino678.medicalclinic.dto.FacilityDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.dto.UpdateDoctorDto;
import com.antomino678.medicalclinic.dto.UserDto;
import com.antomino678.medicalclinic.exception.DoctorAlreadyAssignedException;
import com.antomino678.medicalclinic.exception.DoctorAlreadyExistsException;
import com.antomino678.medicalclinic.exception.DoctorNotFoundException;
import com.antomino678.medicalclinic.exception.FacilityNotFoundException;
import com.antomino678.medicalclinic.exception.UserNotFoundException;
import com.antomino678.medicalclinic.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DoctorControllerTest {

    @MockitoBean
    DoctorService doctorService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getDoctors_DoctorsExist_DoctorsReturned() throws Exception {
        // given
        when(doctorService.getAllDoctors(any(Pageable.class))).thenReturn(new PageDto<>(List.of(
                new DoctorDto("jan", "kowalski", "kardiolog", 1L, new UserDto("jan@test.pl", 1L), Set.of()),
                new DoctorDto("anna", "nowak", "dentysta", 2L, new UserDto("anna@test.pl", 2L), Set.of())), 0, 10, 2, 1));

        // when and then
        mockMvc.perform(get("/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("jan"))
                .andExpect(jsonPath("$.content[0].user.email").value("jan@test.pl"))
                .andExpect(jsonPath("$.content[1].firstName").value("anna"))
                .andExpect(jsonPath("$.content[1].user.email").value("anna@test.pl"));
    }

    @Test
    void getDoctor_DoctorExists_DoctorReturned() throws Exception {
        // given
        when(doctorService.getDoctorById(1L)).thenReturn(
                new DoctorDto("jan", "kowalski", "kardiolog", 1L, new UserDto("jan@test.pl", 1L), Set.of()));

        // when and then
        mockMvc.perform(get("/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("jan"))
                .andExpect(jsonPath("$.lastName").value("kowalski"))
                .andExpect(jsonPath("$.speciality").value("kardiolog"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user.email").value("jan@test.pl"));
    }

    @Test
    void getDoctor_DoctorNotExists_ThrowsDoctorNotFoundException() throws Exception {
        // given
        when(doctorService.getDoctorById(1L)).thenThrow(new DoctorNotFoundException(1L));

        // when and then
        mockMvc.perform(get("/doctors/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Doctor with id 1 not found"));
    }

    @Test
    void createDoctor_UserExists_DoctorCreated() throws Exception {
        // given
        CreateDoctorDto createDoctorDto = new CreateDoctorDto("jan", "kowalski", "kardiolog", 1L);
        when(doctorService.addDoctor(createDoctorDto)).thenReturn(
                new DoctorDto("jan", "kowalski", "kardiolog", 1L, new UserDto("jan@test.pl", 1L), Set.of()));

        // when and then
        mockMvc.perform(post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDoctorDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("jan"))
                .andExpect(jsonPath("$.user.email").value("jan@test.pl"));
    }

    @Test
    void createDoctor_UserNotExists_ThrowsUserNotFoundException() throws Exception {
        // given
        CreateDoctorDto createDoctorDto = new CreateDoctorDto("jan", "kowalski", "kardiolog", 1L);
        when(doctorService.addDoctor(createDoctorDto)).thenThrow(new UserNotFoundException(1L));

        // when and then
        mockMvc.perform(post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDoctorDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDoctor_DoctorExists_ThrowsDoctorAlreadyExistsException() throws Exception {
        // given
        CreateDoctorDto createDoctorDto = new CreateDoctorDto("jan", "kowalski", "kardiolog", 1L);
        when(doctorService.addDoctor(createDoctorDto)).thenThrow(new DoctorAlreadyExistsException(1L));

        // when and then
        mockMvc.perform(post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDoctorDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteDoctor_DoctorExists_DoctorDeleted() throws Exception {
        // when and then
        mockMvc.perform(delete("/doctors/1"))
                .andExpect(status().isNoContent());

        verify(doctorService).deleteDoctorById(1L);
    }

    @Test
    void deleteDoctor_DoctorNotExists_ThrowsDoctorNotFoundException() throws Exception {
        // given
        doThrow(new DoctorNotFoundException(1L)).when(doctorService).deleteDoctorById(1L);

        // when and then
        mockMvc.perform(delete("/doctors/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateDoctor_DoctorExists_DoctorUpdated() throws Exception {
        // given
        UpdateDoctorDto updateDoctorDto = new UpdateDoctorDto("adam", "nowak", "dentysta");
        when(doctorService.updateDoctorById(1L, updateDoctorDto)).thenReturn(
                new DoctorDto("adam", "nowak", "dentysta", 1L, new UserDto("jan@test.pl", 1L), Set.of()));

        // when and then
        mockMvc.perform(put("/doctors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDoctorDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("adam"))
                .andExpect(jsonPath("$.lastName").value("nowak"))
                .andExpect(jsonPath("$.speciality").value("dentysta"));
    }

    @Test
    void updateDoctor_DoctorNotExists_ThrowsDoctorNotFoundException() throws Exception {
        // given
        UpdateDoctorDto updateDoctorDto = new UpdateDoctorDto("adam", "nowak", "dentysta");
        when(doctorService.updateDoctorById(1L, updateDoctorDto)).thenThrow(new DoctorNotFoundException(1L));

        // when and then
        mockMvc.perform(put("/doctors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDoctorDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addFacility_DoctorAndFacilityExist_FacilityAdded() throws Exception {
        // given
        FacilityDto facilityDto = new FacilityDto("szpital nr 1", "warszawa", "00-001", "polna", "10", 2L);
        when(doctorService.addFacility(1L, 2L)).thenReturn(
                new DoctorDto("jan", "kowalski", "kardiolog", 1L, new UserDto("jan@test.pl", 1L), Set.of(facilityDto)));

        // when and then
        mockMvc.perform(post("/doctors/1/facilities/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facilities", hasSize(1)))
                .andExpect(jsonPath("$.facilities[0].name").value("szpital nr 1"))
                .andExpect(jsonPath("$.facilities[0].id").value(2));
    }

    @Test
    void addFacility_FacilityNotExists_ThrowsFacilityNotFoundException() throws Exception {
        // given
        when(doctorService.addFacility(1L, 2L)).thenThrow(new FacilityNotFoundException(2L));

        // when and then
        mockMvc.perform(post("/doctors/1/facilities/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Facility with id 2 not found"));
    }

    @Test
    void addFacility_DoctorAlreadyAssigned_ThrowsDoctorAlreadyAssignedException() throws Exception {
        // given
        when(doctorService.addFacility(1L, 2L)).thenThrow(new DoctorAlreadyAssignedException(1L, 2L));

        // when and then
        mockMvc.perform(post("/doctors/1/facilities/2"))
                .andExpect(status().isConflict());
    }
}
