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
import ru.duskhunter.contacsapp.common.util.PhoneNormalizer;
import ru.duskhunter.contacsapp.common.util.Validator;
import ru.duskhunter.contacsapp.dto.ServerResponse;
import ru.duskhunter.contacsapp.dto.contactowner.ContactCreateOwnerDto;
import ru.duskhunter.contacsapp.dto.contactowner.ContactOwnerDto;
import ru.duskhunter.contacsapp.exception.EntityConflictException;
import ru.duskhunter.contacsapp.exception.InternalServerException;
import ru.duskhunter.contacsapp.exception.NotFoundException;
import ru.duskhunter.contacsapp.model.Role;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;
import ru.duskhunter.contacsapp.model.repository.ContactOwnerRepo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
    1. getContactOwners
    1.1 get list contactOwners
        1.1.1 boolean success == true
        1.1.2 HttpStatus httpStatus == HttpStatus.ok
        1.1.3 List<String> errorMessages -> size == 0
        1.1.4 List<String> owners -> size == 10 and value.equals
        1.1.5 ownerRepo.findAll -> one called
    1.2 no owners -> empty list
        1.2.1 boolean success == true
        1.2.2 HttpStatus httpStatus == HttpStatus.ok
        1.2.3 List<String> errorMessages -> size == 0
        1.2.4 List<String> contactOwners -> size == 0
        1.2.5 ownerRepo.findAll -> one called
    2. getContactOwnerById
    2.1 get contactOwner
        2.1.1 boolean success == true
        2.1.2 HttpStatus httpStatus == HttpStatus.ok
        2.1.3 List<String> errorMessages -> size == 0
        2.1.4 result.contactOwner id == 0
        2.1.5 result.contactOwner username == Name0
        2.1.6 result.contactOwner birthday == now - 20year
        2.1.7 result.contactOwner telephone == +7 234 567 89 20
        2.1.8 result.contactOwner email == email0@mail.ru
        2.1.9 ownerRepo.findById -> one called
    2.2 no contact with this id -> NotFoundException
        2.2.1 NotFoundException
        2.2.2 errorMessages -> "The owner with id 13 does not exist"
        2.2.3 exception.dto is null
        2.2.4 ownerRepo.findById -> one called
    3. createContact
    3.1 create
        3.1.1 boolean success == true
        3.1.2 HttpStatus httpStatus == HttpStatus.created
        3.1.3 List<String> errorMessages -> size == 0
        3.1.4 result.contact id == 0
        3.1.5 result.contact username == Name0
        3.1.6 result.contact telephone == +7 123 456-78-90
        3.1.7 result.contact email == email0@mail.ru
        3.1.8 validator.validate -> one called
        3.1.9 ownerRepo.findByEmail -> one called
        3.1.10 ownerRepo.findByTelephone -> one called
        3.1.11 ownerRepo.saveAndFlush -> one called
    3.2 create
        3.2.1 boolean success == true
        3.2.2 HttpStatus httpStatus == HttpStatus.created
        3.2.3 List<String> errorMessages -> size == 0
        3.2.4 result.contact id == 10
        3.2.5 result.contact username == Name0
        3.2.6 result.contact telephone == +7 123 456-78-90
        3.2.7 result.contact email == email0@mail.ru
        3.2.8 validator.validate -> one called
        3.2.9 ownerRepo.findByEmail -> one called
        3.2.10 ownerRepo.findByTelephone -> one called
        3.2.11 ownerRepo.saveAndFlush -> one called
    3.3 create when Error creating owner
        3.3.1 EntityConflictException
        3.3.2 errorMessages -> "Error creating owner"
        3.3.3 exception.dto equals ownerDto
        3.3.4 ownerRepo.saveAndFlush -> one called
        3.3.5 validator.validate -> one called
        3.3.6 ownerRepo.findByEmail -> one called
        3.3.7 ownerRepo.findByTelephone -> one called
    3.4 create When Email Already Exist
        3.4.1 EntityConflictException
        3.4.2 errorMessages -> "User with this email already exist"
        3.4.3 exception.dto equals ownerDto
        3.4.4 ownerRepo.saveAndFlush -> no called
        3.4.5 validator.validate -> one called
        3.4.6 ownerRepo.findByEmail -> one called
        3.4.7 ownerRepo.findByTelephone -> no called
    3.5 create When telephone Already Exist
        3.5.1 EntityConflictException
        3.5.2 errorMessages -> "User with this telephone already exist"
        3.5.3 exception.dto equals ownerDto
        3.5.4 ownerRepo.saveAndFlush -> no called
        3.5.5 validator.validate -> one called
        3.5.6 ownerRepo.findByEmail -> one called
        3.5.7 ownerRepo.findByTelephone -> one called
    4 delete
    4.1 delete test is unsuccessfully
        4.1.1 InternalServerException
        4.1.2 errorMessages -> "Unexpected server error during deletion operation the owner with id 13"
        4.1.3 exception.dto equals ownerDto
        4.1.4 ownerRepo.findById -> two called
        4.1.5 ownerRepo.deleteById -> called once
    4.2 positive case delete
        4.2.1 boolean success == true
        4.2.2 HttpStatus httpStatus == HttpStatus.OK
        4.2.3 List<String> errorMessages -> size == 0
        4.2.4 result equals ownerDto
        4.2.5 ownerRepo.findById -> two called
        4.2.6 ownerRepo.deleteById -> one called
    5 update
    5.1 update test is unsuccessfully - error update
        5.1.1 EntityConflictException
        5.1.2 errorMessages -> "Error update owner"
        5.1.3 exception.dto equals ownerDto
        5.1.4 ownerRepo.existsById -> one called
        5.1.5 validator.validate -> one called
        5.1.6 ownerRepo.findEmailById -> one called
        5.1.7 ownerRepo.findTelephoneById -> one called
        5.1.8 ownerRepo.findByTelephone -> no called
        5.1.9 ownerRepo.saveAndFlush -> one called
    5.2 update test is unsuccessfully - different emails
        5.2.1 boolean success == false
        5.2.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        5.2.3 List<String> errorMessages -> size == 1
        5.2.4 errorMessages -> "It is forbidden to change the email address. Contact the administrator"
        5.2.5 result equals ownerDto
        5.2.6 validator.validate -> one called
        5.2.7 ownerRepo.findEmailById -> one called
        5.2.8 ownerRepo.existsById() -> one called
        5.2.9 ownerRepo.findTelephoneById -> no called
        5.2.10 ownerRepo.findByTelephone -> no called
        5.2.11 ownerRepo.saveAndFlush -> no called
    5.3 positive case update password and username
        5.3.1 boolean success == true
        5.3.2 HttpStatus httpStatus == HttpStatus.OK
        5.3.3 List<String> errorMessages -> size == 0
        5.3.4 result equals ownerDto
        5.3.5 ownerRepo.existsById() -> one called
        5.3.6 validator.validate -> one called
        5.3.7 ownerRepo.findEmailById -> one called
        5.3.8 ownerRepo.findTelephoneById -> one called
        5.3.9 ownerRepo.findByTelephone -> no called
        5.3.10 ownerRepo.saveAndFlush -> one called
    5.4 update test is unsuccessfully - telephone already exist
        5.4.1 EntityConflictException
        5.4.2 errorMessages -> "User with this telephone already exist"
        5.4.3 exception.dto equals ownerDto
        5.4.4 validator.validate -> one called
        5.4.5 ownerRepo.findEmailById -> one called
        5.4.6 ownerRepo.findTelephoneById -> one called
        5.4.7 ownerRepo.findByTelephone -> one called
        5.4.8 ownerRepo.saveAndFlush -> no called
    5.5 positive case update when update telephone
        5.5.1 boolean success == true
        5.5.2 HttpStatus httpStatus == HttpStatus.OK
        5.5.3 List<String> errorMessages -> size == 0
        5.5.4 result equals ContactOwnerDto
        5.5.5 validator.validate -> one called
        5.5.6 ownerRepo.findEmailById -> one called
        5.5.7 ownerRepo.findTelephoneById -> one called
        5.5.9 ownerRepo.findByTelephone -> one called
        5.5.10 ownerRepo.saveAndFlush -> one called
    5.7 update test is unsuccessful -> The owner with id 14 does not exist
        5.7.1 boolean success == false
        5.7.2 HttpStatus httpStatus == HttpStatus.NO_CONTENT
        5.7.3 List<String> errorMessages -> size == 1
        5.7.4 errorMessages -> "The owner with id 14 does not exist"
        5.7.5 result equals ContactOwnerDto
        5.7.6 validator.validate -> one called
        5.7.7 ownerRepo.existsById -> one called
        5.7.8 ownerRepo.findEmailById -> no called
        5.7.9 ownerRepo.findTelephoneById -> no called
        5.7.10 ownerRepo.findByEmail -> no called
        5.7.11 ownerRepo.findByTelephone -> no called
        5.7.12 ownerRepo.saveAndFlush -> no called
 */

