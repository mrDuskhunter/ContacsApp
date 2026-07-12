package ru.duskhunter.contacsapp.service;

import ru.duskhunter.contacsapp.dto.ServerResponse;
import ru.duskhunter.contacsapp.dto.contact.ContactCreateDto;
import ru.duskhunter.contacsapp.dto.contact.ContactDto;

import java.util.List;

public interface ContactService {
    ServerResponse<List<ContactDto>> getContacts();
    ServerResponse<ContactDto> getContactById(long contactId);
    ServerResponse<ContactDto> createContact(ContactCreateDto contact);
    ServerResponse<ContactDto> deleteContactById(long contactId);
    ServerResponse<ContactDto> updateContact(ContactDto contact);
}