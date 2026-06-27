package com.rentflow.repository;

import com.rentflow.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
    Optional<Property> findByPropertyCode(String propertyCode);
}
