package com.rentflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaseResponse {
    private UUID id;
    private UUID tenantId;
    private String tenantName;
    private UUID unitId;
    private String unitNumber;
    private String propertyName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer gracePeriodDays;
    private String status;
    private String nombaVactRef;
    private String nombaVactNumber;
    private String nombaVactBank;
    private BigDecimal baseRent;
    private BigDecimal depositWalletBalance;

    // Getters and Setters














}
