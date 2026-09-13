package ru.duskhunter.contacsapp.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.duskhunter.contacsapp.exception.NotFoundException;
import ru.duskhunter.contacsapp.model.Role;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;
import ru.duskhunter.contacsapp.model.repository.ContactOwnerRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
Test plan:
1. getCurrentOwner
    1.1 no Authentication in SecurityContext -> AccessDeniedException, ownerRepo untouched
    1.2 Authentication present but not authenticated -> AccessDeniedException, ownerRepo untouched
    1.3 Authentication authenticated, email not found in DB -> NotFoundException
        1.3.1 message contains the email
        1.3.2 dto is null
        1.3.3 ownerRepo.findByEmail -> one called
    1.4 Authentication authenticated, email found -> returns matching ContactOwner
        1.4.1 ownerRepo.findByEmail -> one called with correct email
 */
@ExtendWith(MockitoExtension.class)
public class CurrentUserProviderTest {
    @Mock
    private ContactOwnerRepo ownerRepo;

    private CurrentUserProvider currentUserProvider;

    @BeforeEach
    void setUp() {
        currentUserProvider = new CurrentUserProvider(ownerRepo);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowAccessDeniedWhenNoAuthenticationInContext() {
        assertThrows(AccessDeniedException.class, () -> currentUserProvider.getCurrentOwner());
        verifyNoInteractions(ownerRepo);
    }

    @Test
    void shouldThrowAccessDeniedWhenAuthenticationIsNotAuthenticated() {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken("user@mail.ru", "password");
        // 2-arg constructor defaults to authenticated = false
        SecurityContextHolder.getContext().setAuthentication(token);

        assertThrows(AccessDeniedException.class, () -> currentUserProvider.getCurrentOwner());
        verifyNoInteractions(ownerRepo);
    }

    @Test
    void shouldThrowNotFoundWhenOwnerEmailDoesNotExist() {
        String email = "ghost@mail.ru";
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(token);

        when(ownerRepo.findByEmail(email)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> currentUserProvider.getCurrentOwner());

        assertEquals(String.format("User with email %s does not exist", email), exception.getMessage());
        assertNull(exception.getDto());
        verify(ownerRepo, times(1)).findByEmail(email);
    }

    @Test
    void shouldReturnOwnerWhenAuthenticatedAndOwnerExists() {
        String email = "user@mail.ru";
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(token);

        ContactOwner owner = ContactOwner.builder()
                .id(1L)
                .username("Name")
                .email(email)
                .password("P@ssw0rd1234_QwErt")
                .role(Role.ROLE_USER)
                .birthday(LocalDate.now().minusYears(20))
                .telephone("+79001112233")
                .build();

        when(ownerRepo.findByEmail(email)).thenReturn(Optional.of(owner));

        ContactOwner result = currentUserProvider.getCurrentOwner();

        assertEquals(owner, result);
        assertEquals(email, result.getEmail());
        verify(ownerRepo, times(1)).findByEmail(email);
    }
}