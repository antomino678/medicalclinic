package com.antomino678.medicalclinic.controller;

import com.antomino678.medicalclinic.dto.ChangePasswordDto;
import com.antomino678.medicalclinic.dto.CreateUserDto;
import com.antomino678.medicalclinic.dto.PageDto;
import com.antomino678.medicalclinic.dto.UserDto;
import com.antomino678.medicalclinic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor

public class UserController {
    private final UserService userService;

    @GetMapping
    public PageDto<UserDto> getUsers(Pageable pageable) {
        return userService.findAllUsers(pageable);
    }

    @GetMapping("/{email}")
    public UserDto getUser(@PathVariable String email) {
        return userService.findUserByEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody CreateUserDto createUserDto) {
        return userService.addUser(createUserDto);
    }

    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String email) {
        userService.deleteUserByEmail(email);
    }

    @PatchMapping("/{email}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable String email, @RequestBody ChangePasswordDto changePasswordDto) {
        userService.changePassword(email, changePasswordDto);
    }
}
