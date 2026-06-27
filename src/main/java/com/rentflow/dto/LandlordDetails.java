package com.rentflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LandlordDetails {

    @NotBlank(message = "Bank code is required")
    private String bankCode;

    @NotBlank(message = "Bank account number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Bank account number must be exactly 10 digits")
    private String bankAccountNumber;

    @NotBlank(message = "Bank account name is required")
    private String bankAccountName;

    public LandlordDetails() {}

    public LandlordDetails(String bankCode, String bankAccountNumber, String bankAccountName) {
        this.bankCode = bankCode;
        this.bankAccountNumber = bankAccountNumber;
        this.bankAccountName = bankAccountName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }
}
