package com.securebank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;  // BCrypt hashed

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false, length = 50)
    private String accountType;

    @Column(nullable = false)
    private Double balance;

    @Column(nullable = false)
    private Boolean isFrozen = false;

    @Column(nullable = false, length = 50)
    private String city;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_known_devices", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "device")
    @Builder.Default
    private List<String> knownDevices = new ArrayList<>();

    @Column(nullable = false)
    private Double averageTxAmount;

    public enum Role { USER, ADMIN }
}
