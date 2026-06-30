package com.rentflow.event;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

public class PaymentReceivedEvent extends ApplicationEvent {
    private final UUID transactionId;

    public PaymentReceivedEvent(Object source, UUID transactionId) {
        super(source);
        this.transactionId = transactionId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }
}
