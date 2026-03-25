package com.drivemind.dto;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String email;
    private String role;

    public AuthResponse(String accessToken, String refreshToken, String email, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.role = role;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}