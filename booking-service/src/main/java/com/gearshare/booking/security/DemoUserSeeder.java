package com.gearshare.booking.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates exactly one login the first time booking-service starts against a fresh
 * database, so there's something to authenticate with out of the box. See the README's
 * "Authentication" section for the credentials -- not repeated in a log line here on
 * every startup, only the first time this actually creates the row.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoUserSeeder implements CommandLineRunner {

    private static final String DEMO_USERNAME = "demo";
    private static final String DEMO_PASSWORD = "gearshare123";

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (repository.findByUsername(DEMO_USERNAME).isEmpty()) {
            repository.save(new AppUser(null, DEMO_USERNAME, passwordEncoder.encode(DEMO_PASSWORD), "USER"));
            log.info("Seeded the demo login (username '{}') -- see the README's Authentication section for the password", DEMO_USERNAME);
        }
    }
}
