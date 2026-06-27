package com.rentflow.controller;

import com.rentflow.dto.LeaseRequest;
import com.rentflow.model.Lease;
import com.rentflow.model.Tenant;
import com.rentflow.model.Unit;
import com.rentflow.repository.LeaseRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.repository.UnitRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leases")
public class LeaseController {

    private final LeaseRepository leaseRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;

    public LeaseController(
            LeaseRepository leaseRepository,
            TenantRepository tenantRepository,
            UnitRepository unitRepository
    ) {
        this.leaseRepository = leaseRepository;
        this.tenantRepository = tenantRepository;
        this.unitRepository = unitRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_LANDLORD', 'ROLE_ADMIN')")
    public ResponseEntity<?> createLease(@Valid @RequestBody LeaseRequest request) {
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));

        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"));

        Lease lease = new Lease();
        lease.setTenant(tenant);
        lease.setUnit(unit);
        lease.setStartDate(request.getStartDate());
        lease.setEndDate(request.getEndDate());
        if (request.getGracePeriodDays() != null) {
            lease.setGracePeriodDays(request.getGracePeriodDays());
        }
        if (request.getLateFeePercentage() != null) {
            lease.setLateFeePercentage(request.getLateFeePercentage());
        }
        lease.setNombaVactRef(request.getNombaVactRef());

        Lease savedLease = leaseRepository.save(lease);

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedLease.getId());
        response.put("tenantId", savedLease.getTenant().getId());
        response.put("unitId", savedLease.getUnit().getId());
        response.put("startDate", savedLease.getStartDate());
        response.put("endDate", savedLease.getEndDate());
        response.put("nombaVactRef", savedLease.getNombaVactRef());
        response.put("status", savedLease.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
