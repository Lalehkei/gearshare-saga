package com.gearshare.events.events;

import java.math.BigDecimal;

/**
 * Fulfillment saga (#2) terminal event, no-damage branch. Published by payment-service
 * after converting the full authorization hold into an actual charge.
 */
public record PaymentCaptured(String rentalId, BigDecimal amount) {
}
