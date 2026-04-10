package com.fixit.fixit;

import com.fixit.fixit.entity.City;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.AccountStatus;
import com.fixit.fixit.enums.UserType;
import com.fixit.fixit.exception.DuplicateResourceException;
import com.fixit.fixit.exception.InvalidCredentialsException;
import com.fixit.fixit.exception.UnauthorizedException;
import com.fixit.fixit.repository.CityRepository;
import com.fixit.fixit.repository.UserRepository;
import com.fixit.fixit.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =============================================
    // REGISTER TESTS
    // =============================================

    @Test
    void testRegister_Success() {
        // Arrange
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("john@example.com");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setUserType(UserType.USER);
        savedUser.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = authenticationService.register(
                "john@example.com",
                "password123",
                "John",
                "Doe",
                UserType.USER,
                null
        );

        // Assert
        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals(UserType.USER, result.getUserType());
        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_WithCity_Success() {
        // Arrange
        City city = new City();
        city.setId(1L);
        city.setName("Vancouver");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setEmail("jane@example.com");
        savedUser.setCity(city);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = authenticationService.register(
                "jane@example.com",
                "password123",
                "Jane",
                "Doe",
                UserType.HELPER,
                1L
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCity());
        verify(cityRepository).findById(1L);
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () ->
                authenticationService.register(
                        "existing@example.com",
                        "password123",
                        "John",
                        "Doe",
                        UserType.USER,
                        null
                )
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_AsHelper_Success() {
        // Arrange
        when(userRepository.existsByEmail("helper@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        User savedUser = new User();
        savedUser.setEmail("helper@example.com");
        savedUser.setUserType(UserType.HELPER);
        savedUser.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = authenticationService.register(
                "helper@example.com",
                "password123",
                "Helper",
                "User",
                UserType.HELPER,
                null
        );

        // Assert
        assertEquals(UserType.HELPER, result.getUserType());
        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
    }

    // =============================================
    // LOGIN TESTS
    // =============================================

    @Test
    void testLogin_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setPasswordHash("hashedPassword");
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setUserType(UserType.USER);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword"))
                .thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = authenticationService.login("john@example.com", "password123");

        // Assert
        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).save(any(User.class)); // lastLogin updated
    }

    @Test
    void testLogin_InvalidEmail_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("wrong@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.login("wrong@example.com", "password123")
        );
    }

    @Test
    void testLogin_WrongPassword_ThrowsException() {
        // Arrange
        User user = new User();
        user.setEmail("john@example.com");
        user.setPasswordHash("hashedPassword");
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword"))
                .thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.login("john@example.com", "wrongPassword")
        );
    }

    @Test
    void testLogin_SuspendedAccount_ThrowsException() {
        // Arrange
        User user = new User();
        user.setEmail("suspended@example.com");
        user.setPasswordHash("hashedPassword");
        user.setAccountStatus(AccountStatus.SUSPENDED);

        when(userRepository.findByEmail("suspended@example.com"))
                .thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                authenticationService.login("suspended@example.com", "password123")
        );
    }

    @Test
    void testLogin_BannedAccount_ThrowsException() {
        // Arrange
        User user = new User();
        user.setEmail("banned@example.com");
        user.setPasswordHash("hashedPassword");
        user.setAccountStatus(AccountStatus.BANNED);

        when(userRepository.findByEmail("banned@example.com"))
                .thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                authenticationService.login("banned@example.com", "password123")
        );
    }

    // =============================================
    // GOOGLE OAUTH TESTS
    // =============================================

    @Test
    void testLoginWithGoogle_ExistingUser_Success() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("john@gmail.com");
        existingUser.setGoogleId("google123");

        when(userRepository.findByGoogleId("google123"))
                .thenReturn(Optional.of(existingUser));

        // Act
        User result = authenticationService.loginWithGoogle(
                "google123",
                "john@gmail.com",
                "John",
                "Doe",
                UserType.USER
        );

        // Assert
        assertNotNull(result);
        assertEquals("google123", result.getGoogleId());
        verify(userRepository, never()).save(any()); // no save needed
    }

    @Test
    void testLoginWithGoogle_NewUser_CreatesAccount() {
        // Arrange
        when(userRepository.findByGoogleId("newgoogle123"))
                .thenReturn(Optional.empty());
        when(userRepository.existsByEmail("newuser@gmail.com"))
                .thenReturn(false);

        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("newuser@gmail.com");
        newUser.setGoogleId("newgoogle123");
        newUser.setOauthProvider("GOOGLE");
        newUser.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User result = authenticationService.loginWithGoogle(
                "newgoogle123",
                "newuser@gmail.com",
                "New",
                "User",
                UserType.USER
        );

        // Assert
        assertNotNull(result);
        assertEquals("newgoogle123", result.getGoogleId());
        assertEquals("GOOGLE", result.getOauthProvider());
        verify(userRepository).save(any(User.class));
    }
}