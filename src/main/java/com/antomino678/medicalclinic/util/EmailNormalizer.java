package com.antomino678.medicalclinic.util;

import com.antomino678.medicalclinic.exception.InvalidEmailException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailNormalizer {

    public static String normalize(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidEmailException();
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
