package com.gearshare.events.events;

/**
 * Terminal, happy-path outcome of saga #1. Published by the orchestrator in
 * orchestration mode, or by payment-service (the last participant in the chain) in
 * choreography mode. booking-service listens for this in both modes to flip its
 * read model to CONFIRMED.
 */
public record RentalConfirmed(String rentalId) {
}
