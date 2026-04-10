package com.fixit.fixit;

import com.fixit.fixit.controller.AuthController;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.AccountStatus;
import com.fixit.fixit.enums.UserType;
import com.fixit.fixit.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuthController authController;

    private MockMvc mockMvc;

    private AuthenticationService mockAuthService;

    @BeforeEach
    void setUp() {
        // Build MockMvc manually with Spring Security applied
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Replace real AuthenticationService with mock
        mockAuthService = Mockito.mock(AuthenticationService.class);
        ReflectionTestUtils.setField(
                authController,
                "authenticationService",
                mockAuthService
        );
    }

    @Test
    void testRegister_Success_Returns201() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("john@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setUserType(UserType.USER);
        mockUser.setAccountStatus(AccountStatus.ACTIVE);

        when(mockAuthService.register(
                anyString(), anyString(), anyString(),
                anyString(), any(UserType.class), any()
        )).thenReturn(mockUser);

        String requestBody = """
                {
                    "email": "john@example.com",
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe",
                    "userType": "USER"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.userType").value("USER"));
    }

    @Test
    void testRegister_MissingEmail_Returns400() throws Exception {
        String requestBody = """
                {
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe",
                    "userType": "USER"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_InvalidEmail_Returns400() throws Exception {
        String requestBody = """
                {
                    "email": "not-an-email",
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe",
                    "userType": "USER"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_ShortPassword_Returns400() throws Exception {
        String requestBody = """
                {
                    "email": "john@example.com",
                    "password": "123",
                    "firstName": "John",
                    "lastName": "Doe",
                    "userType": "USER"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Success_Returns200WithToken() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("john@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setUserType(UserType.USER);

        when(mockAuthService.login(anyString(), anyString()))
                .thenReturn(mockUser);

        String requestBody = """
                {
                    "email": "john@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testLogin_MissingPassword_Returns400() throws Exception {
        String requestBody = """
                {
                    "email": "john@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}