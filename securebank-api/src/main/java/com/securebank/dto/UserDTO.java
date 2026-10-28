package com.securebank.dto;

import java.util.List;

/** DTO returned for every user-facing response (password is never included). */
public record UserDTO(
        String id,
        String username,
        String role,
        String fullName,
        String accountNumber,
        String accountType,
        double balance,
        boolean frozen,
        String city,
        List<String> knownDevices,
        double averageTxAmount
) {
    /** Convenience factory from entity. */
    public static UserDTO from(com.securebank.entity.User u) {
        return new UserDTO(
                u.getId(), u.getUsername(), u.getRole(),
                u.getFullName(), u.getAccountNumber(), u.getAccountType(),
                u.getBalance(), u.isFrozen(), u.getCity(),
                u.getKnownDevices(), u.getAverageTxAmount()
        );
    }
}
