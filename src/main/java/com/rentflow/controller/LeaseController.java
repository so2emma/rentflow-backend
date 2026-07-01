package com.rentflow.controller;

import com.rentflow.dto.LeaseRequest;
import com.rentflow.model.Landlord;
import com.rentflow.model.Lease;
import com.rentflow.model.LeaseStatus;
import com.rentflow.model.Tenant;
import com.rentflow.model.User;
import com.rentflow.repository.LandlordRepository;
import com.rentflow.repository.LeaseRepository;
import com.rentflow.repository.TenantRepository;
import com.rentflow.service.LeaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leases")
public class LeaseController {

    private final LeaseService leaseService;
    private final LeaseRepository leaseRepository;
    private final TenantRepository tenantRepository;
    private final LandlordRepository landlordRepository;

    public LeaseController(
            LeaseService leaseService,
            LeaseRepository leaseRepository,
            TenantRepository tenantRepository,
            LandlordRepository landlordRepository
    ) {
        this.leaseService = leaseService;
        this.leaseRepository = leaseRepository;
        this.tenantRepository = tenantRepository;
        this.landlordRepository = landlordRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_LANDLORD')")
    public ResponseEntity<?> getLeases(@AuthenticationPrincipal User user) {
        Landlord landlord = landlordRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Landlord profile not found"));

        List<Lease> landlordLeases = leaseRepository.findAll().stream()
                .filter(l -> l.getUnit().getProperty().getLandlord().getId().equals(landlord.getId()))
                .collect(Collectors.toList());

        List<Map<String, Object>> response = landlordLeases.stream()
                .map(l -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", l.getId().toString());
                    map.put("tenantId", l.getTenant().getId().toString());
                    map.put("tenantName", l.getTenant().getFirstName() + " " + l.getTenant().getLastName());
                    map.put("unitId", l.getUnit().getId().toString());
                    map.put("unitNumber", l.getUnit().getUnitNumber());
                    map.put("propertyName", l.getUnit().getProperty().getName());
                    map.put("startDate", l.getStartDate().toString());
                    map.put("endDate", l.getEndDate().toString());
                    map.put("gracePeriodDays", l.getGracePeriodDays());
                    map.put("status", l.getStatus().toString());
                    map.put("nombaVactNumber", l.getNombaVactNumber());
                    map.put("nombaVactBank", l.getNombaVactBank());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_LANDLORD', 'ROLE_ADMIN')")
    public ResponseEntity<?> createLease(@Valid @RequestBody LeaseRequest request) {
        try {
            Lease savedLease = leaseService.createLease(
                    request.getTenantId(),
                    request.getUnitId(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getGracePeriodDays(),
                    request.getLateFeePercentage()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedLease.getId());
            response.put("tenantId", savedLease.getTenant().getId());
            response.put("unitId", savedLease.getUnit().getId());
            response.put("startDate", savedLease.getStartDate());
            response.put("endDate", savedLease.getEndDate());
            response.put("nombaVactRef", savedLease.getNombaVactRef());
            response.put("nombaVactNumber", savedLease.getNombaVactNumber());
            response.put("nombaVactBank", savedLease.getNombaVactBank());
            response.put("status", savedLease.getStatus());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ROLE_TENANT')")
    public ResponseEntity<?> getActiveLease(@AuthenticationPrincipal User user) {
        Tenant tenant = tenantRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant profile not found"));

        Lease activeLease = leaseRepository.findByTenantAndStatus(tenant, LeaseStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active lease found for this tenant"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", activeLease.getId());
        response.put("tenantId", activeLease.getTenant().getId());
        response.put("unitId", activeLease.getUnit().getId());
        response.put("startDate", activeLease.getStartDate());
        response.put("endDate", activeLease.getEndDate());
        response.put("nombaVactRef", activeLease.getNombaVactRef());
        response.put("nombaVactNumber", activeLease.getNombaVactNumber());
        response.put("nombaVactBank", activeLease.getNombaVactBank());
        response.put("status", activeLease.getStatus());
        response.put("baseRent", activeLease.getUnit().getBaseRent());
        response.put("unitNumber", activeLease.getUnit().getUnitNumber());
        response.put("depositWalletBalance", activeLease.getDepositWalletBalance());

        return ResponseEntity.ok(response);
    }
}
