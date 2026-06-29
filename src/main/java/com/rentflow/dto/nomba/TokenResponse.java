package com.rentflow.dto.nomba;

public record TokenResponse(
    String code,
    String description,
    TokenData data
) {}
