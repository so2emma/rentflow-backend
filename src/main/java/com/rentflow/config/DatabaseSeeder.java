package com.rentflow.config;

import com.rentflow.model.Role;
import com.rentflow.model.User;
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
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedLandlordUser();
    }

    private void seedLandlordUser() {
        String landlordEmail = "landlord@rentflow.com";
        if (userRepository.findByEmail(landlordEmail).isEmpty()) {
            Role landlordRole = roleRepository.findByName("ROLE_LANDLORD")
                    .orElseThrow(() -> new IllegalStateException("ROLE_LANDLORD not found in database. Ensure migrations ran successfully."));

            User landlord = new User();
            landlord.setEmail(landlordEmail);
            landlord.setPasswordHash(passwordEncoder.encode("securepassword"));
            landlord.setRoles(Set.of(landlordRole));
            landlord.setActive(true);

            userRepository.save(landlord);
            System.out.println("Default landlord user seeded: " + landlordEmail);
        }
    }
}
