package com.drivemind.service;

import com.drivemind.dto.*;
import com.drivemind.model.*;
import com.drivemind.repository.*;
import com.drivemind.util.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final VerificationTokenRepository tokenRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepo,
                       VerificationTokenRepository tokenRepo,
                       PasswordEncoder encoder,
                       JwtService jwtService,
                       EmailService emailService){

        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    // ========================= REGISTER =========================

    public String register(RegisterRequest request){

        String email = request.getEmail().toLowerCase().trim();

        if(userRepo.existsByEmail(email))
            throw new IllegalArgumentException("Email already exists");

        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPassword(encoder.encode(request.getPassword()));

        // 🔥 MVP MODE: ENABLE USER DIRECTLY
        user.setEnabled(true);
        user.setEmailVerified(true);

        user.setRole(Role.USER);

        userRepo.save(user);

        // 🔥 OPTIONAL: keep token generation for future use (but no email send)
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken =
                new VerificationToken(token, user, LocalDateTime.now().plusHours(24));

        tokenRepo.save(verificationToken);

        // ❌ DISABLED FOR NOW (email not configured)
        // emailService.sendVerificationEmail(user.getEmail(), token);

        return "Registration successful. You can now login.";
    }

    // ========================= VERIFY =========================

    public String verify(String token){

        VerificationToken vt = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (vt.isUsed()) {
            throw new IllegalArgumentException("Token already used");
        }

        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        User user = vt.getUser();

        user.setEnabled(true);
        user.setEmailVerified(true);

        userRepo.save(user);

        vt.setUsed(true);
        tokenRepo.save(vt);

        return "Email verified.";
    }

    // ========================= LOGIN =========================

    public AuthResponse login(LoginRequest req){

        String email = req.getEmail().toLowerCase().trim();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if(!encoder.matches(req.getPassword(), user.getPassword()))
            throw new IllegalArgumentException("Invalid credentials");

        // 🔥 MVP MODE: SKIP EMAIL VERIFICATION CHECK
        // if(!user.isEmailVerified())
        //     throw new IllegalArgumentException("Verify email first");

        String accessToken = jwtService.generateToken(user.getEmail());

        String refreshToken = UUID.randomUUID().toString();

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getEmail(),
                user.getRole().name()
        );
    }

    // ========================= RESEND VERIFICATION =========================

    public String resendVerificationEmail(String rawEmail) {

        String email = rawEmail.toLowerCase().trim();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        tokenRepo.findByUser(user).ifPresent(tokenRepo::delete);

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken(
                token,
                user,
                LocalDateTime.now().plusHours(24)
        );

        tokenRepo.save(verificationToken);

        // ❌ DISABLED
        // emailService.sendVerificationEmail(user.getEmail(), token);

        return "Verification email logic disabled for MVP";
    }
}