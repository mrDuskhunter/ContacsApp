package ru.duskhunter.contacsapp.service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import ru.duskhunter.contacsapp.model.Role;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
Test plan:
    1. generateToken / extractEmail
        1.1 extractEmail returns the same email the token was generated for
    2. validateToken
        2.1 valid token + matching userDetails + not expired -> true
        2.2 token subject differs from userDetails username -> false
        2.3 expired token -> isTokenExpired == true, validateToken == false
    3. extractEmail/parsing on a token signed with a DIFFERENT key -> SignatureException
       (guards against forged/tampered tokens)
    4. generateRefreshToken has a longer lifetime than the access token
 */

public class JwtSecurityServiceTest {
    private JwtSecurityService jwtSecurityService;
    private ContactOwner owner;

    @BeforeEach
    void setUp() {
        jwtSecurityService = new JwtSecurityService();
        owner = ContactOwner.builder()
                .id(1L)
                .username("Name")
                .email("user@mail.ru")
                .password("P@ssw0rd1234_QwErt")
                .role(Role.ROLE_USER)
                .birthday(LocalDate.now().minusYears(20))
                .telephone("+79001112233")
                .build();
    }

    @Test
    void shouldExtractEmailFromGeneratedToken() {
        String token = jwtSecurityService.generateToken(owner);

        assertEquals(owner.getEmail(), jwtSecurityService.extractEmail(token));
    }

    @Test
    void shouldValidateTokenWhenMatchingAndNotExpired() {
        String token = jwtSecurityService.generateToken(owner);
        UserDetails userDetails = new User(owner.getEmail(), owner.getPassword(), authoritiesFor(owner));

        assertTrue(jwtSecurityService.validateToken(token, userDetails));
        assertFalse(jwtSecurityService.isTokenExpired(token));
    }

    @Test
    void shouldNotValidateTokenWhenUsernameDiffers() {
        String token = jwtSecurityService.generateToken(owner);

        UserDetails anotherUser = new User("another@mail.ru", "password", authoritiesFor(owner));

        assertFalse(jwtSecurityService.validateToken(token, anotherUser));
    }

    @Test
    void shouldDetectExpiredToken() {
        SecretKey signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecurityService.getSECRET_KEY()));

        String expiredToken = Jwts.builder()
                .subject(owner.getEmail())
                .issuedAt(new Date(System.currentTimeMillis() - 100_000))
                .expiration(new Date(System.currentTimeMillis() - 50_000))
                .signWith(signingKey)
                .compact();

        UserDetails userDetails = new User(owner.getEmail(), owner.getPassword(), authoritiesFor(owner));

        assertTrue(jwtSecurityService.isTokenExpired(expiredToken));
        assertFalse(jwtSecurityService.validateToken(expiredToken, userDetails));
    }

    @Test
    void shouldRejectTokenSignedWithAnotherKey() {
        // a second instance generates its own random secret key in the constructor
        SecretKey foreignKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(new JwtSecurityService().getSECRET_KEY()));

        String forgedToken = Jwts.builder()
                .subject(owner.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(foreignKey)
                .compact();

        assertThrows(SignatureException.class, () -> jwtSecurityService.extractEmail(forgedToken));
    }

    @Test
    void refreshTokenShouldOutliveAccessToken() {
        String accessToken = jwtSecurityService.generateToken(owner);
        String refreshToken = jwtSecurityService.generateRefreshToken(new HashMap<>(), owner);

        assertEquals(owner.getEmail(), jwtSecurityService.extractEmail(refreshToken));
        assertTrue(jwtSecurityService.extractExpiration(refreshToken)
                .after(jwtSecurityService.extractExpiration(accessToken)));
    }

    @Test
    void generatedTokenShouldNotBeExpiredImmediately() {
        String token = jwtSecurityService.generateToken(owner);

        assertFalse(jwtSecurityService.extractExpiration(token).before(new Date()));
    }

    // sanity check that authorities survive the round trip via a matching UserDetails
    @Test
    void validateTokenShouldSucceedRegardlessOfAuthoritiesOnUserDetails() {
        String token = jwtSecurityService.generateToken(owner);
        UserDetails sameEmailDifferentAuthorities =
                new User(owner.getEmail(), "irrelevant", List.of());

        assertTrue(jwtSecurityService.validateToken(token, sameEmailDifferentAuthorities));
    }

    private static List<SimpleGrantedAuthority> authoritiesFor(ContactOwner owner) {
        return List.of(new SimpleGrantedAuthority(owner.getRole().name()));
    }
}