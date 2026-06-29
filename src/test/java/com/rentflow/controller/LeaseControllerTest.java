package com.rentflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.dto.LeaseRequest;
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
import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LeaseControllerTest {

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
    private UnitRepository unitRepository;

    @Autowired
    private LeaseRepository leaseRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User landlordUser;
    private User tenantUser;
    private Tenant tenantProfile;
    private Unit unitProfile;

    @BeforeEach
    void setUp() {
        Role roleLandlord = roleRepository.findByName("ROLE_LANDLORD")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_LANDLORD")));
        Role roleTenant = roleRepository.findByName("ROLE_TENANT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_TENANT")));

        // Create Landlord
        landlordUser = new User();
        landlordUser.setEmail("landlord_lease@rentflow.com");
        landlordUser.setPasswordHash(passwordEncoder.encode("password"));
        landlordUser.setRoles(Set.of(roleLandlord));
        landlordUser = userRepository.save(landlordUser);

        Landlord landlord = new Landlord();
        landlord.setUser(landlordUser);
        landlord.setFirstName("L");
        landlord.setLastName("Lease");
        landlord.setPhoneNumber("+2348000000200");
        landlord.setBankCode("058");
        landlord.setBankAccountNumber("8888888888");
        landlord.setBankAccountName("Lease Landlord Account");
        landlord = landlordRepository.save(landlord);

        // Create Tenant
        User tenantAuthUser = new User();
        tenantAuthUser.setEmail("tenant_lease_auth@rentflow.com");
        tenantAuthUser.setPasswordHash(passwordEncoder.encode("password"));
        tenantAuthUser.setRoles(Set.of(roleTenant));
        tenantAuthUser = userRepository.save(tenantAuthUser);

        tenantUser = new User();
        tenantUser.setEmail("tenant_lease@rentflow.com");
        tenantUser.setPasswordHash(passwordEncoder.encode("password"));
        tenantUser.setRoles(Set.of(roleTenant));
        tenantUser = userRepository.save(tenantUser);

        tenantProfile = new Tenant();
        tenantProfile.setUser(tenantUser);
        tenantProfile.setFirstName("T");
        tenantProfile.setLastName("Lease");
        tenantProfile.setPhoneNumber("+2348000000201");
        tenantProfile.setBvn("99999999999");
        tenantProfile = tenantRepository.save(tenantProfile);

        // Create Property & Unit
        Property property = new Property();
        property.setLandlord(landlord);
        property.setName("Lease Property");
        property.setAddress("789 Lease St");
        property.setPropertyCode("LEASEPROP");
        property = propertyRepository.save(property);

        unitProfile = new Unit();
        unitProfile.setProperty(property);
        unitProfile.setUnitNumber("Unit 900");
        unitProfile.setBaseRent(new BigDecimal("100000.00"));
        unitProfile.setStatus(UnitStatus.VACANT);
        unitProfile = unitRepository.save(unitProfile);
    }

    @Test
    void testCreateLeaseSuccess() throws Exception {
        String token = jwtService.generateToken(landlordUser);

        LeaseRequest request = new LeaseRequest(
                tenantProfile.getId(),
                unitProfile.getId(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                "NOMBA-VACT-REF-TEST-123"
        );

        mockMvc.perform(post("/api/v1/leases")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombaVactRef", org.hamcrest.Matchers.startsWith("RF_LSE_")))
                .andExpect(jsonPath("$.nombaVactNumber", notNullValue()))
                .andExpect(jsonPath("$.nombaVactBank", notNullValue()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testCreateLeaseTenantForbidden() throws Exception {
        String token = jwtService.generateToken(tenantUser);

        LeaseRequest request = new LeaseRequest(
                tenantProfile.getId(),
                unitProfile.getId(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                "NOMBA-VACT-REF-TEST-124"
        );

        mockMvc.perform(post("/api/v1/leases")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetActiveLeaseSuccess() throws Exception {
        // Create an active lease for the tenant first
        Lease lease = new Lease();
        lease.setTenant(tenantProfile);
        lease.setUnit(unitProfile);
        lease.setStartDate(LocalDate.now());
        lease.setEndDate(LocalDate.now().plusYears(1));
        lease.setNombaVactRef("RF_LSE_ACTIVE_REF_123");
        lease.setNombaVactNumber("9912345678");
        lease.setNombaVactBank("Nomba/Wema Bank");
        lease.setStatus(LeaseStatus.ACTIVE);
        leaseRepository.save(lease);

        String token = jwtService.generateToken(tenantUser);

        mockMvc.perform(get("/api/v1/leases/active")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombaVactNumber").value("9912345678"))
                .andExpect(jsonPath("$.nombaVactBank").value("Nomba/Wema Bank"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.baseRent").value(100000.00))
                .andExpect(jsonPath("$.unitNumber").value("Unit 900"));
    }

    @Test
    void testGetActiveLeaseNotFound() throws Exception {
        String token = jwtService.generateToken(tenantUser);

        mockMvc.perform(get("/api/v1/leases/active")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
