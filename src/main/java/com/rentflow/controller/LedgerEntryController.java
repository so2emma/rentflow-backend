package com.rentflow.controller;

import com.rentflow.model.Lease;
import com.rentflow.model.LeaseStatus;
import com.rentflow.model.LedgerEntry;
import com.rentflow.model.Tenant;
import com.rentflow.model.User;
import com.rentflow.repository.LeaseRepository;
import com.rentflow.repository.LedgerEntryRepository;
import com.rentflow.repository.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leases/active/ledgers")
public class LedgerEntryController {

    private final TenantRepository tenantRepository;
    private final LeaseRepository leaseRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerEntryController(
            TenantRepository tenantRepository,
            LeaseRepository leaseRepository,
            LedgerEntryRepository ledgerEntryRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.leaseRepository = leaseRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_TENANT')")
    public ResponseEntity<?> getActiveLeaseLedgers(@AuthenticationPrincipal User user) {
        Tenant tenant = tenantRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant profile not found"));

        Lease activeLease = leaseRepository.findByTenantAndStatus(tenant, LeaseStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active lease found for this tenant"));

        List<LedgerEntry> entries = ledgerEntryRepository.findByLeaseOrderByDueDateAsc(activeLease);

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
