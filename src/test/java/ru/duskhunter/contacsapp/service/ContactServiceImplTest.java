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
import ru.duskhunter.contacsapp.common.security.CurrentUserProvider;
import ru.duskhunter.contacsapp.common.util.PhoneNormalizer;
import ru.duskhunter.contacsapp.common.util.Validator;
import ru.duskhunter.contacsapp.dto.ServerResponse;
import ru.duskhunter.contacsapp.dto.contact.ContactCreateDto;
import ru.duskhunter.contacsapp.dto.contact.ContactDto;
import ru.duskhunter.contacsapp.exception.EntityConflictException;
import ru.duskhunter.contacsapp.exception.InternalServerException;
import ru.duskhunter.contacsapp.exception.NotFoundException;
import ru.duskhunter.contacsapp.model.Role;
import ru.duskhunter.contacsapp.model.entity.Contact;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;
import ru.duskhunter.contacsapp.model.repository.ContactRepo;

import java.time.LocalDate;
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
        2.1.3 List<String> errorMessages -> size == 0
        2.1.4 result.contact id == 0
        2.1.5 result.contact firstName == Name0
        2.1.6 result.contact lastName == lastName0
        2.1.7 result.contact telephone == +7 234 567 89 20
        2.1.8 result.contact email == email0@mail.ru
        2.1.9 contacts.findById -> one called
    2.2 no contact with this id
        2.2.1 NotFoundException
        2.2.2 errorMessages -> "The contact with id 13 does not exist"
        2.2.3 result is null
        2.2.4 contacts.findById -> one called
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
        3.3.1 EntityConflictException
        3.3.2 errorMessages -> "Error creating contact"
        3.3.3 exception.dto equals ContactDto
        3.3.4 contacts.saveAndFlush -> one called
        3.3.5 validator.validate -> one called
        3.3.6 contacts.findByEmail -> one called
        3.3.7 contacts.findByTelephone -> one called
    3.4 create When email Already Exist
        3.4.1 EntityConflictException
        3.4.2 errorMessages -> "Contact with this email: email0@mail.ru - already exist"
        3.4.3 exception.dto equals ContactDto
        3.4.4 contacts.saveAndFlush -> no called
        3.4.5 validator.validate -> one called
        3.4.6 contacts.findByEmail -> one called
        3.4.7 contacts.findByTelephone -> no called
    3.5 create When telephone Already Exist
        3.5.1 EntityConflictException
        3.5.2 errorMessages -> "Contact with this telephone: +7 234 567 89 20 - already exist"
        3.5.3 exception.dto equals ContactDto
        3.5.4 contacts.saveAndFlush -> no called
        3.5.5 validator.validate -> one called
        3.5.6 contacts.findByEmail -> one called
        3.5.7 contacts.findByTelephone -> one called
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
        5.1.1 EntityConflictException
        5.1.2 errorMessages -> "Error update contact"
        5.1.3 exception.dto equals ContactDto
        5.1.4 contacts.findTelephoneById -> one called
        5.1.5 contacts.findEmailById -> one called
        5.1.6 contacts.findTelephoneById -> one called
        5.1.7 contacts.saveAndFlush -> one called
    5.2 update test is unsuccessful - email already exist
        5.2.1 EntityConflictException
        5.2.2 errorMessages -> "Contact with this email: email0@mail.ru - already exist"
        5.2.3 exception.dto equals ContactDto
        5.2.4 contacts.validate -> one called
        5.2.5 contacts.findEmailById -> one called
        5.2.6 contacts.findByEmail -> one called
        5.2.7 contacts.findTelephoneById -> one called
        5.2.8 contacts.saveAndFlush -> no called
    5.3 update test is unsuccessfully - telephone already exist
        5.3.1 EntityConflictException
        5.3.2 errorMessages -> "Contact with this telephone: +7 234 567 89 20 - already exist"
        5.3.3 exception.dto equals ContactDto
        5.3.4 contacts.validate -> one called
        5.3.5 contacts.findEmailById -> one called
        5.3.6 contacts.findByEmail -> one called
        5.3.7 contacts.findTelephoneById -> one called
        5.3.8 contacts.findByTelephone -> one called
        5.3.9 contacts.saveAndFlush -> no called
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
    @Mock
    private CurrentUserProvider currentUserProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jakarta.validation.Validator jakartaValidator =
                jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        validator = new Validator(jakartaValidator);

        contactService = new ContactServiceImpl(contacts, modelMapper, validator, currentUserProvider);
    }

    @Test
    void shouldGetContactsForCurrentOwner() {
        /*
        1.1 get list contacts
        1.1.1 boolean success == true
        1.1.2 HttpStatus httpStatus == HttpStatus.ok
        1.1.3 List<String> errorMessages -> size == 0
        1.1.4 List<String> contacts -> size == 10 and value.equals
        1.1.5 contacts.findAll -> one called
        */
        ContactOwner owner = createContactOwner();
        when(currentUserProvider.getCurrentOwner()).thenReturn(owner);
        when(contacts.findAllByOwnerId(owner.getId())).thenReturn(initContacts());

        ServerResponse<List<ContactDto>> response = contactService.getContactsForCurrentOwner();

        verify(contacts, times(1)).findAllByOwnerId(owner.getId());

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());
        assertEquals(10, response.getResult().size());
        assertEquals(mapToContactDto(initContacts()), response.getResult());
    }

    @Test
    void shouldGetEmptyListForCurrentOwner() {
        /*
        1.2 get list contacts -> returns empty list when current owner has none
        1.2.1 boolean success == true
        1.2.2 HttpStatus httpStatus == HttpStatus.ok
        1.2.3 List<String> errorMessages -> size == 0
        1.2.4 List<String> contacts -> size == 0
        1.2.5 contacts.findAll -> one called
        */
        ContactOwner owner = createContactOwner();
        when(currentUserProvider.getCurrentOwner()).thenReturn(owner);
        when(contacts.findAllByOwnerId(owner.getId())).thenReturn(List.of());

        ServerResponse<List<ContactDto>> response = contactService.getContactsForCurrentOwner();

        verify(contacts, times(1)).findAllByOwnerId(owner.getId());

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
        2.1.3 List<String> errorMessages -> size == 0
        2.1.4 result.contact id == 0
        2.1.5 result.contact firstName == Name0
        2.1.6 result.contact lastName == lastName0
        2.1.7 result.contact telephone == +7 (234) 567 89 20
        2.1.8 result.contact email == email0@mail.ru
        2.1.9 contacts.findById -> one called
        */
        Contact contact = initContacts().get(0);
        when(currentUserProvider.getCurrentOwner()).thenReturn(contact.getOwner());
        when(contacts.findById(anyLong())).thenReturn(Optional.of(contact));

        ServerResponse<ContactDto> response = contactService.getContactById(0);

        verify(contacts, times(1)).findById(anyLong());

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());

        ContactDto contactDto = response.getResult();

        assertEquals(0, contactDto.getId());
        assertEquals("Name0", contactDto.getFirstName());
        assertEquals("lastName0", contactDto.getLastName());
        assertEquals("+7 234 567 89 20", contactDto.getTelephone());
        assertEquals("email0@mail.ru", contactDto.getEmail());
    }

    @Test
    void shouldGetHttpStatus204() {
        /*
        2.2 NotFoundException when contact does not exist
        2.2.1 NotFoundException
        2.2.2 errorMessages -> "The contact with id 13 does not exist"
        2.2.3 result is null
        2.2.4 contacts.findById -> one called
         */
        when(currentUserProvider.getCurrentOwner()).thenReturn(createContactOwner());
        when(contacts.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            contactService.getContactById(13);
        });

        verify(contacts, times(1)).findById(anyLong());

        assertEquals("The contact with id 13 does not exist", exception.getMessage());
        assertNull(exception.getDto());
    }

    @Test
    void shouldNotGetContactWhenBelongsToAnotherOwner() {
        /*
        2.3 NotFoundException when contact belongs to a different owner
        */
        Contact contact = initContacts().get(0);
        ContactOwner anotherOwner = ContactOwner.builder()
                .id(999L)
                .username("AnotherUser")
                .email("another@mail.ru")
                .password("P@ssw0rd1234")
                .role(Role.ROLE_USER)
                .birthday(LocalDate.now().minusYears(20))
                .telephone("+79009998877")
                .build();

        when(currentUserProvider.getCurrentOwner()).thenReturn(anotherOwner);
        when(contacts.findById(anyLong())).thenReturn(Optional.of(contact));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            contactService.getContactById(contact.getId());
        });

        assertEquals(String.format("The contact with id %s does not exist", contact.getId()), exception.getMessage());
        assertNull(exception.getDto());
    }

    @Test
    void shouldCreateContact() {
        /*
        3.1 create — owner is taken from CurrentUserProvider, not from client
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

        ContactOwner owner = createContactOwner();
        ContactCreateDto dto = getContactCreateDto(firstName, lastName, telephone, email);

        when(currentUserProvider.getCurrentOwner()).thenReturn(owner);
        when(contacts.findByOwnerIdAndEmail(anyLong(), any(String.class))).thenReturn(Optional.empty());
        when(contacts.findByOwnerIdAndTelephone(anyLong(), any(String.class))).thenReturn(Optional.empty());
        when(contacts.saveAndFlush(any(Contact.class)))
                .thenReturn(new Contact(1L, firstName, lastName, telephone, email, createContactOwner()));

        ServerResponse<ContactDto> response = contactService.createContact(dto);

        verify(contacts, times(1)).findByOwnerIdAndEmail(anyLong(), any(String.class));
        verify(contacts, times(1)).findByOwnerIdAndTelephone(anyLong(), any(String.class));
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
    void shouldNotCreateContactWhenErrorCreatingContact() {
        /*
        3.2 EntityConflictException on DataIntegrityViolationException
        3.2.1 EntityConflictException
        3.2.2 errorMessages -> "Error creating contact"
        3.2.3 exception.dto equals ContactDto
        3.2.4 contacts.saveAndFlush -> one called
        3.2.5 validator.validate -> one called
        3.2.6 contacts.findByOwnerIdAndEmail -> one called
        3.2.7 contacts.findByOwnerIdAndTelephone -> one called
        */
        Contact existing = initContacts().get(0);
        ContactOwner owner = existing.getOwner();

        ContactCreateDto dto = getContactCreateDto(
                existing.getFirstName(), existing.getLastName(), existing.getTelephone(), existing.getEmail());

        when(currentUserProvider.getCurrentOwner()).thenReturn(owner);
        when(contacts.findByOwnerIdAndEmail(anyLong(), any(String.class))).thenReturn(Optional.empty());
        when(contacts.findByOwnerIdAndTelephone(anyLong(), any(String.class))).thenReturn(Optional.empty());
        when(contacts.saveAndFlush(any(Contact.class))).thenThrow(DataIntegrityViolationException.class);

        EntityConflictException exception = assertThrows(EntityConflictException.class, () -> {
            contactService.createContact(dto);
        });

        verify(contacts, times(1)).findByOwnerIdAndEmail(anyLong(), any(String.class));
        verify(contacts, times(1)).findByOwnerIdAndTelephone(anyLong(), any(String.class));
        verify(contacts, times(1)).saveAndFlush(any(Contact.class));

        assertEquals("Error creating contact", exception.getMessage());
    }

    @Test
    void shouldNotCreateContactWhenEmailAlreadyExist() {
        /*
        3.3.1 EntityConflictException
        3.3.2 errorMessages -> "Contact with this email: email0@mail.ru - already exist"
        3.3.3 exception.dto equals ContactDto
        3.3.4 contacts.saveAndFlush -> no called
        3.3.5 validator.validate -> one called
        3.3.6 contacts.findByEmail -> one called
        3.3.7 contacts.findByTelephone -> no called
        */
        Contact existing = initContacts().get(0);
        ContactOwner owner = existing.getOwner();

        ContactCreateDto dto = getContactCreateDto(
                existing.getFirstName(),
                existing.getLastName(),
                existing.getTelephone(),
                existing.getEmail()
        );

        when(currentUserProvider.getCurrentOwner()).thenReturn(owner);
        when(contacts.findByOwnerIdAndEmail(anyLong(), any(String.class))).thenReturn(Optional.of(existing));

        EntityConflictException exception = assertThrows(EntityConflictException.class, () -> {
            contactService.createContact(dto);
        });

        verify(contacts, times(1)).findByOwnerIdAndEmail(anyLong(), any(String.class));
        verify(contacts, never()).findByOwnerIdAndTelephone(anyLong(), any(String.class));
        verify(contacts, never()).saveAndFlush(any(Contact.class));

        assertEquals("Contact with this email: email0@mail.ru - already exist", exception.getMessage());
        assertEquals(modelMapper.map(dto, ContactDto.class), exception.getDto());
    }

    @Test
    void shouldNotCreateContactWhenTelephoneAlreadyExist() {
        /*
        3.4.1 EntityConflictException
        3.4.2 errorMessages -> "Contact with this telephone: +72345678920 - already exist"
        3.4.3 exception.dto equals ContactDto
        3.4.4 contacts.saveAndFlush -> no called
        3.4.5 validator.validate -> one called
        3.4.6 contacts.findByEmail -> one called
        3.4.7 contacts.findByTelephone -> one called
        */
        Contact existing  = initContacts().get(0);
        ContactOwner owner = existing.getOwner();

        ContactCreateDto dto = getContactCreateDto(
                existing .getFirstName(),
                existing .getLastName(),
                existing .getTelephone(),
                existing .getEmail()
        );

        when(currentUserProvider.getCurrentOwner()).thenReturn(owner);
        when(contacts.findByOwnerIdAndEmail(anyLong(), any(String.class))).thenReturn(Optional.empty());
        when(contacts.findByOwnerIdAndTelephone(anyLong(), any(String.class))).thenReturn(Optional.of(existing));

        EntityConflictException exception = assertThrows(EntityConflictException.class, () -> {
            contactService.createContact(dto);
        });

        verify(contacts, times(1)).findByOwnerIdAndEmail(anyLong(), any(String.class));
        verify(contacts, times(1)).findByOwnerIdAndTelephone(anyLong(), any(String.class));
        verify(contacts, never()).saveAndFlush(any(Contact.class));

        assertEquals("Contact with this telephone: +72345678920 - already exist", exception.getMessage());
        assertEquals(modelMapper.map(dto, ContactDto.class), exception.getDto());
    }

    @Test
    void shouldDeleteContactByIdUnsuccessfully() {
        /*
        4.1.1 InternalServerException
        4.1.2 errorMessages -> "Unexpected server error during deletion operation the contact with id 13"
        4.1.3 result equals ContactDto
        4.1.4 contacts.findById -> two called
        4.1.5 contacts.deleteContactById -> called once
        */
        Contact contact = initContacts().get(0);
        when(currentUserProvider.getCurrentOwner()).thenReturn(contact.getOwner());
        when(contacts.findById(anyLong())).thenReturn(Optional.of(contact));
        doNothing().when(contacts).deleteById(anyLong());

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            contactService.deleteContactById(13);
        });

        verify(contacts, times(2)).findById(anyLong());
        verify(contacts, times(1)).deleteById(13L);

        assertEquals(mapToContactDto(List.of(contact)).get(0), exception.getDto());
        assertEquals("Unexpected server error during deletion operation the contact with id 13",
                exception.getMessage());
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

        when(currentUserProvider.getCurrentOwner()).thenReturn(contact.getOwner());
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
    void shouldNotDeleteContactWhenBelongsToAnotherOwner() {
        /*
        4.3.1 boolean success == false
        4.3.2 HttpStatus httpStatus == HttpStatus.NO_CONTENT
        4.3.3 List<String> errorMessages -> size == 1
        4.3.4 errorMessages -> "The contact with id 13 does not exist"
        4.3.5 result is null
        4.3.6 contacts.findById -> one called
        4.3.7 contacts.deleteById -> no called
        */
        Contact contact = initContacts().get(0);
        ContactOwner anotherOwner = ContactOwner.builder()
                .id(999L)
                .username("AnotherUser")
                .email("another@mail.ru")
                .password("P@ssw0rd1234")
                .role(Role.ROLE_USER)
                .birthday(LocalDate.now().minusYears(20))
                .telephone("+79009998877")
                .build();

        when(currentUserProvider.getCurrentOwner()).thenReturn(anotherOwner);
        when(contacts.findById(anyLong())).thenReturn(Optional.of(contact));

        assertThrows(NotFoundException.class,
                () -> contactService.deleteContactById(contact.getId()));

        verify(contacts, never()).deleteById(anyLong());
    }

    @Test
    void shouldGetExceptionErrorUpdatingContact() {
        /*
        5.1.1 EntityConflictException
        5.1.2 errorMessages -> "Error update contact"
        5.1.3 exception.dto equals ContactDto
        5.1.4 contacts.findTelephoneById -> one called
        5.1.5 contacts.findEmailById -> one called
        5.1.6 contacts.findTelephoneById -> one called
        5.1.7 contacts.saveAndFlush -> one called
        */
        Contact contact = initContacts().get(0);
        ContactDto dto = modelMapper.map(contact, ContactDto.class);

        when(currentUserProvider.getCurrentOwner()).thenReturn(contact.getOwner());
        when(contacts.findById(anyLong())).thenReturn(Optional.of(contact));
        when(contacts.findEmailById(anyLong())).thenReturn(Optional.of(contact.getEmail()));
        when(contacts.findTelephoneById(anyLong())).thenReturn(Optional.of(contact.getTelephone()));
        when(contacts.saveAndFlush(any(Contact.class))).thenThrow(DataIntegrityViolationException.class);

        EntityConflictException exception = assertThrows(EntityConflictException.class, () -> {
            contactService.updateContact(dto);
        });

        verify(contacts, times(1)).findEmailById(anyLong());
        verify(contacts, times(1)).findTelephoneById(anyLong());
        verify(contacts, times(1)).saveAndFlush(any(Contact.class));

        assertEquals(mapToContactDto(List.of(contact)).get(0), exception.getDto());
        assertEquals("Error update contact", exception.getMessage());
    }

    @Test
    void shouldGetExceptionEmailAlreadyExistWhenUpdatingContact() {
        /*
        5.2.1 EntityConflictException
        5.2.2 errorMessages -> "Contact with this email: email0@mail.ru - already exist"
        5.2.3 exception.dto equals ContactDto
        5.2.4 contacts.validate -> one called
        5.2.5 contacts.findEmailById -> one called
        5.2.6 contacts.findByEmail -> one called
        5.2.7 contacts.findTelephoneById -> no called
        5.2.7 contacts.findByTelephone -> no called
        5.2.8 contacts.saveAndFlush -> no called
        */
        Contact contact = initContacts().get(0);
        ContactDto dto = modelMapper.map(contact, ContactDto.class);

        when(currentUserProvider.getCurrentOwner()).thenReturn(contact.getOwner());
        when(contacts.findById(anyLong())).thenReturn(Optional.of(contact));
        when(contacts.findEmailById(anyLong())).thenReturn(Optional.of("other@mail.ru"));
        when(contacts.findByOwnerIdAndEmail(anyLong(), any(String.class))).thenReturn(Optional.of(contact));

        EntityConflictException exception = assertThrows(EntityConflictException.class, () -> {
                    contactService.updateContact(dto);
                }
        );

        verify(contacts, times(1)).findEmailById(dto.getId());
        verify(contacts, times(1)).findByOwnerIdAndEmail(anyLong(), anyString());
        verify(contacts, never()).findTelephoneById(anyLong());
        verify(contacts, never()).findByOwnerIdAndTelephone(anyLong(), anyString());
        verify(contacts, never()).saveAndFlush(any(Contact.class));

        assertEquals(mapToContactDto(List.of(contact)).get(0), exception.getDto());
        assertEquals("Contact with this email: email0@mail.ru - already exist", exception.getMessage());
    }

    @Test
    void shouldGetExceptionTelephoneAlreadyExistWhenUpdatingContact() {
        /*
        5.3.1 EntityConflictException
        5.3.2 errorMessages -> "Contact with this telephone: +72345678920 - already exist"
        5.3.3 exception.dto equals ContactDto
        5.3.4 contacts.validate -> one called
        5.3.5 contacts.findEmailById -> one called
        5.3.6 contacts.findByEmail -> one called
        5.3.7 contacts.findTelephoneById -> one called
        5.3.8 contacts.findByTelephone -> one called
        5.3.9 contacts.saveAndFlush -> no called
        */
        Contact contact = initContacts().get(0);
        ContactDto dto = modelMapper.map(contact, ContactDto.class);

        when(currentUserProvider.getCurrentOwner()).thenReturn(contact.getOwner());
        when(contacts.findById(anyLong())).thenReturn(Optional.of(contact));
        when(contacts.findEmailById(anyLong())).thenReturn(Optional.of(contact.getEmail()));
        when(contacts.findTelephoneById(anyLong())).thenReturn(Optional.of("other_telephone"));
        when(contacts.findByOwnerIdAndTelephone(anyLong(), any(String.class))).thenReturn(Optional.of(contact));

        EntityConflictException exception = assertThrows(EntityConflictException.class, () -> {
                    contactService.updateContact(dto);
                }
        );

        verify(contacts, times(1)).findEmailById(anyLong());
        verify(contacts, times(1)).findTelephoneById(anyLong());
        verify(contacts, times(1)).findByOwnerIdAndTelephone(anyLong(), any(String.class));
        verify(contacts, never()).saveAndFlush(any(Contact.class));

        assertEquals(mapToContactDto(List.of(contact)).get(0), exception.getDto());
        assertEquals("Contact with this telephone: +72345678920 - already exist", exception.getMessage());
    }

    @Test
    void shouldUpdateContactSuccessfully() {
        /*
        positive case update first and last name
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

        when(currentUserProvider.getCurrentOwner()).thenReturn(contact.getOwner());
        when(contacts.findById(anyLong())).thenReturn(Optional.of(contact));
        when(contacts.findEmailById(anyLong())).thenReturn(Optional.of(contact.getEmail()));
        when(contacts.findTelephoneById(anyLong())).thenReturn(Optional.of(contact.getTelephone()));
        when(contacts.saveAndFlush(any(Contact.class))).thenReturn(contact);

        ServerResponse<ContactDto> response = contactService.updateContact(dto);

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(modelMapper.map(contact, ContactDto.class), response.getResult());
        assertTrue(response.getErrorMessages().isEmpty());

        verify(contacts, times(1)).findById(anyLong());
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

        when(currentUserProvider.getCurrentOwner()).thenReturn(contact.getOwner());
        when(contacts.findById(anyLong())).thenReturn(Optional.of(contact));
        when(contacts.findEmailById(anyLong())).thenReturn(Optional.of(contact.getEmail()));
        when(contacts.findTelephoneById(anyLong())).thenReturn(Optional.of(PhoneNormalizer.normalize(contact.getTelephone())));
        when(contacts.findByOwnerIdAndEmail(anyLong(), anyString())).thenReturn(Optional.empty());
        when(contacts.saveAndFlush(any(Contact.class))).thenReturn(contact);

        ServerResponse<ContactDto> response = contactService.updateContact(dto);

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(modelMapper.map(contact, ContactDto.class), response.getResult());
        assertTrue(response.getErrorMessages().isEmpty());

        verify(contacts, times(1)).findById(dto.getId());
        verify(contacts, times(1)).findEmailById(anyLong());
        verify(contacts, times(1)).findTelephoneById(anyLong());
        verify(contacts, times(1)).findByOwnerIdAndEmail(anyLong(), anyString());
        verify(contacts, never()).findByOwnerIdAndTelephone(anyLong(), anyString());
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

        when(currentUserProvider.getCurrentOwner()).thenReturn(contact.getOwner());
        when(contacts.findById(anyLong())).thenReturn(Optional.of(contact));
        when(contacts.findEmailById(anyLong())).thenReturn(Optional.of(contact.getEmail()));
        when(contacts.findTelephoneById(anyLong())).thenReturn(Optional.of(contact.getTelephone()));
        when(contacts.findByOwnerIdAndTelephone(anyLong(), any(String.class))).thenReturn(Optional.empty());
        when(contacts.saveAndFlush(any(Contact.class))).thenReturn(contact);

        ServerResponse<ContactDto> response = contactService.updateContact(dto);

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(modelMapper.map(contact, ContactDto.class), response.getResult());
        assertTrue(response.getErrorMessages().isEmpty());

        verify(contacts, times(1)).findById(dto.getId());
        verify(contacts, times(1)).findEmailById(anyLong());
        verify(contacts, times(1)).findTelephoneById(anyLong());
        verify(contacts, never()).findByOwnerIdAndEmail(anyLong(), any(String.class));
        verify(contacts, times(1)).findByOwnerIdAndTelephone(anyLong(), any(String.class));
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

        when(currentUserProvider.getCurrentOwner()).thenReturn(contact.getOwner());
        when(contacts.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            contactService.updateContact(dto);
        });

        assertEquals("The contact with id 14 does not exist", exception.getMessage());
        assertEquals(dto, exception.getDto());

        verify(contacts, times(1)).findById(dto.getId());
        verify(contacts, never()).findEmailById(anyLong());
        verify(contacts, never()).findTelephoneById(anyLong());
        verify(contacts, never()).findByOwnerIdAndEmail(anyLong(), any(String.class));
        verify(contacts, never()).findByOwnerIdAndTelephone(anyLong(), any(String.class));
        verify(contacts, never()).saveAndFlush(any(Contact.class));
    }

    @Test
    void shouldNotUpdateContactWhenBelongsToAnotherOwner() {
        /*
        5.7 (extra) NotFoundException when contact belongs to a different owner
        */
        Contact contact = initContacts().get(0);
        ContactDto dto = modelMapper.map(contact, ContactDto.class);

        ContactOwner anotherOwner = ContactOwner.builder()
                .id(999L)
                .username("AnotherUser")
                .email("another@mail.ru")
                .password("P@ssw0rd1234")
                .role(Role.ROLE_USER)
                .birthday(LocalDate.now().minusYears(20))
                .telephone("+79009998877")
                .build();

        when(currentUserProvider.getCurrentOwner()).thenReturn(anotherOwner);
        when(contacts.findById(dto.getId())).thenReturn(Optional.of(contact));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> contactService.updateContact(dto));

        assertEquals(dto, exception.getDto());
        verify(contacts, never()).saveAndFlush(any(Contact.class));
    }

    private List<Contact> initContacts() {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            contacts.add(new Contact((long) i, "Name" + i, "lastName" + i, "+7 234 567 89 2" + i, "email" + i + "@mail.ru", createContactOwner()));
        }
        return contacts;
    }

    private List<ContactDto> mapToContactDto(List<Contact> contacts) {
        return contacts.stream()
                .map(contact -> modelMapper.map(contact, ContactDto.class))
                .toList();
    }

    private ContactCreateDto getContactCreateDto(String firstName, String lastName, String telephone, String email) {
        return new ContactCreateDto(firstName, lastName, telephone, email);
    }

    private ContactOwner createContactOwner() {
        return ContactOwner.builder()
                .id(1L)
                .username("Name")
                .email("email@mail.ru")
                .password("P@ssw0rd1234_QwErt")
                .role(Role.ROLE_USER)
                .birthday(LocalDate.now().minusYears(20))
                .telephone("+79001112234")
                .build();
    }
}