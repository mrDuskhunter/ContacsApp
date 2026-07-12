package ru.duskhunter.contacsapp.model.repository;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.duskhunter.contacsapp.model.Role;
import ru.duskhunter.contacsapp.model.entity.Contact;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ContactRepoTest {
    @Container
    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16.9")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private ContactOwnerRepo contactOwnerRepo;

    @AfterEach
    void deleteAll() {
        contactRepo.deleteAll();
        contactOwnerRepo.deleteAll();
    }

    /*
    Test plane:
    1.CREATE
        1.1.NegativeCase firstName.
            1.1.1. firstName: null, "", " ", "   " -> "First name cannot be empty"
            1.1.2. firstName length < 2 -> "First name min size: 2, max size: 50"
            1.1.3. firstName length > 50 -> "First name min size: 2, max size: 50"
        1.2.NegativeCase lastName.
            1.2.1. lastName: null, "", " ", "   " -> "Last name cannot be empty"
            1.2.2. lastName length < 2 -> "Last name min size: 2, max size: 50"
            1.2.3. lastName length > 50 -> "Last name min size: 2, max size: 50"
        1.3.NegativeCase telephone.
            1.3.1. telephone null -> "telephone cannot be empty"
            1.3.2. telephone format invalid -> "incorrect tel.number"
            1.3.3. telephone duplicate -> unique constraint violation
        1.4.NegativeCase email.
            1.4.1. email null -> "Email cannot be empty"
            1.4.2. email format invalid -> "incorrect email"
            1.4.3. email duplicate -> unique constraint violation
        1.5.NegativeCase owner.
            1.5.1. owner null -> "Owner cannot be null"
        1.6.PositiveCase. id and created_at is not null
            1.6.1. string in DB was created
            1.6.2. id not null
            1.6.3. created_at not null
            1.6.4. validate: firstName, lastName, telephone, email, owner
    2.findEmailById
        2.1 if id exist return email
        2.2 if id not exist return empty Optional
    3.findTelephoneById
        3.1 if id exist return telephone
        3.2 if id not exist return empty Optional
    4.findByEmail
        4.1 if email exist return contact
        4.2 if email not exist return empty Optional
    5.findByTelephone
        5.1 if telephone exist return contact
        5.2 if telephone not exist return empty Optional
    */

    @Test
    void createContact() {
        /*
        1.6.PositiveCase. id and created_at is not null
            1.6.1. string in DB was created
            1.6.2. id not null
            1.6.3. created_at not null
            1.6.4. validate: firstName, lastName, telephone, email, owner
         */

        ContactOwner owner = createContactOwner("testUser");

        Contact contact = Contact.builder()
                .firstName("x".repeat(50))
                .lastName("x".repeat(50))
                .telephone("+79001112233")
                .email("x".repeat(23) + "@" + "x".repeat(22) + ".com")
                .owner(owner)
                .build();

        Contact newContact = contactRepo.save(contact);
        List<Contact> contacts = contactRepo.findAll();

        assertEquals(1, contacts.size());

        Contact savedContact = contacts.get(0);

        assertEquals(newContact.getId(), savedContact.getId());
        assertEquals(newContact.getFirstName(), savedContact.getFirstName());
        assertEquals(newContact.getLastName(), savedContact.getLastName());
        assertEquals(newContact.getTelephone(), savedContact.getTelephone());
        assertEquals(newContact.getEmail(), savedContact.getEmail());
        assertEquals(newContact.getOwner(), savedContact.getOwner());
        assertNotNull(savedContact.getCreatedAt());
    }

    @ParameterizedTest
    @MethodSource("invalidContactFirstName")
    void testSaveContactWithInvalidFirstName(String invalidFirstName, String expectedMessage) {
        /*
        1.1.NegativeCase firstName.
            1.1.1. firstName: null, "", " ", "   " -> "First name cannot be empty"
            1.1.2. firstName length > 50 -> "First name min size: 2, max size: 50"
         */

        ContactOwner owner = createContactOwner("testUser");

        Contact contact = Contact.builder()
                .firstName(invalidFirstName)
                .lastName("TestLastName")
                .telephone("+79001112233")
                .email("test@example.com")
                .owner(owner)
                .build();

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            contactRepo.save(contact);
        });

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();

        assertFalse(violations.isEmpty(), "Должно быть хотя бы одно нарушение");
        assertEquals(1, violations.size(), "Нарушений более 1");

        ConstraintViolation<?> violation = violations.iterator().next();

        String actualMessage = violation.getMessage();
        assertEquals(expectedMessage, actualMessage);

        entityManager.clear();
        assertEquals(0, contactRepo.findAll().size());
    }

    private static Stream<Arguments> invalidContactFirstName() {
        String expectedMessage = "First name cannot be empty";

        return Stream.of(
                Arguments.of(null, expectedMessage),
                Arguments.of("", expectedMessage),
                Arguments.of(" ", expectedMessage),
                Arguments.of("   ", expectedMessage),
                Arguments.of("x".repeat(51), "First name max size: 50")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidContactLastName")
    void testSaveContactWithInvalidLastName(String invalidLastName, String expectedMessage) {
        /*
        1.2.NegativeCase lastName.
            1.2.1. lastName: null, "", " ", "   " -> "Last name cannot be empty"
            1.2.2. lastName length > 50 -> "Last name min size: 2, max size: 50"
         */

        ContactOwner owner = createContactOwner("testUser");

        Contact contact = Contact.builder()
                .firstName("TestFirstName")
                .lastName(invalidLastName)
                .telephone("+79001112233")
                .email("test@example.com")
                .owner(owner)
                .build();

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            contactRepo.save(contact);
        });

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        assertFalse(violations.isEmpty(), "Должно быть хотя бы одно нарушение");
        assertEquals(1, violations.size(), "Нарушений более 1");

        ConstraintViolation<?> violation = violations.iterator().next();
        String actualMessage = violation.getMessage();
        assertEquals(expectedMessage, actualMessage);

        entityManager.clear();
        assertEquals(0, contactRepo.findAll().size());
    }

    private static Stream<Arguments> invalidContactLastName() {
        String expectedMessage = "Last name cannot be empty";

        return Stream.of(
                Arguments.of(null, expectedMessage),
                Arguments.of("", expectedMessage),
                Arguments.of(" ", expectedMessage),
                Arguments.of("   ", expectedMessage),
                Arguments.of("x".repeat(51), "Last name max size: 50")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidContactTelephone")
    void testSaveContactWithInvalidTelephone(String testCase, String invalidTelephone, String expectedMessage) {
        /*
        1.3.NegativeCase telephone.
            1.3.1. telephone null -> "telephone cannot be empty"
            1.3.2. telephone format invalid -> "incorrect tel.number"
         */

        ContactOwner owner = createContactOwner("testUser");

        Contact contact = Contact.builder()
                .firstName("TestFirstName")
                .lastName("TestLastName")
                .telephone(invalidTelephone)
                .email("test@example.com")
                .owner(owner)
                .build();

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            contactRepo.save(contact);
        });

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        assertFalse(violations.isEmpty(), "Должно быть хотя бы одно нарушение");
        assertEquals(1, violations.size(), "Нарушений более 1");

        ConstraintViolation<?> violation = violations.iterator().next();
        String actualMessage = violation.getMessage();
        assertEquals(expectedMessage, actualMessage);

        entityManager.clear();
        assertEquals(0, contactRepo.findAll().size());
    }

    private static Stream<Arguments> invalidContactTelephone() {
        return Stream.of(
                Arguments.of("null telephone", null, "Telephone cannot be empty"),
                Arguments.of("telephone too short after normalization", "+7 900 111 22", "Incorrect tel.number"),
                Arguments.of("telephone with wrong format", "89001112233", "Incorrect tel.number")
        );
    }

    @Test
    void testSaveContactWithDuplicateTelephone() {
        /*
        1.3.3. telephone duplicate -> unique constraint violation
         */

        ContactOwner owner = createContactOwner("testUser");

        Contact contact1 = Contact.builder()
                .firstName("TestFirstName1")
                .lastName("TestLastName1")
                .telephone("+79001112233")
                .email("test1@example.com")
                .owner(owner)
                .build();

        contactRepo.save(contact1);
        entityManager.flush();
        entityManager.clear();

        Contact contact2 = Contact.builder()
                .firstName("TestFirstName2")
                .lastName("TestLastName2")
                .telephone("+79001112233")
                .email("test2@example.com")
                .owner(owner)
                .build();


        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () ->
                {
                    contactRepo.save(contact2);
                    entityManager.flush();
                }
        );

        assertNotNull(exception.getMessage());
        entityManager.clear();
        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();
    }

    @ParameterizedTest
    @MethodSource("invalidContactEmail")
    void testSaveContactWithInvalidEmail(String testCase, String invalidEmail, String expectedMessage) {
        /*
        1.4.NegativeCase email.
            1.4.1. email null -> "Email cannot be empty"
            1.4.2. email format invalid -> "incorrect email"
         */

        ContactOwner owner = createContactOwner("testUser");

        Contact contact = Contact.builder()
                .firstName("TestFirstName")
                .lastName("TestLastName")
                .telephone("+79001112233")
                .email(invalidEmail)
                .owner(owner)
                .build();

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            contactRepo.save(contact);
        });

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        assertFalse(violations.isEmpty(), "Должно быть хотя бы одно нарушение");
        assertEquals(1, violations.size(), "Нарушений более 1");

        ConstraintViolation<?> violation = violations.iterator().next();
        String actualMessage = violation.getMessage();
        assertEquals(expectedMessage, actualMessage);

        entityManager.clear();
        assertEquals(0, contactRepo.findAll().size());
    }

    private static Stream<Arguments> invalidContactEmail() {
        return Stream.of(
                Arguments.of("null email", null, "Email cannot be empty"),
                Arguments.of("email without @", "testexample.com", "Incorrect email"),
                Arguments.of("email without .", "test@examplecom", "Incorrect email"),
                Arguments.of("email with space", "test example@example.com", "Incorrect email")
        );
    }

    @Test
    void testSaveContactWithDuplicateEmail() {
        /*
        1.4.3. email duplicate -> unique constraint violation
         */

        ContactOwner owner = createContactOwner("testUser");

        Contact contact1 = Contact.builder()
                .firstName("TestFirstName1")
                .lastName("TestLastName1")
                .telephone("+79001112233")
                .email("test@example.com")
                .owner(owner)
                .build();

        contactRepo.save(contact1);
        entityManager.flush();
        entityManager.clear();

        Contact contact2 = Contact.builder()
                .firstName("TestFirstName2")
                .lastName("TestLastName2")
                .telephone("+79001112299")
                .email("test@example.com")
                .owner(owner)
                .build();

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> {
            contactRepo.save(contact2);
            entityManager.flush();
        });

        assertNotNull(exception.getMessage());
        entityManager.clear();
        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();
    }

    @Test
    void testSaveContactWithNullOwner() {
        /*
        1.5.NegativeCase owner.
            1.5.1. owner null -> "Owner cannot be null"
         */
        Contact contact = Contact.builder()
                .firstName("TestFirstName")
                .lastName("TestLastName")
                .telephone("+79001112233")
                .email("test@example.com")
                .owner(null)
                .build();

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            contactRepo.save(contact);
        });

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        assertFalse(violations.isEmpty(), "Должно быть хотя бы одно нарушение");
        assertEquals(1, violations.size(), "Нарушений более 1");

        ConstraintViolation<?> violation = violations.iterator().next();
        String actualMessage = violation.getMessage();
        assertEquals("Owner cannot be null", actualMessage);

        entityManager.clear();
        assertEquals(0, contactRepo.findAll().size());
    }

    @Test
    void testFindEmailById_WithExistingId() {
        /*
        2.1 if id exist return email
         */

        ContactOwner owner = createContactOwner("testUser");

        Contact contact = Contact.builder()
                .firstName("TestFirstName")
                .lastName("TestLastName")
                .telephone("+79001112233")
                .email("test@example.com")
                .owner(owner)
                .build();

        Contact savedContact = contactRepo.save(contact);

        Optional<String> emailOptional = contactRepo.findEmailById(savedContact.getId());

        assertTrue(emailOptional.isPresent());
        assertEquals("test@example.com", emailOptional.get());
    }

    @Test
    void testFindEmailById_WithNonExistingId() {
        /*
        2.2 if id not exist return empty Optional
         */

        Optional<String> emailOptional = contactRepo.findEmailById(999999L);

        assertFalse(emailOptional.isPresent());
    }

    @Test
    void testFindTelephoneById_WithExistingId() {
        /*
        3.1 if id exist return telephone
         */

        ContactOwner owner = createContactOwner("testUser");

        Contact contact = Contact.builder()
                .firstName("TestFirstName")
                .lastName("TestLastName")
                .telephone("+79001112233")
                .email("test@example.com")
                .owner(owner)
                .build();

        Contact savedContact = contactRepo.save(contact);

        Optional<String> telephoneOptional = contactRepo.findTelephoneById(savedContact.getId());

        assertTrue(telephoneOptional.isPresent());
        assertEquals("+79001112233", telephoneOptional.get());
    }

    @Test
    void testFindTelephoneById_WithNonExistingId() {
        /*
        3.2 if id not exist return empty Optional
         */

        Optional<String> telephoneOptional = contactRepo.findTelephoneById(999999L);

        assertFalse(telephoneOptional.isPresent());
    }

    @Test
    void testFindByEmail_WithExistingEmail() {
        /*
        4.1 if email exist return contact
         */

        ContactOwner owner = createContactOwner("testUser");

        Contact contact = Contact.builder()
                .firstName("TestFirstName")
                .lastName("TestLastName")
                .telephone("+79001112233")
                .email("test@example.com")
                .owner(owner)
                .build();

        Contact savedContact = contactRepo.save(contact);

        Optional<Contact> foundContact = contactRepo.findByEmail("test@example.com");

        assertTrue(foundContact.isPresent());
        assertEquals(savedContact.getId(), foundContact.get().getId());
        assertEquals(savedContact.getFirstName(), foundContact.get().getFirstName());
        assertEquals(savedContact.getLastName(), foundContact.get().getLastName());
        assertEquals(savedContact.getTelephone(), foundContact.get().getTelephone());
    }

    @Test
    void testFindByEmail_WithNonExistingEmail() {
        /*
        4.2 if email not exist return empty Optional
         */

        Optional<Contact> foundContact = contactRepo.findByEmail("nonexistent@example.com");

        assertFalse(foundContact.isPresent());
    }

    @Test
    void testFindByTelephone_WithExistingTelephone() {
        /*
        5.1 if telephone exist return contact
         */

        ContactOwner owner = createContactOwner("testUser");

        Contact contact = Contact.builder()
                .firstName("TestFirstName")
                .lastName("TestLastName")
                .telephone("+79001112233")
                .email("test@example.com")
                .owner(owner)
                .build();

        Contact savedContact = contactRepo.save(contact);

        Optional<Contact> foundContact = contactRepo.findByTelephone("+79001112233");

        assertTrue(foundContact.isPresent());
        assertEquals(savedContact.getId(), foundContact.get().getId());
        assertEquals(savedContact.getFirstName(), foundContact.get().getFirstName());
        assertEquals(savedContact.getLastName(), foundContact.get().getLastName());
        assertEquals(savedContact.getEmail(), foundContact.get().getEmail());
    }

    @Test
    void testFindByTelephone_WithNonExistingTelephone() {
        /*
        5.2 if telephone not exist return empty Optional
         */

        Optional<Contact> foundContact = contactRepo.findByTelephone("+79000000000");

        assertFalse(foundContact.isPresent());
    }

    private ContactOwner createContactOwner(String username) {
        ContactOwner owner = ContactOwner.builder()
                .username(username)
                .birthday(LocalDate.now().minusYears(20))
                .email(username + "@example.com")
                .telephone("+79001112233")
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(Role.ROLE_USER)
                .build();

        return contactOwnerRepo.save(owner);
    }
}
