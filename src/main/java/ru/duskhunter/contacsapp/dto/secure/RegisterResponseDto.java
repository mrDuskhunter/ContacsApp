package ru.duskhunter.contacsapp.dto.secure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.duskhunter.contacsapp.common.util.Utils;

@Getter
@Setter
@Builder
public class RegisterResponseDto {
    @NotBlank(message = "Username cannot be empty")
    private String user;

    @NotBlank(message = "Email cannot be empty")
    @Pattern(regexp = Utils.regexEmail, message = "Incorrect email")
    private String email;
}