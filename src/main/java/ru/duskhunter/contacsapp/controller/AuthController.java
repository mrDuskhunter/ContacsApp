package ru.duskhunter.contacsapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.duskhunter.contacsapp.common.util.ServerResponseHelper;
import ru.duskhunter.contacsapp.dto.ServerResponse;
import ru.duskhunter.contacsapp.dto.secure.*;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;
import ru.duskhunter.contacsapp.service.security.AuthService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ServerResponse<ContactOwner> register(@RequestBody RegisterRequestDto registerRequestDto) {
        return ServerResponseHelper.response(true, authService.register(registerRequestDto), HttpStatus.OK, List.of());
    }

    @PostMapping("/login")
    public ServerResponse<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ServerResponseHelper.response(true, authService.login(loginRequestDto), HttpStatus.OK, List.of());
    }

    @PostMapping("/refresh")
    public ServerResponse<RefreshTokenResponseDto> refresh(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto){
        return ServerResponseHelper.response(true, authService.refresh(refreshTokenRequestDto), HttpStatus.OK, List.of());
    }
}