package ru.duskhunter.contacsapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.duskhunter.contacsapp.model.entity.Contact;
import ru.duskhunter.contacsapp.model.entity.ContactsDAO;
import ru.duskhunter.contacsapp.model.entity.ServerResponse;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
    1. getContacts
    1.1 get list contacts
        1.1.1 boolean success == true
        1.1.2 HttpStatus httpStatus == HttpStatus.ok
        1.1.3 List<String> errorMessages -> size == 0
        1.1.4 List<String> contacts -> size == 10 and value.equals
        1.1.5 contacts.getContacts -> one called
    1.2 no contacts -> empty list
        1.2.1 boolean success == true
        1.2.2 HttpStatus httpStatus == HttpStatus.ok
        1.2.3 List<String> errorMessages -> size == 0
        1.2.4 List<String> contacts -> size == 0
        1.2.5 contacts.getContacts -> one called
    2. getContactById
    2.1 get contact
        2.1.1 boolean success == true
        2.1.2 HttpStatus httpStatus == HttpStatus.ok
        2.1.3 HttpStatusEntity == HttpStatus.ok
        2.1.4 List<String> errorMessages -> size == 0
        2.1.5 result.contact id == 0
        2.1.6 result.contact firstName == Name0
        2.1.7 result.contact lastName == lastName0
        2.1.8 result.contact telephone == +1234567890
        2.1.9 result.contact email == email0@mail.ru
        2.1.10 contacts.getContacts -> one called
    2.2 no contact with this id -> httpStatus 204
        2.2.1 boolean success == true
        2.2.2 HttpStatus httpStatus == HttpStatus.NO_CONTENT
        2.2.3 HttpStatusEntity == HttpStatus.NO_CONTENT
        2.2.4 List<String> errorMessages -> size == 0
        2.2.5 result is null
        2.2.6 contacts.getContacts -> one called
    3. createContact
    3.1 create
        3.1.1 boolean success == true
        3.1.2 HttpStatus httpStatus == HttpStatus.created
        3.1.3 List<String> errorMessages -> size == 0
        3.1.4 result.contact id == 0
        3.1.5 result.contact firstName == Name0
        3.1.6 result.contact lastName == lastName0
        3.1.7 result.contact telephone == +1234567890
        3.1.8 result.contact email == email0@mail.ru
        3.1.9 contacts.addContact -> one called
        3.1.10 contacts.getContacts -> two called
    3.2 create
        3.2.1 boolean success == true
        3.2.2 HttpStatus httpStatus == HttpStatus.created
        3.2.3 List<String> errorMessages -> size == 0
        3.2.4 result.contact id == 10
        3.2.5 result.contact firstName == Name0
        3.2.6 result.contact lastName == lastName0
        3.2.7 result.contact telephone == +1234567890
        3.2.8 result.contact email == email0@mail.ru
        3.2.9 contacts.addContact -> one called
        3.2.10 contacts.getContacts -> two called
    3.3 create with sign id
        3.3.1 boolean success == false
        3.3.2 HttpStatus httpStatus == HttpStatus.conflict
        3.3.3 List<String> errorMessages -> size == 1
        3.3.4 errorMessages -> Contact with id 0 already exist
        3.3.5 contacts.addContact -> never called
        3.3.5 contacts.getContacts -> one called
 */

@ExtendWith({MockitoExtension.class})
class ContactServiceImplTest {
    @Mock
    private ContactsDAO contacts;
    private ContactServiceImpl contactService;

    @BeforeEach
    void setUp() {
        contactService = new ContactServiceImpl(contacts);
    }

