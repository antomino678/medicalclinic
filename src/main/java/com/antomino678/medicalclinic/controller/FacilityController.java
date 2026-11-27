package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.CreateFacilityDto;
import com.antomino678.medicalclinic.dto.ErrorMessageDto;
import com.antomino678.medicalclinic.dto.FacilityDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.service.FacilityService;
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

@Tag(name = "Facilities", description = "Facility management")
@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
public class FacilityController {
    private final FacilityService facilityService;

    @Operation(summary = "Get all facilities (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of facilities")
    @GetMapping
    public PageDto<FacilityDto> findAll(@ParameterObject Pageable pageable) {
        return facilityService.getAllFacilities(pageable);
    }

    @Operation(summary = "Get facility by id")
    @ApiResponse(responseCode = "200", description = "Facility found")
    @ApiResponse(responseCode = "404", description = "Facility not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @GetMapping("/{id}")
    public FacilityDto getFacility(@Parameter(description = "Facility id", example = "1")
                                   @PathVariable Long id) {
        return facilityService.getFacilityById(id);
    }

    @Operation(summary = "Create facility")
    @ApiResponse(responseCode = "201", description = "Facility created")
    @ApiResponse(responseCode = "400", description = "Facility name is empty",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @ApiResponse(responseCode = "409", description = "Facility with this name already exists",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageDto.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacilityDto createFacility(@RequestBody CreateFacilityDto createFacilityDto) {
        return facilityService.addFacility(createFacilityDto);
    }
}
