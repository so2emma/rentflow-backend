package com.rentflow.service;

import com.rentflow.model.Tenant;
import com.rentflow.repository.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public List<Tenant> getAllTenants() {
        log.info("Fetching all tenants");
        return tenantRepository.findAll();
    }
}
