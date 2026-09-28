package com.gearshare.events.events;

/**
 * Terminal, failure-path outcome of saga #1 -- published once every compensating
 * action for whatever already succeeded has been triggered. booking-service listens
 * for this in both modes to flip its read model to CANCELLED and record why.
 */
public record RentalCancelled(String rentalId, String reason) {
}
