package com.rentflow.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class WebhookVerifier {

    private final String secretKey;

    public WebhookVerifier(@Value("${nomba.webhook.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean verifySignature(
            String payload,
            String signatureHeader,
            String timestampHeader,
            String eventType,
            String requestId,
            String userId,
            String walletId,
            String transactionId,
            String txType,
            String txTime,
            String responseCode) {

        // Check timestamp (Replay Attack Prevention)
        try {
            long timestamp = Long.parseLong(timestampHeader);
            long currentMillis = System.currentTimeMillis();
            if (Math.abs(currentMillis - timestamp) > 300000) { // 5 minutes window
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        // Clean responseCode null strings
        String cleanResCode = (responseCode == null || "null".equalsIgnoreCase(responseCode)) ? "" : responseCode;

        // Construct validation string
        String validationString = String.format("%s:%s:%s:%s:%s:%s:%s:%s:%s",
                eventType, requestId, userId, walletId, transactionId, txType, txTime, cleanResCode, timestampHeader);

        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKeySpec);
            byte[] hashBytes = sha256HMAC.doFinal(validationString.getBytes(StandardCharsets.UTF_8));
            String calculatedSig = Base64.getEncoder().encodeToString(hashBytes);

            // Constant-time comparison
            return MessageDigest.isEqual(
                calculatedSig.getBytes(StandardCharsets.UTF_8), 
                signatureHeader.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }
}
