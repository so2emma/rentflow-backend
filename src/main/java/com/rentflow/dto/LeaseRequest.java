package com.rentflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class LeaseRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Unit ID is required")
    private UUID unitId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private Integer gracePeriodDays = 5;

    private BigDecimal lateFeePercentage = new BigDecimal("5.00");

    @NotBlank(message = "Nomba virtual account reference is required")
    private String nombaVactRef;

    public LeaseRequest() {}

    public LeaseRequest(UUID tenantId, UUID unitId, LocalDate startDate, LocalDate endDate, String nombaVactRef) {
        this.tenantId = tenantId;
        this.unitId = unitId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.nombaVactRef = nombaVactRef;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public void setUnitId(UUID unitId) {
        this.unitId = unitId;
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
}
