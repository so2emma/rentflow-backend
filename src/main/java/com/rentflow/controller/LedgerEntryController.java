package com.rentflow.controller;

import com.rentflow.model.LedgerEntry;
import com.rentflow.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leases/active/ledgers")
public class LedgerEntryController {

    private final com.rentflow.service.LedgerEntryService ledgerEntryService;

    public LedgerEntryController(com.rentflow.service.LedgerEntryService ledgerEntryService) {
        this.ledgerEntryService = ledgerEntryService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_TENANT')")
    public ResponseEntity<?> getActiveLeaseLedgers(@AuthenticationPrincipal User user) {
        List<LedgerEntry> entries = ledgerEntryService.getActiveLeaseLedgers(user);

        List<Map<String, Object>> response = entries.stream()
                .map(le -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", le.getId().toString());
                    map.put("dueDate", le.getDueDate().toString());
                    map.put("entryType", le.getEntryType());
                    map.put("amountDue", le.getAmountDue());
                    map.put("amountPaid", le.getAmountPaid());
                    map.put("status", le.getStatus());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
