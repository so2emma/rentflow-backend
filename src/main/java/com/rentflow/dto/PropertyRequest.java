package com.rentflow.dto;

import jakarta.validation.constraints.NotBlank;

public class PropertyRequest {

    @NotBlank(message = "Property name is required")
    private String name;

    @NotBlank(message = "Property address is required")
    private String address;

    @NotBlank(message = "Property code is required")
    private String propertyCode;

    public PropertyRequest() {}

    public PropertyRequest(String name, String address, String propertyCode) {
        this.name = name;
        this.address = address;
        this.propertyCode = propertyCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPropertyCode() {
        return propertyCode;
    }

    public void setPropertyCode(String propertyCode) {
        this.propertyCode = propertyCode;
    }
}
