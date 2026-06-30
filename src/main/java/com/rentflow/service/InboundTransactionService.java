package com.rentflow.service;

import com.rentflow.event.PaymentReceivedEvent;
import com.rentflow.model.InboundTransaction;
import com.rentflow.model.Lease;
import com.rentflow.repository.InboundTransactionRepository;
import com.rentflow.repository.LeaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.json.JSONObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class InboundTransactionService {

    private final InboundTransactionRepository transactionRepository;
    private final LeaseRepository leaseRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InboundTransactionService(
            InboundTransactionRepository transactionRepository,
            LeaseRepository leaseRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.transactionRepository = transactionRepository;
        this.leaseRepository = leaseRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void ingestTransaction(String rawPayload, String nombaTxId) {
        // 1. Deduplication check
        if (transactionRepository.existsByNombaTransactionId(nombaTxId)) {
            return; // Duplicate found, stop execution (Idempotent success)
        }

        JSONObject json = new JSONObject(rawPayload);
        JSONObject tx = json.getJSONObject("data").getJSONObject("transaction");
        String accountRef = tx.getString("accountRef");

        Lease lease = leaseRepository.findByNombaVactRef(accountRef)
                .orElseThrow(() -> new EntityNotFoundException("No Lease matches reference: " + accountRef));

        InboundTransaction transaction = new InboundTransaction();
        transaction.setLease(lease);
        transaction.setNombaTransactionId(nombaTxId);
        transaction.setNombaSessionId(tx.optString("sessionId", null));
        transaction.setAmount(new java.math.BigDecimal(tx.get("amount").toString()));
        transaction.setSenderName(tx.optString("senderName", null));
        transaction.setSenderBankName(tx.optString("senderBankName", null));
        transaction.setSenderAccountNumber(tx.optString("senderAccountNumber", null));
        transaction.setTransactionTime(OffsetDateTime.parse(tx.getString("time")));
        transaction.setRawPayload(rawPayload);

        InboundTransaction savedTx = transactionRepository.save(transaction);

        // Publish Spring Event for Asynchronous Processing
        eventPublisher.publishEvent(new PaymentReceivedEvent(this, savedTx.getId()));
    }
}
