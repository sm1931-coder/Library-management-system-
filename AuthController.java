package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.entity.LibraryUser;
import com.library.security.JwtUtils;
import com.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            LibraryUser user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("LibraryUser not found"));

            String jwt = jwtUtils.generateToken(user.getUsername());

            LoginResponse loginResponse = new LoginResponse(
                    jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole().name()
            );

            return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));

        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/debug-match")
    public ResponseEntity<ApiResponse<String>> debugMatch(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LibraryUser user = userService.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("LibraryUser not found"));
            boolean matches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
            String info = "user=" + user.getUsername() + ", matches=" + matches;
            return ResponseEntity.ok(ApiResponse.success("Password check", info));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Debug failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LibraryUser>> register(@Valid @RequestBody LibraryUser user) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username is already taken"));
            }
            
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is already in use"));
            }
            
            LibraryUser savedLibraryUser = userService.save(user);
            savedLibraryUser.setPassword(null); // Don't return password
            
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", savedLibraryUser));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/init")
    public ResponseEntity<ApiResponse<String>> initializeDefaultUser() {
        try {
            LibraryUser defaultLibraryUser = userService.createDefaultUser();
            if (defaultLibraryUser != null) {
                return ResponseEntity.ok(ApiResponse.success("Default user created successfully"));
            } else {
                return ResponseEntity.ok(ApiResponse.success("Default user already exists"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create default user: " + e.getMessage()));
        }
    }
}



