package com.fixit.fixit.security;

import com.fixit.fixit.entity.User;
import com.fixit.fixit.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Get Authorization header
        final String authHeader = request
                .getHeader("Authorization");

        String email = null;
        String jwt = null;

        // Check if header starts with Bearer
        if (authHeader != null &&
                authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                email = jwtUtil.extractEmail(jwt);
            } catch (Exception e) {
                // Invalid token — continue without authentication
            }
        }

        // Validate token and set authentication
        if (email != null &&
                SecurityContextHolder.getContext()
                        .getAuthentication() == null) {

            // Find user by email
            User user = userRepository
                    .findByEmail(email)
                    .orElse(null);

            if (user != null &&
                    jwtUtil.validateToken(jwt, email)) {

                // Create authority from user type
                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getUserType().name());

                // Create authentication token
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(authority));

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));

                // Set authentication in context
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}