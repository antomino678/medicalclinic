package com.antomino678.medicalclinic.service;

import com.antomino678.medicalclinic.dto.ChangePasswordDto;
import com.antomino678.medicalclinic.dto.CreateUserDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.dto.UserDto;
import com.antomino678.medicalclinic.exception.UserAlreadyExistsException;
import com.antomino678.medicalclinic.exception.UserHasDoctorException;
import com.antomino678.medicalclinic.exception.UserHasPatientException;
import com.antomino678.medicalclinic.exception.UserNotFoundException;
import com.antomino678.medicalclinic.mapper.UserMapper;
import com.antomino678.medicalclinic.model.User;
import com.antomino678.medicalclinic.repository.DoctorRepository;
import com.antomino678.medicalclinic.repository.PatientRepository;
import com.antomino678.medicalclinic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    private UserService userService;
    private UserRepository userRepository;
    private PatientRepository patientRepository;
    private DoctorRepository doctorRepository;
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        this.userRepository = Mockito.mock(UserRepository.class);
        this.patientRepository = Mockito.mock(PatientRepository.class);
        this.doctorRepository = Mockito.mock(DoctorRepository.class);
        this.userMapper = Mappers.getMapper(UserMapper.class);
        this.userService = new UserService(userRepository, patientRepository, doctorRepository, userMapper);
    }

    @Test
    void findAllUsers_UsersExist_UsersReturned() {
        // given
        List<User> users = List.of(
                new User("test@test.pl", "pass123"),
                new User("email@test.pl", "pass"));
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(users, pageable, users.size()));

        // when
        PageDto<UserDto> result = userService.findAllUsers(pageable);
        List<UserDto> userDtos = result.content();

        // then
        assertAll(
                () -> assertEquals(2, result.totalElements()),
                () -> assertEquals(2, userDtos.size()),
                () -> assertEquals("test@test.pl", userDtos.get(0).email()),
                () -> assertEquals("email@test.pl", userDtos.get(1).email())
        );
    }

    @Test
    void findAllUsers_NoUsers_EmptyListReturned() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        PageDto<UserDto> result = userService.findAllUsers(pageable);
        List<UserDto> userDtos = result.content();

        // then
        assertTrue(userDtos.isEmpty());
    }

    @Test
    void findUserByEmail_UserExists_UserReturned() {
        // given
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.of(new User("test@test.pl", "pass123")));

        // when
        UserDto userDto = userService.findUserByEmail("test@test.pl");

        // then
        assertEquals("test@test.pl", userDto.email());
    }

    @Test
    void findUserByEmail_EmailWithUpperCaseAndSpaces_UserReturned() {
        // given
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.of(new User("test@test.pl", "pass123")));

        // when
        UserDto userDto = userService.findUserByEmail("  TEST@TEST.PL  ");

        // then
        assertEquals("test@test.pl", userDto.email());
    }

    @Test
    void findUserByEmail_UserNotExists_ThrowsUserNotFoundException() {
        // given
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.empty());

        // when
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.findUserByEmail("test@test.pl"));

        // then
        assertEquals("User with email test@test.pl not found", exception.getMessage());
    }

    @Test
    void addUser_UserNotExists_UserAdded() {
        // given
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.empty());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // when
        UserDto userDto = userService.addUser(new CreateUserDto("test@test.pl", "pass123"));

        // then
        verify(userRepository).save(userCaptor.capture());
        assertAll(
                () -> assertEquals("test@test.pl", userDto.email()),
                () -> assertEquals("test@test.pl", userCaptor.getValue().getEmail()),
                () -> assertEquals("pass123", userCaptor.getValue().getPassword())
        );
    }

    @Test
    void addUser_EmailWithUpperCaseAndSpaces_UserSavedWithNormalizedEmail() {
        // given
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.empty());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // when
        userService.addUser(new CreateUserDto("  TEST@TEST.PL  ", "pass123"));

        // then
        verify(userRepository).findUserByEmail("test@test.pl");
        verify(userRepository).save(userCaptor.capture());
        assertEquals("test@test.pl", userCaptor.getValue().getEmail());
    }

    @Test
    void addUser_UserExists_ThrowsUserAlreadyExistsException() {
        // given
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.of(new User("test@test.pl", "pass123")));

        // when
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> userService.addUser(new CreateUserDto("test@test.pl", "pass123")));

        // then
        assertEquals("User with email test@test.pl already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserByEmail_UserWithoutPatientAndDoctor_UserDeleted() {
        // given
        User user = new User("test@test.pl", "pass123");
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.of(user));
        when(patientRepository.existsByUserId(user.getId())).thenReturn(false);
        when(doctorRepository.existsByUserId(user.getId())).thenReturn(false);

        // when
        userService.deleteUserByEmail("test@test.pl");

        // then
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserByEmail_UserNotExists_ThrowsUserNotFoundException() {
        // given
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.empty());

        // when
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUserByEmail("test@test.pl"));

        // then
        assertEquals("User with email test@test.pl not found", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUserByEmail_UserHasPatient_ThrowsUserHasPatientException() {
        // given
        User user = new User("test@test.pl", "pass123");
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.of(user));
        when(patientRepository.existsByUserId(user.getId())).thenReturn(true);

        // when
        UserHasPatientException exception = assertThrows(UserHasPatientException.class,
                () -> userService.deleteUserByEmail("test@test.pl"));

        // then
        assertEquals("User with email test@test.pl has an assigned patient and cannot be deleted", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUserByEmail_UserHasDoctor_ThrowsUserHasDoctorException() {
        // given
        User user = new User("test@test.pl", "pass123");
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.of(user));
        when(patientRepository.existsByUserId(user.getId())).thenReturn(false);
        when(doctorRepository.existsByUserId(user.getId())).thenReturn(true);

        // when
        UserHasDoctorException exception = assertThrows(UserHasDoctorException.class,
                () -> userService.deleteUserByEmail("test@test.pl"));

        // then
        assertEquals("User with email test@test.pl has an assigned doctor and cannot be deleted", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void changePassword_UserExists_PasswordChanged() {
        // given
        User user = new User("test@test.pl", "pass123");
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.of(user));

        // when
        userService.changePassword("test@test.pl", new ChangePasswordDto("siemanko"));

        // then
        assertAll(
                () -> verify(userRepository).save(user),
                () -> assertEquals("siemanko", user.getPassword())
        );
    }

    @Test
    void changePassword_EmailWithUpperCaseAndSpaces_PasswordChanged() {
        // given
        User user = new User("test@test.pl", "pass123");
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.of(user));

        // when
        userService.changePassword("  TEST@TEST.PL  ", new ChangePasswordDto("siemanko"));

        // then
        assertEquals("siemanko", user.getPassword());
    }

    @Test
    void changePassword_UserNotExists_ThrowsUserNotFoundException() {
        // given
        when(userRepository.findUserByEmail("test@test.pl")).thenReturn(Optional.empty());

        // when
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.changePassword("test@test.pl", new ChangePasswordDto("siemanko")));

        // then
        assertEquals("User with email test@test.pl not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}
