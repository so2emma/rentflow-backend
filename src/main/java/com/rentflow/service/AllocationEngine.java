package com.rentflow.service;

import com.rentflow.event.PayoutSplitRequiredEvent;
import com.rentflow.model.Lease;
import com.rentflow.model.LedgerEntry;
import com.rentflow.model.PaymentAllocation;
import com.rentflow.repository.LeaseRepository;
import com.rentflow.repository.LedgerEntryRepository;
import com.rentflow.repository.PaymentAllocationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AllocationEngine {

    private final LeaseRepository leaseRepository;
    private final LedgerEntryRepository ledgerRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AllocationEngine(
            LeaseRepository leaseRepository,
            LedgerEntryRepository ledgerRepository,
            PaymentAllocationRepository allocationRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.leaseRepository = leaseRepository;
        this.ledgerRepository = ledgerRepository;
        this.allocationRepository = allocationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void allocatePayment(UUID inboundTransactionId, BigDecimal totalAmount, Lease leaseRef) {
        // 1. Lock Lease & retrieve details
        Lease lease = leaseRepository.findByIdForUpdate(leaseRef.getId())
                .orElseThrow(() -> new EntityNotFoundException("Lease not found"));

        // 2. Fetch outstanding ledger entries locked
        List<LedgerEntry> entries = ledgerRepository.findOutstandingEntriesForUpdate(lease);

        // 3. Sort entries: LATE_FEE (1), UTILITIES (2), RENT (3) sorted by due date ascending
        entries.sort(Comparator.comparing(LedgerEntry::getDueDate)
                .thenComparing(entry -> getPriority(entry.getEntryType())));

        BigDecimal remainingFunds = totalAmount;

        for (LedgerEntry entry : entries) {
            if (remainingFunds.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal outstanding = entry.getAmountDue().subtract(entry.getAmountPaid());

            if (remainingFunds.compareTo(outstanding) >= 0) {
                // Fully Pay Entry
                entry.setAmountPaid(entry.getAmountDue());
                entry.setStatus("PAID");
                remainingFunds = remainingFunds.subtract(outstanding);

                logAllocation(inboundTransactionId, entry, outstanding, false);
            } else {
                // Partially Pay Entry
                entry.setAmountPaid(entry.getAmountPaid().add(remainingFunds));
                entry.setStatus("PARTIALLY_PAID");

                logAllocation(inboundTransactionId, entry, remainingFunds, false);
                remainingFunds = BigDecimal.ZERO;
            }
            ledgerRepository.save(entry);
        }

        // 4. Overpayment (Rollover Credit)
        if (remainingFunds.compareTo(BigDecimal.ZERO) > 0) {
            lease.setDepositWalletBalance(lease.getDepositWalletBalance().add(remainingFunds));
            leaseRepository.save(lease);

            logAllocation(inboundTransactionId, null, remainingFunds, true);
        }

        // 5. Fire Payout splits for Rent and Late Fees
        BigDecimal splitTotal = calculateSplitBase(inboundTransactionId);
        if (splitTotal.compareTo(BigDecimal.ZERO) > 0) {
            eventPublisher.publishEvent(new PayoutSplitRequiredEvent(this, inboundTransactionId, splitTotal));
        }
    }

    private int getPriority(String entryType) {
        if ("LATE_FEE".equals(entryType)) return 1;
        if (entryType != null && entryType.startsWith("UTILITY_")) return 2;
        if ("RENT".equals(entryType)) return 3;
        return 4;
    }

    private void logAllocation(UUID txId, LedgerEntry entry, BigDecimal amount, boolean isRollover) {
        PaymentAllocation allocation = new PaymentAllocation();
        allocation.setInboundTransactionId(txId);
        allocation.setLedgerEntry(entry);
        allocation.setAmountAllocated(amount);
        allocation.setDepositRollover(isRollover);
        allocationRepository.save(allocation);
    }

    private BigDecimal calculateSplitBase(UUID txId) {
        return allocationRepository.sumRentAndLateFeesForTransaction(txId);
    }
}
