package ru.evolenta.user.dto;

import lombok.Data;

@Data
public class SignUpRequest {
    private String firstname;
    private String surname;
    private String lastname;
    private String username;
    private String password;
}
