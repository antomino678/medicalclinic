package com.antomino678.medicalclinic.repository;

import com.antomino678.medicalclinic.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    boolean existsByDoctorIdAndStartTimeLessThanAndEndTimeGreaterThan(Long doctorId,
                                                                      LocalDateTime endTime,
                                                                      LocalDateTime startTime);
    List<Visit> findByPatientUserEmail(String email);
}


