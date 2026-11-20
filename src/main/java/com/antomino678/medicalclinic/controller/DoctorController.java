package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.*;
import com.antomino678.medicalclinic.model.Doctor;
import com.antomino678.medicalclinic.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;

    @GetMapping
    public List<DoctorDto> getDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    public DoctorDto getDoctor(@PathVariable Long id) {
        return doctorService.getDoctorById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorDto createDoctor(@RequestBody CreateDoctorDto createDoctorDto) {
        return doctorService.addDoctor(createDoctorDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctorById(id);
    }

    @PutMapping("/{id}")
    public DoctorDto updateDoctor(@PathVariable Long id, @RequestBody UpdateDoctorDto updateDoctorDto) {
        return doctorService.updateDoctorById(id, updateDoctorDto);
    }

    @PostMapping("/{doctorId}/facilities/{facilityId}")
    public DoctorDto addFacility(@PathVariable Long doctorId, @PathVariable Long facilityId) {
        return doctorService.addFacility(doctorId, facilityId);
    }
}
