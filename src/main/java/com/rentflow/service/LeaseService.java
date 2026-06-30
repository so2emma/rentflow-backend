package com.rentflow.service;

import com.rentflow.dto.nomba.VActData;
import com.rentflow.dto.nomba.VirtualAccountRequest;
import com.rentflow.model.*;
import com.rentflow.repository.LeaseRepository;
import com.rentflow.repository.LedgerEntryRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import com.rentflow.service.nomba.NombaClient;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;
    private final NombaClient nombaClient;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LeaseService(
            LeaseRepository leaseRepository,
            TenantRepository tenantRepository,
            UnitRepository unitRepository,
            NombaClient nombaClient,
            LedgerEntryRepository ledgerEntryRepository
    ) {
        this.leaseRepository = leaseRepository;
        this.tenantRepository = tenantRepository;
        this.unitRepository = unitRepository;
        this.nombaClient = nombaClient;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public Lease createLease(UUID tenantId, UUID unitId, LocalDate start, LocalDate end, Integer gracePeriodDays, BigDecimal lateFeePercentage) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new EntityNotFoundException("Unit not found"));

        UUID leaseId = UUID.randomUUID();
        String accountRef = "RF_LSE_" + leaseId.toString().replace("-", "");
        String accountName = "RentFlow / " + tenant.getFirstName() + " " + tenant.getLastName();

        // Call Nomba API
        VActData vAct = nombaClient.createVirtualAccount(
                new VirtualAccountRequest(accountRef, accountName, tenant.getBvn(), unit.getBaseRent())
        );

        Lease lease = new Lease();
        lease.setId(leaseId);
        lease.setTenant(tenant);
        lease.setUnit(unit);
        lease.setStartDate(start);
        lease.setEndDate(end);
        lease.setNombaVactRef(accountRef);
        lease.setNombaVactNumber(vAct.bankAccountNumber());
        lease.setNombaVactBank(vAct.bankName());
        lease.setStatus(LeaseStatus.ACTIVE);

        if (gracePeriodDays != null) {
            lease.setGracePeriodDays(gracePeriodDays);
        }
        if (lateFeePercentage != null) {
            lease.setLateFeePercentage(lateFeePercentage);
        }

        unit.setStatus(UnitStatus.OCCUPIED);
        unitRepository.save(unit);

        Lease savedLease = leaseRepository.save(lease);

        // Auto-generate outstanding invoices upon lease activation
        // 1. Water Utility
        LedgerEntry waterUtility = new LedgerEntry();
        waterUtility.setLease(savedLease);
        waterUtility.setEntryType("UTILITY_WATER");
        waterUtility.setAmountDue(new BigDecimal("10000.00"));
        waterUtility.setDueDate(start);
        waterUtility.setStatus("UNPAID");
        ledgerEntryRepository.save(waterUtility);

        // 2. Rent
        LedgerEntry rent = new LedgerEntry();
        rent.setLease(savedLease);
        rent.setEntryType("RENT");
        rent.setAmountDue(unit.getBaseRent());
        rent.setDueDate(start);
        rent.setStatus("UNPAID");
        ledgerEntryRepository.save(rent);

        return savedLease;
    }
}
