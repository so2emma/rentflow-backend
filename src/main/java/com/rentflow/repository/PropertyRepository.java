package com.rentflow.repository;

import com.rentflow.model.Landlord;
import com.rentflow.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
    Optional<Property> findByPropertyCode(String propertyCode);
    List<Property> findByLandlord(Landlord landlord);
}
