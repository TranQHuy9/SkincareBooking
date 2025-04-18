package com.example.skincare.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private List<String> roles;

    // Constructor với ba tham số
    public AuthResponse(String token, String username, List<String> roles) {
        this.token = token;
        this.username = username;
        this.roles = roles;
    }

    // Giữ lại constructor với một tham số (nếu cần)
    public AuthResponse(String token) {
        this.token = token;
    }
}