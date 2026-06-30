package com.rentflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.model.*;
import com.rentflow.repository.*;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class NombaWebhookControllerTest {

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
    private InboundTransactionRepository inboundTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Lease testLease;

    @BeforeEach
    void setUp() {
        Role roleLandlord = roleRepository.findByName("ROLE_LANDLORD")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_LANDLORD")));
        Role roleTenant = roleRepository.findByName("ROLE_TENANT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_TENANT")));

        // Create Landlord
        User landlordUser = new User();
        landlordUser.setEmail("landlord_webhook@rentflow.com");
        landlordUser.setPasswordHash(passwordEncoder.encode("password"));
        landlordUser.setRoles(Set.of(roleLandlord));
        landlordUser = userRepository.save(landlordUser);

        Landlord landlord = new Landlord();
        landlord.setUser(landlordUser);
        landlord.setFirstName("L");
        landlord.setLastName("Webhook");
        landlord.setPhoneNumber("+2348000000300");
        landlord.setBankCode("058");
        landlord.setBankAccountNumber("8888888888");
        landlord.setBankAccountName("Webhook Landlord Account");
        landlord = landlordRepository.save(landlord);

        // Create Tenant
        User tenantUser = new User();
        tenantUser.setEmail("tenant_webhook@rentflow.com");
        tenantUser.setPasswordHash(passwordEncoder.encode("password"));
        tenantUser.setRoles(Set.of(roleTenant));
        tenantUser = userRepository.save(tenantUser);

        Tenant tenant = new Tenant();
        tenant.setUser(tenantUser);
        tenant.setFirstName("T");
        tenant.setLastName("Webhook");
        tenant.setPhoneNumber("+2348000000301");
        tenant.setBvn("33333333333");
        tenant = tenantRepository.save(tenant);

        // Create Property & Unit
        Property property = new Property();
        property.setLandlord(landlord);
        property.setName("Webhook Property");
        property.setAddress("123 Webhook St");
        property.setPropertyCode("WEBPROP");
        property = propertyRepository.save(property);

        Unit unit = new Unit();
        unit.setProperty(property);
        unit.setUnitNumber("Unit 404");
        unit.setBaseRent(new BigDecimal("200000.00"));
        unit.setStatus(UnitStatus.VACANT);
        unit = unitRepository.save(unit);

        // Create Lease
        testLease = new Lease();
        testLease.setTenant(tenant);
        testLease.setUnit(unit);
        testLease.setStartDate(LocalDate.now());
        testLease.setEndDate(LocalDate.now().plusYears(1));
        testLease.setNombaVactRef("RF_LSE_WEBHOOK_REF");
        testLease.setNombaVactNumber("9900990099");
        testLease.setNombaVactBank("Nomba/Wema Bank");
        testLease.setStatus(LeaseStatus.ACTIVE);
        testLease = leaseRepository.save(testLease);
    }

    @Test
    void testAuthorizedWebhookIngestionSuccess() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String payload = getPayload("tx_authorized_1", "RF_LSE_WEBHOOK_REF", "150000.00", "2026-06-26T01:30:00Z");

        String signature = generateSignature(
                timestamp,
                "payment_success",
                "req_auth_123",
                "user_auth_1",
                "wallet_auth_1",
                "tx_authorized_1",
                "vact_transfer",
                "2026-06-26T01:30:00Z",
                "null"
        );

        mockMvc.perform(post("/api/v1/webhooks/nomba")
                .header("nomba-signature", signature)
                .header("nomba-timestamp", timestamp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        // Verify logged transaction
        assertTrue(inboundTransactionRepository.existsByNombaTransactionId("tx_authorized_1"));
        InboundTransaction tx = inboundTransactionRepository.findAll().stream()
                .filter(t -> t.getNombaTransactionId().equals("tx_authorized_1"))
                .findFirst().orElseThrow();

        assertEquals(0, tx.getAmount().compareTo(new BigDecimal("150000.00")));
        assertEquals("RF_LSE_WEBHOOK_REF", tx.getLease().getNombaVactRef());
        assertEquals("Jane Sender", tx.getSenderName());
        assertEquals("Wema Bank", tx.getSenderBankName());
        assertEquals("0123456789", tx.getSenderAccountNumber());
    }

    @Test
    void testUnauthorizedWebhookRejected() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String payload = getPayload("tx_unauthorized_1", "RF_LSE_WEBHOOK_REF", "150000.00", "2026-06-26T01:30:00Z");

        // Send incorrect signature
        String badSignature = "invalid_signature_hash_value=";

        mockMvc.perform(post("/api/v1/webhooks/nomba")
                .header("nomba-signature", badSignature)
                .header("nomba-timestamp", timestamp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testExpiredTimestampRejected() throws Exception {
        // Send timestamp 10 minutes in the past
        String expiredTimestamp = String.valueOf(System.currentTimeMillis() - 600000);
        String payload = getPayload("tx_expired_1", "RF_LSE_WEBHOOK_REF", "150000.00", "2026-06-26T01:30:00Z");

        String signature = generateSignature(
                expiredTimestamp,
                "payment_success",
                "req_auth_123",
                "user_auth_1",
                "wallet_auth_1",
                "tx_expired_1",
                "vact_transfer",
                "2026-06-26T01:30:00Z",
                "null"
        );

        mockMvc.perform(post("/api/v1/webhooks/nomba")
                .header("nomba-signature", signature)
                .header("nomba-timestamp", expiredTimestamp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testWebhookDeduplicationIdempotency() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String payload = getPayload("tx_duplicate_1", "RF_LSE_WEBHOOK_REF", "150000.00", "2026-06-26T01:30:00Z");

        String signature = generateSignature(
                timestamp,
                "payment_success",
                "req_auth_123",
                "user_auth_1",
                "wallet_auth_1",
                "tx_duplicate_1",
                "vact_transfer",
                "2026-06-26T01:30:00Z",
                "null"
        );

        // Perform first post
        mockMvc.perform(post("/api/v1/webhooks/nomba")
                .header("nomba-signature", signature)
                .header("nomba-timestamp", timestamp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        // Perform second post (exact duplicate)
        mockMvc.perform(post("/api/v1/webhooks/nomba")
                .header("nomba-signature", signature)
                .header("nomba-timestamp", timestamp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        // Verify only ONE transaction is logged in the database
        long count = inboundTransactionRepository.findAll().stream()
                .filter(t -> t.getNombaTransactionId().equals("tx_duplicate_1"))
                .count();

        assertEquals(1, count);
    }

    private String generateSignature(String timestamp, String eventType, String requestId, String userId,
                                      String walletId, String transactionId, String txType, String txTime,
                                      String responseCode) throws Exception {
        String cleanResCode = (responseCode == null || "null".equalsIgnoreCase(responseCode)) ? "" : responseCode;
        String validationString = String.format("%s:%s:%s:%s:%s:%s:%s:%s:%s",
                eventType, requestId, userId, walletId, transactionId, txType, txTime, cleanResCode, timestamp);

        javax.crypto.Mac sha256HMAC = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                "rentflow_webhook_secret_key".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256HMAC.init(secretKeySpec);
        byte[] hashBytes = sha256HMAC.doFinal(validationString.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    private String getPayload(String txId, String accountRef, String amount, String time) {
        return "{\n" +
                "  \"event_type\": \"payment_success\",\n" +
                "  \"requestId\": \"req_auth_123\",\n" +
                "  \"data\": {\n" +
                "    \"merchant\": {\n" +
                "      \"userId\": \"user_auth_1\",\n" +
                "      \"walletId\": \"wallet_auth_1\"\n" +
                "    },\n" +
                "    \"transaction\": {\n" +
                "      \"transactionId\": \"" + txId + "\",\n" +
                "      \"type\": \"vact_transfer\",\n" +
                "      \"time\": \"" + time + "\",\n" +
                "      \"responseCode\": null,\n" +
                "      \"accountRef\": \"" + accountRef + "\",\n" +
                "      \"amount\": " + amount + ",\n" +
                "      \"senderName\": \"Jane Sender\",\n" +
                "      \"senderBankName\": \"Wema Bank\",\n" +
                "      \"senderAccountNumber\": \"0123456789\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
