package com.gearshare.events.events;

/**
 * Choreography-only. Published when the owner explicitly declines the request.
 * catalog-service listens for this (alongside ApprovalTimedOut) to release the
 * availability hold it placed earlier -- this IS the compensating action.
 */
public record ApprovalRejected(String rentalId, String itemId, String reason) {
}
