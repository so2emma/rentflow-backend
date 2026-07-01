package com.rentflow.controller;

import com.rentflow.dto.TenantResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final com.rentflow.service.TenantService tenantService;

    public TenantController(com.rentflow.service.TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_LANDLORD', 'ROLE_ADMIN')")
    public ResponseEntity<?> getAllTenants() {
        List<TenantResponse> tenants = tenantService.getAllTenants().stream()
                .map(t -> {
                    TenantResponse resp = new TenantResponse();
                    resp.setId(t.getId());
                    resp.setName(t.getFirstName() + " " + t.getLastName());
                    resp.setEmail(t.getUser() != null ? t.getUser().getEmail() : "no-email@tenant.com");
                    return resp;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(tenants);
    }
}
