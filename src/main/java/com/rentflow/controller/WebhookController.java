package com.rentflow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    @PostMapping("/nomba")
    public ResponseEntity<?> nombaWebhook(@RequestBody(required = false) String payload) {
        return ResponseEntity.ok("Webhook received successfully");
    }
}
