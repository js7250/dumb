package com.securebank.service;

import com.securebank.dto.UserDto;
import com.securebank.entity.AuditLog;
import com.securebank.entity.User;
import com.securebank.repository.AuditLogRepository;
import com.securebank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final EntityMapper mapper;

    public UserDto getByUsername(String username) {
        return mapper.toUserDto(userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)));
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(mapper::toUserDto).toList();
    }

    @Transactional
    public UserDto toggleFreeze(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        boolean wasFrozen = user.getIsFrozen();
        user.setIsFrozen(!wasFrozen);
        userRepository.save(user);

        auditLogRepository.save(AuditLog.builder()
                .actor(adminUsername)
                .action(wasFrozen ? "ACCOUNT_UNFROZEN" : "ACCOUNT_FROZEN")
                .details(String.format("Admin %s %s account of user %s",
                        adminUsername, wasFrozen ? "unfroze" : "froze", user.getUsername()))
                .build());

        return mapper.toUserDto(user);
    }
}
