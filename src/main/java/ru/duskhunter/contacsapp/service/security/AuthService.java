package ru.duskhunter.contacsapp.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.duskhunter.contacsapp.dto.secure.*;
import ru.duskhunter.contacsapp.model.Role;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;
import ru.duskhunter.contacsapp.model.repository.ContactOwnerRepo;
import ru.duskhunter.contacsapp.service.ContactOwnerService;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final ContactOwnerRepo contactOwnerRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtSecurityService jwtSecurityService;
    private final AuthenticationManager authenticationManager;
    private final ContactOwnerService contactOwnerService;

    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
        ContactOwner contactOwner = ContactOwner.builder()
                .username(registerRequestDto.getUser())
                .email(registerRequestDto.getEmail())
                .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                .role(Role.ROLE_USER).build();

        ContactOwner owner = contactOwnerRepo.save(contactOwner);

        return RegisterResponseDto.builder()
                .user(owner.getUsername())
                .email(owner.getEmail()).build();
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        ContactOwner contactOwner = contactOwnerRepo.findByEmail(
                loginRequestDto.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtSecurityService.generateToken(contactOwner);
        String refreshToken = jwtSecurityService.generateRefreshToken(new HashMap<>(), contactOwner);

        return LoginResponseDto.builder()
                .email(contactOwner.getEmail())
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    public RefreshTokenResponseDto refresh(RefreshTokenRequestDto refreshTokenRequestDto) {
        String jwt = refreshTokenRequestDto.getRefreshToken();
        String email = jwtSecurityService.extractEmail(jwt);
        ContactOwner contactOwner = contactOwnerRepo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDetails userDetails = contactOwnerService.getUserDetailsService().loadUserByUsername(email);

        if (jwtSecurityService.validateToken(jwt, userDetails)) {
            RefreshTokenResponseDto refreshTokenResponseDto = new RefreshTokenResponseDto();

            refreshTokenResponseDto.setToken(jwtSecurityService.generateToken(contactOwner));
            refreshTokenResponseDto.setRefreshToken(jwtSecurityService.generateRefreshToken(new HashMap<>(), contactOwner));

            return refreshTokenResponseDto;
        }
        return null;
    }
}