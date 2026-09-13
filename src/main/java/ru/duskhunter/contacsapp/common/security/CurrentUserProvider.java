package ru.duskhunter.contacsapp.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.duskhunter.contacsapp.exception.NotFoundException;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;
import ru.duskhunter.contacsapp.model.repository.ContactOwnerRepo;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {
    private final ContactOwnerRepo ownerRepo;

    public ContactOwner getCurrentOwner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String email = authentication.getName();

        return ownerRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with email %s does not exist", email), null));
    }
}