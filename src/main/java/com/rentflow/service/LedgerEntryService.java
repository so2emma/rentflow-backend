package com.rentflow.service;

import com.rentflow.model.Lease;
import com.rentflow.model.LeaseStatus;
import com.rentflow.model.LedgerEntry;
import com.rentflow.model.Tenant;
import com.rentflow.model.User;
import com.rentflow.repository.LeaseRepository;
import com.rentflow.repository.LedgerEntryRepository;
import com.rentflow.repository.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
public class LedgerEntryService {

    private final TenantRepository tenantRepository;
    private final LeaseRepository leaseRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerEntryService(
            TenantRepository tenantRepository,
            LeaseRepository leaseRepository,
            LedgerEntryRepository ledgerEntryRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.leaseRepository = leaseRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public List<LedgerEntry> getActiveLeaseLedgers(User user) {
        log.info("Fetching ledger entries for active lease userId={}", user.getId());
        Tenant tenant = tenantRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant profile not found"));

        Lease activeLease = leaseRepository.findByTenantAndStatus(tenant, LeaseStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active lease found for this tenant"));

        return ledgerEntryRepository.findByLeaseOrderByDueDateAsc(activeLease);
    }
}
