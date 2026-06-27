package com.rentflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leases")
public class Lease {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "grace_period_days")
    private Integer gracePeriodDays = 5;

    @Column(name = "late_fee_percentage", precision = 5, scale = 2)
    private BigDecimal lateFeePercentage = new BigDecimal("5.00");

    @Column(name = "nomba_vact_ref", unique = true, nullable = false, length = 64)
    private String nombaVactRef;

    @Column(name = "nomba_vact_number", unique = true, length = 20)
    private String nombaVactNumber;

    @Column(name = "nomba_vact_bank", length = 100)
    private String nombaVactBank;

    @Column(name = "deposit_wallet_balance", precision = 15, scale = 2)
    private BigDecimal depositWalletBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaseStatus status = LeaseStatus.PENDING_VIRTUAL_ACCOUNT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Lease() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getGracePeriodDays() {
        return gracePeriodDays;
    }

    public void setGracePeriodDays(Integer gracePeriodDays) {
        this.gracePeriodDays = gracePeriodDays;
    }

    public BigDecimal getLateFeePercentage() {
        return lateFeePercentage;
    }

    public void setLateFeePercentage(BigDecimal lateFeePercentage) {
        this.lateFeePercentage = lateFeePercentage;
    }

    public String getNombaVactRef() {
        return nombaVactRef;
    }

    public void setNombaVactRef(String nombaVactRef) {
        this.nombaVactRef = nombaVactRef;
    }

    public String getNombaVactNumber() {
        return nombaVactNumber;
    }

    public void setNombaVactNumber(String nombaVactNumber) {
        this.nombaVactNumber = nombaVactNumber;
    }

    public String getNombaVactBank() {
        return nombaVactBank;
    }

    public void setNombaVactBank(String nombaVactBank) {
        this.nombaVactBank = nombaVactBank;
    }

    public BigDecimal getDepositWalletBalance() {
        return depositWalletBalance;
    }

    public void setDepositWalletBalance(BigDecimal depositWalletBalance) {
        this.depositWalletBalance = depositWalletBalance;
    }

    public LeaseStatus getStatus() {
        return status;
    }

    public void setStatus(LeaseStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
