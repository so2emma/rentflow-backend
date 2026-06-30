package com.rentflow.controller;

import com.rentflow.security.WebhookVerifier;
import com.rentflow.service.InboundTransactionService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final WebhookVerifier verifier;
    private final InboundTransactionService transactionService;

    public WebhookController(WebhookVerifier verifier, InboundTransactionService transactionService) {
        this.verifier = verifier;
        this.transactionService = transactionService;
    }

    @PostMapping("/nomba")
    public ResponseEntity<?> handleNombaWebhook(
            @RequestHeader(value = "nomba-signature", required = false) String signature,
            @RequestHeader(value = "nomba-timestamp", required = false) String timestamp,
            @RequestBody(required = false) String rawPayload) {

        if (signature == null || timestamp == null || rawPayload == null || rawPayload.trim().isEmpty() || rawPayload.equals("{}")) {
            return ResponseEntity.ok("Webhook received successfully");
        }

        try {
            // 1. Parse JSON fields for signature calculation
            JSONObject json = new JSONObject(rawPayload);
            JSONObject data = json.getJSONObject("data");
            JSONObject tx = data.getJSONObject("transaction");
            JSONObject merchant = data.getJSONObject("merchant");

            // 2. Validate signature
            boolean isValid = verifier.verifySignature(
                    rawPayload, signature, timestamp,
                    json.getString("event_type"),
                    json.getString("requestId"),
                    merchant.getString("userId"),
                    merchant.getString("walletId"),
                    tx.getString("transactionId"),
                    tx.getString("type"),
                    tx.getString("time"),
                    tx.optString("responseCode", "")
            );

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 3. Process Ingestion
            transactionService.ingestTransaction(rawPayload, tx.getString("transactionId"));

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
