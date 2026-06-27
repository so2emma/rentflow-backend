package com.rentflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.dto.LoginRequest;
import com.rentflow.model.Role;
import com.rentflow.model.User;
import com.rentflow.repository.RoleRepository;
import com.rentflow.repository.UserRepository;
import com.rentflow.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        String testEmail = "landlord@rentflow.com";
        if (userRepository.findByEmail(testEmail).isEmpty()) {
            Role landlordRole = roleRepository.findByName("ROLE_LANDLORD")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_LANDLORD")));

            User landlord = new User();
            landlord.setEmail(testEmail);
            landlord.setPasswordHash(passwordEncoder.encode("securepassword"));
            landlord.setRoles(Set.of(landlordRole));
            landlord.setActive(true);

            userRepository.save(landlord);
        }
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        LoginRequest request = new LoginRequest("landlord@rentflow.com", "securepassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("landlord@rentflow.com")))
                .andExpect(jsonPath("$.roles", hasItem("ROLE_LANDLORD")));
    }

    @Test
    void testFailedLoginInvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest("landlord@rentflow.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUnsecuredRouteAccessBlocked() throws Exception {
        mockMvc.perform(get("/api/v1/properties"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSecuredRouteAccessAllowed() throws Exception {
        User user = userRepository.findByEmail("landlord@rentflow.com")
                .orElseThrow(() -> new IllegalStateException("Test landlord user not found"));
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/v1/properties")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("Property A")));
    }

    @Test
    void testWebhookPermitTest() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/nomba")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("Webhook received successfully")));
    }
}
