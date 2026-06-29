package com.rentflow.config;

import com.rentflow.model.Landlord;
import com.rentflow.model.Role;
import com.rentflow.model.User;
import com.rentflow.repository.LandlordRepository;
import com.rentflow.repository.RoleRepository;
import com.rentflow.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LandlordRepository landlordRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(
            UserRepository userRepository,
            RoleRepository roleRepository,
            LandlordRepository landlordRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.landlordRepository = landlordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedLandlordUser();
    }

    private void seedLandlordUser() {
        String landlordEmail = "landlord@rentflow.com";
        User savedUser;
        if (userRepository.findByEmail(landlordEmail).isEmpty()) {
            Role landlordRole = roleRepository.findByName("ROLE_LANDLORD")
                    .orElseThrow(() -> new IllegalStateException("ROLE_LANDLORD not found in database. Ensure migrations ran successfully."));

            User landlord = new User();
            landlord.setEmail(landlordEmail);
            landlord.setPasswordHash(passwordEncoder.encode("securepassword"));
            landlord.setRoles(Set.of(landlordRole));
            landlord.setActive(true);

            savedUser = userRepository.save(landlord);
            System.out.println("Default landlord user seeded: " + landlordEmail);
        } else {
            savedUser = userRepository.findByEmail(landlordEmail).get();
        }

        if (landlordRepository.findByUser(savedUser).isEmpty()) {
            // Seed Landlord Profile
            Landlord landlordProfile = new Landlord();
            landlordProfile.setUser(savedUser);
            landlordProfile.setFirstName("Default");
            landlordProfile.setLastName("Landlord");
            landlordProfile.setPhoneNumber("+2348000000000");
            landlordProfile.setBankCode("058");
            landlordProfile.setBankAccountNumber("0123456789");
            landlordProfile.setBankAccountName("Default Landlord Trust");
            landlordRepository.save(landlordProfile);
            System.out.println("Default landlord profile seeded for: " + landlordEmail);
        }
    }
}
