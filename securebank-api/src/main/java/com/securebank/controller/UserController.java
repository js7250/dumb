package com.securebank.controller;

import com.securebank.dto.UserDTO;
import com.securebank.entity.User;
import com.securebank.repository.UserRepository;
import com.securebank.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository     userRepo;
    private final TransactionService txService;

    public UserController(UserRepository userRepo, TransactionService txService) {
        this.userRepo  = userRepo;
        this.txService = txService;
    }

    /** GET /api/users — list all users (ADMIN only, enforced in SecurityConfig) */
    @GetMapping
    public ResponseEntity<List<UserDTO>> allUsers() {
        return ResponseEntity.ok(
            userRepo.findAll().stream().map(UserDTO::from).toList()
        );
    }

    /** GET /api/users/me — get current authenticated user's fresh profile */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepo.findByUsernameIgnoreCase(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(UserDTO.from(user));
    }

    /** POST /api/users/{id}/freeze — toggle account freeze (ADMIN only) */
    @PostMapping("/{id}/freeze")
    public ResponseEntity<UserDTO> toggleFreeze(@PathVariable String id,
                                                @AuthenticationPrincipal UserDetails principal) {
        User updated = txService.toggleFreeze(id, principal.getUsername());
        return ResponseEntity.ok(UserDTO.from(updated));
    }
}
