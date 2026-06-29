package com.rentflow.dto.nomba;

public record VirtualAccountResponse(
    String code,
    String description,
    VActData data
) {}
