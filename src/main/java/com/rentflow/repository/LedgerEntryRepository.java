package com.rentflow.repository;

import com.rentflow.model.Lease;
import com.rentflow.model.LedgerEntry;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT le FROM LedgerEntry le WHERE le.lease = :lease AND le.status IN ('UNPAID', 'PARTIALLY_PAID')")
    List<LedgerEntry> findOutstandingEntriesForUpdate(@Param("lease") Lease lease);

    List<LedgerEntry> findByLeaseOrderByDueDateAsc(Lease lease);
}
