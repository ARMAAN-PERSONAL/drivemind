package com.drivemind.controller;

import com.drivemind.dto.*;
import com.drivemind.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService=authService;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest req){
        return authService.register(req);
    }

    @GetMapping("/verify")
    public String verify(@RequestParam String token){
        return authService.verify(token);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req){
        return authService.login(req);
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.resendVerificationEmail(request.getEmail());
    }
}