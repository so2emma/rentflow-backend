package com.rentflow.controller;

import com.rentflow.dto.LeaseRequest;
import com.rentflow.dto.LeaseResponse;
import com.rentflow.model.Lease;
import com.rentflow.model.User;

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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leases")
public class LeaseController {

    private final LeaseService leaseService;
    public LeaseController(LeaseService leaseService) {
        this.leaseService = leaseService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_LANDLORD')")
    public ResponseEntity<?> getLeases(@AuthenticationPrincipal User user) {
        List<Lease> landlordLeases = leaseService.getLeasesForLandlord(user);

        List<LeaseResponse> response = landlordLeases.stream()
                .map(l -> {
                    LeaseResponse resp = new LeaseResponse();
                    resp.setId(l.getId());
                    resp.setTenantId(l.getTenant().getId());
                    resp.setTenantName(l.getTenant().getFirstName() + " " + l.getTenant().getLastName());
                    resp.setUnitId(l.getUnit().getId());
                    resp.setUnitNumber(l.getUnit().getUnitNumber());
                    resp.setPropertyName(l.getUnit().getProperty().getName());
                    resp.setStartDate(l.getStartDate());
                    resp.setEndDate(l.getEndDate());
                    resp.setGracePeriodDays(l.getGracePeriodDays());
                    resp.setStatus(l.getStatus().toString());
                    resp.setNombaVactNumber(l.getNombaVactNumber());
                    resp.setNombaVactBank(l.getNombaVactBank());
                    return resp;
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

            LeaseResponse response = new LeaseResponse();
            response.setId(savedLease.getId());
            response.setTenantId(savedLease.getTenant().getId());
            response.setUnitId(savedLease.getUnit().getId());
            response.setStartDate(savedLease.getStartDate());
            response.setEndDate(savedLease.getEndDate());
            response.setNombaVactRef(savedLease.getNombaVactRef());
            response.setNombaVactNumber(savedLease.getNombaVactNumber());
            response.setNombaVactBank(savedLease.getNombaVactBank());
            response.setStatus(savedLease.getStatus().toString());

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
        Lease activeLease = leaseService.getActiveLeaseForTenant(user);

        LeaseResponse response = new LeaseResponse();
        response.setId(activeLease.getId());
        response.setTenantId(activeLease.getTenant().getId());
        response.setUnitId(activeLease.getUnit().getId());
        response.setStartDate(activeLease.getStartDate());
        response.setEndDate(activeLease.getEndDate());
        response.setNombaVactRef(activeLease.getNombaVactRef());
        response.setNombaVactNumber(activeLease.getNombaVactNumber());
        response.setNombaVactBank(activeLease.getNombaVactBank());
        response.setStatus(activeLease.getStatus().toString());
        response.setBaseRent(activeLease.getUnit().getBaseRent());
        response.setUnitNumber(activeLease.getUnit().getUnitNumber());
        response.setDepositWalletBalance(activeLease.getDepositWalletBalance());

        return ResponseEntity.ok(response);
    }
}
