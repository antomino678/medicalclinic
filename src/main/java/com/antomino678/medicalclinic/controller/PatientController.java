package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.CreatePatientDto;
import com.antomino678.medicalclinic.dto.ErrorMessageDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.dto.PatientDto;
import com.antomino678.medicalclinic.dto.UpdatePatientDto;
import com.antomino678.medicalclinic.service.PatientService;
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

@Tag(name = "Patients", description = "Patient management")
@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;

    @Operation(summary = "Get all patients (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of patients")
    @GetMapping
    public PageDto<PatientDto> getPatients(@ParameterObject Pageable pageable) {
        return patientService.getAllPatients(pageable);
    }

    @Operation(summary = "Get patient by email")
    @ApiResponse(responseCode = "200", description = "Patient found")
    @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/{email}")
    public PatientDto getPatient(@Parameter(description = "Patient email", example = "jan@test.pl")
                                 @PathVariable String email) {
        return patientService.getPatientByEmail(email);
    }

    @Operation(summary = "Create patient for existing user")
    @ApiResponse(responseCode = "201", description = "Patient created")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "409", description = "Patient for this user already exists",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto createPatient(@RequestBody CreatePatientDto createPatientDto) {
        return patientService.addPatient(createPatientDto);
    }

    @Operation(summary = "Delete patient by email")
    @ApiResponse(responseCode = "204", description = "Patient deleted")
    @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatient(@Parameter(description = "Patient email", example = "jan@test.pl")
                              @PathVariable String email) {
        patientService.deletePatientByEmail(email);
    }

    @Operation(summary = "Update patient by email")
    @ApiResponse(responseCode = "200", description = "Patient updated")
    @ApiResponse(responseCode = "404", description = "Patient not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @PutMapping("/{email}")
    public PatientDto updatePatient(@Parameter(description = "Patient email", example = "jan@test.pl")
                                    @PathVariable String email,
                                    @RequestBody UpdatePatientDto updatePatientDto) {
        return patientService.updatePatientByEmail(email, updatePatientDto);
    }
}
