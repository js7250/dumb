package com.securebank.entity;

import com.securebank.util.StringListConverter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank customer or admin user.
 * knownDevices is stored as a JSON array in a TEXT column via {@link StringListConverter}.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(length = 50)
    private String id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    /** USER or ADMIN */
    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false)
    private String accountType;

    private double balance;

    private boolean frozen;

    @Column(length = 100)
    private String city;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> knownDevices = new ArrayList<>();

    private double averageTxAmount;

    // ── Constructors ─────────────────────────────────────────────
    public User() {}

    // ── Getters & Setters ────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public List<String> getKnownDevices() { return knownDevices; }
    public void setKnownDevices(List<String> knownDevices) { this.knownDevices = knownDevices; }

    public double getAverageTxAmount() { return averageTxAmount; }
    public void setAverageTxAmount(double averageTxAmount) { this.averageTxAmount = averageTxAmount; }
}
