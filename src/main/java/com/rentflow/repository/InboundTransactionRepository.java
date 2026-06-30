package com.rentflow.repository;

import com.rentflow.model.InboundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InboundTransactionRepository extends JpaRepository<InboundTransaction, UUID> {
    boolean existsByNombaTransactionId(String nombaTransactionId);
}
