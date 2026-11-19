package com.antomino678.medicalclinic.mapper;

import com.antomino678.medicalclinic.dto.CreateFacilityDto;
import com.antomino678.medicalclinic.dto.FacilityDto;
import com.antomino678.medicalclinic.model.Facility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FacilityMapper {
    FacilityDto toDto(Facility facility);

    @Mapping(target = "name", source = "name")
    Facility toFacility(String name, CreateFacilityDto createFacilityDto);

}
