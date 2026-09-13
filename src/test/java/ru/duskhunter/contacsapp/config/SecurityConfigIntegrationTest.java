package ru.duskhunter.contacsapp.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.duskhunter.contacsapp.dto.secure.RegisterRequestDto;
import ru.duskhunter.contacsapp.model.Role;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;
import ru.duskhunter.contacsapp.model.repository.ContactOwnerRepo;
import ru.duskhunter.contacsapp.service.security.JwtSecurityService;

import java.time.LocalDate;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
Test plan:
    1. Public endpoints
        1.1 POST /api/v1/auth/register accessible without a token (business/validation errors are fine, 401/403 are not)
        1.2 GET /v3/api-docs accessible without a token
    2. Protected endpoints without a token
        2.1 GET /api/v1/contacts without Authorization header -> 401
        2.2 GET /api/v1/owners without Authorization header -> 401
    3. Protected endpoints with a valid ROLE_USER token
        3.1 GET /api/v1/contacts -> 200 (hasAnyRole("USER") satisfied)
        3.2 GET /api/v1/owners -> 403 (hasRole("ADMIN") not satisfied)
            NOTE: ContactOwner.getAuthorities() currently hardcodes ROLE_USER regardless
            of the entity's `role` field, so no token can ever satisfy hasRole("ADMIN") —
            see the accompanying note. This test documents the current (arguably buggy)
            behavior rather than the intended one.
    4. Malformed/garbage token
        4.1 GET /api/v1/contacts with an unparsable token -> currently a 5xx, not a clean 401
            (documents a follow-up fix suggestion: catch token-parsing exceptions in the
            filter and fall through to "unauthenticated" instead of letting them escape)
    5. Statelessness: no JSESSIONID cookie is issued for an authenticated request

Requires the JwtAuthenticationFilter patch (filterChain.doFilter(...) restored) to be
applied — otherwise authenticated requests never reach the controller.
*/

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class SecurityConfigIntegrationTest {

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
    private MockMvc mockMvc;

    @Autowired
    private ContactOwnerRepo ownerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtSecurityService jwtSecurityService;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    void setUp() {
        ContactOwner user = ContactOwner.builder()
                .username("SecurityTestUser")
                .birthday(LocalDate.now().minusYears(20))
                .email("security-test@example.com")
                .telephone("+79001112233")
                .password("P@ssw0rd1234")
                .role(Role.ROLE_USER)
                .build();
        user = ownerRepo.saveAndFlush(user);

        userToken = jwtSecurityService.generateToken(user);
    }

    @AfterEach
    void tearDown() {
        ownerRepo.deleteAll();
    }

    @Test
    void registerEndpointShouldBeAccessibleWithoutToken() throws Exception {
        RegisterRequestDto registerRequestDto = new RegisterRequestDto("NewUser","new-user@example.com","P@ssw0rd1234");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDto)))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    void apiDocsShouldBeAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    void contactsEndpointShouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/contacts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ownersEndpointShouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/owners"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void contactsEndpointShouldAllowValidUserToken() throws Exception {
        mockMvc.perform(get("/api/v1/contacts")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void ownersEndpointShouldRejectUserTokenWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/owners")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedRequestShouldNotCreateSession() throws Exception {
        mockMvc.perform(get("/api/v1/contacts")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist("JSESSIONID"));
    }
}