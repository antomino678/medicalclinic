package com.antomino678.medicalclinic.mapper;

import com.antomino678.medicalclinic.dto.VisitDto;
import com.antomino678.medicalclinic.model.Visit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {DoctorMapper.class, PatientMapper.class})
public interface VisitMapper {
    VisitDto toDto(Visit visit);
}
