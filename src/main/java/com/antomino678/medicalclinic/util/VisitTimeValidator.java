package com.antomino678.medicalclinic.util;

import com.antomino678.medicalclinic.exception.InvalidVisitTimeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VisitTimeValidator {
    public static void validate(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidVisitTimeException("Visit start and end must not be empty");
        }
        if (!startTime.isBefore(endTime)) {
            throw new InvalidVisitTimeException("Visit start must be before end");
        }
        validateNotInPast(startTime);
        if (!isFullQuarter(startTime) || !isFullQuarter(endTime)) {
            throw new InvalidVisitTimeException("Visit must start and end at a full quarter of an hour");
        }
    }

    private static boolean isFullQuarter(LocalDateTime time) {
        return time.getMinute() % 15 == 0 && time.getSecond() == 0 && time.getNano() == 0;
    }

    public static void validateNotInPast(LocalDateTime startTime) {
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new InvalidVisitTimeException("Visit must not be in the past");
        }
    }
}
