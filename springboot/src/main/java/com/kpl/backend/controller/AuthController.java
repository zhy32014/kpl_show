package com.kpl.backend.controller;

import com.kpl.backend.dto.*;
import com.kpl.backend.entity.User;
import com.kpl.backend.repository.UserRepository;
import com.kpl.backend.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String token = tokenProvider.generateToken(authentication);
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        AuthResponse response = new AuthResponse(token, user.getUsername(),
                user.getNickname(), user.getRole(), user.getAvatar());
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名已被使用"));
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("邮箱已被注册"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole("USER");
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("注册成功，请登录", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Object>> getCurrentUser(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            org.springframework.security.core.userdetails.UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("email", user.getEmail());
        data.put("avatar", user.getAvatar());
        data.put("role", user.getRole());
        data.put("createdAt", user.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
