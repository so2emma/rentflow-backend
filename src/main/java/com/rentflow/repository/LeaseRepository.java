package com.rentflow.repository;

import com.rentflow.model.Lease;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LeaseRepository extends JpaRepository<Lease, UUID> {
}
