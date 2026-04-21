package com.kpl.backend.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private String username;


    private String nickname;
    private String role;
    private String avatar;

    public AuthResponse(String token, String username, String nickname, String role, String avatar) {
        this.token = token;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
        this.avatar = avatar;
    }

}
