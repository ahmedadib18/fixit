package com.fixit.fixit.service;

import com.fixit.fixit.entity.City;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.AccountStatus;
import com.fixit.fixit.enums.UserType;
import com.fixit.fixit.exception.DuplicateResourceException;
import com.fixit.fixit.exception.InvalidCredentialsException;
import com.fixit.fixit.exception.ResourceNotFoundException;
import com.fixit.fixit.exception.UnauthorizedException;
import com.fixit.fixit.repository.CityRepository;
import com.fixit.fixit.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private City testCity;

    @BeforeEach
    void setUp() {
        testCity = new City();
        testCity.setId(1L);
        testCity.setName("Vancouver");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setUserType(UserType.USER);
        testUser.setAccountStatus(AccountStatus.ACTIVE);
        testUser.setCity(testCity);
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(cityRepository.findById(1L)).thenReturn(Optional.of(testCity));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authenticationService.register(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                UserType.USER,
                1L
        );

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                authenticationService.register(
                        "test@example.com",
                        "password123",
                        "John",
                        "Doe",
                        UserType.USER,
                        1L
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_InvalidCity_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(cityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                authenticationService.register(
                        "test@example.com",
                        "password123",
                        "John",
                        "Doe",
                        UserType.USER,
                        999L
                )
        );
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authenticationService.login("test@example.com", "password123");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertNotNull(result.getLastLogin());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_InvalidEmail_ThrowsException() {
        when(userRepository.findByEmail("invalid@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.login("invalid@example.com", "password123")
        );
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.login("test@example.com", "wrongpassword")
        );
    }

    @Test
    void login_SuspendedAccount_ThrowsException() {
        testUser.setAccountStatus(AccountStatus.SUSPENDED);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(UnauthorizedException.class, () ->
                authenticationService.login("test@example.com", "password123")
        );
    }

    @Test
    void login_BannedAccount_ThrowsException() {
        testUser.setAccountStatus(AccountStatus.BANNED);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(UnauthorizedException.class, () ->
                authenticationService.login("test@example.com", "password123")
        );
    }

    @Test
    void loginWithGoogle_NewUser_Success() {
        when(userRepository.findByGoogleId("google123")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authenticationService.loginWithGoogle(
                "google123",
                "test@example.com",
                "John",
                "Doe",
                UserType.USER
        );

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void loginWithGoogle_ExistingGoogleId_Success() {
        testUser.setGoogleId("google123");
        when(userRepository.findByGoogleId("google123")).thenReturn(Optional.of(testUser));

        User result = authenticationService.loginWithGoogle(
                "google123",
                "test@example.com",
                "John",
                "Doe",
                UserType.USER
        );

        assertNotNull(result);
        assertEquals("google123", result.getGoogleId());
    }
}
