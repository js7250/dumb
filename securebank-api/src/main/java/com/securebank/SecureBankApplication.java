package com.securebank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecureBankApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecureBankApplication.class, args);
        System.out.println("""
                ╔══════════════════════════════════════════════════════╗
                ║  SecureBank API — Core Banking with Fraud Detection  ║
                ║  Running on http://localhost:8080                    ║
                ║  H2 Console  → http://localhost:8080/h2-console     ║
                ╚══════════════════════════════════════════════════════╝
                """);
    }
}
