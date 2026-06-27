package com.rentflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SignUpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Role is required")
    private String role; // LANDLORD, TENANT, ADMIN

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Valid
    private LandlordDetails landlordDetails;

    @Valid
    private TenantDetails tenantDetails;

    public SignUpRequest() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LandlordDetails getLandlordDetails() {
        return landlordDetails;
    }

    public void setLandlordDetails(LandlordDetails landlordDetails) {
        this.landlordDetails = landlordDetails;
    }

    public TenantDetails getTenantDetails() {
        return tenantDetails;
    }

    public void setTenantDetails(TenantDetails tenantDetails) {
        this.tenantDetails = tenantDetails;
    }
}
