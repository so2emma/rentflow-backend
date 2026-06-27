package com.rentflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.dto.PropertyRequest;
import com.rentflow.dto.UnitRequest;
import com.rentflow.model.*;
import com.rentflow.repository.*;
import com.rentflow.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PropertyControllerTest {

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
    private PropertyRepository propertyRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User landlordAUser;
    private User landlordBUser;
    private User tenantUser;

    private Landlord landlordA;
    private Landlord landlordB;

    private Property propertyB;

    @BeforeEach
    void setUp() {
        Role roleLandlord = roleRepository.findByName("ROLE_LANDLORD")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_LANDLORD")));
        Role roleTenant = roleRepository.findByName("ROLE_TENANT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_TENANT")));

        // Create Landlord A
        landlordAUser = new User();
        landlordAUser.setEmail("landlorda@rentflow.com");
        landlordAUser.setPasswordHash(passwordEncoder.encode("password"));
        landlordAUser.setRoles(Set.of(roleLandlord));
        landlordAUser = userRepository.save(landlordAUser);

        landlordA = new Landlord();
        landlordA.setUser(landlordAUser);
        landlordA.setFirstName("Landlord");
        landlordA.setLastName("A");
        landlordA.setPhoneNumber("+2348000000010");
        landlordA.setBankCode("058");
        landlordA.setBankAccountNumber("1111111111");
        landlordA.setBankAccountName("Landlord A Account");
        landlordA = landlordRepository.save(landlordA);

        // Create Landlord B
        landlordBUser = new User();
        landlordBUser.setEmail("landlordb@rentflow.com");
        landlordBUser.setPasswordHash(passwordEncoder.encode("password"));
        landlordBUser.setRoles(Set.of(roleLandlord));
        landlordBUser = userRepository.save(landlordBUser);

        landlordB = new Landlord();
        landlordB.setUser(landlordBUser);
        landlordB.setFirstName("Landlord");
        landlordB.setLastName("B");
        landlordB.setPhoneNumber("+2348000000011");
        landlordB.setBankCode("058");
        landlordB.setBankAccountNumber("2222222222");
        landlordB.setBankAccountName("Landlord B Account");
        landlordB = landlordRepository.save(landlordB);

        // Create Tenant
        tenantUser = new User();
        tenantUser.setEmail("tenant@rentflow.com");
        tenantUser.setPasswordHash(passwordEncoder.encode("password"));
        tenantUser.setRoles(Set.of(roleTenant));
        tenantUser = userRepository.save(tenantUser);

        Tenant tenant = new Tenant();
        tenant.setUser(tenantUser);
        tenant.setFirstName("Tenant");
        tenant.setLastName("One");
        tenant.setPhoneNumber("+2348000000012");
        tenant.setBvn("12345678901");
        tenantRepository.save(tenant);

        // Create Property B (owned by Landlord B)
        propertyB = new Property();
        propertyB.setLandlord(landlordB);
        propertyB.setName("Property B");
        propertyB.setAddress("456 Landlord B St");
        propertyB.setPropertyCode("PROPB");
        propertyB = propertyRepository.save(propertyB);
    }

    @Test
    void testTenantPropertyBlockTest() throws Exception {
        String token = jwtService.generateToken(tenantUser);

        PropertyRequest request = new PropertyRequest("Tenant's Illegal Property", "123 St", "ILLEGAL");

        mockMvc.perform(post("/api/v1/properties")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCrossPropertyUnitBlockTest() throws Exception {
        // Authenticate as Landlord A
        String token = jwtService.generateToken(landlordAUser);

        // Attempt to add a unit to Landlord B's property
        UnitRequest request = new UnitRequest("Unit 101", new BigDecimal("150000.00"), UnitStatus.VACANT);

        mockMvc.perform(post("/api/v1/properties/" + propertyB.getId() + "/units")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLandlordPropertyAndUnitCreationSuccess() throws Exception {
        // Authenticate as Landlord A
        String token = jwtService.generateToken(landlordAUser);

        // 1. Create Property
        PropertyRequest propRequest = new PropertyRequest("Landlord A's Plaza", "789 Plaza Rd", "PLAZAA");
        String propResponse = mockMvc.perform(post("/api/v1/properties")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(propRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract property ID from response
        String propertyIdStr = objectMapper.readTree(propResponse).get("id").asText();
        UUID propertyId = UUID.fromString(propertyIdStr);

        // 2. Create Unit under that property
        UnitRequest unitRequest = new UnitRequest("Suite 100", new BigDecimal("200000.00"), UnitStatus.VACANT);
        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/units")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unitRequest)))
                .andExpect(status().isCreated());
    }
}
