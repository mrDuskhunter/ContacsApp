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
import ru.duskhunter.contacsapp.dto.contactowner.ContactCreateOwnerDto;
import ru.duskhunter.contacsapp.dto.contactowner.ContactOwnerDto;
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
        1.1.5 contactOwners.findAll -> one called
    1.2 no owners -> empty list
        1.2.1 boolean success == true
        1.2.2 HttpStatus httpStatus == HttpStatus.ok
        1.2.3 List<String> errorMessages -> size == 0
        1.2.4 List<String> contactOwners -> size == 0
        1.2.5 contacts.findAll -> one called
    2. getContactOwnerById
    2.1 get contactOwner
        2.1.1 boolean success == true
        2.1.2 HttpStatus httpStatus == HttpStatus.ok
        2.1.3 HttpStatusEntity == HttpStatus.ok
        2.1.4 List<String> errorMessages -> size == 0
        2.1.5 result.contactOwner id == 0
        2.1.6 result.contactOwner username == Name0
        2.1.7 result.contactOwner birthday == now - 20year
        2.1.8 result.contactOwner telephone == +7 (234) 567 89 20
        2.1.9 result.contactOwner email == email0@mail.ru
        2.1.10 contacts.findById -> one called
    2.2 no contact with this id -> httpStatus 204
        2.2.1 boolean success == false
        2.2.2 HttpStatus httpStatus == HttpStatus.NO_CONTENT
        2.2.3 HttpStatusEntity == HttpStatus.NO_CONTENT
        2.2.4 List<String> errorMessages -> size == 1
        2.2.5 errorMessages -> "The owner with id 13 does not exist"
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
    3.3 create when Error creating owner
        3.3.1 boolean success == false
        3.3.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        3.3.3 List<String> errorMessages -> size == 1
        3.3.4 errorMessages -> "Error creating owner"
        3.3.5 result equals ownerDto
        3.3.6 contacts.saveAndFlush -> one called
        3.3.7 validator.validate -> one called
        3.3.8 contacts.findByEmail -> one called
    3.4 create When Email Already Exist
        3.4.1 boolean success == false
        3.4.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        3.4.3 List<String> errorMessages -> size == 1
        3.4.4 errorMessages -> "User with this email already exist"
        3.4.5 result equals ownerDto
        3.4.6 contacts.saveAndFlush -> no called
        3.4.7 validator.validate -> one called
        3.4.8 contacts.findByEmail -> one called
    4 delete
    4.1 delete test is unsuccessfully
        4.1.1 boolean success == false
        4.1.2 HttpStatus httpStatus == HttpStatus.INTERNAL_SERVER_ERROR
        4.1.3 List<String> errorMessages -> size == 1
        4.1.4 errorMessages -> "Unexpected server error during deletion operation the owner with id 13"
        4.1.5 result equals ownerDto
        4.1.6 contacts.findById -> two called
    4.2 positive case delete
        4.2.1 boolean success == true
        4.2.2 HttpStatus httpStatus == HttpStatus.OK
        4.2.3 List<String> errorMessages -> size == 0
        4.2.4 result equals ownerDto
        4.2.5 contacts.findById -> two called
        4.2.6 contacts.deleteById -> one called
    5 update
    5.1 delete test is unsuccessfully - error update
        5.1.1 boolean success == false
        5.1.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        5.1.3 List<String> errorMessages -> size == 1
        5.1.4 errorMessages -> "Error update owner"
        5.1.5 result equals ownerDto
        5.1.6 contacts.findAll -> one called
        5.1.7 contacts.validate -> one called
        5.1.8 contacts.findEmailByOwnerId -> one called
        5.1.9 contacts.saveAndFlush -> one called
    5.2 delete test is unsuccessfully - different emails
        5.2.1 boolean success == false
        5.2.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        5.2.3 List<String> errorMessages -> size == 1
        5.2.4 errorMessages -> "These users have different emails"
        5.2.5 result equals ownerDto
        5.2.6 contacts.saveAndFlush -> no called
        5.2.7 contacts.validate -> no called
        5.2.8 contacts.findEmailByOwnerId -> one called
        5.2.9 contacts.findAll() -> one called
    5.3 positive case delete
        5.3.1 boolean success == true
        5.3.2 HttpStatus httpStatus == HttpStatus.OK
        5.3.3 List<String> errorMessages -> size == 0
        5.3.4 result equals ownerDto
        5.3.5 contacts.saveAndFlush -> one called
        5.3.6 contacts.validate -> one called
        5.3.7 contacts.findEmailByOwnerId -> one called
        5.3.8 contacts.findAll() -> one called
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
        1.1.5 contactOwners.findAll -> one called
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
        1.2.5 contacts.findAll -> one called
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
        2.1.3 HttpStatusEntity == HttpStatus.ok
        2.1.4 List<String> errorMessages -> size == 0
        2.1.5 result.contact equals
        2.1.6 contacts.findById -> one called
        */
        ContactOwner prevOwner = initContactOwners().get(0);
        when(ownerRepo.findById(anyLong())).thenReturn(Optional.of(prevOwner));

        ResponseEntity<ServerResponse<ContactOwnerDto>> response = contactOwnerService.getOwnerById(0);

        verify(ownerRepo, times(1)).findById(anyLong());

        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK, response.getBody().getHttpStatus());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getErrorMessages().size());

        assertEquals(mapToContactOwnerDto(List.of(prevOwner)).get(0), response.getBody().getResult());
    }

    @Test
    void getContactOwnerByNotExistingId() {
        /*
        2.2.1 boolean success == false
        2.2.2 HttpStatus httpStatus == HttpStatus.NO_CONTENT
        2.2.3 HttpStatusEntity == HttpStatus.NO_CONTENT
        2.2.4 List<String> errorMessages -> size == 1
        2.2.5 errorMessages -> "The contact with id 13 does not exist"
        2.2.6 result is null
        2.2.7 contacts.findById -> one called
         */
        when(ownerRepo.findById(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<ServerResponse<ContactOwnerDto>> response = contactOwnerService.getOwnerById(13);

        verify(ownerRepo, times(1)).findById(anyLong());

        assertFalse(response.getBody().isSuccess());
        assertEquals(HttpStatus.NO_CONTENT, response.getBody().getHttpStatus());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(1, response.getBody().getErrorMessages().size());
        assertEquals("The owner with id 13 does not exist", response.getBody().getErrorMessages().get(0));
        assertNull(response.getBody().getResult());
    }

    @Test
    void shouldCreateContactWhenContactOwnersIsEmpty() {
        /*
        3.1 create
            3.1.1 boolean success == true
            3.1.2 HttpStatus httpStatus == HttpStatus.created
            3.1.3 List<String> errorMessages -> size == 0
            3.1.4 result.contactOwner equals
            3.1.5 contacts.saveAndFlush -> one called
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
        when(ownerRepo.saveAndFlush(any(ContactOwner.class))).thenReturn((owner));

        ServerResponse<ContactOwnerDto> response = contactOwnerService.createOwner(dto);

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
            3.2.4 result.contactOwner equals
            3.2.5 contacts.saveAndFlush -> one called
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
        when(ownerRepo.saveAndFlush(any(ContactOwner.class))).thenReturn(owner);

        ServerResponse<ContactOwnerDto> response = contactOwnerService.createOwner(dto);

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
            3.3.1 boolean success == false
            3.3.2 HttpStatus httpStatus == HttpStatus.CONFLICT
            3.3.3 List<String> errorMessages -> size == 1
            3.3.4 errorMessages -> "Error creating owner"
            3.3.5 result equals ownerDto
            3.3.6 contacts.saveAndFlush -> one called
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

//        ContactOwner owner1 = ContactOwner.builder()
//                .username(owner.getUsername())
//                .birthday(owner.getBirthday())
//                .email(owner.getEmail())
//                .telephone(owner.getTelephone())
//                .password(owner.getPassword())
//                .role(owner.getRole())
//                .build();

        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(ownerRepo.saveAndFlush(any(ContactOwner.class))).thenThrow(DataIntegrityViolationException.class);

        ServerResponse<ContactOwnerDto> response = contactOwnerService.createOwner(dto);

        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findByEmail(any(String.class));
        verify(ownerRepo, times(1)).saveAndFlush(any(ContactOwner.class));

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.CONFLICT, response.getHttpStatus());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("Error creating owner", response.getErrorMessages().get(0));
        assertEquals(modelMapper.map(dto,ContactOwnerDto.class), response.getResult());
    }

    @Test
    void shouldNotCreateContactWhenEmailAlreadyExist() {
        /*
        3.3 create When Email Already Exist
            3.4.1 boolean success == false
            3.4.2 HttpStatus httpStatus == HttpStatus.CONFLICT
            3.4.3 List<String> errorMessages -> size == 1
            3.4.4 errorMessages -> "User with this email already exist"
            3.4.5 result equals ownerDto
            3.4.6 contacts.saveAndFlush -> no called
            3.4.7 validator.validate -> one called
            3.4.8 contacts.findByEmail -> one called
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

//        ContactOwner owner1 = ContactOwner.builder()
//                .username(owner.getUsername())
//                .birthday(owner.getBirthday())
//                .email(owner.getEmail())
//                .telephone(owner.getTelephone())
//                .password(owner.getPassword())
//                .role(owner.getRole())
//                .build();

        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.findByEmail(any(String.class))).thenReturn(Optional.of(owner));

        ServerResponse<ContactOwnerDto> response = contactOwnerService.createOwner(dto);

        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findByEmail(any(String.class));
        verify(ownerRepo, times(0)).saveAndFlush(any(ContactOwner.class));

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.CONFLICT, response.getHttpStatus());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("User with this email already exist", response.getErrorMessages().get(0));
        assertEquals(modelMapper.map(dto,ContactOwnerDto.class), response.getResult());
    }

    @Test
    void shouldDeleteOwnerByIdUnsuccessfully() {
        /*
        4.1.1 boolean success == false
        4.1.2 HttpStatus httpStatus == HttpStatus.INTERNAL_SERVER_ERROR
        4.1.3 List<String> errorMessages -> size == 1
        4.1.4 errorMessages -> "Unexpected server error during deletion operation the owner with id 13"
        4.1.5 result equals ownerDto
        4.1.6 contacts.findById -> two called
         */
        ContactOwner prevOwner = initContactOwners().get(0);
        when(ownerRepo.findById(anyLong())).thenReturn(Optional.of(prevOwner));

        ServerResponse<ContactOwnerDto> response = contactOwnerService.deleteOwnerById(13);

        verify(ownerRepo, times(2)).findById(anyLong());

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
        assertEquals(mapToContactOwnerDto(List.of(prevOwner)).get(0), response.getResult());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("Unexpected server error during deletion operation the owner with id 13",
                response.getErrorMessages().get(0));
    }

    @Test
    void shouldDeleteOwnerByIdSuccessfully() {
        /*
        4.2 positive case delete
        4.2.1 boolean success == true
        4.2.2 HttpStatus httpStatus == HttpStatus.OK
        4.2.3 List<String> errorMessages -> size == 0
        4.2.4 result equals ownerDto
        4.2.5 contacts.findById -> two called
        4.2.6 contacts.deleteById -> one called
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
    void shouldGetExceptionErrorUpdatingOwner() {
        /*
        5.1.1 boolean success == false
        5.1.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        5.1.3 List<String> errorMessages -> size == 1
        5.1.4 errorMessages -> "Error update owner"
        5.1.5 result equals ownerDto
        5.1.6 contacts.findAll -> one called
        5.1.7 contacts.validate -> one called
        5.1.8 contacts.findEmailByOwnerId -> one called
        5.1.9 contacts.saveAndFlush -> one called
         */
        ContactOwner owner = initContactOwners().get(0);

        ContactOwnerDto dto = modelMapper.map(owner, ContactOwnerDto.class);

        when(ownerRepo.findAll()).thenReturn(initContactOwners());
        when(ownerRepo.findEmailById(anyLong())).thenReturn(owner.getEmail());
        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.saveAndFlush(any(ContactOwner.class))).thenThrow(DataIntegrityViolationException.class);

        ServerResponse<ContactOwnerDto> response = contactOwnerService.updateOwner(dto);

        verify(ownerRepo, times(1)).findAll();
        verify(ownerRepo, times(1)).findEmailById(anyLong());
        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).saveAndFlush(any(ContactOwner.class));

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.CONFLICT, response.getHttpStatus());
        assertEquals(mapToContactOwnerDto(List.of(owner)).get(0), response.getResult());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("Error update owner",
                response.getErrorMessages().get(0));
    }

    @Test
    void shouldGetExceptionDifferentEmailsErrorWhenUpdatingOwner() {
        ContactOwner owner = initContactOwners().get(0);
        /*
        5.2.1 boolean success == false
        5.2.2 HttpStatus httpStatus == HttpStatus.CONFLICT
        5.2.3 List<String> errorMessages -> size == 1
        5.2.4 errorMessages -> "These users have different emails"
        5.2.5 result equals ownerDto
        5.2.6 contacts.saveAndFlush -> no called
        5.2.7 contacts.validate -> no called
        5.2.8 contacts.findEmailByOwnerId -> one called
        5.2.9 contacts.findAll() -> one called
         */

        ContactOwnerDto dto = modelMapper.map(owner, ContactOwnerDto.class);

        when(ownerRepo.findAll()).thenReturn(initContactOwners());
        when(ownerRepo.findEmailById(anyLong())).thenReturn("email@gmail.com");

        ServerResponse<ContactOwnerDto> response = contactOwnerService.updateOwner(dto);

        verify(ownerRepo, times(1)).findAll();
        verify(ownerRepo, times(1)).findEmailById(anyLong());
        verify(validator, times(0)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(0)).saveAndFlush(any(ContactOwner.class));

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.CONFLICT, response.getHttpStatus());
        assertEquals(mapToContactOwnerDto(List.of(owner)).get(0), response.getResult());
        assertEquals(1, response.getErrorMessages().size());
        assertEquals("These users have different emails",
                response.getErrorMessages().get(0));
    }

    @Test
    void shouldUpdateOwnerSuccessfully() {
         /*
        5.3 positive case delete
        5.3.1 boolean success == true
        5.3.2 HttpStatus httpStatus == HttpStatus.OK
        5.3.3 List<String> errorMessages -> size == 0
        5.3.4 result equals ownerDto
        5.3.5 contacts.saveAndFlush -> one called
        5.3.6 contacts.validate -> one called
        5.3.7 contacts.findEmailByOwnerId -> one called
        5.3.8 contacts.findAll() -> one called
         */
        ContactOwner owner = initContactOwners().get(0);

        when(ownerRepo.findAll()).thenReturn(initContactOwners());
        when(validator.validate(any(ContactOwner.class))).thenReturn(List.of());
        when(ownerRepo.saveAndFlush(any(ContactOwner.class))).thenReturn(owner);
        when(ownerRepo.findEmailById(anyLong())).thenReturn(owner.getEmail());

        ServerResponse<ContactOwnerDto> response = contactOwnerService.updateOwner(modelMapper.map(owner, ContactOwnerDto.class));

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals(modelMapper.map(owner, ContactOwnerDto.class), response.getResult());
        assertTrue(response.getErrorMessages().isEmpty());

        verify(ownerRepo, times(1)).findAll();
        verify(validator, times(1)).validate(any(ContactOwner.class));
        verify(ownerRepo, times(1)).findEmailById(anyLong());
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
                            .role(Role.User)
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