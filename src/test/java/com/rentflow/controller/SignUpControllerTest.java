package com.rentflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.dto.LandlordDetails;
import com.rentflow.dto.SignUpRequest;
import com.rentflow.dto.TenantDetails;
import com.rentflow.model.Landlord;
import com.rentflow.model.Role;
import com.rentflow.model.Tenant;
import com.rentflow.model.User;
import com.rentflow.repository.LandlordRepository;
import com.rentflow.repository.RoleRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SignUpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private LandlordRepository landlordRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Ensure roles exist
        ensureRoleExists("ROLE_LANDLORD");
        ensureRoleExists("ROLE_TENANT");
        ensureRoleExists("ROLE_ADMIN");
    }

    private void ensureRoleExists(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            roleRepository.save(new Role(roleName));
        }
    }

    @Test
    void testLandlordSignUpSuccess() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("newlandlord@rentflow.com");
        request.setPassword("securepass123");
        request.setRole("LANDLORD");
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setPhoneNumber("+2348000000001");

        LandlordDetails landlordDetails = new LandlordDetails();
        landlordDetails.setBankCode("058");
        landlordDetails.setBankAccountNumber("0123456789");
        landlordDetails.setBankAccountName("Jane Doe");
        request.setLandlordDetails(landlordDetails);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email").value("newlandlord@rentflow.com"));

        // Verify records in DB
        Optional<User> userOpt = userRepository.findByEmail("newlandlord@rentflow.com");
        assertTrue(userOpt.isPresent());

        Optional<Landlord> landlordOpt = landlordRepository.findByUser(userOpt.get());
        assertTrue(landlordOpt.isPresent());
    }

    @Test
    void testDuplicateEmailReject() throws Exception {
        // Sign up first landlord
        SignUpRequest request = new SignUpRequest();
        request.setEmail("duplicate@rentflow.com");
        request.setPassword("securepass123");
        request.setRole("LANDLORD");
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setPhoneNumber("+2348000000001");

        LandlordDetails landlordDetails = new LandlordDetails();
        landlordDetails.setBankCode("058");
        landlordDetails.setBankAccountNumber("0123456789");
        landlordDetails.setBankAccountName("Jane Doe");
        request.setLandlordDetails(landlordDetails);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Try signing up again with same email
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testTenantMissingBvnReject() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("tenantnoBvn@rentflow.com");
        request.setPassword("securepass123");
        request.setRole("TENANT");
        request.setFirstName("Tenny");
        request.setLastName("Tenant");
        request.setPhoneNumber("+2348000000002");
        // No tenantDetails (BVN missing)

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLandlordMissingBankDetailsReject() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("landlordnobank@rentflow.com");
        request.setPassword("securepass123");
        request.setRole("LANDLORD");
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setPhoneNumber("+2348000000001");
        // No landlordDetails (Bank details missing)

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
