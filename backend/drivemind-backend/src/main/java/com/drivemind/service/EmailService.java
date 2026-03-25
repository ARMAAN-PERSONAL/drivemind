package com.drivemind.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendVerificationEmail(String email, String token) {

        String link = "http://localhost:8080/api/auth/verify?token=" + token;

        System.out.println("Verification email sent to: " + email);
        System.out.println("Verification link: " + link);
    }
}