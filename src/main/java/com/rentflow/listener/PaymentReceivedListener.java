package com.rentflow.listener;

import com.rentflow.event.PaymentReceivedEvent;
import com.rentflow.model.InboundTransaction;
import com.rentflow.repository.InboundTransactionRepository;
import com.rentflow.service.AllocationEngine;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentReceivedListener {

    private final InboundTransactionRepository transactionRepository;
    private final AllocationEngine allocationEngine;

    public PaymentReceivedListener(
            InboundTransactionRepository transactionRepository,
            AllocationEngine allocationEngine
    ) {
        this.transactionRepository = transactionRepository;
        this.allocationEngine = allocationEngine;
    }

    @EventListener
    public void handlePaymentReceived(PaymentReceivedEvent event) {
        InboundTransaction tx = transactionRepository.findById(event.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + event.getTransactionId()));

        allocationEngine.allocatePayment(tx.getId(), tx.getAmount(), tx.getLease());
    }
}
