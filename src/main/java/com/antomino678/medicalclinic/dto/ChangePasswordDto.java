package com.antomino678.medicalclinic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChangePasswordDto(
        @Schema(example = "haslo123") String newPassword) {
}
