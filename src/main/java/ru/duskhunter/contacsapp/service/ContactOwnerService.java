package ru.duskhunter.contacsapp.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.duskhunter.contacsapp.dto.ServerResponse;
import ru.duskhunter.contacsapp.dto.contactowner.ContactCreateOwnerDto;
import ru.duskhunter.contacsapp.dto.contactowner.ContactOwnerDto;

import java.util.List;

public interface ContactOwnerService {
    ServerResponse<List<ContactOwnerDto>> getOwners();
    ServerResponse<ContactOwnerDto> getOwnerById(long ownerId);
    ServerResponse<ContactOwnerDto> getOwnerByEmail(String email);
    ServerResponse<ContactOwnerDto> createOwner(ContactCreateOwnerDto owner);
    ServerResponse<ContactOwnerDto> deleteOwnerById(long ownerId);
    ServerResponse<ContactOwnerDto> updateOwner(ContactOwnerDto owner);
    UserDetailsService getUserDetailsService();
}