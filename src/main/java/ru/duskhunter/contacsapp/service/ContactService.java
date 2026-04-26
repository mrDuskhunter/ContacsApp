package ru.duskhunter.contacsapp.service;

import org.springframework.http.ResponseEntity;
import ru.duskhunter.contacsapp.model.entity.Contact;
import ru.duskhunter.contacsapp.model.entity.ServerResponse;

import java.util.List;

public interface ContactService {
    ServerResponse<List<Contact>> getContacts();
    ResponseEntity<ServerResponse<Contact>> getContactById(int id);
    ServerResponse<Contact> createContact(Contact contact);
}