package com.antomino678.medicalclinic.repository;

import com.antomino678.medicalclinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUserEmail(String email);

    @Transactional
    void deleteByUserEmail(String email);

    boolean existsByUserId(Long userId);
}
