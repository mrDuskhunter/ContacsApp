package ru.duskhunter.contacsapp.service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtSecurityService {
    private final String SECRET_KEY;

    public JwtSecurityService() {
        byte[] keyBytes = new byte[256 / 8];
        new SecureRandom().nextBytes(keyBytes);
        this.SECRET_KEY = Base64.getEncoder().encodeToString(keyBytes);
    }

    public String getSECRET_KEY() {
        return SECRET_KEY;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        ContactOwner contactOwner = (ContactOwner) userDetails;
        return Jwts.builder()
                .subject(contactOwner.getEmail())
                .claim("contactOwnerId", contactOwner.getId())
                .claim("role", contactOwner.getAuthorities().stream()
                        .findFirst()
                        .map(GrantedAuthority::getAuthority)
                        .orElse("USER"))
                .issuedAt(new Date(System.currentTimeMillis())) // когда выдан токен
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24)) //время жизни токена
                .signWith(getSigningKey()) //подпись токена
                .compact();
    }

    public String generateRefreshToken(Map<String, String> claims, UserDetails userDetails) {
        ContactOwner contactOwner = (ContactOwner) userDetails;
        return Jwts.builder()
                .claims(claims)
                .subject(contactOwner.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .signWith(getSigningKey())
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}