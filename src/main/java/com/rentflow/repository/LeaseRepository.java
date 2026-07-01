package com.rentflow.repository;

import com.rentflow.model.Lease;
import com.rentflow.model.Tenant;
import com.rentflow.model.LeaseStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LeaseRepository extends JpaRepository<Lease, UUID> {
    Optional<Lease> findByTenantAndStatus(Tenant tenant, LeaseStatus status);
    Optional<Lease> findByNombaVactRef(String nombaVactRef);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lease l WHERE l.id = :id")
    Optional<Lease> findByIdForUpdate(@Param("id") UUID id);
}
