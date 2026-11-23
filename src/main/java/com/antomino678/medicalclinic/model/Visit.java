package com.antomino678.medicalclinic.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    public Visit(LocalDateTime startTime, LocalDateTime endTime, Doctor doctor) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.doctor = doctor;
    }

    public void assignPatient(Patient patient) {
        this.patient = patient;
    }

    public boolean isBooked() {
        return patient != null;
    }
}
