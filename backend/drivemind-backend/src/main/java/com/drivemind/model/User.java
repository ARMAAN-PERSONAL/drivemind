package com.drivemind.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private boolean enabled = false;

    private boolean emailVerified = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public Long getId(){ return id; }

    public String getFullName(){ return fullName; }

    public void setFullName(String fullName){ this.fullName = fullName; }

    public String getEmail(){ return email; }

    public void setEmail(String email){ this.email = email.toLowerCase().trim(); }

    public String getPassword(){ return password; }

    public void setPassword(String password){ this.password = password; }

    public Role getRole(){ return role; }

    public void setRole(Role role){ this.role = role; }

    public boolean isEnabled(){ return enabled; }

    public void setEnabled(boolean enabled){ this.enabled = enabled; }

    public boolean isEmailVerified(){ return emailVerified; }

    public void setEmailVerified(boolean emailVerified){ this.emailVerified = emailVerified; }
}