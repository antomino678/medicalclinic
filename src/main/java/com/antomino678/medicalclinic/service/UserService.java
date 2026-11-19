package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.ChangePasswordDto;
import com.antomino678.medicalclinic.dto.CreateUserDto;
import com.antomino678.medicalclinic.dto.UserDto;
import com.antomino678.medicalclinic.exception.UserAlreadyExistsException;
import com.antomino678.medicalclinic.exception.UserHasPatientException;
import com.antomino678.medicalclinic.exception.UserNotFoundException;
import com.antomino678.medicalclinic.mapper.UserMapper;
import com.antomino678.medicalclinic.model.User;
import com.antomino678.medicalclinic.repository.PatientRepository;
import com.antomino678.medicalclinic.repository.UserRepository;
import com.antomino678.medicalclinic.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final UserMapper userMapper;

    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto findUserByEmail(String rawEmail) {
        String email = EmailNormalizer.normalize(rawEmail);
        return userRepository.findUserByEmail(email)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public UserDto addUser(CreateUserDto createUserDto) {
        String email = EmailNormalizer.normalize(createUserDto.email());
        if (userRepository.findUserByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(email);
        }
        User user = new User(email, createUserDto.password());
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    public void deleteUserByEmail(String rawEmail) {
        String email = EmailNormalizer.normalize(rawEmail);
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        if (patientRepository.existsByUserId(user.getId())) {
            throw new UserHasPatientException(email);
        }
        userRepository.delete(user);
    }

    public void changePassword(String rawEmail, ChangePasswordDto changePasswordDto) {
        String email = EmailNormalizer.normalize(rawEmail);
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        user.changePassword(changePasswordDto.newPassword());
        userRepository.save(user);
    }

}
