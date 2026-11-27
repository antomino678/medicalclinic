package com.antomino678.medicalclinic.repository;

import com.antomino678.medicalclinic.model.Visit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    boolean existsByDoctorIdAndStartTimeLessThanAndEndTimeGreaterThan(Long doctorId,
                                                                      LocalDateTime endTime,
                                                                      LocalDateTime startTime);
    Page<Visit> findByPatientUserEmail(String email, Pageable pageable);
}

