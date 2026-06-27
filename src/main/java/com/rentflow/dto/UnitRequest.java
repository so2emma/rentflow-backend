package com.rentflow.dto;

import com.rentflow.model.UnitStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class UnitRequest {

    @NotBlank(message = "Unit number is required")
    private String unitNumber;

    @NotNull(message = "Base rent is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base rent must be greater than 0")
    private BigDecimal baseRent;

    private UnitStatus status = UnitStatus.VACANT;

    public UnitRequest() {}

    public UnitRequest(String unitNumber, BigDecimal baseRent, UnitStatus status) {
        this.unitNumber = unitNumber;
        this.baseRent = baseRent;
        this.status = status;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public BigDecimal getBaseRent() {
        return baseRent;
    }

    public void setBaseRent(BigDecimal baseRent) {
        this.baseRent = baseRent;
    }

    public UnitStatus getStatus() {
        return status;
    }

    public void setStatus(UnitStatus status) {
        this.status = status;
    }
}
