package ru.duskhunter.contacsapp.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.duskhunter.contacsapp.common.util.ServerResponseHelper;
import ru.duskhunter.contacsapp.dto.ContactCreateDtoRequest;
import ru.duskhunter.contacsapp.dto.ContactDto;
import ru.duskhunter.contacsapp.model.entity.Contact;
import ru.duskhunter.contacsapp.model.entity.ContactsDAO;
import ru.duskhunter.contacsapp.model.entity.ServerResponse;

import java.util.List;
import java.util.Optional;

@Service
public class ContactServiceImpl implements ContactService {
    private final ContactsDAO contacts;
    private final ModelMapper mapper;

    @Autowired
    public ContactServiceImpl(ContactsDAO contacts, ModelMapper mapper) {
        this.contacts = contacts;
        this.mapper = mapper;
    }

    @Override
    public ServerResponse<List<ContactDto>> getContacts() {
        List<ContactDto> contactDtos = contacts.getContacts()
                .stream()
                .map(contact -> mapper.map(contact, ContactDto.class))
                .toList();
        return ServerResponseHelper.response(true, contactDtos, HttpStatus.OK, List.of());
    }

    @Override
    public ResponseEntity<ServerResponse<ContactDto>> responseGetContactById(long contactId) {
        Optional<Contact> contactOptional = getContactById(contactId);

        return contactOptional.map(contact -> ServerResponseHelper.response(mapper.map(contact, ContactDto.class), HttpStatus.OK))
                .orElseGet(() -> ServerResponseHelper.response(null, HttpStatus.NO_CONTENT));
    }

    @Override
    public ServerResponse<ContactDto> createContact(ContactCreateDtoRequest contactCreateDtoRequest) {
        Contact contact = mapper.map(contactCreateDtoRequest, Contact.class);

        int countCheckingForUniquenessId = 0;
        boolean isNonUniquenessId = false;

        do {
            contact.setId((long) contacts.getContacts().size());
            countCheckingForUniquenessId++;
            if (countCheckingForUniquenessId > 7) {
                isNonUniquenessId = true;
                break;
            }
        } while (matchId(contact.getId()));

        if (isNonUniquenessId) {
            return ServerResponseHelper.response(false, mapper.map(contact, ContactDto.class), HttpStatus.INTERNAL_SERVER_ERROR,
                    List.of("Id assignment error"));
        }

        Optional<Contact> optNewContact = getContactById(contact.getId());

        return optNewContact.isEmpty()
                ? ServerResponseHelper.response(true, mapper.map(contact, ContactDto.class), HttpStatus.CONFLICT,
                List.of("Error creating a contact"))
                : ServerResponseHelper.response(true, mapper.map(contact, ContactDto.class), HttpStatus.CREATED, List.of());
    }

    @Override
    public ServerResponse<ContactDto> deleteContactById(long contactId) {
        Optional<Contact> contactOptional = getContactById(contactId);
        if (contactOptional.isEmpty()) {
            return ServerResponseHelper.response(false, null, HttpStatus.NO_CONTENT,
                    List.of(String.format("The contact with id %s does not exist", contactId)));
        }

        ContactDto contactDto = mapper.map(contactOptional.get(), ContactDto.class);

        return contacts.deleteByContactId(contactId)
                ? ServerResponseHelper.response(true, contactDto, HttpStatus.OK, List.of())
                : ServerResponseHelper.response(false, contactDto, HttpStatus.INTERNAL_SERVER_ERROR,
                List.of(String.format("Unexpected server error during deletion operation the contact with id %s", contactId)));
    }

    @Override
    public ServerResponse<ContactDto> updateContact(ContactDto contactDto) {
        Long id = contactDto.getId();

        if (!matchId(id)) {
            return ServerResponseHelper.response(false, contactDto, HttpStatus.NO_CONTENT,
                    List.of(String.format("The contact with id %s does not exist", id)));
        }

        Contact contact = contacts.updateContact(mapper.map(contactDto, Contact.class));

        return ServerResponseHelper.response(true, mapper.map(contact, ContactDto.class), HttpStatus.OK, List.of());
    }

    private boolean matchId(long contactId) {
        return contacts.getContacts().stream().mapToLong(Contact::getId).anyMatch(id -> id == contactId);
    }

    private Optional<Contact> getContactById(long contactId) {
        return contacts.getContacts().stream().filter(c -> c.getId() == contactId).findFirst();
    }
}