package com.gearshare.events.events;

/**
 * Choreography-only. Published when the (simulated) card authorization fails. Both
 * catalog-service and approval-service listen for this to compensate: catalog releases
 * the availability hold, approval marks its record moot.
 */
public record PaymentAuthorizationFailed(String rentalId, String reason) {
}
