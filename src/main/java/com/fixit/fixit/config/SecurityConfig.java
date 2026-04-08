package com.fixit.fixit.config;

import com.fixit.fixit.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // =============================================
    // PASSWORD ENCODER BEAN
    // Fixes PasswordEncoder error in
    // AuthenticationService
    // =============================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =============================================
    // SECURITY FILTER CHAIN
    // =============================================
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // Disable CSRF for REST API
                .csrf(AbstractHttpConfigurer::disable)

                // Set session management to stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                // Configure endpoint access rules
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints — no token needed
                        .requestMatchers(
                                "/api/auth/**",
                                "/h2-console/**",
                                "/ws/**",
                                "/error"
                        ).permitAll()

                        // Admin only endpoints
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        // Helper only endpoints
                        .requestMatchers(
                                "/api/helpers/profile/**",
                                "/api/earnings/**"
                        ).hasAnyRole("HELPER", "ADMIN")

                        // All other endpoints need authentication
                        .anyRequest().authenticated()
                )

                // Allow H2 console frames
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()))

                // Add JWT filter before authentication filter
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}