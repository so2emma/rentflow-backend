package com.rentflow.service;

import com.rentflow.dto.nomba.VActData;
import com.rentflow.model.*;
import com.rentflow.repository.*;
import com.rentflow.service.nomba.NombaClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class LeaseServiceIntegrationTest {

    @Autowired
    private LeaseService leaseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private LandlordRepository landlordRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private LeaseRepository leaseRepository;

    @MockBean
    private NombaClient nombaClient;

    private Tenant testTenant;
    private Unit testUnit;

    @BeforeEach
    void setUp() {
        Role roleTenant = roleRepository.findByName("ROLE_TENANT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_TENANT")));
        Role roleLandlord = roleRepository.findByName("ROLE_LANDLORD")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_LANDLORD")));

        // Create Landlord
        User landlordUser = new User();
        landlordUser.setEmail("landlord_int@rentflow.com");
        landlordUser.setPasswordHash("pass");
        landlordUser.setRoles(Set.of(roleLandlord));
        landlordUser = userRepository.save(landlordUser);

        Landlord landlord = new Landlord();
        landlord.setUser(landlordUser);
        landlord.setFirstName("L");
        landlord.setLastName("Int");
        landlord.setPhoneNumber("+2348000000300");
        landlord.setBankCode("058");
        landlord.setBankAccountNumber("8888888888");
        landlord.setBankAccountName("Int Landlord Account");
        landlord = landlordRepository.save(landlord);

        // Create Tenant
        User tenantUser = new User();
        tenantUser.setEmail("tenant_int@rentflow.com");
        tenantUser.setPasswordHash("pass");
        tenantUser.setRoles(Set.of(roleTenant));
        tenantUser = userRepository.save(tenantUser);

        testTenant = new Tenant();
        testTenant.setUser(tenantUser);
        testTenant.setFirstName("Tenant");
        testTenant.setLastName("Int");
        testTenant.setPhoneNumber("+2348000000301");
        testTenant.setBvn("22222222222");
        testTenant = tenantRepository.save(testTenant);

        // Create Property & Unit
        Property property = new Property();
        property.setLandlord(landlord);
        property.setName("Int Property");
        property.setAddress("456 Int Rd");
        property.setPropertyCode("INTPROP");
        property = propertyRepository.save(property);

        testUnit = new Unit();
        testUnit.setProperty(property);
        testUnit.setUnitNumber("Unit 101");
        testUnit.setBaseRent(new BigDecimal("150000.00"));
        testUnit.setStatus(UnitStatus.VACANT);
        testUnit = unitRepository.save(testUnit);
    }

    @Test
    void testCreateLeaseSuccess() {
        VActData mockVAct = new VActData("1234567890", "Nomba/Wema Bank", "RF_LSE_test");
        when(nombaClient.createVirtualAccount(any())).thenReturn(mockVAct);

        Lease lease = leaseService.createLease(
                testTenant.getId(),
                testUnit.getId(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                5,
                new BigDecimal("5.00")
        );

        assertNotNull(lease.getId());
        assertEquals("1234567890", lease.getNombaVactNumber());
        assertEquals("Nomba/Wema Bank", lease.getNombaVactBank());
        assertEquals(LeaseStatus.ACTIVE, lease.getStatus());

        Unit updatedUnit = unitRepository.findById(testUnit.getId()).orElseThrow();
        assertEquals(UnitStatus.OCCUPIED, updatedUnit.getStatus());
    }

    @Test
    void testCreateLeaseRollbackOnError() {
        when(nombaClient.createVirtualAccount(any()))
                .thenThrow(new RuntimeException("Nomba API Error: Invalid BVN length"));

        assertThrows(RuntimeException.class, () -> {
            leaseService.createLease(
                    testTenant.getId(),
                    testUnit.getId(),
                    LocalDate.now(),
                    LocalDate.now().plusYears(1),
                    5,
                    new BigDecimal("5.00")
            );
        });

        Unit sameUnit = unitRepository.findById(testUnit.getId()).orElseThrow();
        assertEquals(UnitStatus.VACANT, sameUnit.getStatus());
    }
}
