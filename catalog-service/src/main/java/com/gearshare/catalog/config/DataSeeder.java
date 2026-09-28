package com.gearshare.catalog.config;

import com.gearshare.catalog.domain.GearItem;
import com.gearshare.catalog.domain.GearItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds a handful of listings on startup so there's something to rent immediately.
 * item-005 is deliberately seeded as unavailable forever ("the owner took it off the
 * market") -- POST a rental for it to reliably see the first-step-rejection path
 * without needing to race anything or get lucky. See the README's "things to try".
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final GearItemRepository itemRepository;

    @Override
    public void run(String... args) {
        if (itemRepository.count() > 0) {
            return;
        }
        itemRepository.saveAll(List.of(
                new GearItem("item-001", "owner-amy", "Canon EOS R6 Camera", new BigDecimal("45.00"), true),
                new GearItem("item-002", "owner-amy", "DJI Mavic 3 Drone", new BigDecimal("60.00"), true),
                new GearItem("item-003", "owner-ben", "MSR Camping Tent (4-person)", new BigDecimal("20.00"), true),
                new GearItem("item-004", "owner-ben", "Makita Cordless Drill Set", new BigDecimal("15.00"), true),
                new GearItem("item-005", "owner-cleo", "Yamaha PA Speaker System", new BigDecimal("80.00"), false)
        ));
    }
}
