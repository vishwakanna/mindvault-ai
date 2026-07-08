package com.SecondBrain.project.controller;

import com.SecondBrain.project.dto.AuthResponse;
import com.SecondBrain.project.dto.LoginRequest;
import com.SecondBrain.project.dto.RegisterRequest;
import com.SecondBrain.project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController               // Combines @Controller + @ResponseBody (auto-serialize to JSON)
@RequestMapping("/api/auth")  // Base path for all methods in this controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        // @Valid triggers Bean Validation (the @NotBlank, @Email annotations on the DTO)
        // @RequestBody deserializes JSON request body into RegisterRequest object
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);  // 201 Created
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);  // 200 OK
    }
}