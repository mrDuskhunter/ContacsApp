package ru.duskhunter.contacsapp.config;


/*
    Test plan:
    1. no Authorization header -> chain continues untouched, no authentication set, jwtSecurityService untouched
    2. header present but not "Bearer " prefixed -> same as (1)
    3. valid Bearer token -> SecurityContext gets an Authentication with the right principal/authorities
    4. token present but validateToken == false -> no authentication set
    5. SecurityContext already holds an Authentication -> filter must not overwrite it
       (contactOwnerService should not even be touched)
    6. filterChain.doFilter must be invoked exactly once for EVERY request, including the
       "valid token" branch, otherwise the request never reaches the controller.
       NOTE: this currently FAILS against JwtAuthenticationFilter as written — doFilterInternal
       never calls filterChain.doFilter(...) once it enters the Bearer-token branch. See the
       inline comment on the test for the one-line fix.
 */

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.duskhunter.contacsapp.service.ContactOwnerService;
import ru.duskhunter.contacsapp.service.security.JwtSecurityService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    @Mock
    private JwtSecurityService jwtSecurityService;
    @Mock
    private ContactOwnerService contactOwnerService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtSecurityService, contactOwnerService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPassThroughWhenNoAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtSecurityService, contactOwnerService);
    }

    @Test
    void shouldPassThroughWhenHeaderIsNotBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtSecurityService, contactOwnerService);
    }

    @Test
    void shouldAuthenticateWhenTokenIsValid() throws Exception {
        String token = "valid.jwt.token";
        String email = "user@mail.ru";
        UserDetails userDetails = new User(email, "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtSecurityService.extractEmail(token)).thenReturn(email);
        when(contactOwnerService.getUserDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtSecurityService.validateToken(token, userDetails)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "Authentication should be set for a valid token");
        assertEquals(email, authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void shouldNotAuthenticateWhenTokenFailsValidation() throws Exception {
        String token = "expired.jwt.token";
        String email = "user@mail.ru";
        UserDetails userDetails = new User(email, "password", List.of());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtSecurityService.extractEmail(token)).thenReturn(email);
        when(contactOwnerService.getUserDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtSecurityService.validateToken(token, userDetails)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotOverwriteExistingAuthentication() throws Exception {
        String token = "valid.jwt.token";
        String email = "user@mail.ru";

        UsernamePasswordAuthenticationToken existing =
                new UsernamePasswordAuthenticationToken("already-authenticated@mail.ru", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtSecurityService.extractEmail(token)).thenReturn(email);

        filter.doFilter(request, response, filterChain);

        assertEquals("already-authenticated@mail.ru",
                SecurityContextHolder.getContext().getAuthentication().getName());
        verifyNoInteractions(contactOwnerService);
    }

    @Test
    void filterChainShouldAlwaysBeInvokedEvenForAuthenticatedRequests() throws Exception {
        /*
        This documents a real bug: doFilterInternal only calls filterChain.doFilter(...)
        in the early-return branch (missing/invalid header). Once it enters the
        "Bearer <token>" branch it never calls filterChain.doFilter(...) again, so an
        authenticated request never reaches the controller.

        Fix: add filterChain.doFilter(request, response); at the end of doFilterInternal
        (after the closing brace of the `if (StringUtils.isNotEmpty(email) ...)` block),
        so it runs unconditionally once the early return is not taken.
        */
        String token = "valid.jwt.token";
        String email = "user@mail.ru";
        UserDetails userDetails = new User(email, "password", List.of());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtSecurityService.extractEmail(token)).thenReturn(email);
        when(contactOwnerService.getUserDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtSecurityService.validateToken(token, userDetails)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}