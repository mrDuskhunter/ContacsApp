package ru.duskhunter.contacsapp.dto.secure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.duskhunter.contacsapp.common.util.Utils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {
    @NotBlank(message = "Username cannot be empty")
    private String user;

    @NotBlank(message = "Email cannot be empty")
    @Pattern(regexp = Utils.regexEmail, message = "Incorrect email")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\"!@#$%^&*_-]).{8,50}", message = "Incorrect password")
    private String password;
}