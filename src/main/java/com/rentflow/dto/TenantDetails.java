package com.rentflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantDetails {

    @NotBlank(message = "BVN is required")
    @Pattern(regexp = "^\\d{11}$", message = "BVN must be exactly 11 digits")
    private String bvn;




}
