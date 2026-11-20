package com.antomino678.medicalclinic.util;

import com.antomino678.medicalclinic.exception.InvalidFacilityNameException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FacilityNameNormalizer {

    public static String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidFacilityNameException();
        }
        return name.trim().replaceAll("\\s+", " ");
    }
}
