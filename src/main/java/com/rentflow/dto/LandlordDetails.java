package com.rentflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LandlordDetails {

    @NotBlank(message = "Bank code is required")
    private String bankCode;

    @NotBlank(message = "Bank account number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Bank account number must be exactly 10 digits")
    private String bankAccountNumber;

    @NotBlank(message = "Bank account name is required")
    private String bankAccountName;








}
