package com.rentflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequest {

    @NotBlank(message = "Property name is required")
    private String name;

    @NotBlank(message = "Property address is required")
    private String address;

    @NotBlank(message = "Property code is required")
    private String propertyCode;








}
