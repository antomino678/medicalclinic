package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.ChangePasswordDto;
import com.antomino678.medicalclinic.dto.CreateUserDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.dto.UserDto;
import com.antomino678.medicalclinic.exception.UserAlreadyExistsException;
import com.antomino678.medicalclinic.exception.UserHasPatientException;
import com.antomino678.medicalclinic.exception.UserNotFoundException;
import com.antomino678.medicalclinic.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @MockitoBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getUsers_UsersExist_UsersReturned() throws Exception {
        // given
        when(userService.findAllUsers(any(Pageable.class))).thenReturn(new PageDto<>(List.of(
                new UserDto("test@test.pl", 1L),
                new UserDto("email@test.pl", 2L)), 0, 10, 2, 1));

        // when and then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].email").value("test@test.pl"))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].email").value("email@test.pl"))
                .andExpect(jsonPath("$.content[1].id").value(2));
    }

    @Test
    void getUser_UserExists_UserReturned() throws Exception {
        // given
        when(userService.findUserByEmail("test@test.pl")).thenReturn(new UserDto("test@test.pl", 1L));

        // when and then
        mockMvc.perform(get("/users/test@test.pl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.pl"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUser_UserNotExists_ThrowsUserNotFoundException() throws Exception {
        // given
        when(userService.findUserByEmail("test@test.pl")).thenThrow(new UserNotFoundException("test@test.pl"));

        // when and then
        mockMvc.perform(get("/users/test@test.pl"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with email test@test.pl not found"));
    }

    @Test
    void createUser_UserNotExists_UserCreated() throws Exception {
        // given
        CreateUserDto createUserDto = new CreateUserDto("test@test.pl", "pass123");
        when(userService.addUser(createUserDto)).thenReturn(new UserDto("test@test.pl", 1L));

        // when and then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.pl"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createUser_UserExists_ThrowsUserAlreadyExistsException() throws Exception {
        // given
        CreateUserDto createUserDto = new CreateUserDto("test@test.pl", "pass123");
        when(userService.addUser(createUserDto)).thenThrow(new UserAlreadyExistsException("test@test.pl"));

        // when and then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteUser_UserExists_UserDeleted() throws Exception {
        // when and then
        mockMvc.perform(delete("/users/test@test.pl"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserByEmail("test@test.pl");
    }

    @Test
    void deleteUser_UserNotExists_ThrowsUserNotFoundException() throws Exception {
        // given
        doThrow(new UserNotFoundException("test@test.pl"))
                .when(userService).deleteUserByEmail("test@test.pl");

        // when and then
        mockMvc.perform(delete("/users/test@test.pl"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_UserHasPatient_ThrowsUserHasPatientException() throws Exception {
        // given
        doThrow(new UserHasPatientException("test@test.pl"))
                .when(userService).deleteUserByEmail("test@test.pl");

        // when and then
        mockMvc.perform(delete("/users/test@test.pl"))
                .andExpect(status().isConflict());
    }

    @Test
    void changePassword_UserExists_PasswordChanged() throws Exception {
        // given
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("test123");

        // when and then
        mockMvc.perform(patch("/users/test@test.pl/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isNoContent());

        verify(userService).changePassword("test@test.pl", changePasswordDto);
    }

    @Test
    void changePassword_UserNotExists_ThrowsUserNotFoundException() throws Exception {
        // given
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("test123");
        doThrow(new UserNotFoundException("test@test.pl"))
                .when(userService).changePassword("test@test.pl", changePasswordDto);

        // when and then
        mockMvc.perform(patch("/users/test@test.pl/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isNotFound());
    }
}
