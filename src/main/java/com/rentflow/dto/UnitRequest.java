package com.rentflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.rentflow.model.UnitStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitRequest {

    @NotBlank(message = "Unit number is required")
    private String unitNumber;

    @NotNull(message = "Base rent is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base rent must be greater than 0")
    private BigDecimal baseRent;

    private UnitStatus status = UnitStatus.VACANT;








}
