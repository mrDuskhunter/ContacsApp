package ru.duskhunter.contacsapp.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.duskhunter.contacsapp.common.util.ServerResponseHelper;
import ru.duskhunter.contacsapp.common.util.Validator;
import ru.duskhunter.contacsapp.dto.contact.ContactCreateDtoRequest;
import ru.duskhunter.contacsapp.dto.contact.ContactDto;
import ru.duskhunter.contacsapp.model.entity.Contact;
import ru.duskhunter.contacsapp.model.repository.ContactRepo;
import ru.duskhunter.contacsapp.dto.ServerResponse;

import java.sql.SQLDataException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {
    private final ContactRepo contacts;
    private final ModelMapper mapper;
    private final Validator validator;

    @Override
    public ServerResponse<List<ContactDto>> getContacts() {
        List<ContactDto> contactDtos = contacts.findAll()
                .stream()
                .map(contact -> mapper.map(contact, ContactDto.class))
                .toList();
        return ServerResponseHelper.response(true, contactDtos, HttpStatus.OK, List.of());
    }

    @Override
    public ResponseEntity<ServerResponse<ContactDto>> getContactById(long contactId) {
        Optional<Contact> contactOptional = contacts.findById(contactId);

        return contactOptional.map(contact -> ServerResponseHelper.responseEntity(true, mapper.map(contact, ContactDto.class), HttpStatus.OK, List.of()))
                .orElseGet(() -> ServerResponseHelper.responseEntity(false, null,
                        HttpStatus.NO_CONTENT, List.of(String.format("The contact with id %s does not exist", contactId))));
    }

    @Override
    public ServerResponse<ContactDto> createContact(ContactCreateDtoRequest contactCreateDtoRequest) {
        Contact contact = mapper.map(contactCreateDtoRequest, Contact.class);
        List<String> entityErrors = validator.validate(contact);

        if (!entityErrors.isEmpty()) {
            return ServerResponseHelper.response(false, mapper.map(contact, ContactDto.class), HttpStatus.BAD_REQUEST,
                    entityErrors);
        }

        try {
            Contact savedContact = contacts.saveAndFlush(contact);
            return ServerResponseHelper.response(true, mapper.map(savedContact, ContactDto.class), HttpStatus.CREATED, List.of());
        } catch (DataIntegrityViolationException e) {
            return ServerResponseHelper.response(false, mapper.map(contact, ContactDto.class), HttpStatus.CONFLICT,
                    List.of("Error creating a contact"));
        }
    }

    @Override
    public ServerResponse<ContactDto> deleteContactById(long contactId) {
        Optional<Contact> contactOptional = contacts.findById(contactId);
        if (contactOptional.isEmpty()) {
            return ServerResponseHelper.response(false, null, HttpStatus.NO_CONTENT,
                    List.of(String.format("The contact with id %s does not exist", contactId)));
        }

        ContactDto contactDto = mapper.map(contactOptional.get(), ContactDto.class);

        try {
            contacts.deleteById(contactId);
            Optional<Contact> optionalContact = contacts.findById(contactId);
            if (optionalContact.isPresent()) {
                throw new SQLDataException();
            }
        } catch (Exception e) {
            return ServerResponseHelper.response(false, contactDto, HttpStatus.INTERNAL_SERVER_ERROR,
                    List.of(String.format("Unexpected server error during deletion operation the contact with id %s", contactId)));
        }
        return ServerResponseHelper.response(true, contactDto, HttpStatus.OK, List.of());
    }

    @Override
    public ServerResponse<ContactDto> updateContact(ContactDto contactDto) {
        Long id = contactDto.getId();

        if (!matchId(id)) {
            return ServerResponseHelper.response(false, contactDto, HttpStatus.NO_CONTENT,
                    List.of(String.format("The contact with id %s does not exist", id)));
        }

        Contact contact = mapper.map(contactDto, Contact.class);

        List<String> entityErrors = validator.validate(contact);

        if (!entityErrors.isEmpty()) {
            return ServerResponseHelper.response(false, mapper.map(contact, ContactDto.class), HttpStatus.BAD_REQUEST,
                    entityErrors);
        }

        try {
            Contact savedContact = contacts.saveAndFlush(contact);
            return ServerResponseHelper.response(true, mapper.map(savedContact, ContactDto.class), HttpStatus.OK, List.of());
        } catch (DataIntegrityViolationException e) {
            return ServerResponseHelper.response(true, mapper.map(contact, ContactDto.class), HttpStatus.CONFLICT,
                    List.of("Error update a contact"));
        }
    }

    private boolean matchId(long contactId) {
        return contacts.findAll().stream().mapToLong(Contact::getId).anyMatch(id -> id == contactId);
    }
}