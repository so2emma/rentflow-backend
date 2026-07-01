package com.rentflow.controller;

import com.rentflow.dto.LedgerEntryResponse;
import com.rentflow.model.LedgerEntry;
import com.rentflow.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
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

        List<LedgerEntryResponse> response = entries.stream()
                .map(le -> {
                    LedgerEntryResponse resp = new LedgerEntryResponse();
                    resp.setId(le.getId());
                    resp.setDueDate(le.getDueDate());
                    resp.setEntryType(le.getEntryType());
                    resp.setAmountDue(le.getAmountDue());
                    resp.setAmountPaid(le.getAmountPaid());
                    resp.setStatus(le.getStatus());
                    return resp;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
