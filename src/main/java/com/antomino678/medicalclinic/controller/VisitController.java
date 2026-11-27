package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.BookVisitDto;
import com.antomino678.medicalclinic.dto.CreateVisitDto;
import com.antomino678.medicalclinic.dto.ErrorMessageDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.dto.VisitDto;
import com.antomino678.medicalclinic.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Visits", description = "Visit management and booking")
@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
public class VisitController {
    private final VisitService visitService;

    @Operation(summary = "Get all visits (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of visits")
    @GetMapping
    public PageDto<VisitDto> getVisits(@ParameterObject Pageable pageable) {
        return visitService.getAllVisits(pageable);
    }

    @Operation(summary = "Get visit by id")
    @ApiResponse(responseCode = "200", description = "Visit found")
    @ApiResponse(responseCode = "404", description = "Visit not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/{id}")
    public VisitDto getVisit(@Parameter(description = "Visit id", example = "1")
                             @PathVariable Long id) {
        return visitService.findById(id);
    }

    @Operation(summary = "Get visits of patient (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of patient visits")
    @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/patient/{email}")
    public PageDto<VisitDto> getPatientVisits(@Parameter(description = "Patient email", example = "jan@test.pl")
                                              @PathVariable String email,
                                              @ParameterObject Pageable pageable) {
        return visitService.getPatientVisits(email, pageable);
    }

    @Operation(summary = "Create visit slot for doctor",
            description = "Visit must be in the future, start before end and start/end at a full quarter of an hour.")
    @ApiResponse(responseCode = "201", description = "Visit created")
    @ApiResponse(responseCode = "400", description = "Invalid visit time",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "404", description = "Doctor not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "409", description = "Doctor already has a visit at this time",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDto createVisit(@RequestBody CreateVisitDto createVisitDto) {
        return visitService.addVisit(createVisitDto);
    }

    @Operation(summary = "Book visit for patient")
    @ApiResponse(responseCode = "200", description = "Visit booked")
    @ApiResponse(responseCode = "400", description = "Visit is in the past or email is empty",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "404", description = "Visit or patient not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "409", description = "Visit is already booked",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @PatchMapping("/{id}/patient")
    public VisitDto bookVisit(@Parameter(description = "Visit id", example = "1")
                              @PathVariable Long id,
                              @RequestBody BookVisitDto bookVisitDto) {
        return visitService.registerPatient(id, bookVisitDto);
    }
}