@ExtendWith({MockitoExtension.class})
class ContactOwnerServiceImplTest {
    @Mock
    private ContactOwnerRepo ownerRepo;
    @Mock
    private Validator validator;
    private final ModelMapper modelMapper = new ModelMapper();
    private ContactOwnerServiceImpl contactOwnerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        contactOwnerService = new ContactOwnerServiceImpl(ownerRepo, modelMapper, validator);
    }

    @Test
    void shouldGetContacts() {
        /*
        1.1 get list contactOwners
        1.1.1 boolean success == true
        1.1.2 HttpStatus httpStatus == HttpStatus.ok
        1.1.3 List<String> errorMessages -> size == 0
        1.1.4 List<String> owners -> size == 10 and value.equals
        1.1.5 ownerRepo.findAll -> one called
        */
        when(ownerRepo.findAll()).thenReturn(initContactOwners());

        ServerResponse<List<ContactOwnerDto>> response = contactOwnerService.getOwners();

        verify(ownerRepo, times(1)).findAll();

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());
        assertEquals(10, response.getResult().size());
        assertEquals(mapToContactOwnerDto(initContactOwners()), response.getResult());
    }

    @Test
    void shouldGetEmptyListContacts() {
        /*
        1.2 no owners -> empty list
        1.2.1 boolean success == true
        1.2.2 HttpStatus httpStatus == HttpStatus.ok
        1.2.3 List<String> errorMessages -> size == 0
        1.2.4 List<String> contactOwners -> size == 0
        1.2.5 ownerRepo.findAll -> one called
        */
        when(ownerRepo.findAll()).thenReturn(List.of());

        ServerResponse<List<ContactOwnerDto>> response = contactOwnerService.getOwners();

        verify(ownerRepo, times(1)).findAll();

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
        2.1.4 result.contactOwner id == 0
        2.1.5 result.contactOwner username == Name0
        2.1.6 result.contactOwner birthday == now - 20year
        2.1.7 result.contactOwner telephone == +7 234 567 89 20
        2.1.8 result.contactOwner email == email0@mail.ru
        2.1.9 ownerRepo.findById -> one called
        */
        ContactOwner prevOwner = initContactOwners().get(0);
        when(ownerRepo.findById(anyLong())).thenReturn(Optional.of(prevOwner));

        ServerResponse<ContactOwnerDto> response = contactOwnerService.getOwnerById(0);

        verify(ownerRepo, times(1)).findById(anyLong());

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());

        assertEquals(mapToContactOwnerDto(List.of(prevOwner)).get(0), response.getResult());
    }

    @Test
    void getContactOwnerByNotExistingId() {
        /*
        2.2.1 NotFoundException
        2.2.2 errorMessages -> "The owner with id 13 does not exist"
        2.2.3 exception.dto is null
        2.2.4 ownerRepo.findById -> one called
         */
        when(ownerRepo.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            contactOwnerService.getOwnerById(13);
        });

        verify(ownerRepo, times(1)).findById(anyLong());

        assertEquals("The owner with id 13 does not exist", exception.getMessage());
        assertNull(exception.getDto());
    }

    @Test
    void shouldCreateContactWhenContactOwnersIsEmpty() {
        /*
        3.1.1 boolean success == true
        3.1.2 HttpStatus httpStatus == HttpStatus.created
        3.1.3 List<String> errorMessages -> size == 0
        3.1.4 result.contact id == 0
        3.1.5 result.contact username == Name0
        3.1.6 result.contact telephone == +7 123 456-78-90
        3.1.7 result.contact email == email0@mail.ru
        3.1.8 validator.validate -> one called
        3.1.9 ownerRepo.findByEmail -> one called
        3.1.10 ownerRepo.findByTelephone -> one called
        3.1.11 ownerRepo.saveAndFlush -> one called
         */
        ContactOwner owner = initContactOwners().get(0);

        ContactCreateOwnerDto dto = new ContactCreateOwnerDto(
                owner.getUsername(),
                owner.getBirthday(),
                owner.getEmail(),
                owner.getTelephone(),
                owner.getPassword(),
                owner.getRole()
        );

        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(ownerRepo.findByTelephone(any(String.class))).thenReturn(Optional.empty());
        when(ownerRepo.saveAndFlush(any(ContactOwner.class))).thenReturn((owner));

        ServerResponse<ContactOwnerDto> response = contactOwnerService.createOwner(dto);

        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findByEmail(anyString());
        verify(ownerRepo, times(1)).findByTelephone(anyString());
        verify(ownerRepo, times(1)).saveAndFlush(any(ContactOwner.class));

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.CREATED, response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());

        assertEquals(mapToContactOwnerDto(List.of(owner)).get(0), response.getResult());
    }

    @Test
    void ShouldCreateContactWhenContactOwnersIsNonEmpty() {
        /*
        3.2 create
            3.2.1 boolean success == true
            3.2.2 HttpStatus httpStatus == HttpStatus.created
            3.2.3 List<String> errorMessages -> size == 0
            3.2.4 result.contact id == 10
            3.2.5 result.contact username == Name0
            3.2.6 result.contact telephone == +7 123 456-78-90
            3.2.7 result.contact email == email0@mail.ru
            3.2.8 validator.validate -> one called
            3.2.9 ownerRepo.findByEmail -> one called
            3.2.10 ownerRepo.findByTelephone -> one called
            3.2.11 ownerRepo.saveAndFlush -> one called
         */

        ContactOwner owner = initContactOwners().get(0);

        ContactCreateOwnerDto dto = new ContactCreateOwnerDto(
                owner.getUsername(),
                owner.getBirthday(),
                owner.getEmail(),
                owner.getTelephone(),
                owner.getPassword(),
                owner.getRole()
        );

        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(ownerRepo.findByTelephone(any(String.class))).thenReturn(Optional.empty());
        when(ownerRepo.saveAndFlush(any(ContactOwner.class))).thenReturn(owner);

        ServerResponse<ContactOwnerDto> response = contactOwnerService.createOwner(dto);

        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findByEmail(anyString());
        verify(ownerRepo, times(1)).findByTelephone(anyString());
        verify(ownerRepo, times(1)).saveAndFlush(any(ContactOwner.class));

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.CREATED, response.getHttpStatus());
        assertEquals(0, response.getErrorMessages().size());

        assertEquals(mapToContactOwnerDto(List.of(owner)).get(0), response.getResult());
    }

    @Test
    void shouldNotCreateContactWhenErrorCreatingOwner() {
        /*
        3.3 create
            3.3.1 EntityConflictException
            3.3.2 errorMessages -> "Error creating owner"
            3.3.3 exception.dto equals ownerDto
            3.3.4 ownerRepo.saveAndFlush -> one called
            3.3.5 validator.validate -> one called
            3.3.6 ownerRepo.findByEmail -> one called
            3.3.7 ownerRepo.findByTelephone -> one called
         */
        ContactOwner owner = initContactOwners().get(0);

        ContactCreateOwnerDto dto = new ContactCreateOwnerDto(
                owner.getUsername(),
                owner.getBirthday(),
                owner.getEmail(),
                owner.getTelephone(),
                owner.getPassword(),
                owner.getRole()
        );

        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(ownerRepo.findByTelephone(any(String.class))).thenReturn(Optional.empty());
        when(ownerRepo.saveAndFlush(any(ContactOwner.class))).thenThrow(DataIntegrityViolationException.class);

        EntityConflictException exception = assertThrows(EntityConflictException.class, () -> {
                    contactOwnerService.createOwner(dto);
                }
        );

        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findByEmail(any(String.class));
        verify(ownerRepo, times(1)).findByTelephone(any(String.class));
        verify(ownerRepo, times(1)).saveAndFlush(any(ContactOwner.class));

        assertEquals("Error creating owner", exception.getMessage());
        assertEquals(modelMapper.map(dto, ContactOwnerDto.class), exception.getDto());
    }

    @Test
    void shouldNotCreateContactWhenEmailAlreadyExist() {
        /*
        3.4 create When Email Already Exist
            3.4.1 EntityConflictException
            3.4.2 errorMessages -> "User with this email already exist"
            3.4.3 exception.dto equals ownerDto
            3.4.4 ownerRepo.saveAndFlush -> no called
            3.4.5 validator.validate -> one called
            3.4.6 ownerRepo.findByEmail -> one called
            3.4.7 ownerRepo.findByTelephone -> no called
         */
        ContactOwner owner = initContactOwners().get(0);

        ContactCreateOwnerDto dto = new ContactCreateOwnerDto(
                owner.getUsername(),
                owner.getBirthday(),
                owner.getEmail(),
                owner.getTelephone(),
                owner.getPassword(),
                owner.getRole()
        );

        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.findByEmail(any(String.class))).thenReturn(Optional.of(owner));

        EntityConflictException exception = assertThrows(EntityConflictException.class, () -> {
            contactOwnerService.createOwner(dto);
        });

        assertEquals("User with this email already exist", exception.getMessage());
        assertEquals(modelMapper.map(dto, ContactOwnerDto.class), exception.getDto());

        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findByEmail(any(String.class));
        verify(ownerRepo, never()).findByTelephone(any(String.class));
        verify(ownerRepo, never()).saveAndFlush(any(ContactOwner.class));
    }

    @Test
    void shouldNotCreateContactWhenTelephoneAlreadyExist() {
        /*
        3.5 create When telephone Already Exist
        3.5.1 EntityConflictException
        3.5.2 errorMessages -> "User with this telephone already exist"
        3.5.3 exception.dto equals ownerDto
        3.5.4 ownerRepo.saveAndFlush -> no called
        3.5.5 validator.validate -> one called
        3.5.6 ownerRepo.findByEmail -> one called
        3.5.7 ownerRepo.findByTelephone -> one called
         */
        ContactOwner owner = initContactOwners().get(0);

        ContactCreateOwnerDto dto = new ContactCreateOwnerDto(
                owner.getUsername(),
                owner.getBirthday(),
                owner.getEmail(),
                owner.getTelephone(),
                owner.getPassword(),
                owner.getRole()
        );

        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(ownerRepo.findByTelephone(any(String.class))).thenReturn(Optional.of(owner));

        EntityConflictException exception = assertThrows(EntityConflictException.class, () -> {
                    contactOwnerService.createOwner(dto);
                }
        );

        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findByEmail(anyString());
        verify(ownerRepo, times(1)).findByTelephone(anyString());
        verify(ownerRepo, never()).saveAndFlush(any(ContactOwner.class));

        assertEquals("User with this telephone already exist", exception.getMessage());
        assertEquals(modelMapper.map(dto, ContactOwnerDto.class), exception.getDto());
    }

    @Test
    void shouldDeleteOwnerByIdUnsuccessfully() {
        /*
        4.1.1 InternalServerException
        4.1.2 errorMessages -> "Unexpected server error during deletion operation the owner with id 13"
        4.1.3 exception.dto equals ownerDto
        4.1.4 ownerRepo.findById -> two called
        4.1.5 ownerRepo.deleteById -> called once
         */
        ContactOwner prevOwner = initContactOwners().get(0);
        when(ownerRepo.findById(anyLong())).thenReturn(Optional.of(prevOwner));
        doNothing().when(ownerRepo).deleteById(anyLong());

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            contactOwnerService.deleteOwnerById(13);
        });

        verify(ownerRepo, times(2)).findById(anyLong());
        verify(ownerRepo, times(1)).deleteById(13L);


        assertEquals(mapToContactOwnerDto(List.of(prevOwner)).get(0), exception.getDto());
        assertEquals("Unexpected server error during deletion operation the owner with id 13",
                exception.getMessage());
    }

    @Test
    void shouldDeleteOwnerByIdSuccessfully() {
        /*
        4.2 positive case delete
        4.2.1 boolean success == true
        4.2.2 HttpStatus httpStatus == HttpStatus.OK
        4.2.3 List<String> errorMessages -> size == 0
        4.2.4 result equals ownerDto
        4.2.5 ownerRepo.findById -> two called
        4.2.6 ownerRepo.deleteById -> one called
         */
        ContactOwner prevOwner = initContactOwners().get(0);
        ContactOwnerDto expectedDto = mapToContactOwnerDto(List.of(prevOwner)).get(0);

        when(ownerRepo.findById(7L))
                .thenReturn(Optional.of(prevOwner)) // 1-й вызов
                .thenReturn(Optional.empty());      // 2-й вызов

        doNothing().when(ownerRepo).deleteById(7L);

        // Act
        ServerResponse<ContactOwnerDto> response = contactOwnerService.deleteOwnerById(7L);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(expectedDto, response.getResult());
        assertTrue(response.getErrorMessages().isEmpty());

        verify(ownerRepo, times(2)).findById(7L);
        verify(ownerRepo, times(1)).deleteById(7L);
    }

    @Test
    void shouldUpdateOwnerWhenTelephoneAlreadyExist() {
        /*
        5.4 update test is unsuccessfully - telephone already exist
        5.4.1 EntityConflictException
        5.4.2 errorMessages -> "User with this telephone already exist"
        5.4.3 exception.dto equals ownerDto
        5.4.4 validator.validate -> one called
        5.4.5 ownerRepo.findEmailById -> one called
        5.4.6 ownerRepo.findTelephoneById -> one called
        5.4.7 ownerRepo.findByTelephone -> one called
        5.4.8 ownerRepo.saveAndFlush -> no called
         */
        ContactOwner owner = initContactOwners().get(0);
        ContactOwnerDto dto = modelMapper.map(owner, ContactOwnerDto.class);
        dto.setTelephone("+7 999 999-99-99");

        when(ownerRepo.existsById(anyLong())).thenReturn(true);
        when(ownerRepo.findEmailById(anyLong())).thenReturn(Optional.of(owner.getEmail()));
        when(ownerRepo.findTelephoneById(anyLong())).thenReturn(Optional.of("+7 888 888-88-88"));
        when(ownerRepo.findByTelephone(anyString())).thenReturn(Optional.of(owner));
        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());

        EntityConflictException exception = assertThrows(EntityConflictException.class, () -> {
                    contactOwnerService.updateOwner(dto);
                }
        );

        verify(ownerRepo, times(1)).existsById(anyLong());
        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findEmailById(anyLong());
        verify(ownerRepo, times(1)).findTelephoneById(anyLong());
        verify(ownerRepo, times(1)).findByTelephone(anyString());
        verify(ownerRepo, never()).saveAndFlush(any(ContactOwner.class));

        assertEquals(dto, exception.getDto());
        assertEquals("User with this telephone already exist", exception.getMessage());
    }

    @Test
    void shouldUpdateOwnerSuccessfullyWithTelephoneChange() {
        /*
        5.5 positive case update when update telephone
        5.5.1 boolean success == true
        5.5.2 HttpStatus httpStatus == HttpStatus.OK
        5.5.3 List<String> errorMessages -> size == 0
        5.5.4 result equals ContactOwnerDto
        5.5.5 validator.validate -> one called
        5.5.6 ownerRepo.findEmailById -> one called
        5.5.7 ownerRepo.findTelephoneById -> one called
        5.5.9 ownerRepo.findByTelephone -> one called
        5.5.10 ownerRepo.saveAndFlush -> one called
         */
        ContactOwner owner = initContactOwners().get(0);
        ContactOwnerDto dto = modelMapper.map(owner, ContactOwnerDto.class);
        dto.setTelephone("+7 999 999-99-99");

        when(ownerRepo.existsById(anyLong())).thenReturn(true);
        when(ownerRepo.findEmailById(anyLong())).thenReturn(Optional.of(owner.getEmail()));
        when(ownerRepo.findTelephoneById(anyLong())).thenReturn(Optional.of("+7 888 888-88-88"));
        when(ownerRepo.findByTelephone(anyString())).thenReturn(Optional.empty());
        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.saveAndFlush(any(ContactOwner.class))).thenReturn(owner);

        ServerResponse<ContactOwnerDto> response = contactOwnerService.updateOwner(dto);

        verify(ownerRepo, times(1)).existsById(anyLong());
        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findEmailById(anyLong());
        verify(ownerRepo, times(1)).findTelephoneById(anyLong());
        verify(ownerRepo, times(1)).findByTelephone(anyString());
        verify(ownerRepo, times(1)).saveAndFlush(any(ContactOwner.class));

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(dto, response.getResult());
        assertTrue(response.getErrorMessages().isEmpty());
    }

    @Test
    void getContactOwnerByNotExistingIdForUpdate() {
        /*
        5.7 update test is unsuccessful -> The owner with id 14 does not exist
        5.7.4 errorMessages -> "The owner with id 14 does not exist"
        5.7.5 exception.dto equals ContactOwnerDto
        5.7.6 validator.validate -> one called
        5.7.7 ownerRepo.existsById -> one called
        5.7.8 ownerRepo.findEmailById -> no called
        5.7.9 ownerRepo.findTelephoneById -> no called
        5.7.10 ownerRepo.findByEmail -> no called
        5.7.11 ownerRepo.findByTelephone -> no called
        5.7.12 ownerRepo.saveAndFlush -> no called
         */
        ContactOwner owner = initContactOwners().get(0);
        ContactOwnerDto dto = modelMapper.map(owner, ContactOwnerDto.class);
        dto.setId(14L);

        when(ownerRepo.existsById(14L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            contactOwnerService.updateOwner(dto);
        });

        verify(ownerRepo, times(1)).existsById(14L);
        verify(ownerRepo, never()).findEmailById(anyLong());
        verify(ownerRepo, never()).findTelephoneById(anyLong());
        verify(ownerRepo, never()).findByEmail(anyString());
        verify(ownerRepo, never()).findByTelephone(anyString());
        verify(ownerRepo, never()).saveAndFlush(any(ContactOwner.class));

        assertEquals(dto, exception.getDto());
        assertEquals("The owner with id 14 does not exist", exception.getMessage());
    }

    @Test
    void shouldGetExceptionErrorUpdatingOwner() {
        /*
        5.1.1 EntityConflictException
        5.1.2 errorMessages -> "Error update owner"
        5.1.3 exception.dto equals ownerDto
        5.1.4 ownerRepo.existsById -> one called
        5.1.5 validator.validate -> one called
        5.1.6 ownerRepo.findEmailById -> one called
        5.1.7 ownerRepo.findTelephoneById -> one called
        5.1.8 ownerRepo.findByTelephone -> no called
        5.1.9 ownerRepo.saveAndFlush -> one called
         */
        ContactOwner owner = initContactOwners().get(0);

        ContactOwnerDto dto = modelMapper.map(owner, ContactOwnerDto.class);

        when(ownerRepo.existsById(anyLong())).thenReturn(true);
        when(ownerRepo.findEmailById(anyLong())).thenReturn(Optional.of(owner.getEmail()));
        when(ownerRepo.findTelephoneById(anyLong())).thenReturn(Optional.of(PhoneNormalizer.normalize(owner.getTelephone())));
        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.saveAndFlush(any(ContactOwner.class))).thenThrow(DataIntegrityViolationException.class);

        EntityConflictException exception = assertThrows(EntityConflictException.class, () -> {
                    contactOwnerService.updateOwner(dto);
                }
        );

        verify(ownerRepo, times(1)).existsById(owner.getId());
        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findEmailById(anyLong());
        verify(ownerRepo, times(1)).findTelephoneById(anyLong());
        verify(ownerRepo, never()).findByTelephone(anyString());
        verify(ownerRepo, times(1)).saveAndFlush(any(ContactOwner.class));

        assertEquals(mapToContactOwnerDto(List.of(owner)).get(0), exception.getDto());
        assertEquals("Error update owner", exception.getMessage());
    }

    @Test
    void shouldGetExceptionDifferentEmailsErrorWhenUpdatingOwner() {
        ContactOwner owner = initContactOwners().get(0);
        /*
        5.2.4 errorMessages -> "It is forbidden to change the email address. Contact the administrator"
        5.2.5 exception.dto equals ownerDto
        5.2.6 validator.validate -> one called
        5.2.7 ownerRepo.findEmailById -> one called
        5.2.8 ownerRepo.existsById() -> one called
        5.2.9 ownerRepo.findTelephoneById -> no called
        5.2.10 ownerRepo.findByTelephone -> no called
        5.2.11 ownerRepo.saveAndFlush -> no called
         */

        ContactOwnerDto dto = modelMapper.map(owner, ContactOwnerDto.class);

        when(ownerRepo.existsById(anyLong())).thenReturn(true);
        when(ownerRepo.findEmailById(anyLong())).thenReturn(Optional.of("email@gmail.com"));
        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());

        EntityConflictException exception = assertThrows(EntityConflictException.class, () -> {
            contactOwnerService.updateOwner(dto);
        });

        verify(ownerRepo, times(1)).existsById(dto.getId());
        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findEmailById(anyLong());
        verify(ownerRepo, never()).findTelephoneById(anyLong());
        verify(ownerRepo, never()).findByTelephone(anyString());
        verify(ownerRepo, never()).saveAndFlush(any(ContactOwner.class));


        assertEquals(mapToContactOwnerDto(List.of(owner)).get(0), exception.getDto());
        assertEquals("It is forbidden to change the email address. Contact the administrator",
                exception.getMessage());
    }

    @Test
    void shouldUpdateOwnerSuccessfully() {
         /*
        5.3 positive case update password and username
        5.3.1 boolean success == true
        5.3.2 HttpStatus httpStatus == HttpStatus.OK
        5.3.3 List<String> errorMessages -> size == 0
        5.3.4 result equals ownerDto
        5.3.5 ownerRepo.existsById() -> one called
        5.3.6 validator.validate -> one called
        5.3.7 ownerRepo.findEmailById -> one called
        5.3.8 ownerRepo.findTelephoneById -> one called
        5.3.9 ownerRepo.findByTelephone -> no called
        5.3.10 ownerRepo.saveAndFlush -> one called
         */
        ContactOwner owner = initContactOwners().get(0);

        when(ownerRepo.existsById(anyLong())).thenReturn(true);
        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.saveAndFlush(any(ContactOwner.class))).thenReturn(owner);
        when(ownerRepo.findEmailById(anyLong())).thenReturn(Optional.of(owner.getEmail()));
        when(ownerRepo.findTelephoneById(anyLong())).thenReturn(Optional.of(PhoneNormalizer.normalize(owner.getTelephone())));

        ServerResponse<ContactOwnerDto> response = contactOwnerService.updateOwner(modelMapper.map(owner, ContactOwnerDto.class));

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(modelMapper.map(owner, ContactOwnerDto.class), response.getResult());
        assertTrue(response.getErrorMessages().isEmpty());

        verify(ownerRepo, times(1)).existsById(owner.getId());
        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findEmailById(anyLong());
        verify(ownerRepo, times(1)).findTelephoneById(anyLong());
        verify(ownerRepo, never()).findByTelephone(anyString());
        verify(ownerRepo, times(1)).saveAndFlush(any(ContactOwner.class));
    }

    @Test
    void shouldUpdateOwnerSuccessfullyWhenEmailDiffersOnlyByCase() {
    /*
    Нормализация email: обновление с тем же email, но в другом регистре,
    не должно считаться попыткой смены email
    */
        ContactOwner owner = initContactOwners().get(0);
        ContactOwnerDto dto = modelMapper.map(owner, ContactOwnerDto.class);
        dto.setEmail(owner.getEmail().toUpperCase());

        when(ownerRepo.existsById(anyLong())).thenReturn(true);
        when(ownerRepo.findEmailById(anyLong())).thenReturn(Optional.of(owner.getEmail()));
        when(ownerRepo.findTelephoneById(anyLong()))
                .thenReturn(Optional.of(PhoneNormalizer.normalize(owner.getTelephone())));
        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.saveAndFlush(any(ContactOwner.class))).thenReturn(owner);

        ServerResponse<ContactOwnerDto> response = contactOwnerService.updateOwner(dto);

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());

        verify(ownerRepo, never()).findByTelephone(anyString());
        verify(ownerRepo, times(1)).saveAndFlush(any(ContactOwner.class));
    }


    private List<ContactOwner> initContactOwners() {
        List<ContactOwner> owners = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            owners.add(
                    ContactOwner.builder()
                            .id((long) i)
                            .username("Name" + i)
                            .email("email" + i + "@mail.ru")
                            .password("email" + i + "@mail.ru")
                            .role(Role.ROLE_USER)
                            .birthday(LocalDate.now().minusYears(20))
                            .telephone("+7 (123) 456-78-9" + i)
                            .build()
            );
        }
        return owners;
    }

    private List<ContactOwnerDto> mapToContactOwnerDto(List<ContactOwner> owners) {
        return owners.stream().map(contact -> modelMapper.map(contact, ContactOwnerDto.class)).toList();
    }
}