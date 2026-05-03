package ru.duskhunter.contacsapp.service;

import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.duskhunter.contacsapp.dto.contact.ContactCreateDtoRequest;
import ru.duskhunter.contacsapp.dto.contact.ContactDto;
import ru.duskhunter.contacsapp.model.entity.Contact;
import ru.duskhunter.contacsapp.model.repository.ContactRepo;
import ru.duskhunter.contacsapp.dto.ServerResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        2.1.8 result.contact telephone == +7 (234) 567 89 20
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
        3.1.7 result.contact telephone == +7 (123) 456-78-90
        3.1.8 result.contact email == email0@mail.ru
        3.1.9 contacts.saveAndFlush -> one called
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

@ExtendWith({MockitoExtension.class})
class ContactServiceImplTest {
    @Mock
    private ContactRepo contacts;
    @Mock
    private Validator validator;
    private final ModelMapper modelMapper = new ModelMapper();
    private ContactServiceImpl contactService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        verify(contacts,times(1)).findAll();

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK,response.getHttpStatus());
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

        verify(contacts,times(1)).findAll();

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK,response.getHttpStatus());
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

        verify(contacts,times(1)).findById(anyLong());

        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK, response.getBody().getHttpStatus());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getErrorMessages().size());

        ContactDto contact = response.getBody().getResult();

        assertEquals(0, contact.getId());
        assertEquals("Name0", contact.getFirstName());
        assertEquals("lastName0", contact.getLastName());
        assertEquals("+7 (234) 567 89 20", contact.getTelephone());
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

        verify(contacts,times(1)).findById(anyLong());

        assertFalse(response.getBody().isSuccess());
        assertEquals(HttpStatus.NO_CONTENT, response.getBody().getHttpStatus());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(1, response.getBody().getErrorMessages().size());
        assertEquals("The contact with id 13 does not exist", response.getBody().getErrorMessages().get(0));
        assertNull(response.getBody().getResult());
    }

    @Test
    void shouldCreateContactWhenContactsIsEmpty(){
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
        String telephone = "+7 (123) 456-78-90";
        String email = "email0@mail.ru";

        ContactCreateDtoRequest dto = new ContactCreateDtoRequest(firstName, lastName, telephone, email);

        when(validator.validate(any(Contact.class))).thenReturn(Set.of());
        when(contacts.saveAndFlush(any(Contact.class)))
                .thenReturn(new Contact(1L, firstName, lastName, telephone, email, LocalDateTime.now()));

        ServerResponse<ContactDto> response = contactService.createContact(dto);

        verify(contacts,times(1)).saveAndFlush(any(Contact.class));

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
    void ShouldCreateContactWhenContactsIsNonEmpty(){
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
        String telephone = "+7 (123) 456-78-90";
        String email = "email0@mail.ru";

        when(validator.validate(any(Contact.class))).thenReturn(Set.of());
        when(contacts.saveAndFlush(any(Contact.class)))
                .thenReturn(new Contact(10L, firstName, lastName, telephone, email, LocalDateTime.now()));

        ContactCreateDtoRequest dto = new ContactCreateDtoRequest(firstName, lastName, telephone, email);

        ServerResponse<ContactDto> response = contactService.createContact(dto);

        verify(contacts,times(1)).saveAndFlush(any(Contact.class));

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

    private List<Contact> initContacts() {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            contacts.add(new Contact((long) i, "Name" + i, "lastName" + i, "+7 (234) 567 89 2" + i, "email" + i + "@mail.ru", LocalDateTime.now()));
        }
        return contacts;
    }

    private List<ContactDto> mapToContactDto(List<Contact> contacts){
        return contacts.stream().map(contact -> modelMapper.map(contact, ContactDto.class)).toList();
    }
}