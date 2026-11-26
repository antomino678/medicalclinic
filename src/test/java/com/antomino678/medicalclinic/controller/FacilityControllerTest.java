package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.CreateFacilityDto;
import com.antomino678.medicalclinic.dto.FacilityDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.exception.FacilityAlreadyExistsException;
import com.antomino678.medicalclinic.exception.FacilityNotFoundException;
import com.antomino678.medicalclinic.exception.InvalidFacilityNameException;
import com.antomino678.medicalclinic.service.FacilityService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FacilityControllerTest {

    @MockitoBean
    FacilityService facilityService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void findAll_FacilitiesExist_FacilitiesReturned() throws Exception {
        // given
        when(facilityService.getAllFacilities(any(Pageable.class))).thenReturn(new PageDto<>(List.of(
                new FacilityDto("szpital nr 1", "warszawa", "00-001", "polna", "10", 1L),
                new FacilityDto("przychodnia", "krakow", "30-001", "lesna", "5", 2L)), 0, 10, 2, 1));

        // when and then
        mockMvc.perform(get("/facilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].name").value("szpital nr 1"))
                .andExpect(jsonPath("$.content[0].city").value("warszawa"))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].name").value("przychodnia"))
                .andExpect(jsonPath("$.content[1].id").value(2));
    }

    @Test
    void getFacility_FacilityExists_FacilityReturned() throws Exception {
        // given
        when(facilityService.getFacilityById(1L))
                .thenReturn(new FacilityDto("szpital nr 1", "warszawa", "00-001", "polna", "10", 1L));

        // when and then
        mockMvc.perform(get("/facilities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("szpital nr 1"))
                .andExpect(jsonPath("$.city").value("warszawa"))
                .andExpect(jsonPath("$.zipCode").value("00-001"))
                .andExpect(jsonPath("$.street").value("polna"))
                .andExpect(jsonPath("$.buildingNumber").value("10"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getFacility_FacilityNotExists_ThrowsFacilityNotFoundException() throws Exception {
        // given
        when(facilityService.getFacilityById(1L)).thenThrow(new FacilityNotFoundException(1L));

        // when and then
        mockMvc.perform(get("/facilities/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Facility with id 1 not found"));
    }

    @Test
    void createFacility_FacilityNotExists_FacilityCreated() throws Exception {
        // given
        CreateFacilityDto createFacilityDto = new CreateFacilityDto("szpital nr 1", "warszawa", "00-001", "polna", "10");
        when(facilityService.addFacility(createFacilityDto))
                .thenReturn(new FacilityDto("szpital nr 1", "warszawa", "00-001", "polna", "10", 1L));

        // when and then
        mockMvc.perform(post("/facilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createFacilityDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("szpital nr 1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createFacility_FacilityExists_ThrowsFacilityAlreadyExistsException() throws Exception {
        // given
        CreateFacilityDto createFacilityDto = new CreateFacilityDto("szpital nr 1", "warszawa", "00-001", "polna", "10");
        when(facilityService.addFacility(createFacilityDto))
                .thenThrow(new FacilityAlreadyExistsException("szpital nr 1"));

        // when and then
        mockMvc.perform(post("/facilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createFacilityDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createFacility_EmptyName_ThrowsInvalidFacilityNameException() throws Exception {
        // given
        CreateFacilityDto createFacilityDto = new CreateFacilityDto("", "warszawa", "00-001", "polna", "10");
        when(facilityService.addFacility(createFacilityDto)).thenThrow(new InvalidFacilityNameException());

        // when and then
        mockMvc.perform(post("/facilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createFacilityDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Facility must not be empty"));
    }
}
