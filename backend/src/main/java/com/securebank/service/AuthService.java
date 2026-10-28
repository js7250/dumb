package com.securebank.service;

import com.securebank.dto.*;
import com.securebank.entity.*;
import com.securebank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final EntityMapper mapper;
    private final com.securebank.security.JwtTokenProvider jwtProvider;

    @Transactional
    public LoginResponse login(String username, String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            String token = jwtProvider.generateToken(auth);

            User user = userRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            auditLogRepository.save(AuditLog.builder()
                    .actor(user.getUsername())
                    .action("LOGIN_SUCCESS")
                    .details("Successful authenticated session started as role " + user.getRole())
                    .riskScore(0)
                    .build());

            log.info("Login successful for user: {}", username);
            return new LoginResponse(token, mapper.toUserDto(user));

        } catch (BadCredentialsException e) {
            auditLogRepository.save(AuditLog.builder()
                    .actor(username != null ? username : "ANONYMOUS")
                    .action("LOGIN_FAILURE")
                    .details("Failed authentication attempt for username \"" + username + "\".")
                    .riskScore(40)
                    .build());
            throw new BadCredentialsException("Invalid credentials. Check username or password.");
        }
    }

    @Transactional
    public void logout(String username) {
        auditLogRepository.save(AuditLog.builder()
                .actor(username)
                .action("LOGOUT")
                .details("User cleanly terminated active session.")
                .build());
    }
}