    @Test
    void shouldGetContacts() {
        /*
        1.1 get list contacts
        1.1.1 boolean success == true
        1.1.2 HttpStatus httpStatus == HttpStatus.ok
        1.1.3 List<String> errorMessages -> size == 0
        1.1.4 List<String> contacts -> size == 10 and value.equals
        1.1.5 contacts.getContacts -> one called
        */
        when(contacts.getContacts()).thenReturn(initContacts());

        ServerResponse<List<Contact>> response = contactService.getContacts();

        verify(contacts,times(1)).getContacts();

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK,response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());
        assertEquals(10, response.getResult().size());
        assertEquals(initContacts(), response.getResult());
    }

    @Test
    void shouldGetEmptyListContacts() {
        /*
        1.2 get list contacts
        1.2.1 boolean success == true
        1.2.2 HttpStatus httpStatus == HttpStatus.ok
        1.2.3 List<String> errorMessages -> size == 0
        1.2.4 List<String> contacts -> size == 0
        1.2.5 contacts.getContacts -> one called
        */
        when(contacts.getContacts()).thenReturn(List.of());

        ServerResponse<List<Contact>> response = contactService.getContacts();

        verify(contacts,times(1)).getContacts();

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK,response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());
        assertEquals(0, response.getResult().size());
    }

    @Test
    void ShouldGetContactById() {
        /*
        2.1.1 boolean success == true
        2.1.2 HttpStatus httpStatus == HttpStatus.ok
        2.1.3 HttpStatusEntity == HttpStatus.ok
        2.1.4 List<String> errorMessages -> size == 0
        2.1.5 result.contact id == 0
        2.1.6 result.contact firstName == Name0
        2.1.7 result.contact lastName == lastName0
        2.1.8 result.contact telephone == +1234567890
        2.1.9 result.contact email == email0@mail.ru
        2.1.10 contacts.getContacts -> one called
        */
        when(contacts.getContacts()).thenReturn(initContacts());

        ResponseEntity<ServerResponse<Contact>> response = contactService.getContactById(0);

        verify(contacts,times(1)).getContacts();

        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK, response.getBody().getHttpStatus());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getErrorMessages().size());

        Contact contact = response.getBody().getResult();

        assertEquals(0, contact.getId());
        assertEquals("Name0", contact.getFirstName());
        assertEquals("lastName0", contact.getLastName());
        assertEquals("+1234567890", contact.getTelephone());
        assertEquals("email0@mail.ru", contact.getEmail());
    }

    @Test
    void ShouldGetHttpStatus204() {
        /*
        2.2.1 boolean success == true
        2.2.2 HttpStatus httpStatus == HttpStatus.NO_CONTENT
        2.2.3 HttpStatusEntity == HttpStatus.NO_CONTENT
        2.2.4 List<String> errorMessages -> size == 0
        2.2.5 result is null
        2.2.6 contacts.getContacts -> one called
         */
        when(contacts.getContacts()).thenReturn(initContacts());

        ResponseEntity<ServerResponse<Contact>> response = contactService.getContactById(13);

        verify(contacts,times(1)).getContacts();

        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.NO_CONTENT, response.getBody().getHttpStatus());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(0, response.getBody().getErrorMessages().size());
        assertNull(response.getBody().getResult());
    }

    @Test
    void ShouldCreateContactWhenContactsIsEmpty(){
        /*
        3.1 create
            3.1.1 boolean success == true
            3.1.2 HttpStatus httpStatus == HttpStatus.created
            3.1.3 List<String> errorMessages -> size == 0
            3.1.4 result.contact id == 0
            3.1.5 result.contact firstName == Name0
            3.1.6 result.contact lastName == lastName0
            3.1.7 result.contact telephone == +1234567890
            3.1.8 result.contact email == email0@mail.ru
            3.1.9 contacts.addContact -> one called
            3.1.10 contacts.getContacts -> two called
         */
        when(contacts.getContacts()).thenReturn(List.of());
        String firstName = "Name0";
        String lastName = "lastName0";
        String telephone = "+1234567890";
        String email = "email0@mail.ru";

        ServerResponse<Contact> response = contactService.createContact(
          new Contact(firstName,lastName,telephone,email)
        );

        verify(contacts,times(1)).addContact(any(Contact.class));
        verify(contacts,times(2)).getContacts();

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.CREATED, response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());

        Contact contact = response.getResult();
        assertEquals(0, contact.getId());
        assertEquals("Name0", contact.getFirstName());
        assertEquals("lastName0", contact.getLastName());
        assertEquals("+1234567890", contact.getTelephone());
        assertEquals("email0@mail.ru", contact.getEmail());
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
            3.2.7 result.contact telephone == +1234567890
            3.2.8 result.contact email == email0@mail.ru
            3.2.9 contacts.addContact -> one called
            3.2.10 contacts.getContacts -> two called
         */
        when(contacts.getContacts()).thenReturn(initContacts());
        String firstName = "Name0";
        String lastName = "lastName0";
        String telephone = "+1234567890";
        String email = "email0@mail.ru";

        ServerResponse<Contact> response = contactService.createContact(
                new Contact(firstName,lastName,telephone,email)
        );

        verify(contacts,times(1)).addContact(any(Contact.class));
        verify(contacts,times(2)).getContacts();

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.CREATED, response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());

        Contact contact = response.getResult();
        assertEquals(10, contact.getId());
        assertEquals("Name0", contact.getFirstName());
        assertEquals("lastName0", contact.getLastName());
        assertEquals("+1234567890", contact.getTelephone());
        assertEquals("email0@mail.ru", contact.getEmail());
    }

    @Test
    void ShouldNonCreateContactAndGetErrorMessage(){
        /*
        3.3 create
            3.3.1 boolean success == false
            3.3.2 HttpStatus httpStatus == HttpStatus.conflict
            3.3.3 List<String> errorMessages -> size == 1
            3.3.4 errorMessages -> Contact with id 0 already exist
            3.3.5 contacts.addContact -> never called
            3.3.6 contacts.getContacts -> one called
         */

        when(contacts.getContacts()).thenReturn(initContacts());
        String firstName = "Name13";
        Long id = (long) 0;
        String lastName = "lastName13";
        String telephone = "+1234567813";
        String email = "email013@mail.ru";

        ServerResponse<Contact> response = contactService.createContact(
                new Contact(id,firstName,lastName,telephone,email)
        );

        verify(contacts,never()).addContact(any(Contact.class));
        verify(contacts,times(1)).getContacts();

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.CONFLICT, response.getHttpStatus());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("Contact with id 0 already exist", response.getErrorMessages().get(0));
    }

    private List<Contact> initContacts() {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            contacts.add(new Contact((long) i, "Name" + i, "lastName" + i, "+123456789" + i, "email" + i + "@mail.ru"));
        }
        return contacts;
    }
}