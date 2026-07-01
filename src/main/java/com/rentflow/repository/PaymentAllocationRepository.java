package com.rentflow.repository;

import com.rentflow.model.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, UUID> {

    @Query("SELECT COALESCE(SUM(pa.amountAllocated), 0) FROM PaymentAllocation pa " +
           "WHERE pa.inboundTransactionId = :txId " +
           "AND pa.ledgerEntry IS NOT NULL " +
           "AND (pa.ledgerEntry.entryType = 'RENT' OR pa.ledgerEntry.entryType = 'LATE_FEE')")
    BigDecimal sumRentAndLateFeesForTransaction(@Param("txId") UUID txId);
}
