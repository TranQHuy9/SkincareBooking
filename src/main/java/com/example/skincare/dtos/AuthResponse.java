package com.example.skincare.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@Getter
@Setter
public class AuthResponse {
    private String token;

    public AuthResponse(String jwt) {
        this.token = jwt;
    }
}
