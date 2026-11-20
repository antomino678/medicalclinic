package com.antomino678.medicalclinic.mapper;

import com.antomino678.medicalclinic.dto.CreateDoctorDto;
import com.antomino678.medicalclinic.dto.DoctorDto;
import com.antomino678.medicalclinic.dto.UpdateDoctorDto;
import com.antomino678.medicalclinic.model.Doctor;
import com.antomino678.medicalclinic.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {
    DoctorDto toDto(Doctor doctor);

    @Mapping(source = "user", target = "user")
    @Mapping(target = "facilities", ignore = true)
    Doctor toDoctor(User user, CreateDoctorDto createDoctorDto);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "facilities", ignore = true)
    Doctor toDoctor(UpdateDoctorDto updateDoctorDto);
}
