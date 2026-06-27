package com.rentflow.repository;

import com.rentflow.model.Landlord;
import com.rentflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LandlordRepository extends JpaRepository<Landlord, UUID> {
    Optional<Landlord> findByUser(User user);
    Optional<Landlord> findByUserEmail(String email);
}
