package com.rentflow.repository;

import com.rentflow.model.Lease;
import com.rentflow.model.Tenant;
import com.rentflow.model.LeaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LeaseRepository extends JpaRepository<Lease, UUID> {
    Optional<Lease> findByTenantAndStatus(Tenant tenant, LeaseStatus status);
}
