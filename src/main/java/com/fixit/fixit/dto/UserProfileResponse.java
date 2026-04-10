package com.fixit.fixit.dto;

import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.AccountStatus;
import com.fixit.fixit.enums.UserType;

public class UserProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Long cityId;
    private String cityName;
    private Long countryId;
    private String countryName;
    private UserType userType;
    private String profileImageUrl;
    private AccountStatus accountStatus;

    public UserProfileResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.phone = user.getPhone();
        this.userType = user.getUserType();
        this.profileImageUrl = user.getProfileImageUrl();
        this.accountStatus = user.getAccountStatus();
        
        if (user.getCity() != null) {
            this.cityId = user.getCity().getId();
            this.cityName = user.getCity().getName();
            if (user.getCity().getCountry() != null) {
                this.countryId = user.getCity().getCountry().getId();
                this.countryName = user.getCity().getCountry().getName();
            }
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public Long getCountryId() { return countryId; }
    public void setCountryId(Long countryId) { this.countryId = countryId; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }
}
