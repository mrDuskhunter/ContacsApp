package ru.duskhunter.contacsapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.duskhunter.contacsapp.common.util.Validator;
import ru.duskhunter.contacsapp.dto.ServerResponse;
import ru.duskhunter.contacsapp.dto.contact.ContactCreateDto;
import ru.duskhunter.contacsapp.dto.contact.ContactDto;
import ru.duskhunter.contacsapp.model.Role;
import ru.duskhunter.contacsapp.model.entity.Contact;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;
import ru.duskhunter.contacsapp.model.repository.ContactRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
    1. getContacts
    1.1 get list contacts
        1.1.1 boolean success == true
        1.1.2 HttpStatus httpStatus == HttpStatus.ok
        1.1.3 List<String> errorMessages -> size == 0
        1.1.4 List<String> contacts -> size == 10 and value.equals
        1.1.5 contacts.findAll -> one called
    1.2 no contacts -> empty list
        1.2.1 boolean success == true
        1.2.2 HttpStatus httpStatus == HttpStatus.ok
        1.2.3 List<String> errorMessages -> size == 0
        1.2.4 List<String> contacts -> size == 0
        1.2.5 contacts.findAll -> one called
    2. getContactById
    2.1 get contact
        2.1.1 boolean success == true
        2.1.2 HttpStatus httpStatus == HttpStatus.ok
        2.1.3 HttpStatusEntity == HttpStatus.ok
        2.1.4 List<String> errorMessages -> size == 0
        2.1.5 result.contact id == 0
        2.1.6 result.contact firstName == Name0
        2.1.7 result.contact lastName == lastName0
        2.1.8 result.contact telephone == +7 234 567 89 20
        2.1.9 result.contact email == email0@mail.ru
        2.1.10 contacts.findById -> one called
    2.2 no contact with this id -> httpStatus 204
        2.2.1 boolean success == false
        2.2.2 HttpStatus httpStatus == HttpStatus.NO_CONTENT
        2.2.3 HttpStatusEntity == HttpStatus.NO_CONTENT
        2.2.4 List<String> errorMessages -> size == 1
        2.2.5 errorMessages -> "The contact with id 13 does not exist"
        2.2.6 result is null
        2.2.7 contacts.findById -> one called
    3. createContact
    3.1 create
        3.1.1 boolean success == true
        3.1.2 HttpStatus httpStatus == HttpStatus.created
        3.1.3 List<String> errorMessages -> size == 0
        3.1.4 result.contact id == 0
        3.1.5 result.contact firstName == Name0
        3.1.6 result.contact lastName == lastName0
        3.1.7 result.contact telephone == +7 123 456-78-90
        3.1.8 result.contact email == email0@mail.ru
        3.1.9 contacts.saveAndFlush -> one called
    3.2 create
        3.2.1 boolean success == true
        3.2.2 HttpStatus httpStatus == HttpStatus.created
        3.2.3 List<String> errorMessages -> size == 0
        3.2.4 result.contact id == 10
        3.2.5 result.contact firstName == Name0
        3.2.6 result.contact lastName == lastName0
        3.2.7 result.contact telephone == +7 123 456-78-90
        3.2.8 result.contact email == email0@mail.ru
        3.2.9 contacts.saveAndFlush -> one called
        3.2.10 validator.validate -> one called
        3.2.11 contacts.findByEmail -> one called
        3.2.12 contacts.findByTelephone -> one called
    3.3 create when Error creating contact
        3.3.1 boolean success == false
        3.3.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        3.3.3 List<String> errorMessages -> size == 1
        3.3.4 errorMessages -> "Error creating contact"
        3.3.5 result equals ContactDto
        3.3.6 contacts.saveAndFlush -> one called
        3.3.7 validator.validate -> one called
        3.3.8 contacts.findByEmail -> one called
        3.3.9 contacts.findByTelephone -> one called
    3.4 create When email Already Exist
        3.4.1 boolean success == false
        3.4.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        3.4.3 List<String> errorMessages -> size == 1
        3.4.4 errorMessages -> "Contact with this email: {email} - already exist"
        3.4.5 result equals ContactDto
        3.4.6 contacts.saveAndFlush -> no called
        3.4.7 validator.validate -> one called
        3.4.8 contacts.findByEmail -> one called
        3.4.9 contacts.findByTelephone -> no called
    3.5 create When telephone Already Exist
        3.4.1 boolean success == false
        3.4.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        3.4.3 List<String> errorMessages -> size == 1
        3.4.4 errorMessages -> "Contact with this telephone: {telephone} - already exist"
        3.4.5 result equals ContactDto
        3.4.6 contacts.saveAndFlush -> no called
        3.4.7 validator.validate -> one called
        3.4.8 contacts.findByEmail -> one called
        3.4.9 contacts.findByTelephone -> one called
    4 delete
    4.1 delete test is unsuccessfully
        4.1.1 boolean success == false
        4.1.2 HttpStatus httpStatus == HttpStatus.INTERNAL_SERVER_ERROR
        4.1.3 List<String> errorMessages -> size == 1
        4.1.4 errorMessages -> "Unexpected server error during deletion operation the contact with id 13"
        4.1.5 result equals ContactDto
        4.1.6 contacts.findById -> two called
    4.2 positive case delete
        4.2.1 boolean success == true
        4.2.2 HttpStatus httpStatus == HttpStatus.OK
        4.2.3 List<String> errorMessages -> size == 0
        4.2.4 result equals ContactDto
        4.2.5 contacts.findById -> two called
        4.2.6 contacts.deleteById -> one called
    4.3 delete test is unsuccessfully -> The contact with id 13 does not exist
        4.3.1 boolean success == false
        4.3.2 HttpStatus httpStatus == HttpStatus.NO_CONTENT
        4.3.3 List<String> errorMessages -> size == 1
        4.3.4 errorMessages -> "The contact with id 13 does not exist"
        4.3.5 result is null
        4.3.6 contacts.findById -> one called
        4.3.7 contacts.deleteById -> no called
    5 update
    5.1 update test is unsuccessfully - error update
        5.1.1 boolean success == false
        5.1.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        5.1.3 List<String> errorMessages -> size == 1
        5.1.4 errorMessages -> "Error update contact"
        5.1.5 result equals ContactDto
        5.1.6 contacts.existsById -> one called
        5.1.7 contacts.validate -> one called
        5.1.8 contacts.findTelephoneById -> one called
        5.1.9 contacts.findEmailById(id) -> one called
        5.1.10 contacts.findByEmail -> one called
        5.1.11 contacts.findByTelephone -> one called
        5.1.12 contacts.saveAndFlush -> one called
    5.2 update test is unsuccessful - email already exist
        5.2.1 boolean success == false
        5.2.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        5.2.3 List<String> errorMessages -> size == 1
        5.2.4 errorMessages -> "Contact with this email - already exist"
        5.2.5 result equals ContactDto
        5.2.6 contacts.validate -> one called
        5.2.7 contacts.findEmailById -> one called
        5.2.8 contacts.findByEmail -> one called
        5.2.9 contacts.findTelephoneById -> one called
        5.2.10 contacts.saveAndFlush -> no called
    5.3 update test is unsuccessfully - telephone already exist
        5.3.1 boolean success == false
        5.3.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        5.3.3 List<String> errorMessages -> size == 1
        5.3.4 errorMessages -> "Contact with this telephone: {telephone} - already exists"
        5.3.5 result equals contactDto
        5.3.6 contacts.validate -> one called
        5.3.7 contacts.findEmailById -> one called
        5.3.8 contacts.findByEmail -> one called
        5.3.9 contacts.findTelephoneById -> one called
        5.3.10 contacts.findByTelephone -> one called
        5.3.11 contacts.saveAndFlush -> no called
    5.4 positive case update first and last name
        5.4.1 boolean success == true
        5.4.2 HttpStatus httpStatus == HttpStatus.OK
        5.4.3 List<String> errorMessages -> size == 0
        5.4.4 result equals ContactDto
        5.4.6 contacts.validate -> one called
        5.4.7 contacts.findEmailById -> one called
        5.4.8 contacts.findTelephoneById -> one called
        5.4.9 contacts.findByEmail -> no called
        5.4.10 contacts.findByTelephone -> no called
        5.4.11 contacts.saveAndFlush -> one called
    5.5 positive case update when update Email
        5.5.1 boolean success == true
        5.5.2 HttpStatus httpStatus == HttpStatus.OK
        5.5.3 List<String> errorMessages -> size == 0
        5.5.4 result equals ContactDto
        5.5.5 contacts.validate -> one called
        5.5.6 contacts.findEmailById -> one called
        5.5.7 contacts.findTelephoneById -> one called
        5.5.8 contacts.findByEmail -> one called
        5.5.9 contacts.findByTelephone -> no called
        5.5.10 contacts.saveAndFlush -> one called
    5.6 positive case update when update Telephone
        5.6.1 boolean success == true
        5.6.2 HttpStatus httpStatus == HttpStatus.OK
        5.6.3 List<String> errorMessages -> size == 0
        5.6.4 result equals ContactDto
        5.6.5 contacts.validate -> one called
        5.6.6 contacts.findEmailById -> one called
        5.6.7 contacts.findTelephoneById -> one called
        5.6.8 contacts.findByEmail -> no called
        5.6.9 contacts.findByTelephone -> one called
        5.6.10 contacts.saveAndFlush -> one called
    5.7 update test is unsuccessful -> The contact with id 14 does not exist
        5.7.1 boolean success == false
        5.7.2 HttpStatus httpStatus == HttpStatus.NO_CONTENT
        5.7.3 List<String> errorMessages -> size == 1
        5.7.4 errorMessages -> "The contact with id 14 does not exist"
        5.7.5 result equals ContactDto
        5.7.6 contacts.existsById -> one called
        5.7.7 contacts.findEmailById -> no called
        5.7.8 contacts.findTelephoneById -> no called
        5.7.9 contacts.findByEmail -> no called
        5.7.10 contacts.findByTelephone -> no called
        5.7.11 contacts.saveAndFlush -> no called
 */

