package com.drivemind.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne
    private User user;

    private LocalDateTime expiryDate;

    private boolean used = false;

    public VerificationToken(){}

    public VerificationToken(String token, User user, LocalDateTime expiryDate){
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }

    public String getToken(){ return token; }

    public User getUser(){ return user; }

    public LocalDateTime getExpiryDate(){ return expiryDate; }

    public boolean isUsed(){ return used; }

    public void setUsed(boolean used){ this.used = used; }
}