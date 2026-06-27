package com.rentflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class TenantDetails {

    @NotBlank(message = "BVN is required")
    @Pattern(regexp = "^\\d{11}$", message = "BVN must be exactly 11 digits")
    private String bvn;

    public TenantDetails() {}

    public TenantDetails(String bvn) {
        this.bvn = bvn;
    }

    public String getBvn() {
        return bvn;
    }

    public void setBvn(String bvn) {
        this.bvn = bvn;
    }
}
