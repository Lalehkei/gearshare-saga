package com.gearshare.events.events;

/**
 * Choreography-only. Published by catalog-service when the item is not available
 * (already held, unknown item, or the item is owner-marked unavailable). This is a
 * first-step failure, so there is nothing upstream left to compensate -- booking-service
 * listens for this directly to close out the saga as cancelled.
 */
public record AvailabilityRejected(String rentalId, String itemId, String reason) {
}
