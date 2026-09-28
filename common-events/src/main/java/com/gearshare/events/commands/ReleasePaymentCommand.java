package com.gearshare.events.commands;

/**
 * Orchestration-only. The compensating command for {@link AuthorizePaymentCommand}.
 * Not currently triggered in saga #1 (payment authorization is the last step, so there's
 * nothing after it to fail), but kept as a real command because the fulfillment saga's
 * damage-claim path exercises the same "release part of a hold" logic in payment-service.
 */
public record ReleasePaymentCommand(String rentalId) {
}
