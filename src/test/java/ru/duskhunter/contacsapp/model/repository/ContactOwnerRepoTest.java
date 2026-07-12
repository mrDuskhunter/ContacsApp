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
import ru.duskhunter.contacsapp.model.entity.ContactOwner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ContactOwnerRepoTest {
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
    private ContactOwnerRepo contactOwnerRepo;

    /*
    Test plane:
    1.CREATE
        1.1.NegativeCase Username.
            1.1.1. username: null, "", " ", "   " -> "Username cannot be empty"
            1.1.2. username length > 50  -> "Username max size: 50"
        1.2.NegativeCase birthday.
            1.2.1. birthday null -> "Birthday cannot be empty"
            1.2.2. birthday in future or now -> "Birthday must be in the past"
        1.3.NegativeCase email.
            1.3.1. email null -> "Email cannot be empty"
            1.3.2. email > 50, without @ or . -> "Incorrect email"
        1.4.NegativeCase telephone.
            1.4.1. telephone null -> "Telephone cannot be empty"
            1.4.2. telephone > 12,< 12 -> "Incorrect tel.number"
        1.5.NegativeCase password.
            1.5.1. password < 8, > 50, non-upper, non-lower, non-specSymbols, non-digit -> "Incorrect password"
            1.5.2. password is null -> "Password cannot be empty"
        1.6.NegativeCase role. role is null -> "Role cannot be empty"
        1.7 positiveCase. id and created_at is not null
            1.7.1. string in DB was created
            1.7.2. id not null
            1.7.3. created_at not null
            1.7.4. validate: username, birthday, email, telephone, password, role
    2.findEmailById
        2.1 if email exist return email
        2.2 if email exist return empty Optional
    3.findTelephoneById
        3.1 if telephone exist return telephone
        3.2 if telephone exist return empty Optional
    4.findByEmail
        4.1 if email exist return contactOwner
        4.2 if email exist return empty Optional
    5.findByTelephone
        5.1 if telephone exist return contactOwner
        5.2 if telephone exist return empty Optional
    */

    @AfterEach
    void deleteAll() {
        contactOwnerRepo.deleteAll();
    }

    @Test
    void createContactOwner() {
        /*
        1.7 positiveCase. id and created_at is not null
            1.7.1. string in DB was created
            1.7.2. id not null
            1.7.3. created_at not null
            1.7.4. validate: username, birthday, email, telephone, password, role
         */

        ContactOwner contactOwner = ContactOwner.builder()
                .username("x".repeat(50))
                .birthday(LocalDate.now().minusYears(20))
                .email("x".repeat(23) + "@" + "x".repeat(22) + ".com")
                .telephone("+79001112233")
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(Role.ROLE_USER)
                .build();

        ContactOwner newContactOwner = contactOwnerRepo.save(contactOwner);
        List<ContactOwner> contactOwners = contactOwnerRepo.findAll();

        assertEquals(1, contactOwners.size());

        ContactOwner savedContactOwner = contactOwners.get(0);

        assertEquals(newContactOwner.getId(), savedContactOwner.getId());
        assertEquals(newContactOwner.getUsername(), savedContactOwner.getUsername());
        assertEquals(newContactOwner.getBirthday(), savedContactOwner.getBirthday());
        assertEquals(newContactOwner.getEmail(), savedContactOwner.getEmail());
        assertEquals(newContactOwner.getTelephone(), savedContactOwner.getTelephone());
        assertEquals(newContactOwner.getPassword(), savedContactOwner.getPassword());
        assertEquals(newContactOwner.getRole(), savedContactOwner.getRole());
        assertEquals(newContactOwner.getCreatedAt(), savedContactOwner.getCreatedAt());
    }

    @ParameterizedTest
    @MethodSource("invalidContactOwnerUsername")
    void testSaveTaskOwnerWithInvalidFirstName(String invalidUsername, String expectedMessage) {
        /*
        1.1.NegativeCase Username.
            1.1.1. username: null, "", " ", "   " -> "Username cannot be empty"
            1.1.2. username length > 50  -> "Username max size: 50"
         */

        ContactOwner contactOwner = ContactOwner.builder()
                .username(invalidUsername)
                .birthday(LocalDate.now().minusYears(20))
                .email("x".repeat(23) + "@" + "x".repeat(22) + ".com")
                .telephone("+79001112233")
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(Role.ROLE_USER)
                .build();

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            contactOwnerRepo.save(contactOwner);
        });

        // Извлекаем список нарушений
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();

        // Проверяем, что нарушение действительно произошло
        assertFalse(violations.isEmpty(), "Должно быть хотя бы одно нарушение");
        assertEquals(1, violations.size(), "Нарушений более 1");

        ConstraintViolation<?> violation = violations.iterator().next();

        // Сравниваем ИМЕННО текст сообщения валидации
        String actualMessage = violation.getMessage();
        assertEquals(expectedMessage, actualMessage);

        // Проверяем, что сущность не сохранилась в БД
        entityManager.clear();
        assertEquals(0, contactOwnerRepo.findAll().size());
    }

    private static Stream<Arguments> invalidContactOwnerUsername() {
        String expectedMessage = "Username cannot be empty";

        return Stream.of(
                Arguments.of(null, expectedMessage),
                Arguments.of("", expectedMessage),
                Arguments.of(" ", expectedMessage),
                Arguments.of("   ", expectedMessage),
                Arguments.of("x".repeat(51), "Username max size: 50")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidContactOwnerBirthday")
    void testSaveContactOwnerWithInvalidBirthday(String testCase, LocalDate invalidBirthday, String expectedMessage) {
        /*
        1.2.NegativeCase birthday.
            1.2.1. birthday null -> "Birthday cannot be empty"
            1.2.2. birthday in future or now -> "Birthday must be in the past"
         */

        ContactOwner contactOwner = ContactOwner.builder()
                .username("TestUser")
                .birthday(invalidBirthday)
                .email("test@example.com")
                .telephone("+79001112233")
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(Role.ROLE_USER)
                .build();

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            contactOwnerRepo.save(contactOwner);
        });

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        assertFalse(violations.isEmpty(), "Должно быть хотя бы одно нарушение");
        assertEquals(1, violations.size(), "Нарушений более 1");

        ConstraintViolation<?> violation = violations.iterator().next();
        String actualMessage = violation.getMessage();
        assertEquals(expectedMessage, actualMessage);

        entityManager.clear();
        assertEquals(0, contactOwnerRepo.findAll().size());
    }

    private static Stream<Arguments> invalidContactOwnerBirthday() {
        return Stream.of(
                Arguments.of("null birthday", null, "Birthday cannot be empty"),
                Arguments.of("future birthday", LocalDate.now().plusDays(1), "Birthday must be in the past"),
                Arguments.of("today birthday", LocalDate.now(), "Birthday must be in the past")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidContactOwnerEmail")
    void testSaveContactOwnerWithInvalidEmail(String testCase, String invalidEmail, String expectedMessage) {
        /*
        1.3.NegativeCase email.
            1.3.1. email null -> "Email cannot be empty"
            1.3.2. email > 50, without @ or . -> "Incorrect email"
         */

        ContactOwner contactOwner = ContactOwner.builder()
                .username("TestUser")
                .birthday(LocalDate.now().minusYears(20))
                .email(invalidEmail)
                .telephone("+79001112233")
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(Role.ROLE_USER)
                .build();

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            contactOwnerRepo.save(contactOwner);
        });

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        assertFalse(violations.isEmpty(), "Должно быть хотя бы одно нарушение");
        assertEquals(1, violations.size(), "Нарушений более 1");

        ConstraintViolation<?> violation = violations.iterator().next();
        String actualMessage = violation.getMessage();
        assertEquals(expectedMessage, actualMessage);

        entityManager.clear();
        assertEquals(0, contactOwnerRepo.findAll().size());
    }

    private static Stream<Arguments> invalidContactOwnerEmail() {
        String expectedMessage = "Incorrect email";

        return Stream.of(
                Arguments.of("null email", null, "Email cannot be empty"),
                Arguments.of("email without @", "testexample.com", expectedMessage),
                Arguments.of("email without .", "test@examplecom", expectedMessage),
                Arguments.of("email without domain", "asdfqwer@", expectedMessage),
                Arguments.of("email without domain part", "asdfqwer@.com", expectedMessage),
                Arguments.of("email without local part", "@sadcom", expectedMessage),
                Arguments.of("email without local part and dot", "@sad.com", expectedMessage),
                Arguments.of("email with space in local part", "asdf qwer@sad.com", expectedMessage),
                Arguments.of("email with space in domain", "asdfqwer@s ad.com", expectedMessage),
                Arguments.of("email with double @", "asd@fqwer@sad.com", expectedMessage),
                Arguments.of("email with dot at start of domain", "asdfqwers@.d.com", expectedMessage),
                Arguments.of("email with dot at end of domain", "asdfqwersd.c@m", expectedMessage),
                Arguments.of("email > 50 chars", "x".repeat(23) + "@" + "x".repeat(22) + "x" + ".com", expectedMessage)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidContactOwnerTelephone")
    void testSaveContactOwnerWithInvalidTelephone(String testCase, String invalidTelephone, String expectedMessage) {
        /*
        1.4.NegativeCase telephone.
            1.4.1. telephone null -> "Telephone cannot be empty"
            1.4.2. telephone > 12,< 12 -> "Incorrect tel.number"
         */

        ContactOwner contactOwner = ContactOwner.builder()
                .username("TestUser")
                .birthday(LocalDate.now().minusYears(20))
                .email("test@example.com")
                .telephone(invalidTelephone)
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(Role.ROLE_USER)
                .build();

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            contactOwnerRepo.save(contactOwner);
        });

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        assertFalse(violations.isEmpty(), "Должно быть хотя бы одно нарушение");
        assertEquals(1, violations.size(), "Нарушений более 1");

        ConstraintViolation<?> violation = violations.iterator().next();
        String actualMessage = violation.getMessage();
        assertEquals(expectedMessage, actualMessage);

        entityManager.clear();
        assertEquals(0, contactOwnerRepo.findAll().size());
    }

    private static Stream<Arguments> invalidContactOwnerTelephone() {
        return Stream.of(
                Arguments.of("null telephone", null, "Telephone cannot be empty"),
                Arguments.of("telephone < 12 chars", "+7900111223", "Incorrect tel.number"),
                Arguments.of("telephone > 12 chars", "+790011122334", "Incorrect tel.number")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidContactOwnerPassword")
    void testSaveContactOwnerWithInvalidPassword(String testCase, String invalidPassword, String expectedMessage) {
        /*
        1.5.NegativeCase password.
            1.5.1. password < 8, > 50, non-upper, non-lower, non-specSymbols, non-digit -> "Incorrect password"
            1.5.2. password is null -> "Password cannot be empty"
         */

        ContactOwner contactOwner = ContactOwner.builder()
                .username("TestUser")
                .birthday(LocalDate.now().minusYears(20))
                .email("test@example.com")
                .telephone("+79001112233")
                .password(invalidPassword)
                .role(Role.ROLE_USER)
                .build();

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            contactOwnerRepo.save(contactOwner);
        });

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        assertFalse(violations.isEmpty(), "Должно быть хотя бы одно нарушение");
        assertEquals(1, violations.size(), "Нарушений более 1");

        ConstraintViolation<?> violation = violations.iterator().next();
        String actualMessage = violation.getMessage();
        assertEquals(expectedMessage, actualMessage);

        entityManager.clear();
        assertEquals(0, contactOwnerRepo.findAll().size());
    }

    private static Stream<Arguments> invalidContactOwnerPassword() {
        return Stream.of(
                Arguments.of("null password", null, "Password cannot be empty"),
                Arguments.of("password < 8 chars", "P@ss1", "Incorrect password"),
                Arguments.of("password > 50 chars", "P@ssw0rd1234_QwErt" + "x".repeat(43), "Incorrect password"),
                Arguments.of("password without uppercase", "p@ssword123", "Incorrect password"),
                Arguments.of("password without lowercase", "P@SSWORD123", "Incorrect password"),
                Arguments.of("password without digit", "P@sswordPassword", "Incorrect password"),
                Arguments.of("password without special char", "Password1234Password", "Incorrect password")
        );
    }

    @Test
    void testSaveContactOwnerWithNullRole() {
        /*
        1.6.NegativeCase role. role is null -> "Role cannot be empty"
         */

        ContactOwner contactOwner = ContactOwner.builder()
                .username("TestUser")
                .birthday(LocalDate.now().minusYears(20))
                .email("test@example.com")
                .telephone("+79001112233")
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(null)
                .build();

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            contactOwnerRepo.save(contactOwner);
        });

        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        assertFalse(violations.isEmpty(), "Должно быть хотя бы одно нарушение");
        assertEquals(1, violations.size(), "Нарушений более 1");

        ConstraintViolation<?> violation = violations.iterator().next();
        String actualMessage = violation.getMessage();
        assertEquals("Role cannot be empty", actualMessage);

        entityManager.clear();
        assertEquals(0, contactOwnerRepo.findAll().size());
    }

    @Test
    void testFindEmailById_WithExistingId() {
        /*
        2.1 if email exist return email
         */

        contactOwnerRepo.saveAllAndFlush(initContactOwners());

        ContactOwner contactOwner = ContactOwner.builder()
                .username("TestUser")
                .birthday(LocalDate.now().minusYears(20))
                .email("test@example.com")
                .telephone("+79001112290")
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(Role.ROLE_USER)
                .build();

        ContactOwner savedContactOwner = contactOwnerRepo.save(contactOwner);

        Optional<String> emailOptional = contactOwnerRepo.findEmailById(savedContactOwner.getId());

        assertTrue(emailOptional.isPresent());
        assertEquals("test@example.com", emailOptional.get());
    }

    @Test
    void testFindEmailById_WithNonExistingId() {
        /*
        2.2 if email exist return empty Optional
         */
        contactOwnerRepo.saveAllAndFlush(initContactOwners());

        Optional<String> emailOptional = contactOwnerRepo.findEmailById(999999L);

        assertFalse(emailOptional.isPresent());
    }

    @Test
    void testFindTelephoneById_WithExistingId() {
        /*
        3.1 if telephone exist return telephone
         */
        contactOwnerRepo.saveAllAndFlush(initContactOwners());

        ContactOwner contactOwner = ContactOwner.builder()
                .username("TestUser")
                .birthday(LocalDate.now().minusYears(20))
                .email("test@example.com")
                .telephone("+79001112290")
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(Role.ROLE_USER)
                .build();

        ContactOwner savedContactOwner = contactOwnerRepo.save(contactOwner);

        Optional<String> telephoneOptional = contactOwnerRepo.findTelephoneById(savedContactOwner.getId());

        assertTrue(telephoneOptional.isPresent());
        assertEquals("+79001112290", telephoneOptional.get());
    }

    @Test
    void testFindTelephoneById_WithNonExistingId() {
        /*
        3.2 if telephone exist return empty Optional
         */
        contactOwnerRepo.saveAllAndFlush(initContactOwners());

        Optional<String> telephoneOptional = contactOwnerRepo.findTelephoneById(999999L);

        assertFalse(telephoneOptional.isPresent());
    }

    @Test
    void testFindByEmail_WithExistingEmail() {
        /*
        4.1 if email exist return contactOwner
         */
        contactOwnerRepo.saveAllAndFlush(initContactOwners());

        ContactOwner contactOwner = ContactOwner.builder()
                .username("TestUser")
                .birthday(LocalDate.now().minusYears(20))
                .email("test@example.com")
                .telephone("+79001112290")
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(Role.ROLE_USER)
                .build();

        ContactOwner savedContactOwner = contactOwnerRepo.save(contactOwner);

        Optional<ContactOwner> foundContactOwner = contactOwnerRepo.findByEmail("test@example.com");

        assertTrue(foundContactOwner.isPresent());
        assertEquals(savedContactOwner.getId(), foundContactOwner.get().getId());
        assertEquals(savedContactOwner.getUsername(), foundContactOwner.get().getUsername());
        assertEquals(savedContactOwner.getBirthday(), foundContactOwner.get().getBirthday());
        assertEquals(savedContactOwner.getTelephone(), foundContactOwner.get().getTelephone());
        assertEquals(savedContactOwner.getRole(), foundContactOwner.get().getRole());
    }

    @Test
    void testFindByEmail_WithNonExistingEmail() {
        /*
        4.2 if email exist return empty Optional
         */
        contactOwnerRepo.saveAllAndFlush(initContactOwners());

        Optional<ContactOwner> foundContactOwner = contactOwnerRepo.findByEmail("nonexistent@example.com");

        assertFalse(foundContactOwner.isPresent());
    }

    @Test
    void testFindByTelephone_WithExistingTelephone() {
        /*
        5.1 if telephone exist return contactOwner
         */
        contactOwnerRepo.saveAllAndFlush(initContactOwners());

        ContactOwner contactOwner = ContactOwner.builder()
                .username("TestUser")
                .birthday(LocalDate.now().minusYears(20))
                .email("test@example.com")
                .telephone("+79001112290")
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(Role.ROLE_USER)
                .build();

        ContactOwner savedContactOwner = contactOwnerRepo.save(contactOwner);

        Optional<ContactOwner> foundContactOwner = contactOwnerRepo.findByTelephone("+79001112290");

        assertTrue(foundContactOwner.isPresent());
        assertEquals(savedContactOwner.getId(), foundContactOwner.get().getId());
        assertEquals(savedContactOwner.getUsername(), foundContactOwner.get().getUsername());
        assertEquals(savedContactOwner.getBirthday(), foundContactOwner.get().getBirthday());
        assertEquals(savedContactOwner.getEmail(), foundContactOwner.get().getEmail());
        assertEquals(savedContactOwner.getRole(), foundContactOwner.get().getRole());
    }

    @Test
    void testFindByTelephone_WithNonExistingTelephone() {
        /*
        5.2 if telephone exist return empty Optional
         */
        contactOwnerRepo.saveAllAndFlush(initContactOwners());

        Optional<ContactOwner> foundContactOwner = contactOwnerRepo.findByTelephone("+79000000000");

        assertFalse(foundContactOwner.isPresent());
    }

    @Test
    void testSaveContactOwnerWithDuplicateEmailDifferentCase() {
        ContactOwner owner1 = ContactOwner.builder()
                .username("User1")
                .birthday(LocalDate.now().minusYears(20))
                .email("Test@Example.com")
                .telephone("+79001112233")
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(Role.ROLE_USER)
                .build();

        contactOwnerRepo.saveAndFlush(owner1);

        ContactOwner owner2 = ContactOwner.builder()
                .username("User2")
                .birthday(LocalDate.now().minusYears(20))
                .email("test@example.com")
                .telephone("+79001112244")
                .password("P@ssw0rd1234_QwErt" + "x".repeat(32))
                .role(Role.ROLE_USER)
                .build();

        assertThrows(DataIntegrityViolationException.class, () ->
                contactOwnerRepo.saveAndFlush(owner2));

        entityManager.clear();
        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();
    }

    private List<ContactOwner> initContactOwners() {
        List<ContactOwner> owners = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            owners.add(
                    ContactOwner.builder()
                            .username("Name" + i)
                            .email("email" + i + "@mail.ru")
                            .password("P@ssw0rd" + i + "x")
                            .role(Role.ROLE_USER)
                            .birthday(LocalDate.now().minusYears(20))
                            .telephone("+7900111223" + i)
                            .build()
            );
        }
        return owners;
    }
}