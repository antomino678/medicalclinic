package com.antomino678.medicalclinic.repository;

import com.antomino678.medicalclinic.model.Patient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PatientRepository {
    private final Map<String, Patient> patients = new ConcurrentHashMap<>();

    public void save(Patient patient) {
        patients.put(patient.getEmail(), patient);
    }

    public Optional<Patient> findByEmail(String email) {
        return Optional.ofNullable(patients.get(email));
    }

    public List<Patient> findAll() {
        return List.copyOf(patients.values());
    }

    public void deleteByEmail(String email) {
        patients.remove(email);
    }
}
