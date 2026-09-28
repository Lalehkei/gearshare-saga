package com.gearshare.events.events;

import java.math.BigDecimal;

/**
 * Choreography-only. Published by catalog-service after it successfully places a
 * hold on the item's availability calendar. approval-service listens for this as its
 * trigger to ask the owner for approval.
 */
public record AvailabilityHeld(String rentalId, String itemId, String ownerId, BigDecimal totalPrice) {
}
