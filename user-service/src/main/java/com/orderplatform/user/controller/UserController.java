package com.orderplatform.user.controller;

import com.orderplatform.user.dto.AuthResponse;
import com.orderplatform.user.dto.LoginRequest;
import com.orderplatform.user.dto.UserRequest;
import com.orderplatform.user.entity.User;
import com.orderplatform.user.repository.UserRepository;
import com.orderplatform.user.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already registered"));
        }
        User user = new User(null, request.getEmail(), request.getName(), request.getAddress(),
                passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, saved.getId(), saved.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return userRepository.findByEmailIgnoreCase(request.getEmail())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .map(u -> ResponseEntity.ok(
                        new AuthResponse(jwtUtil.generateToken(u.getId(), u.getEmail()), u.getId(), u.getName())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials")));
    }

    // Requires a valid JWT now (enforced by SecurityConfig).
    @GetMapping
    public List<User> getAll() {
        return userRepository.findAll();
    }

    // Requires a valid JWT now.
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
