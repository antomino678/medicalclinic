package com.antomino678.medicalclinic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateUserDto(
        @Schema(example = "jan@test.pl") String email,
        @Schema(example = "haslo123") String password) {
}
