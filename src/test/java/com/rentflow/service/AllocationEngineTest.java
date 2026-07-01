package com.rentflow.service;

import com.rentflow.dto.nomba.VActData;
import com.rentflow.dto.nomba.VirtualAccountRequest;
import com.rentflow.model.*;
import com.rentflow.repository.*;
import com.rentflow.service.nomba.NombaClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class AllocationEngineTest {

    @Autowired
    private LeaseService leaseService;

    @Autowired
    private AllocationEngine allocationEngine;

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
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private PaymentAllocationRepository paymentAllocationRepository;

    @Autowired
    private InboundTransactionRepository inboundTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private NombaClient nombaClient;

    private Lease testLease;
    private Unit testUnit;

    @BeforeEach
    void setUp() {
        // Clean DB tables in logical order
        paymentAllocationRepository.deleteAllInBatch();
        inboundTransactionRepository.deleteAllInBatch();
        ledgerEntryRepository.deleteAllInBatch();
        leaseRepository.deleteAllInBatch();
        unitRepository.deleteAllInBatch();
        propertyRepository.deleteAllInBatch();
        tenantRepository.deleteAllInBatch();
        landlordRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // Seed Roles
        Role roleLandlord = roleRepository.findByName("ROLE_LANDLORD")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_LANDLORD")));
        Role roleTenant = roleRepository.findByName("ROLE_TENANT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_TENANT")));

        // Create Landlord
        User landlordUser = new User();
        landlordUser.setEmail("landlord_alloc@rentflow.com");
        landlordUser.setPasswordHash(passwordEncoder.encode("password"));
        landlordUser.setRoles(Set.of(roleLandlord));
        landlordUser = userRepository.save(landlordUser);

        Landlord landlord = new Landlord();
        landlord.setUser(landlordUser);
        landlord.setFirstName("L");
        landlord.setLastName("Alloc");
        landlord.setPhoneNumber("+2348000000400");
        landlord.setBankCode("058");
        landlord.setBankAccountNumber("7777777777");
        landlord.setBankAccountName("Alloc Landlord");
        landlordRepository.save(landlord);

        // Create Tenant
        User tenantUser = new User();
        tenantUser.setEmail("tenant_alloc@rentflow.com");
        tenantUser.setPasswordHash(passwordEncoder.encode("password"));
        tenantUser.setRoles(Set.of(roleTenant));
        tenantUser = userRepository.save(tenantUser);

        Tenant tenant = new Tenant();
        tenant.setUser(tenantUser);
        tenant.setFirstName("T");
        tenant.setLastName("Alloc");
        tenant.setPhoneNumber("+2348000000401");
        tenant.setBvn("44444444444");
        tenant = tenantRepository.save(tenant);

        // Create Property & Unit
        Property property = new Property();
        property.setLandlord(landlord);
        property.setName("Alloc Property");
        property.setAddress("456 Alloc St");
        property.setPropertyCode("ALLPROP");
        property = propertyRepository.save(property);

        testUnit = new Unit();
        testUnit.setProperty(property);
        testUnit.setUnitNumber("Suite 10");
        testUnit.setBaseRent(new BigDecimal("150000.00")); // 150k Rent
        testUnit.setStatus(UnitStatus.VACANT);
        testUnit = unitRepository.save(testUnit);

        // Mock NombaClient VA generation
        Mockito.when(nombaClient.createVirtualAccount(any())).thenReturn(
                new VActData("9911991199", "Nomba/Wema Bank", "RF_LSE_ALLOC_REF")
        );

        // Create Lease (automatically generates Water 10k & Rent 150k)
        testLease = leaseService.createLease(
                tenant.getId(),
                testUnit.getId(),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                5,
                new BigDecimal("5.00")
        );
    }

    private UUID createAndSaveInboundTransaction(BigDecimal amount) {
        InboundTransaction tx = new InboundTransaction();
        tx.setLease(testLease);
        tx.setNombaTransactionId("MOCK_TX_" + java.util.UUID.randomUUID().toString());
        tx.setAmount(amount);
        tx.setTransactionTime(java.time.OffsetDateTime.now());
        tx.setRawPayload("{}");
        InboundTransaction saved = inboundTransactionRepository.save(tx);
        return saved.getId();
    }

    @Test
    @Transactional
    void testExactPaymentScenario() {
        // Send exactly ₦160,000.00 (10k Water + 150k Rent)
        UUID txId = createAndSaveInboundTransaction(new BigDecimal("160000.00"));
        allocationEngine.allocatePayment(txId, new BigDecimal("160000.00"), testLease);

        List<LedgerEntry> entries = ledgerEntryRepository.findByLeaseOrderByDueDateAsc(testLease);
        assertEquals(2, entries.size());

        for (LedgerEntry entry : entries) {
            assertEquals("PAID", entry.getStatus());
            assertEquals(0, entry.getAmountDue().compareTo(entry.getAmountPaid()));
        }

        Lease refreshedLease = leaseRepository.findById(testLease.getId()).orElseThrow();
        assertEquals(0, refreshedLease.getDepositWalletBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    @Transactional
    void testUnderpaymentSplittingScenario() {
        // Send ₦100,000.00 (Water 10k is fully paid, remaining 90k goes to Rent)
        UUID txId = createAndSaveInboundTransaction(new BigDecimal("100000.00"));
        allocationEngine.allocatePayment(txId, new BigDecimal("100000.00"), testLease);

        List<LedgerEntry> entries = ledgerEntryRepository.findByLeaseOrderByDueDateAsc(testLease);
        assertEquals(2, entries.size());

        // Entry 1: Water (due first, sorted first)
        LedgerEntry water = entries.stream().filter(e -> e.getEntryType().equals("UTILITY_WATER")).findFirst().orElseThrow();
        assertEquals("PAID", water.getStatus());
        assertEquals(0, water.getAmountPaid().compareTo(new BigDecimal("10000.00")));

        // Entry 2: Rent (due second, partially paid)
        LedgerEntry rent = entries.stream().filter(e -> e.getEntryType().equals("RENT")).findFirst().orElseThrow();
        assertEquals("PARTIALLY_PAID", rent.getStatus());
        assertEquals(0, rent.getAmountPaid().compareTo(new BigDecimal("90000.00")));

        Lease refreshedLease = leaseRepository.findById(testLease.getId()).orElseThrow();
        assertEquals(0, refreshedLease.getDepositWalletBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    @Transactional
    void testOverpaymentRolloverCreditScenario() {
        // Send ₦180,000.00 (Water 10k paid, Rent 150k paid, 20k Rollover)
        UUID txId = createAndSaveInboundTransaction(new BigDecimal("180000.00"));
        allocationEngine.allocatePayment(txId, new BigDecimal("180000.00"), testLease);

        List<LedgerEntry> entries = ledgerEntryRepository.findByLeaseOrderByDueDateAsc(testLease);
        assertEquals(2, entries.size());

        for (LedgerEntry entry : entries) {
            assertEquals("PAID", entry.getStatus());
        }

        Lease refreshedLease = leaseRepository.findById(testLease.getId()).orElseThrow();
        assertEquals(0, refreshedLease.getDepositWalletBalance().compareTo(new BigDecimal("20000.00")));
    }

    @Test
    void testConcurrentPaymentAllocations() throws Exception {
        // Pre-create transaction records to avoid constraint errors
        UUID tx1 = createAndSaveInboundTransaction(new BigDecimal("80000.00"));
        UUID tx2 = createAndSaveInboundTransaction(new BigDecimal("80000.00"));

        // Run outside @Transactional block to allow distinct transactions/threads to commit to DB.
        // Launch 2 threads concurrently sending ₦80,000.00 each (total ₦160,000.00)
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            final UUID txId = (index == 0) ? tx1 : tx2;
            executor.submit(() -> {
                try {
                    latch.await();
                    allocationEngine.allocatePayment(txId, new BigDecimal("80000.00"), testLease);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Refresh and verify
        Lease refreshedLease = leaseRepository.findById(testLease.getId()).orElseThrow();
        List<LedgerEntry> entries = ledgerEntryRepository.findByLeaseOrderByDueDateAsc(refreshedLease);

        for (LedgerEntry entry : entries) {
            assertEquals("PAID", entry.getStatus());
            assertEquals(0, entry.getAmountDue().compareTo(entry.getAmountPaid()));
        }
        assertEquals(0, refreshedLease.getDepositWalletBalance().compareTo(BigDecimal.ZERO));
    }
}
