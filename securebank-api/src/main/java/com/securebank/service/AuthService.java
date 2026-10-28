package com.securebank.service;

import com.securebank.dto.LoginRequest;
import com.securebank.dto.LoginResponse;
import com.securebank.dto.UserDTO;
import com.securebank.entity.User;
import com.securebank.repository.UserRepository;
import com.securebank.security.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Handles login authentication and JWT generation. */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtil               jwtUtil;
    private final UserRepository        userRepo;
    private final AuditService          auditService;

    public AuthService(AuthenticationManager authManager, JwtUtil jwtUtil,
                       UserRepository userRepo, AuditService auditService) {
        this.authManager  = authManager;
        this.jwtUtil      = jwtUtil;
        this.userRepo     = userRepo;
        this.auditService = auditService;
    }

    /** Authenticate credentials; returns JWT + user profile on success. */
    public LoginResponse login(LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );

            User user = userRepo.findByUsernameIgnoreCase(auth.getName())
                    .orElseThrow(() -> new BadCredentialsException("User not found after auth."));

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

            auditService.log(user.getUsername(), "LOGIN",
                String.format("Successful login from account %s.", user.getAccountNumber()));

            return new LoginResponse(token, UserDTO.from(user));

        } catch (AuthenticationException e) {
            auditService.log(req.username(), "FAILED_LOGIN",
                "Authentication attempt failed. Invalid credentials.");
            throw new BadCredentialsException("Invalid username or password.");
        }
    }

    /** Fetch fresh user profile by username (used by /api/auth/me). */
    public UserDTO getMe(String username) {
        User user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BadCredentialsException("User not found."));
        return UserDTO.from(user);
    }
}
