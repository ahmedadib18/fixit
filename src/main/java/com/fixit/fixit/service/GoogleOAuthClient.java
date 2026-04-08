package com.fixit.fixit.service;

import com.fixit.fixit.entity.User;
import com.fixit.fixit.enums.UserType;
import com.fixit.fixit.exception.InvalidOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Service
public class GoogleOAuthClient {

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String clientSecret;

    @Autowired
    private AuthenticationService authenticationService;

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    // =============================================
    // EXCHANGE AUTHORIZATION CODE FOR ACCESS TOKEN
    // =============================================
    public String exchangeCodeForToken(String authorizationCode, String redirectUri) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> requestBody = Map.of(
                "code", authorizationCode,
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "grant_type", "authorization_code"
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    GOOGLE_TOKEN_URL,
                    requestBody,
                    Map.class
            );

            return (String) response.get("access_token");
        } catch (Exception e) {
            throw new InvalidOperationException("Failed to exchange authorization code: " + e.getMessage());
        }
    }

    // =============================================
    // GET USER INFO FROM GOOGLE
    // =============================================
    public GoogleUserInfo getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    GOOGLE_USERINFO_URL,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> userInfo = response.getBody();

            GoogleUserInfo googleUser = new GoogleUserInfo();
            googleUser.setGoogleId((String) userInfo.get("id"));
            googleUser.setEmail((String) userInfo.get("email"));
            googleUser.setGivenName((String) userInfo.get("given_name"));
            googleUser.setFamilyName((String) userInfo.get("family_name"));
            googleUser.setPicture((String) userInfo.get("picture"));
            googleUser.setVerifiedEmail((Boolean) userInfo.get("verified_email"));

            return googleUser;
        } catch (Exception e) {
            throw new InvalidOperationException("Failed to get user info from Google: " + e.getMessage());
        }
    }

    // =============================================
    // AUTHENTICATE WITH GOOGLE
    // =============================================
    public User authenticateWithGoogle(String accessToken, UserType userType) {
        GoogleUserInfo googleUser = getUserInfo(accessToken);

        return authenticationService.loginWithGoogle(
                googleUser.getGoogleId(),
                googleUser.getEmail(),
                googleUser.getGivenName(),
                googleUser.getFamilyName(),
                userType
        );
    }

    // =============================================
    // INNER CLASS: GOOGLE USER INFO
    // =============================================
    public static class GoogleUserInfo {
        private String googleId;
        private String email;
        private String givenName;
        private String familyName;
        private String picture;
        private Boolean verifiedEmail;

        public GoogleUserInfo() {}

        public String getGoogleId() { return googleId; }
        public void setGoogleId(String googleId) { this.googleId = googleId; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getGivenName() { return givenName; }
        public void setGivenName(String givenName) { this.givenName = givenName; }

        public String getFamilyName() { return familyName; }
        public void setFamilyName(String familyName) { this.familyName = familyName; }

        public String getPicture() { return picture; }
        public void setPicture(String picture) { this.picture = picture; }

        public Boolean getVerifiedEmail() { return verifiedEmail; }
        public void setVerifiedEmail(Boolean verifiedEmail) { this.verifiedEmail = verifiedEmail; }
    }
}
