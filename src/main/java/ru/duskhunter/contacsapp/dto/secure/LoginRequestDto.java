package ru.duskhunter.contacsapp.dto.secure;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    private String email,password;
}
