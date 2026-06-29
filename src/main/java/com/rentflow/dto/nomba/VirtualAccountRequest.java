package com.rentflow.dto.nomba;

import java.math.BigDecimal;

public record VirtualAccountRequest(
    String accountRef,
    String accountName,
    String bvn,
    BigDecimal expectedAmount
) {}
