package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.*;
import com.antomino678.medicalclinic.service.DoctorService;
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

@Tag(name = "Doctors", description = "Doctor management")
@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;

    @Operation(summary = "Get all doctors (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of doctors")
    @GetMapping
    public PageDto<DoctorDto> getDoctors(@ParameterObject Pageable pageable) {
        return doctorService.getAllDoctors(pageable);
    }

    @Operation(summary = "Get doctor by id")
    @ApiResponse(responseCode = "200", description = "Doctor found")
    @ApiResponse(responseCode = "404", description = "Doctor not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/{id}")
    public DoctorDto getDoctor(@Parameter(description = "Doctor id", example = "1")
                               @PathVariable Long id) {
        return doctorService.getDoctorById(id);
    }

    @Operation(summary = "Create doctor for existing user")
    @ApiResponse(responseCode = "201", description = "Doctor created")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "409", description = "User is already a doctor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorDto createDoctor(@RequestBody CreateDoctorDto createDoctorDto) {
        return doctorService.addDoctor(createDoctorDto);
    }

    @Operation(summary = "Delete doctor by id")
    @ApiResponse(responseCode = "204", description = "Doctor deleted")
    @ApiResponse(responseCode = "404", description = "Doctor not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDoctor(@Parameter(description = "Doctor id", example = "1")
                             @PathVariable Long id) {
        doctorService.deleteDoctorById(id);
    }

    @Operation(summary = "Update doctor by id")
    @ApiResponse(responseCode = "200", description = "Doctor updated")
    @ApiResponse(responseCode = "404", description = "Doctor not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @PutMapping("/{id}")
    public DoctorDto updateDoctor(@Parameter(description = "Doctor id", example = "1")
                                  @PathVariable Long id,
                                  @RequestBody UpdateDoctorDto updateDoctorDto) {
        return doctorService.updateDoctorById(id, updateDoctorDto);
    }

    @Operation(summary = "Assign doctor to facility")
    @ApiResponse(responseCode = "200", description = "Doctor assigned to facility")
    @ApiResponse(responseCode = "404", description = "Doctor or facility not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "409", description = "Doctor already assigned to this facility",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @PostMapping("/{doctorId}/facilities/{facilityId}")
    public DoctorDto addFacility(@Parameter(description = "Doctor id", example = "1")
                                 @PathVariable Long doctorId,
                                 @Parameter(description = "Facility id", example = "1")
                                 @PathVariable Long facilityId) {
        return doctorService.addFacility(doctorId, facilityId);
    }
}
