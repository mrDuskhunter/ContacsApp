package ru.duskhunter.contacsapp.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.duskhunter.contacsapp.common.security.CurrentUserProvider;
import ru.duskhunter.contacsapp.common.util.EmailNormalizer;
import ru.duskhunter.contacsapp.common.util.PhoneNormalizer;
import ru.duskhunter.contacsapp.common.util.ServerResponseHelper;
import ru.duskhunter.contacsapp.common.util.Validator;
import ru.duskhunter.contacsapp.dto.ServerResponse;
import ru.duskhunter.contacsapp.dto.contact.ContactCreateDto;
import ru.duskhunter.contacsapp.dto.contact.ContactDto;
import ru.duskhunter.contacsapp.exception.EntityConflictException;
import ru.duskhunter.contacsapp.exception.InternalServerException;
import ru.duskhunter.contacsapp.exception.NotFoundException;
import ru.duskhunter.contacsapp.exception.ValidationException;
import ru.duskhunter.contacsapp.model.entity.Contact;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;
import ru.duskhunter.contacsapp.model.repository.ContactRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {
    private final ContactRepo contacts;
    private final ModelMapper mapper;
    private final Validator validator;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ServerResponse<ContactDto> getContactById(long contactId) {
        Contact contact = findOwnedContactOrThrow(contactId, null);
        return ServerResponseHelper.response(true, mapper.map(contact, ContactDto.class), HttpStatus.OK, List.of());
    }

    @Transactional
    @Override
    public ServerResponse<ContactDto> createContact(ContactCreateDto contactCreateDto) {
        ContactOwner owner = currentUserProvider.getCurrentOwner();

        Contact contact = mapper.map(contactCreateDto, Contact.class);
        contact.setOwner(owner);
        contact.setTelephone(PhoneNormalizer.normalize(contact.getTelephone()));
        contact.setEmail(EmailNormalizer.normalize(contact.getEmail()));

        List<String> entityErrors = validator.validate(contact);
        if (!entityErrors.isEmpty()) {
            throw new ValidationException(entityErrors, mapper.map(contact, ContactDto.class));
        }

        assertAttributeNotTaken(
                contacts.findByOwnerIdAndEmail(contact.getOwner().getId(), contact.getEmail()).isPresent(),
                "email: " + contact.getEmail(), contact
        );

        assertAttributeNotTaken(
                contacts.findByOwnerIdAndTelephone(contact.getOwner().getId(), contact.getTelephone()).isPresent(),
                "telephone: " + contact.getTelephone(), contact
        );

        try {
            Contact savedContact = contacts.saveAndFlush(contact);
            return ServerResponseHelper.response(true, mapper.map(savedContact, ContactDto.class), HttpStatus.CREATED, List.of());
        } catch (DataIntegrityViolationException e) {
            throw new EntityConflictException("Error creating contact", mapper.map(contact, ContactDto.class));
        }
    }

    @Transactional
    @Override
    public ServerResponse<ContactDto> deleteContactById(long contactId) {
        Contact contact = findOwnedContactOrThrow(contactId, null);
        ContactDto contactDto = mapper.map(contact, ContactDto.class);

        contacts.deleteById(contactId);
        if (contacts.findById(contactId).isPresent()) {
            throw new InternalServerException(
                    String.format("Unexpected server error during deletion operation the contact with id %s", contactId),
                    contactDto);
        }

        return ServerResponseHelper.response(true, contactDto, HttpStatus.OK, List.of());
    }

    @Transactional
    @Override
    public ServerResponse<ContactDto> updateContact(ContactDto contactDto) {
        Long id = contactDto.getId();
        Contact existingContact = findOwnedContactOrThrow(id,contactDto);

        Contact contact = mapper.map(contactDto, Contact.class);
        contact.setOwner(existingContact.getOwner());
        contact.setTelephone(PhoneNormalizer.normalize(contact.getTelephone()));
        contact.setEmail(EmailNormalizer.normalize(contact.getEmail()));

        List<String> entityErrors = validator.validate(contact);
        if (!entityErrors.isEmpty()) {
            throw new ValidationException(entityErrors, mapper.map(contact, ContactDto.class));
        }

        long ownerId = existingContact.getOwner().getId();

        String existingEmail = contacts.findEmailById(id).orElse(null);
        if (existingEmail != null && !existingEmail.equals(contact.getEmail())) {
            assertAttributeNotTaken(
                    contacts.findByOwnerIdAndEmail(ownerId, contact.getEmail()).isPresent(),
                    "email: " + contact.getEmail(), contact
            );
        }

        String existingTelephone = contacts.findTelephoneById(id).orElse(null);
        if (existingTelephone != null && !existingTelephone.equals(contact.getTelephone())) {
            assertAttributeNotTaken(
                    contacts.findByOwnerIdAndTelephone(ownerId, contact.getTelephone()).isPresent(),
                    "telephone: " + contact.getTelephone(), contact
            );
        }

        try {
            Contact savedContact = contacts.saveAndFlush(contact);
            return ServerResponseHelper.response(true, mapper.map(savedContact, ContactDto.class), HttpStatus.OK, List.of());
        } catch (DataIntegrityViolationException e) {
            throw new EntityConflictException("Error update contact", mapper.map(contact, ContactDto.class));
        } catch (ConstraintViolationException cve) {
            List<String> errors = cve.getConstraintViolations().stream().map(ConstraintViolation::getMessage).toList();
            throw new ValidationException(errors, mapper.map(contact, ContactDto.class));
        }
    }

    @Override
    public ServerResponse<List<ContactDto>> getContactsForCurrentOwner() {
        long ownerId = currentUserProvider.getCurrentOwner().getId();

        List<ContactDto> contactDtos = contacts.findAllByOwnerId(ownerId)
                .stream()
                .map(contact -> mapper.map(contact, ContactDto.class))
                .toList();

        return ServerResponseHelper.response(true, contactDtos, HttpStatus.OK, List.of());
    }

    private Contact findOwnedContactOrThrow(long contactId, ContactDto dtoForException) {
        long currentOwnerId = currentUserProvider.getCurrentOwner().getId();

        Contact contact = contacts.findById(contactId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("The contact with id %s does not exist", contactId),dtoForException));

        if (contact.getOwner().getId() != currentOwnerId) {
            throw new NotFoundException(
                    String.format("The contact with id %s does not exist", contactId), dtoForException);
        }
        return contact;
    }

    private void assertAttributeNotTaken(boolean alreadyExists, String message, Contact contact) {
        if (alreadyExists) {
            throw new EntityConflictException(String.format("Contact with this %s - already exist", message),
                    mapper.map(contact, ContactDto.class));
        }
    }
}