package com.fixit.fixit.dto;

public class AuthResponse {

    private String token;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String userType;
    private String message;

    // =====================
    // Constructors
    // =====================

    public AuthResponse() {}

    public AuthResponse(String token,
                        Long userId,
                        String email,
                        String firstName,
                        String lastName,
                        String userType) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.message = "Success";
    }

    // =====================
    // Getters and Setters
    // =====================

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}