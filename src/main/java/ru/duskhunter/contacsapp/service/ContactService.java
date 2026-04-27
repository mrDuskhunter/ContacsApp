package ru.duskhunter.contacsapp.service;

import org.springframework.http.ResponseEntity;
import ru.duskhunter.contacsapp.dto.ContactCreateDtoRequest;
import ru.duskhunter.contacsapp.dto.ContactDto;
import ru.duskhunter.contacsapp.model.entity.ServerResponse;

import java.util.List;

public interface ContactService {
    ServerResponse<List<ContactDto>> getContacts();
    ResponseEntity<ServerResponse<ContactDto>> responseGetContactById(long contactId);
    ServerResponse<ContactDto> createContact(ContactCreateDtoRequest contact);
    ServerResponse<ContactDto> deleteContactById(long contactId);
    ServerResponse<ContactDto> updateContact(ContactDto contact);
}