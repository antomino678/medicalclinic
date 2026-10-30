package com.antomino678.medicalclinic.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class Patient {
    private String email;
    private String password;
    private String idCardNo;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthday;

    public void update(Patient newData) {
        this.password = newData.getPassword();
        this.idCardNo = newData.getIdCardNo();
        this.firstName = newData.getFirstName();
        this.lastName = newData.getLastName();
        this.phoneNumber = newData.getPhoneNumber();
        this.birthday = newData.getBirthday();
    }
}
