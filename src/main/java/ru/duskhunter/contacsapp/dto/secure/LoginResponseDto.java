package ru.duskhunter.contacsapp.dto.secure;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDto {
    private String email, token, refreshToken;
}