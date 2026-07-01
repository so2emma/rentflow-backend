package com.rentflow.event;

import org.springframework.context.ApplicationEvent;
import java.math.BigDecimal;
import java.util.UUID;

public class PayoutSplitRequiredEvent extends ApplicationEvent {
    private final UUID inboundTransactionId;
    private final BigDecimal splitTotal;

    public PayoutSplitRequiredEvent(Object source, UUID inboundTransactionId, BigDecimal splitTotal) {
        super(source);
        this.inboundTransactionId = inboundTransactionId;
        this.splitTotal = splitTotal;
    }

    public UUID getInboundTransactionId() {
        return inboundTransactionId;
    }

    public BigDecimal getSplitTotal() {
        return splitTotal;
    }
}
