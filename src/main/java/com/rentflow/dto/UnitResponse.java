package com.rentflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitResponse {
    private UUID id;
    private UUID propertyId;
    private String propertyName;
    private String unitNumber;
    private BigDecimal baseRent;
    private String status;






}
