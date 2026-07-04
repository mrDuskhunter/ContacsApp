package ru.duskhunter.contacsapp.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.duskhunter.contacsapp.common.util.ServerResponseHelper;
import ru.duskhunter.contacsapp.common.util.Validator;
import ru.duskhunter.contacsapp.dto.ServerResponse;
import ru.duskhunter.contacsapp.dto.contact.ContactCreateDto;
import ru.duskhunter.contacsapp.dto.contact.ContactDto;
import ru.duskhunter.contacsapp.model.entity.Contact;
import ru.duskhunter.contacsapp.model.repository.ContactRepo;

import java.sql.SQLDataException;
import java.util.List;
import java.util.Optional;

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
    public ServerResponse<ContactDto> createContact(ContactCreateDto contactCreateDto) {
        Contact contact = mapper.map(contactCreateDto, Contact.class);
        List<String> entityErrors = validator.validate(contact);

        if (!entityErrors.isEmpty()) {
            return ServerResponseHelper.response(false, mapper.map(contact, ContactDto.class), HttpStatus.BAD_REQUEST,
                    entityErrors);
        }

        if (contacts.findByEmail(contact.getEmail()).isPresent()) {
            return getResponseContactWithThisAttributeAlreadyExists("email: " + contact.getEmail(), contact);
        }

        if (contacts.findByTelephone(contact.getTelephone()).isPresent()) {
            return getResponseContactWithThisAttributeAlreadyExists("telephone: " + contact.getTelephone(),contact);
        }

        try {
            Contact savedContact = contacts.saveAndFlush(contact);
            return ServerResponseHelper.response(true, mapper.map(savedContact, ContactDto.class), HttpStatus.CREATED, List.of());
        } catch (DataIntegrityViolationException e) {
            return ServerResponseHelper.response(false, mapper.map(contact, ContactDto.class), HttpStatus.CONFLICT,
                    List.of("Error creating contact"));
        }
    }

    @Override
    public ServerResponse<ContactDto> deleteContactById(long contactId) {
        Optional<Contact> contactOptional = contacts.findById(contactId);
        if (contactOptional.isEmpty()) {
            return getResponseContactWithIdNotExists(contactId, null);
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

        if (!contacts.existsById(id)) {
            return getResponseContactWithIdNotExists(id, contactDto);
        }

        Contact contact = mapper.map(contactDto, Contact.class);

        List<String> entityErrors = validator.validate(contact);

        if (!entityErrors.isEmpty()) {
            return ServerResponseHelper.response(false, mapper.map(contact, ContactDto.class), HttpStatus.BAD_REQUEST,
                    entityErrors);
        }

        String existingEmail = contacts.findEmailById(id).orElse(null);
        String existingTelephone = contacts.findTelephoneById(id).orElse(null);

        if (existingEmail != null && !existingEmail.equals(contact.getEmail())) {
            if (contacts.findByEmail(contact.getEmail()).isPresent()) {
                return getResponseContactWithThisAttributeAlreadyExists("email: " + contact.getEmail(), contact);
            }
        }

        if (existingTelephone != null && !existingTelephone.equals(contact.getTelephone())) {
            if (contacts.findByTelephone(contact.getTelephone()).isPresent()) {
                return getResponseContactWithThisAttributeAlreadyExists("telephone: " + contact.getTelephone(),contact);
            }
        }

        try {
            Contact savedContact = contacts.saveAndFlush(contact);
            return ServerResponseHelper.response(true, mapper.map(savedContact, ContactDto.class), HttpStatus.OK, List.of());
        } catch (DataIntegrityViolationException e) {
            return ServerResponseHelper.response(false, mapper.map(contact, ContactDto.class), HttpStatus.CONFLICT,
                    List.of("Error update contact"));
        }
    }

    private ServerResponse<ContactDto> getResponseContactWithThisAttributeAlreadyExists(String message, Contact contact) {
        return ServerResponseHelper.response(false, mapper.map(contact, ContactDto.class), HttpStatus.CONFLICT,
                List.of(String.format("Contact with this %s - already exist", message)));
    }

    private ServerResponse<ContactDto> getResponseContactWithIdNotExists(Long id, ContactDto contactDto) {
        return ServerResponseHelper.response(false, contactDto, HttpStatus.NO_CONTENT,
                List.of(String.format("The contact with id %s does not exist", id)));
    }
}