package com.gearshare.events.events;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Published once by booking-service when a renter submits a rental request.
 * This is the trigger for saga #1 in BOTH coordination styles: the orchestrator listens
 * for it in orchestration mode, and catalog-service listens for it directly in
 * choreography mode.
 */
public record RentalRequested(
        String rentalId,
        String itemId,
        String renterId,
        String ownerId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalPrice
) {
}
