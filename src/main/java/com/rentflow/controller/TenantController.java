package com.rentflow.controller;

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
        List<Map<String, Object>> tenants = tenantService.getAllTenants().stream()
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", t.getId().toString());
                    map.put("name", t.getFirstName() + " " + t.getLastName());
                    map.put("email", t.getUser() != null ? t.getUser().getEmail() : "no-email@tenant.com");
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(tenants);
    }
}
