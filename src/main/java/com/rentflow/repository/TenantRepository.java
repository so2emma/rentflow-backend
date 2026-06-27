package com.rentflow.repository;

import com.rentflow.model.Tenant;
import com.rentflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByUser(User user);
    Optional<Tenant> findByUserEmail(String email);
}
