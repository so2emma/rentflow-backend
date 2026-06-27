package com.rentflow.repository;

import com.rentflow.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID> {
}
