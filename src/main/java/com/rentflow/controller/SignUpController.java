package com.rentflow.controller;

import com.rentflow.dto.SignUpRequest;
import com.rentflow.model.User;
import com.rentflow.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class SignUpController {

    private final OnboardingService onboardingService;

    public SignUpController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest request) {
        User user = onboardingService.registerUser(request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("email", user.getEmail());
        response.put("id", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
