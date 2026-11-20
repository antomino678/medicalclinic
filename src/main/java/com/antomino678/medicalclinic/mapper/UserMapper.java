package com.antomino678.medicalclinic.mapper;

import com.antomino678.medicalclinic.dto.UserDto;
import com.antomino678.medicalclinic.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
