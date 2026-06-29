package com.rentflow.dto.nomba;

public record TokenData(
    String accessToken,
    int expiresIn,
    String tokenType
) {}
