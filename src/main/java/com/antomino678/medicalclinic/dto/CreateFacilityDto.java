package com.antomino678.medicalclinic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateFacilityDto(
        @Schema(example = "przychodnia") String name,
        @Schema(example = "warszawa") String city,
        @Schema(example = "00-001") String zipCode,
        @Schema(example = "polna") String street,
        @Schema(example = "10") String buildingNumber) {
}
