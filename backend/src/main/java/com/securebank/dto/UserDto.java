package com.securebank.dto;

import lombok.Data;

import java.util.List;

/** User profile sent to the frontend — passwords are NEVER included. */
@Data
public class UserDto {
    private Long id;
    private String username;
    private String role;
    private String fullName;
    private String accountNumber;
    private String accountType;
    private Double balance;
    private Boolean isFrozen;
    private String city;
    private List<String> knownDevices;
    private Double averageTxAmount;
}
