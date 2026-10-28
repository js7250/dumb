package com.securebank.controller;

import com.securebank.dto.LoginRequest;
import com.securebank.dto.LoginResponse;
import com.securebank.dto.UserDTO;
import com.securebank.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/login — authenticate and receive JWT */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    /** GET /api/auth/me — fetch fresh user profile from JWT identity */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getMe(userDetails.getUsername()));
    }

    /** POST /api/auth/logout — stateless: client discards token; server logs the event */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // JWT is stateless — no server-side invalidation needed.
        // Client is responsible for removing the token from storage.
        return ResponseEntity.ok().build();
    }
}
