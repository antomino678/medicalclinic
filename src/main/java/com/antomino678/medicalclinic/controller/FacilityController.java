package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.CreateFacilityDto;
import com.antomino678.medicalclinic.dto.FacilityDto;
import com.antomino678.medicalclinic.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
public class FacilityController {
    private final FacilityService facilityService;

    @GetMapping
    public List<FacilityDto> findAll() {
        return facilityService.getAllFacilities();
    }

    @GetMapping("/{id}")
    public FacilityDto getFacility(@PathVariable Long id) {
        return facilityService.getFacilityById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacilityDto createFacility(@RequestBody CreateFacilityDto createFacilityDto) {
        return facilityService.addFacility(createFacilityDto);
    }
}
