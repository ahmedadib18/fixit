package com.fixit.fixit.controller;

import com.fixit.fixit.dto.AuthResponse;
import com.fixit.fixit.dto.LoginRequest;
import com.fixit.fixit.dto.RegisterRequest;
import com.fixit.fixit.entity.City;
import com.fixit.fixit.entity.Country;
import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.UserType;
import com.fixit.fixit.repository.CityRepository;
import com.fixit.fixit.repository.CountryRepository;
import com.fixit.fixit.security.JwtUtil;
import com.fixit.fixit.service.AuthenticationService;
import com.fixit.fixit.service.GoogleOAuthClient;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private GoogleOAuthClient googleOAuthClient;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authenticationService.register(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getUserType(),
                    request.getCityId()
            );

            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getId(),
                    user.getUserType().name()
            );

            AuthResponse response = new AuthResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUserType().name()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = authenticationService.login(request.getEmail(), request.getPassword());

            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getId(),
                    user.getUserType().name()
            );

            AuthResponse response = new AuthResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUserType().name()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getCountries() {
        List<Country> countries = countryRepository.findAll();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/countries/{countryId}/cities")
    public ResponseEntity<List<City>> getCitiesByCountry(@PathVariable Long countryId) {
        List<City> cities = cityRepository.findByCountryId(countryId);
        return ResponseEntity.ok(cities);
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        try {
            User user = googleOAuthClient.authenticateWithGoogle(
                    request.getAccessToken(),
                    request.getUserType()
            );

            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getId(),
                    user.getUserType().name()
            );

            AuthResponse response = new AuthResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUserType().name()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // Inner classes for requests and responses
    private static class GoogleLoginRequest {
        private String accessToken;
        private UserType userType;

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

        public UserType getUserType() { return userType; }
        public void setUserType(UserType userType) { this.userType = userType; }
    }

    // Inner class for error responses
    private static class ErrorResponse {
        private String message;
        private int status;

        public ErrorResponse(String message, int status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
    }
}
