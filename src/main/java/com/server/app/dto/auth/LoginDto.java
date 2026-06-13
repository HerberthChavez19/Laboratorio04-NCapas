package com.server.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {

    @NotBlank(message = "The username cannot be empty")
    private String username;

    @NotBlank(message = "The password cannot be empty")
    private String password;
}
