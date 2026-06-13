package com.server.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordDto {

    @NotBlank(message = "The old password cannot be empty")
    private String oldpassword;

    @NotBlank(message = "The new password cannot be empty")
    @Size(min = 8, max = 100, message = "The new password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&._-]).+$",
        message = "The new password must include at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String newpassword;

    @NotBlank(message = "The confirm password cannot be empty")
    private String confirmpassword;
}
