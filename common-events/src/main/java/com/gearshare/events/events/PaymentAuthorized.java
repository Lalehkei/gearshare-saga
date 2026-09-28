package com.gearshare.events.events;

/**
 * Choreography-only. Published by payment-service after it places a successful
 * authorization hold (NOT a capture -- see the README for why holds and captures are
 * kept separate). This is the last step of saga #1's happy path, so payment-service
 * itself also publishes RentalConfirmed right after this.
 */
public record PaymentAuthorized(String rentalId) {
}
