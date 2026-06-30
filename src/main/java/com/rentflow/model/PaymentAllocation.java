package com.rentflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_allocations")
public class PaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "inbound_transaction_id", nullable = false, columnDefinition = "UUID")
    private UUID inboundTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_entry_id")
    private LedgerEntry ledgerEntry;

    @Column(name = "amount_allocated", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountAllocated;

    @Column(name = "is_deposit_rollover", nullable = false)
    private boolean isDepositRollover = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public PaymentAllocation() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInboundTransactionId() {
        return inboundTransactionId;
    }

    public void setInboundTransactionId(UUID inboundTransactionId) {
        this.inboundTransactionId = inboundTransactionId;
    }

    public LedgerEntry getLedgerEntry() {
        return ledgerEntry;
    }

    public void setLedgerEntry(LedgerEntry ledgerEntry) {
        this.ledgerEntry = ledgerEntry;
    }

    public BigDecimal getAmountAllocated() {
        return amountAllocated;
    }

    public void setAmountAllocated(BigDecimal amountAllocated) {
        this.amountAllocated = amountAllocated;
    }

    public boolean isDepositRollover() {
        return isDepositRollover;
    }

    public void setDepositRollover(boolean depositRollover) {
        this.isDepositRollover = depositRollover;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
