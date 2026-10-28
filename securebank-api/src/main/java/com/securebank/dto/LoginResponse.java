package com.securebank.dto;

/** Response returned after successful authentication. */
public record LoginResponse(String token, UserDTO user) {}
