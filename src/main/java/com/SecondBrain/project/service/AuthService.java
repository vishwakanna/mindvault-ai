package com.SecondBrain.project.service;

import com.SecondBrain.project.dto.AuthResponse;
import com.SecondBrain.project.dto.LoginRequest;
import com.SecondBrain.project.dto.RegisterRequest;
import com.SecondBrain.project.entity.User;
import com.SecondBrain.project.exception.UserAlreadyExistsException;
import com.SecondBrain.project.repository.UserRepository;
import com.SecondBrain.project.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // 1. Check if email already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User with email " + request.getEmail() + " already exists"
            );
        }

        // 2. Build User entity — HASH the password before saving
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // BCrypt hash
                .role(User.Role.USER)
                .build();

        // 3. Save to DB (Hibernate generates INSERT statement)
        userRepository.save(user);

        // 4. Generate JWT for the newly registered user
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // 5. Return response with token
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Delegate to Spring Security's AuthenticationManager
        //    This internally: loads user by email, checks BCrypt hash, throws BadCredentialsException if wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. If authenticate() didn't throw, credentials are valid — load the user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();  // Won't happen if authenticate succeeded

        // 3. Generate a fresh JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}