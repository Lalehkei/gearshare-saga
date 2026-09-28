package com.gearshare.events.events;

import java.math.BigDecimal;

/**
 * Choreography-only. Published by approval-service once the (simulated) owner
 * approves the request. payment-service listens for this as its trigger to authorize
 * a payment hold on the renter's card.
 */
public record ApprovalGranted(String rentalId, String renterId, BigDecimal totalPrice) {
}
