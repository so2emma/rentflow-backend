package com.rentflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

//    @NotBlank(message = "Nomba virtual account reference is required")
    private String nombaVactRef;
















}
