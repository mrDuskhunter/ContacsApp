package ru.duskhunter.contacsapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.duskhunter.contacsapp.common.util.ServerResponseHelper;
import ru.duskhunter.contacsapp.model.entity.Contact;
import ru.duskhunter.contacsapp.model.entity.ContactsDAO;
import ru.duskhunter.contacsapp.model.entity.ServerResponse;

import java.util.List;
import java.util.Optional;

@Service
public class ContactServiceImpl implements ContactService {
    private final ContactsDAO contacts;

    @Autowired
    public ContactServiceImpl(ContactsDAO contacts) {
        this.contacts = contacts;
    }

    @Override
    public ServerResponse<List<Contact>> getContacts() {
        return ServerResponseHelper.response(true, contacts.getContacts(), HttpStatus.OK, List.of());
    }

    @Override
    public ResponseEntity<ServerResponse<Contact>> getContactById(int id) {
        Optional<Contact> contactOptional = contacts.getContacts().stream().filter(c -> c.getId() == id).findFirst();
        return contactOptional.map(contact -> ServerResponseHelper.response(contact, HttpStatus.OK))
                .orElseGet(() -> ServerResponseHelper.response(null, HttpStatus.NO_CONTENT));
    }

    @Override
    public ServerResponse<Contact> createContact(Contact contact) {
        if (contact.getId() == null) {
            contact.setId((long) contacts.getContacts().size());
        }

        if (matchId(contact)) {
            return ServerResponseHelper.response(false, contact, HttpStatus.CONFLICT,
                    List.of(String.format("Contact with id %s already exist", contact.getId())));
        }

        contacts.addContact(contact);
        return ServerResponseHelper.response(true, contact, HttpStatus.CREATED, List.of());
    }

    private boolean matchId(Contact contact) {
        return contacts.getContacts().stream()
                .mapToLong(Contact::getId)
                .anyMatch(id -> id == contact.getId());
    }
}