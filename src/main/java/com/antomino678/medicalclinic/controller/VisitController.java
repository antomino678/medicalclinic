package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.BookVisitDto;
import com.antomino678.medicalclinic.dto.CreateVisitDto;
import com.antomino678.medicalclinic.dto.VisitDto;
import com.antomino678.medicalclinic.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
public class VisitController {
    private final VisitService visitService;

    @GetMapping
    public List<VisitDto> getVisits() {
        return visitService.getAllVisits();
    }

    @GetMapping("/{id}")
    public VisitDto getVisit(@PathVariable Long id) {
        return visitService.findById(id);
    }

    @GetMapping("/patient/{email}")
    public List<VisitDto> getPatientVisits(@PathVariable String email) {
        return visitService.getPatientVisits(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDto createVisit(@RequestBody CreateVisitDto createVisitDto) {
        return visitService.addVisit(createVisitDto);
    }

    @PatchMapping("/{id}/patient")
    public VisitDto bookVisit(@PathVariable Long id, @RequestBody BookVisitDto bookVisitDto) {
        return visitService.registerPatient(id, bookVisitDto);
    }
}
