package com.rentflow.service;

import com.rentflow.dto.SignUpRequest;
import com.rentflow.exception.DuplicateEmailException;
import com.rentflow.exception.ValidationException;
import com.rentflow.model.*;
import com.rentflow.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class OnboardingService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LandlordRepository landlordRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public OnboardingService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            LandlordRepository landlordRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.landlordRepository = landlordRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(SignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email " + request.getEmail() + " is already registered");
        }

        // Determine matching role name
        String rawRole = request.getRole();
        if (rawRole == null || rawRole.trim().isEmpty()) {
            throw new ValidationException("Role is required");
        }
        String roleName = rawRole.toUpperCase().startsWith("ROLE_") ? rawRole.toUpperCase() : "ROLE_" + rawRole.toUpperCase();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ValidationException("Invalid role specified: " + rawRole));

        // Create User security account
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(role));
        user.setActive(true);
        User savedUser = userRepository.save(user);

        // Bind profile based on role
        if (roleName.equals("ROLE_LANDLORD")) {
            if (request.getLandlordDetails() == null) {
                throw new ValidationException("Landlord details are required for Landlord signup");
            }
            if (request.getLandlordDetails().getBankCode() == null || request.getLandlordDetails().getBankCode().trim().isEmpty() ||
                request.getLandlordDetails().getBankAccountNumber() == null || request.getLandlordDetails().getBankAccountNumber().trim().isEmpty() ||
                request.getLandlordDetails().getBankAccountName() == null || request.getLandlordDetails().getBankAccountName().trim().isEmpty()) {
                throw new ValidationException("Invalid landlord details");
            }
            Landlord landlord = new Landlord();
            landlord.setUser(savedUser);
            landlord.setFirstName(request.getFirstName());
            landlord.setLastName(request.getLastName());
            landlord.setPhoneNumber(request.getPhoneNumber());
            landlord.setBankCode(request.getLandlordDetails().getBankCode());
            landlord.setBankAccountNumber(request.getLandlordDetails().getBankAccountNumber());
            landlord.setBankAccountName(request.getLandlordDetails().getBankAccountName());
            landlordRepository.save(landlord);
        } else if (roleName.equals("ROLE_TENANT")) {
            if (request.getTenantDetails() == null) {
                throw new ValidationException("Tenant details are required for Tenant signup");
            }
            if (request.getTenantDetails().getBvn() == null || request.getTenantDetails().getBvn().trim().isEmpty()) {
                throw new ValidationException("Invalid tenant details");
            }
            Tenant tenant = new Tenant();
            tenant.setUser(savedUser);
            tenant.setFirstName(request.getFirstName());
            tenant.setLastName(request.getLastName());
            tenant.setPhoneNumber(request.getPhoneNumber());
            tenant.setBvn(request.getTenantDetails().getBvn());
            tenantRepository.save(tenant);
        }

        return savedUser;
    }
}