@ExtendWith({MockitoExtension.class})
class ContactServiceImplTest {
    @Mock
    private ContactRepo contacts;
    private Validator validator;
    private final ModelMapper modelMapper = new ModelMapper();
    private ContactServiceImpl contactService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jakarta.validation.Validator jakartaValidator =
                jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        validator = new Validator(jakartaValidator);

        contactService = new ContactServiceImpl(contacts, modelMapper, validator);
    }

    @Test
    void shouldGetContacts() {
        /*
        1.1 get list contacts
        1.1.1 boolean success == true
        1.1.2 HttpStatus httpStatus == HttpStatus.ok
        1.1.3 List<String> errorMessages -> size == 0
        1.1.4 List<String> contacts -> size == 10 and value.equals
        1.1.5 contacts.findAll -> one called
        */
        when(contacts.findAll()).thenReturn(initContacts());

        ServerResponse<List<ContactDto>> response = contactService.getContacts();

        verify(contacts, times(1)).findAll();

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());
        assertEquals(10, response.getResult().size());
        assertEquals(mapToContactDto(initContacts()), response.getResult());
    }

    @Test
    void shouldGetEmptyListContacts() {
        /*
        1.2 get list contacts
        1.2.1 boolean success == true
        1.2.2 HttpStatus httpStatus == HttpStatus.ok
        1.2.3 List<String> errorMessages -> size == 0
        1.2.4 List<String> contacts -> size == 0
        1.2.5 contacts.findAll -> one called
        */
        when(contacts.findAll()).thenReturn(List.of());

        ServerResponse<List<ContactDto>> response = contactService.getContacts();

        verify(contacts, times(1)).findAll();

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());
        assertEquals(0, response.getResult().size());
    }

    @Test
    void shouldGetContactById() {
        /*
        2.1.1 boolean success == true
        2.1.2 HttpStatus httpStatus == HttpStatus.ok
        2.1.3 HttpStatusEntity == HttpStatus.ok
        2.1.4 List<String> errorMessages -> size == 0
        2.1.5 result.contact id == 0
        2.1.6 result.contact firstName == Name0
        2.1.7 result.contact lastName == lastName0
        2.1.8 result.contact telephone == +7 (234) 567 89 20
        2.1.9 result.contact email == email0@mail.ru
        2.1.10 contacts.findById -> one called
        */
        when(contacts.findById(anyLong())).thenReturn(Optional.of(initContacts().get(0)));

        ResponseEntity<ServerResponse<ContactDto>> response = contactService.getContactById(0);

        verify(contacts, times(1)).findById(anyLong());

        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK, response.getBody().getHttpStatus());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getErrorMessages().size());

        ContactDto contact = response.getBody().getResult();

        assertEquals(0, contact.getId());
        assertEquals("Name0", contact.getFirstName());
        assertEquals("lastName0", contact.getLastName());
        assertEquals("+7 234 567 89 20", contact.getTelephone());
        assertEquals("email0@mail.ru", contact.getEmail());
    }

    @Test
    void shouldGetHttpStatus204() {
        /*
        2.2.1 boolean success == false
        2.2.2 HttpStatus httpStatus == HttpStatus.NO_CONTENT
        2.2.3 HttpStatusEntity == HttpStatus.NO_CONTENT
        2.2.4 List<String> errorMessages -> size == 1
        2.2.5 errorMessages -> "The contact with id 13 does not exist"
        2.2.6 result is null
        2.2.7 contacts.findById -> one called
         */
        when(contacts.findById(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<ServerResponse<ContactDto>> response = contactService.getContactById(13);

        verify(contacts, times(1)).findById(anyLong());

        assertFalse(response.getBody().isSuccess());
        assertEquals(HttpStatus.NO_CONTENT, response.getBody().getHttpStatus());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(1, response.getBody().getErrorMessages().size());
        assertEquals("The contact with id 13 does not exist", response.getBody().getErrorMessages().get(0));
        assertNull(response.getBody().getResult());
    }

    @Test
    void shouldCreateContactWhenContactsIsEmpty() {
        /*
        3.1 create
            3.1.1 boolean success == true
            3.1.2 HttpStatus httpStatus == HttpStatus.created
            3.1.3 List<String> errorMessages -> size == 0
            3.1.4 result.contact id == 1L
            3.1.5 result.contact firstName == Name0
            3.1.6 result.contact lastName == lastName0
            3.1.7 result.contact telephone == +7 (123) 456-78-90
            3.1.8 result.contact email == email0@mail.ru
            3.1.9 contacts.saveAndFlush -> one called
         */

        String firstName = "Name0";
        String lastName = "lastName0";
        String telephone = "+7 234 567 89 20";
        String email = "email0@mail.ru";

        ContactCreateDto dto = getContactCreateDto(firstName, lastName, telephone, email);

        when(contacts.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(contacts.findByTelephone(any(String.class))).thenReturn(Optional.empty());
        when(contacts.saveAndFlush(any(Contact.class)))
                .thenReturn(new Contact(1L, firstName, lastName, telephone, email, createContactOwner(), LocalDateTime.now()));

        ServerResponse<ContactDto> response = contactService.createContact(dto);

        verify(contacts, times(1)).findByEmail(any(String.class));
        verify(contacts, times(1)).findByTelephone(any(String.class));
        verify(contacts, times(1)).saveAndFlush(any(Contact.class));

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.CREATED, response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());

        ContactDto contact = response.getResult();
        assertEquals(1L, contact.getId());
        assertEquals(firstName, contact.getFirstName());
        assertEquals(lastName, contact.getLastName());
        assertEquals(telephone, contact.getTelephone());
        assertEquals(email, contact.getEmail());
    }

    @Test
    void ShouldCreateContactWhenContactsIsNonEmpty() {
        /*
        3.2 create
            3.2.1 boolean success == true
            3.2.2 HttpStatus httpStatus == HttpStatus.created
            3.2.3 List<String> errorMessages -> size == 0
            3.2.4 result.contact id == 10
            3.2.5 result.contact firstName == Name0
            3.2.6 result.contact lastName == lastName0
            3.2.7 result.contact telephone == +7 (123) 456-78-90
            3.2.8 result.contact email == email0@mail.ru
            3.2.9 contacts.saveAndFlush -> one called
         */

        String firstName = "Name0";
        String lastName = "lastName0";
        String telephone = "+7 234 567 89 20";
        String email = "email0@mail.ru";

        when(contacts.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(contacts.findByTelephone(any(String.class))).thenReturn(Optional.empty());
        when(contacts.saveAndFlush(any(Contact.class)))
                .thenReturn(new Contact(10L, firstName, lastName, telephone, email, createContactOwner(), LocalDateTime.now()));

        ContactCreateDto dto = getContactCreateDto(firstName, lastName, telephone, email);

        ServerResponse<ContactDto> response = contactService.createContact(dto);

        verify(contacts, times(1)).findByEmail(any(String.class));
        verify(contacts, times(1)).findByTelephone(any(String.class));
        verify(contacts, times(1)).saveAndFlush(any(Contact.class));

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.CREATED, response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());

        ContactDto contact = response.getResult();
        assertEquals(10L, contact.getId());
        assertEquals(firstName, contact.getFirstName());
        assertEquals(lastName, contact.getLastName());
        assertEquals(telephone, contact.getTelephone());
        assertEquals(email, contact.getEmail());
    }

    @Test
    void shouldNotCreateContactWhenErrorCreatingOwner() {
        /*
        3.3.1 boolean success == false
        3.3.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        3.3.3 List<String> errorMessages -> size == 1
        3.3.4 errorMessages -> "Error creating contact"
        3.3.5 result equals ContactDto
        3.3.6 contacts.saveAndFlush -> one called
        3.3.7 validator.validate -> one called
        3.3.8 contacts.findByEmail -> one called
        3.3.8 contacts.findByTelephone -> one called
    */
        Contact contact = initContacts().get(0);

        ContactCreateDto dto = getContactCreateDto(
                contact.getFirstName(),
                contact.getLastName(),
                contact.getTelephone(),
                contact.getEmail()
        );

        when(contacts.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(contacts.findByTelephone(any(String.class))).thenReturn(Optional.empty());
        when(contacts.saveAndFlush(any(Contact.class))).thenThrow(DataIntegrityViolationException.class);

        ServerResponse<ContactDto> response = contactService.createContact(dto);

        verify(contacts, times(1)).findByEmail(any(String.class));
        verify(contacts, times(1)).findByTelephone(any(String.class));
        verify(contacts, times(1)).saveAndFlush(any(Contact.class));

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.CONFLICT, response.getHttpStatus());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("Error creating contact", response.getErrorMessages().get(0));
        assertEquals(modelMapper.map(dto,ContactDto.class), response.getResult());
    }

    @Test
    void shouldNotCreateContactWhenEmailAlreadyExist() {
        /*
        3.4.1 boolean success == false
        3.4.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        3.4.3 List<String> errorMessages -> size == 1
        3.4.4 errorMessages -> "Contact with this email: email0@mail.ru - already exist"
        3.4.5 result equals ContactDto
        3.4.6 contacts.saveAndFlush -> no called
        3.4.7 validator.validate -> one called
        3.4.8 contacts.findByEmail -> one called
        3.4.9 contacts.findByTelephone -> no called
        */
        Contact contact = initContacts().get(0);

        ContactCreateDto dto = getContactCreateDto(
                contact.getFirstName(),
                contact.getLastName(),
                contact.getTelephone(),
                contact.getEmail()
        );

        when(contacts.findByEmail(any(String.class))).thenReturn(Optional.of(contact));

        ServerResponse<ContactDto> response = contactService.createContact(dto);

        verify(contacts, times(1)).findByEmail(any(String.class));
        verify(contacts, never()).findByTelephone(any(String.class));
        verify(contacts, never()).saveAndFlush(any(Contact.class));

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.CONFLICT, response.getHttpStatus());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("Contact with this email: email0@mail.ru - already exist", response.getErrorMessages().get(0));
        assertEquals(modelMapper.map(dto, ContactDto.class), response.getResult());
    }

    @Test
    void shouldNotCreateContactWhenTelephoneAlreadyExist() {
        /*
        3.5.1 boolean success == false
        3.5.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        3.5.3 List<String> errorMessages -> size == 1
        3.5.4 errorMessages -> "Contact with this telephone: +7 234 567 89 20 - already exist"
        3.5.5 result equals ContactDto
        3.5.6 contacts.saveAndFlush -> no called
        3.5.7 validator.validate -> one called
        3.5.8 contacts.findByEmail -> one called
        3.5.9 contacts.findByTelephone -> one called
        */
        Contact contact = initContacts().get(0);

        ContactCreateDto dto = getContactCreateDto(
                contact.getFirstName(),
                contact.getLastName(),
                contact.getTelephone(),
                contact.getEmail()
        );

        when(contacts.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(contacts.findByTelephone(any(String.class))).thenReturn(Optional.of(contact));

        ServerResponse<ContactDto> response = contactService.createContact(dto);

        verify(contacts, times(1)).findByEmail(any(String.class));
        verify(contacts, times(1)).findByTelephone(any(String.class));
        verify(contacts, never()).saveAndFlush(any(Contact.class));

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.CONFLICT, response.getHttpStatus());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("Contact with this telephone: +7 234 567 89 20 - already exist", response.getErrorMessages().get(0));
        assertEquals(modelMapper.map(dto, ContactDto.class), response.getResult());
    }

    @Test
    void shouldDeleteContactByIdUnsuccessfully() {
        /*
        4.1.1 boolean success == false
        4.1.2 HttpStatus httpStatus == HttpStatus.INTERNAL_SERVER_ERROR
        4.1.3 List<String> errorMessages -> size == 1
        4.1.4 errorMessages -> "Unexpected server error during deletion operation the contact with id 13"
        4.1.5 result equals ContactDto
        4.1.6 contacts.findById -> two called
        */
        Contact contact = initContacts().get(0);

        when(contacts.findById(anyLong())).thenReturn(Optional.of(contact));

        ServerResponse<ContactDto> response = contactService.deleteContactById(13);

        verify(contacts, times(2)).findById(anyLong());

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
        assertEquals(mapToContactDto(List.of(contact)).get(0), response.getResult());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("Unexpected server error during deletion operation the contact with id 13",
                response.getErrorMessages().get(0));
    }

    @Test
    void shouldDeleteContactByIdSuccessfully() {
        /*
        4.2.1 boolean success == true
        4.2.2 HttpStatus httpStatus == HttpStatus.OK
        4.2.3 List<String> errorMessages -> size == 0
        4.2.4 result equals ContactDto
        4.2.5 contacts.findById -> two called
        4.2.6 contacts.deleteById -> one called
        */
        Contact contact = initContacts().get(0);
        ContactDto expectedDto = mapToContactDto(List.of(contact)).get(0);

        when(contacts.findById(7L))
                .thenReturn(Optional.of(contact))
                .thenReturn(Optional.empty());

        doNothing().when(contacts).deleteById(7L);

        ServerResponse<ContactDto> response = contactService.deleteContactById(7L);

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(expectedDto, response.getResult());
        assertTrue(response.getErrorMessages().isEmpty());

        verify(contacts, times(2)).findById(7L);
        verify(contacts, times(1)).deleteById(7L);
    }

    @Test
    void shouldNotDeleteContactWhenIdNotExist() {
        /*
        4.3.1 boolean success == false
        4.3.2 HttpStatus httpStatus == HttpStatus.NO_CONTENT
        4.3.3 List<String> errorMessages -> size == 1
        4.3.4 errorMessages -> "The contact with id 13 does not exist"
        4.3.5 result is null
        4.3.6 contacts.findById -> one called
        4.3.7 contacts.deleteById -> no called
        */
        when(contacts.findById(anyLong())).thenReturn(Optional.empty());

        ServerResponse<ContactDto> response = contactService.deleteContactById(13);

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("The contact with id 13 does not exist", response.getErrorMessages().get(0));
        assertNull(response.getResult());

        verify(contacts, times(1)).findById(anyLong());
        verify(contacts, never()).deleteById(anyLong());
    }

    @Test
    void shouldGetExceptionErrorUpdatingContact() {
        /*
        5.1.1 boolean success == false
        5.1.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        5.1.3 List<String> errorMessages -> size == 1
        5.1.4 errorMessages -> "Error update contact"
        5.1.5 result equals ContactDto
        5.1.8 contacts.findTelephoneById -> one called
        5.1.9 contacts.findEmailById -> one called
        5.1.12 contacts.saveAndFlush -> one called
        */
        Contact contact = initContacts().get(0);
        ContactDto dto = modelMapper.map(contact, ContactDto.class);

        when(contacts.existsById(anyLong())).thenReturn(true);
        when(contacts.findEmailById(anyLong())).thenReturn(Optional.of(contact.getEmail()));
        when(contacts.findTelephoneById(anyLong())).thenReturn(Optional.of(contact.getTelephone()));
        when(contacts.saveAndFlush(any(Contact.class))).thenThrow(DataIntegrityViolationException.class);

        ServerResponse<ContactDto> response = contactService.updateContact(dto);

        verify(contacts, times(1)).existsById(dto.getId());
        verify(contacts, times(1)).findEmailById(anyLong());
        verify(contacts, times(1)).findTelephoneById(anyLong());
        verify(contacts, times(1)).saveAndFlush(any(Contact.class));

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.CONFLICT, response.getHttpStatus());
        assertEquals(mapToContactDto(List.of(contact)).get(0), response.getResult());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("Error update contact", response.getErrorMessages().get(0));
    }

    @Test
    void shouldGetExceptionEmailAlreadyExistWhenUpdatingContact() {
        /*
        5.2.1 boolean success == false
        5.2.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        5.2.3 List<String> errorMessages -> size == 1
        5.2.4 errorMessages -> "Contact with this email: email0@mail.ru - already exist"
        5.2.5 result equals ContactDto
        5.2.6 contacts.validate -> one called
        5.2.7 contacts.findEmailById -> one called
        5.2.8 contacts.findByEmail -> one called
        5.2.9 contacts.findTelephoneById -> one called
        5.2.10 contacts.saveAndFlush -> no called
        */
        Contact contact = initContacts().get(0);
        ContactDto dto = modelMapper.map(contact, ContactDto.class);

        when(contacts.existsById(dto.getId())).thenReturn(true);
        when(contacts.findEmailById(anyLong())).thenReturn(Optional.of("other@mail.ru"));
        when(contacts.findByEmail(any(String.class))).thenReturn(Optional.of(contact));
        when(contacts.findTelephoneById(anyLong())).thenReturn(Optional.of(contact.getTelephone()));

        ServerResponse<ContactDto> response = contactService.updateContact(dto);

        verify(contacts, times(1)).existsById(dto.getId());
        verify(contacts, times(1)).findEmailById(anyLong());
        verify(contacts, times(1)).findByEmail(any(String.class));
        verify(contacts, times(1)).findTelephoneById(anyLong());
        verify(contacts, never()).saveAndFlush(any(Contact.class));

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.CONFLICT, response.getHttpStatus());
        assertEquals(mapToContactDto(List.of(contact)).get(0), response.getResult());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("Contact with this email: email0@mail.ru - already exist", response.getErrorMessages().get(0));
    }

    @Test
    void shouldGetExceptionTelephoneAlreadyExistWhenUpdatingContact() {
        /*
        5.3.1 boolean success == false
        5.3.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        5.3.3 List<String> errorMessages -> size == 1
        5.3.4 errorMessages -> "Contact with this telephone: +7 234 567 89 20 - already exist"
        5.3.5 result equals ContactDto
        5.3.6 contacts.validate -> one called
        5.3.7 contacts.findEmailById -> one called
        5.3.8 contacts.findByEmail -> one called
        5.3.9 contacts.findTelephoneById -> one called
        5.3.10 contacts.findByTelephone -> one called
        5.3.11 contacts.saveAndFlush -> no called
        */
        Contact contact = initContacts().get(0);
        ContactDto dto = modelMapper.map(contact, ContactDto.class);

        when(contacts.existsById(dto.getId())).thenReturn(true);
        when(contacts.findEmailById(anyLong())).thenReturn(Optional.of(contact.getEmail()));
        when(contacts.findTelephoneById(anyLong())).thenReturn(Optional.of("other_telephone"));
        when(contacts.findByTelephone(any(String.class))).thenReturn(Optional.of(contact));

        ServerResponse<ContactDto> response = contactService.updateContact(dto);

        verify(contacts, times(1)).existsById(dto.getId());
        verify(contacts, times(1)).findEmailById(anyLong());
        verify(contacts, times(1)).findTelephoneById(anyLong());
        verify(contacts, times(1)).findByTelephone(any(String.class));
        verify(contacts, never()).saveAndFlush(any(Contact.class));

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.CONFLICT, response.getHttpStatus());
        assertEquals(mapToContactDto(List.of(contact)).get(0), response.getResult());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("Contact with this telephone: +7 234 567 89 20 - already exist", response.getErrorMessages().get(0));
    }

    @Test
    void shouldUpdateContactSuccessfully() {
        /*
        5.4.1 boolean success == true
        5.4.2 HttpStatus httpStatus == HttpStatus.OK
        5.4.3 List<String> errorMessages -> size == 0
        5.4.4 result equals ContactDto
        5.4.6 contacts.validate -> one called
        5.4.7 contacts.findEmailById -> one called
        5.4.8 contacts.findTelephoneById -> one called
        5.4.9 contacts.saveAndFlush -> one called
        */
        Contact contact = initContacts().get(0);
        ContactDto dto = modelMapper.map(contact, ContactDto.class);

        when(contacts.existsById(dto.getId())).thenReturn(true);
        when(contacts.findEmailById(anyLong())).thenReturn(Optional.of(contact.getEmail()));
        when(contacts.findTelephoneById(anyLong())).thenReturn(Optional.of(contact.getTelephone()));
        when(contacts.saveAndFlush(any(Contact.class))).thenReturn(contact);

        ServerResponse<ContactDto> response = contactService.updateContact(dto);

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(modelMapper.map(contact, ContactDto.class), response.getResult());
        assertTrue(response.getErrorMessages().isEmpty());

        verify(contacts, times(1)).existsById(dto.getId());
        verify(contacts, times(1)).findEmailById(anyLong());
        verify(contacts, times(1)).findTelephoneById(anyLong());
        verify(contacts, times(1)).saveAndFlush(any(Contact.class));
    }

    @Test
    void shouldUpdateContactEmailSuccessfully() {
        /*
        5.5.1 boolean success == true
        5.5.2 HttpStatus httpStatus == HttpStatus.OK
        5.5.3 List<String> errorMessages -> size == 0
        5.5.4 result equals ContactDto
        5.5.5 contacts.validate -> one called
        5.5.6 contacts.findEmailById -> one called
        5.5.7 contacts.findTelephoneById -> one called
        5.5.8 contacts.findByEmail -> one called
        5.5.9 contacts.findByTelephone -> no called
        5.5.10 contacts.saveAndFlush -> one called
        */
        Contact contact = initContacts().get(0);
        ContactDto dto = modelMapper.map(contact, ContactDto.class);
        String newEmail = "newemail@mail.ru";
        dto.setEmail(newEmail);

        when(contacts.existsById(dto.getId())).thenReturn(true);
        when(contacts.findEmailById(anyLong())).thenReturn(Optional.of(contact.getEmail()));
        when(contacts.findTelephoneById(anyLong())).thenReturn(Optional.of(contact.getTelephone()));
        when(contacts.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(contacts.saveAndFlush(any(Contact.class))).thenReturn(contact);

        ServerResponse<ContactDto> response = contactService.updateContact(dto);

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(modelMapper.map(contact, ContactDto.class), response.getResult());
        assertTrue(response.getErrorMessages().isEmpty());

        verify(contacts, times(1)).existsById(dto.getId());
        verify(contacts, times(1)).findEmailById(anyLong());
        verify(contacts, times(1)).findTelephoneById(anyLong());
        verify(contacts, times(1)).findByEmail(any(String.class));
        verify(contacts, never()).findByTelephone(any(String.class));
        verify(contacts, times(1)).saveAndFlush(any(Contact.class));
    }

    @Test
    void shouldUpdateContactTelephoneSuccessfully() {
        /*
        5.6.1 boolean success == true
        5.6.2 HttpStatus httpStatus == HttpStatus.OK
        5.6.3 List<String> errorMessages -> size == 0
        5.6.4 result equals ContactDto
        5.6.5 contacts.validate -> one called
        5.6.6 contacts.findEmailById -> one called
        5.6.7 contacts.findTelephoneById -> one called
        5.6.8 contacts.findByEmail -> no called
        5.6.9 contacts.findByTelephone -> one called
        5.6.10 contacts.saveAndFlush -> one called
        */
        Contact contact = initContacts().get(0);
        ContactDto dto = modelMapper.map(contact, ContactDto.class);
        String newTelephone = "+7 999 888 77 66";
        dto.setTelephone(newTelephone);

        when(contacts.existsById(dto.getId())).thenReturn(true);
        when(contacts.findEmailById(anyLong())).thenReturn(Optional.of(contact.getEmail()));
        when(contacts.findTelephoneById(anyLong())).thenReturn(Optional.of(contact.getTelephone()));
        when(contacts.findByTelephone(any(String.class))).thenReturn(Optional.empty());
        when(contacts.saveAndFlush(any(Contact.class))).thenReturn(contact);

        ServerResponse<ContactDto> response = contactService.updateContact(dto);

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(modelMapper.map(contact, ContactDto.class), response.getResult());
        assertTrue(response.getErrorMessages().isEmpty());

        verify(contacts, times(1)).existsById(dto.getId());
        verify(contacts, times(1)).findEmailById(anyLong());
        verify(contacts, times(1)).findTelephoneById(anyLong());
        verify(contacts, never()).findByEmail(any(String.class));
        verify(contacts, times(1)).findByTelephone(any(String.class));
        verify(contacts, times(1)).saveAndFlush(any(Contact.class));
    }

    @Test
    void shouldNotUpdateContactWhenIdNotExist() {
        /*
        5.7.1 boolean success == false
        5.7.2 HttpStatus httpStatus == HttpStatus.NO_CONTENT
        5.7.3 List<String> errorMessages -> size == 1
        5.7.4 errorMessages -> "The contact with id 14 does not exist"
        5.7.5 result equals ContactDto
        5.7.6 contacts.existsById -> one called
        5.7.7 contacts.findEmailById -> no called
        5.7.8 contacts.findTelephoneById -> no called
        5.7.9 contacts.findByEmail -> no called
        5.7.10 contacts.findByTelephone -> no called
        5.7.11 contacts.saveAndFlush -> no called
        */
        Contact contact = initContacts().get(0);
        ContactDto dto = modelMapper.map(contact, ContactDto.class);
        dto.setId(14L);

        when(contacts.existsById(dto.getId())).thenReturn(false);

        ServerResponse<ContactDto> response = contactService.updateContact(dto);

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("The contact with id 14 does not exist", response.getErrorMessages().get(0));
        assertEquals(dto, response.getResult());

        verify(contacts, times(1)).existsById(dto.getId());
        verify(contacts, never()).findEmailById(anyLong());
        verify(contacts, never()).findTelephoneById(anyLong());
        verify(contacts, never()).findByEmail(any(String.class));
        verify(contacts, never()).findByTelephone(any(String.class));
        verify(contacts, never()).saveAndFlush(any(Contact.class));
    }

    private List<Contact> initContacts() {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            contacts.add(new Contact((long) i, "Name" + i, "lastName" + i, "+7 234 567 89 2" + i, "email" + i + "@mail.ru", createContactOwner(), LocalDateTime.now()));
        }
        return contacts;
    }

    private List<ContactDto> mapToContactDto(List<Contact> contacts) {
        return contacts.stream().map(contact -> modelMapper.map(contact, ContactDto.class)).toList();
    }
    
    private ContactCreateDto getContactCreateDto(String firstName,String lastName,String telephone,String email){
        return new ContactCreateDto(firstName,lastName,telephone,email, createContactOwner());
    }

    private ContactOwner createContactOwner() {
        return ContactOwner.builder()
                .id(1L)
                .username("Name")
                .email("email@mail.ru")
                .password("P@ssw0rd1234_QwErt")
                .role(Role.User)
                .birthday(LocalDate.now().minusYears(20))
                .telephone("+79001112234")
                .build();
    }
}