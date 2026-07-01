package com.rentflow.service;

import com.rentflow.dto.PropertyRequest;
import com.rentflow.dto.UnitRequest;
import com.rentflow.model.Landlord;
import com.rentflow.model.Property;
import com.rentflow.model.Unit;
import com.rentflow.model.User;
import com.rentflow.repository.LandlordRepository;
import com.rentflow.repository.PropertyRepository;
import com.rentflow.repository.UnitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final LandlordRepository landlordRepository;
    private final UnitRepository unitRepository;

    public PropertyService(
            PropertyRepository propertyRepository,
            LandlordRepository landlordRepository,
            UnitRepository unitRepository
    ) {
        this.propertyRepository = propertyRepository;
        this.landlordRepository = landlordRepository;
        this.unitRepository = unitRepository;
    }

    public List<Property> getProperties(User user) {
        log.info("Fetching properties for landlord userId={}", user != null ? user.getId() : "null");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Optional<Landlord> landlordOpt = landlordRepository.findByUser(user);
        if (landlordOpt.isEmpty()) {
            return List.of();
        }

        return propertyRepository.findByLandlord(landlordOpt.get());
    }

    public List<Unit> getUnits(User user) {
        log.info("Fetching units for landlord userId={}", user.getId());
        Landlord landlord = landlordRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Landlord profile not found"));

        List<Property> properties = propertyRepository.findByLandlord(landlord);

        return properties.stream()
                .flatMap(p -> unitRepository.findByProperty(p).stream())
                .collect(java.util.stream.Collectors.toList());
    }

    public Property createProperty(PropertyRequest request, User user) {
        log.info("Creating property name={} userId={}", request.getName(), user.getId());
        Landlord landlord = landlordRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Landlord profile not found"));

        Property property = new Property();
        property.setLandlord(landlord);
        property.setName(request.getName());
        property.setAddress(request.getAddress());
        property.setPropertyCode(request.getPropertyCode());

        Property savedProperty = propertyRepository.save(property);
        log.info("Property created successfully propertyId={}", savedProperty.getId());
        return savedProperty;
    }

    public Unit createUnit(UUID propertyId, UnitRequest request, User user) {
        log.info("Creating unit propertyId={} unitNumber={} userId={}", propertyId, request.getUnitNumber(), user.getId());
        Landlord landlord = landlordRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Landlord profile not found"));

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        // Verify property belongs to the logged-in landlord
        if (!property.getLandlord().getId().equals(landlord.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Property does not belong to you");
        }

        Unit unit = new Unit();
        unit.setProperty(property);
        unit.setUnitNumber(request.getUnitNumber());
        unit.setBaseRent(request.getBaseRent());
        unit.setStatus(request.getStatus());

        Unit savedUnit = unitRepository.save(unit);
        log.info("Unit created successfully unitId={} propertyId={}", savedUnit.getId(), propertyId);
        return savedUnit;
    }
}
